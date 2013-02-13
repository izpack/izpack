/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * Copyright 2009 Dennis Reil
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

package com.izforge.izpack.panels.userinput.gui.file;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;


public class MultipleFileInputField extends JPanel implements ActionListener, FocusListener
{
    private static final long serialVersionUID = 4673684743657328492L;

    private static final transient Logger logger = Logger.getLogger(MultipleFileInputField.class.getName());

    private final MultipleFileField field;
    boolean isDirectory;
    InstallerFrame parentFrame;

    DefaultListModel model;
    JList fileList;
    JButton browseBtn;
    JButton deleteBtn;

    String set;
    int size;
    GUIInstallData data;
    String fileExtension;
    String fileExtensionDescription;

    boolean allowEmpty;
    boolean createMultipleVariables;

    int visibleRows = 10;
    int preferredX = 200;
    int preferredY = 200;

    String labeltext;

    public MultipleFileInputField(MultipleFileField field, InstallerFrame parent, GUIInstallData data,
                                  boolean directory)
    {
        this.field = field;
        this.parentFrame = parent;
        this.data = data;
        this.set = field.getDefaultValue();
        this.size = field.getSize();
        if (size < 1)
        {
            size = 1;
        }
        this.fileExtension = field.getFileExtension();
        this.fileExtensionDescription = field.getFileExtensionDescription();
        this.isDirectory = directory;
        this.createMultipleVariables = field.getCreateMultipleVariables();
        this.visibleRows = field.getVisibleRows() > 0 ? field.getVisibleRows() : 10;
        this.preferredX = field.getPreferredWidth() > 0 ? field.getPreferredWidth() : 200;
        this.preferredY = field.getPreferredHeight() > 0 ? field.getPreferredHeight() : 200;
        this.labeltext = field.getLabel();
        this.allowEmpty = field.getAllowEmptyValue();
        this.initialize();
    }

    public void clearFiles()
    {
        this.model.clear();
    }

    public void addFile(String file)
    {
        this.model.addElement(file);
    }

    public void initialize()
    {
        JPanel main = new JPanel();


        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        JLabel label = new JLabel(this.labeltext);
        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());
        main.add(labelPanel);

        model = new DefaultListModel();
        fileList = new JList(model);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setVisibleRowCount(visibleRows);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        Messages messages = data.getMessages();
        browseBtn = ButtonFactory.createButton(messages.get("UserInputPanel.search.browse"), data.buttonsHColor);
        browseBtn.setName(GuiId.BUTTON_BROWSE.id);
        browseBtn.addActionListener(this);

        deleteBtn = ButtonFactory.createButton(messages.get("UserInputPanel.search.delete"), data.buttonsHColor);
        deleteBtn.addActionListener(this);

        JScrollPane scroller = new JScrollPane(fileList);
        scroller.setPreferredSize(new Dimension(preferredX, preferredY));
        panel.add(scroller);

        buttonPanel.add(browseBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(Box.createVerticalGlue());
        panel.add(buttonPanel);
        main.add(panel);
        main.add(Box.createVerticalGlue());
        add(main);
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource() == browseBtn)
        {
            logger.fine("Show directory chooser");
            String initialPath = ".";
            if (fileList.getSelectedValue() != null)
            {
                initialPath = (String) fileList.getSelectedValue();
            }
            JFileChooser filechooser = new JFileChooser(initialPath);
            if (isDirectory)
            {
                filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            else
            {
                filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if ((fileExtension != null) && (fileExtensionDescription != null))
                {
                    UserInputFileFilter fileFilter = new UserInputFileFilter();
                    fileFilter.setFileExt(fileExtension);
                    fileFilter.setFileExtDesc(fileExtensionDescription);
                    filechooser.setFileFilter(fileFilter);
                }
            }

            if (filechooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION)
            {
                String selectedFile = filechooser.getSelectedFile().getAbsolutePath();
                model.addElement(selectedFile);
                logger.fine("Setting current file chooser directory to: " + selectedFile);
            }
        }
        if (arg0.getSource() == deleteBtn)
        {
            logger.fine("Delete selected file from list");
            if (fileList.getSelectedValue() != null)
            {
                model.removeElement(fileList.getSelectedValue());
            }
        }
    }

    public List<String> getSelectedFiles()
    {
        List<String> result = null;
        if (model.size() > 0)
        {
            result = new ArrayList<String>();

            Enumeration<?> elements = model.elements();
            for (; elements.hasMoreElements(); )
            {
                String element = (String) elements.nextElement();
                result.add(element);
            }
        }
        return result;
    }

    private void showMessage(String messageType)
    {
        JOptionPane.showMessageDialog(parentFrame,
                                      parentFrame.getMessages().get("UserInputPanel." + messageType + ".message"),
                                      parentFrame.getMessages().get("UserInputPanel." + messageType + ".caption"),
                                      JOptionPane.WARNING_MESSAGE);
    }

    private boolean validateFile(String input)
    {
        boolean result = false;
        if (allowEmpty && ((input == null) || (input.length() == 0)))
        {
            result = true;
        }
        else if (input != null)
        {
            File file = new File(input);

            if (isDirectory && !file.isDirectory())
            {
                result = false;
                showMessage("dir.notdirectory");
            }
            else if (!isDirectory && !file.isFile())
            {
                result = false;
                showMessage("file.notfile");
            }
            else
            {
                ValidationStatus status = field.validate(input);
                if (!status.isValid())
                {
                    JOptionPane.showMessageDialog(parentFrame, status.getMessage(),
                                                  parentFrame.getMessages().get("UserInputPanel.error.caption"),
                                                  JOptionPane.WARNING_MESSAGE);
                }
                else
                {
                    result = true;
                }
            }
        }
        else
        {
            if (isDirectory)
            {
                showMessage("dir.nodirectory");
            }
            else
            {
                showMessage("file.nofile");
            }
        }
        return result;
    }

    public boolean validateField()
    {
        boolean result = false;
        int fileCount = model.getSize();

        if (fileCount == 0 && allowEmpty)
        {
            return true;
        }
        else
        {
            for (int i = 0; i < fileCount; i++)
            {
                result = validateFile((String) model.getElementAt(i));
                if (!result)
                {
                    break;
                }
            }
        }

        return result;

    }

    @Override
    public void focusGained(FocusEvent e)
    {
    }

    @Override
    public void focusLost(FocusEvent e)
    {
    }

    public boolean isCreateMultipleVariables()
    {
        return createMultipleVariables;
    }

}
