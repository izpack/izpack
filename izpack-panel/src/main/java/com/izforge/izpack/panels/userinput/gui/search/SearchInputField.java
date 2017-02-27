/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels.userinput.gui.search;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.userinput.field.search.ResultType;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.field.search.SearchType;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class encapsulates a lot of search field functionality.
 * <p/>
 * A search field supports searching directories and files on the target system.
 * This is a helper class to manage all installDataGUI belonging to a search
 * field.
 */
public class SearchInputField implements ActionListener
{

    private static final Logger LOGGER = Logger.getLogger(SearchInputField.class.getName());

    private final SearchField field;

    private final String filename;

    private final String checkFilename;

    private final JButton autodetectButton;

    private final JButton browseButton;

    private final JComboBox pathComboBox;

    private final SearchType searchType;

    private final ResultType resultType;

    private final InstallerFrame parent;

    private final InstallData installData;

    /*---------------------------------------------------------------------------*/
    /**
     * Constructor - initializes the object, adds it as action listener to the
     * "autodetect" button.
     *
     * @param field the search field
     * @param combobox the <code>JComboBox</code> holding the list of choices;
     * it should be editable and contain only Strings
     * @param autobutton the autodetection button for triggering autodetection
     * @param browsebutton the browse button to look for the file
     */
    public SearchInputField(final SearchField field, final InstallerFrame parent, JComboBox combobox, JButton autobutton,
            JButton browsebutton, InstallData installData)
    {
        this.field = field;
        this.filename = field.getFilename();
        this.checkFilename = field.getCheckFilename();
        this.parent = parent;
        this.autodetectButton = autobutton;
        this.browseButton = browsebutton;
        this.pathComboBox = combobox;
        this.searchType = field.getType();
        this.resultType = field.getResultType();
        this.installData = installData;

        this.autodetectButton.addActionListener(this);
        this.browseButton.addActionListener(this);

        /*
         * add DocumentListener to manage nextButton if user enters input
         */
        final JTextField editor = (JTextField) pathComboBox.getEditor().getEditorComponent();
        editor.getDocument().addDocumentListener(new DocumentListener()
        {

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                checkNextButtonState();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                checkNextButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                checkNextButtonState();
            }

            private void checkNextButtonState()
            {
                Document doc = editor.getDocument();
                try
                {
                    if (field.pathMatches(doc.getText(0, doc.getLength())))
                    {
                        parent.unlockNextButton(false);
                    }
                    else
                    {
                        parent.lockNextButton();
                    }
                }
                catch (BadLocationException e)
                {/* ignore, it not happens */
                }
            }
        });
    }

    /**
     * perform autodetection
     */
    public boolean autodetect()
    {
        // Try all of <choice> options - see if any are valid
        for (int x = 0; x < pathComboBox.getItemCount(); x++)
        {
            if (field.pathMatches((String) pathComboBox.getItemAt(x)))
            {
                pathComboBox.setSelectedIndex(x);
                break;
            }
        }

        /*
         * Check if the user has entered installDataGUI into the ComboBox and add it to the Itemlist
         */
        String selected = (String) pathComboBox.getSelectedItem();
        if (selected == null)
        {
            parent.lockNextButton();
            return false;
        }
        boolean found = false;
        for (int x = 0; x < pathComboBox.getItemCount(); x++)
        {
            if (pathComboBox.getItemAt(x).equals(selected))
            {
                found = true;
            }
        }
        if (!found)
        {
            // Not found in item list
            pathComboBox.addItem(selected);
        }

        List<String> currentItems = new ArrayList<String>();
        for (int i = 0; i < pathComboBox.getItemCount(); ++i)
        {
            currentItems.add((String) pathComboBox.getItemAt(i));
        }
        List<String> items = field.getChoices(currentItems);

        // Make the entries in the vector unique
        items = new ArrayList<String>(new LinkedHashSet<String>(items));

        // Now clear the combobox and add the items out of the newly
        // generated vector
        pathComboBox.removeAllItems();
        for (String item : items)
        {
            item = installData.getVariables().replace(item);
            pathComboBox.addItem(item);
        }

        // loop through all items
        for (int i = 0; i < pathComboBox.getItemCount(); ++i)
        {
            String path = (String) pathComboBox.getItemAt(i);

            if (field.pathMatches(path))
            {
                pathComboBox.setSelectedIndex(i);
                parent.unlockNextButton();
                return true;
            }

        }

        // if the user entered something else, it's not listed as an item
        if (field.pathMatches((String) pathComboBox.getSelectedItem()))
        {
            parent.unlockNextButton();
            return true;
        }
        parent.lockNextButton();
        return false;
    }


    /*--------------------------------------------------------------------------*/
    /**
     * This is called if one of the buttons has been pressed.
     * <p/>
     * It checks, which button caused the action and acts accordingly.
     */
    /*--------------------------------------------------------------------------*/
    @Override
    public void actionPerformed(ActionEvent event)
    {
        Object source = event.getSource();
        if (source == autodetectButton)
        {
            if (!autodetect())
            {
                warning("UserInputPanel.search.autodetect.failed.caption",
                        "UserInputPanel.search.autodetect.failed.message");
            }
        }
        else if (source == browseButton)
        {
            JFileChooser chooser = new JFileChooser();

            if (resultType != ResultType.FILE)
            {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }

            String startingPath = (String) pathComboBox.getSelectedItem();
            if (startingPath == null && pathComboBox.getItemCount() != 0)
            {
                startingPath = (String) pathComboBox.getItemAt(0);
            }
            if (startingPath != null)
            {
                File dir = new File(startingPath);
                if (dir.exists())
                {
                    chooser.setCurrentDirectory(dir);
                }
            }

            int result = chooser.showOpenDialog(parent);

            if (result == JFileChooser.APPROVE_OPTION)
            {
                File selectedFile = chooser.getSelectedFile();

                pathComboBox.setSelectedItem(selectedFile.getAbsolutePath());

                // use any given directory directly
                if (resultType != ResultType.FILE && !field.pathMatches(selectedFile.getAbsolutePath()))
                {
                    warning("UserInputPanel.search.wrongselection.caption",
                            "UserInputPanel.search.wrongselection.message");
                }
            }

        }

        // we don't care for anything more here - getResult() does the rest
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Return the result of the search according to result type.
     * <p/>
     * Sometimes, the whole path of the file is wanted, sometimes only the
     * directory where the file is in, sometimes the parent directory.
     *
     * @return null on error
     */
    /*--------------------------------------------------------------------------*/
    public String getResult()
    {
        return field.getResult((String) pathComboBox.getSelectedItem());
    }

    public void setResult(String item)
    {
        pathComboBox.setSelectedItem(item);
    }

    private void warning(String title, String message)
    {
        Messages messages = parent.getMessages();
        title = messages.get(title);
        message = messages.get(message);
        new GUIPrompt().message(Prompt.Type.WARNING, title, message);
    }

}
