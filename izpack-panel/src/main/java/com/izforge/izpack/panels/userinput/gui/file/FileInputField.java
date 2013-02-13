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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.file.AbstractFileField;
import com.izforge.izpack.panels.userinput.field.file.FileFieldView;

public class FileInputField extends JPanel implements ActionListener
{
    private static final long serialVersionUID = 4673684743657328492L;

    private static final transient Logger logger = Logger.getLogger(FileInputField.class.getName());

    /**
     * The field view.
     */
    private final FileFieldView view;

    /**
     * The field.
     */
    private final AbstractFileField field;

    InstallerFrame parentFrame;

    IzPanel parent;

    JTextField filetxt;

    JButton browseBtn;

    GUIInstallData installDataGUI;

    public FileInputField(FileFieldView view, IzPanel parent, GUIInstallData installDataGUI)
    {
        this.view = view;
        this.field = view.getField();
        this.parent = parent;
        this.parentFrame = parent.getInstallerFrame();
        this.installDataGUI = installDataGUI;
        this.initialize();
    }

    public void setFile(String filename)
    {
        filetxt.setText(filename);
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource() == browseBtn)
        {
            logger.fine("Show directory chooser");
            String initialPath = ".";
            if (filetxt.getText() != null)
            {
                initialPath = filetxt.getText();
            }
            JFileChooser filechooser = new JFileChooser(initialPath);
            prepareFileChooser(filechooser);

            if (filechooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION)
            {
                String selectedFile = filechooser.getSelectedFile().getAbsolutePath();
                filetxt.setText(selectedFile);
                logger.fine("Setting current file chooser directory to: " + selectedFile);
            }
        }
    }

    protected void prepareFileChooser(JFileChooser filechooser)
    {
        filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String fileExtension = field.getFileExtension();
        String fileExtensionDescription = field.getFileExtensionDescription();
        if ((fileExtension != null) && (fileExtensionDescription != null))
        {
            UserInputFileFilter fileFilter = new UserInputFileFilter();
            fileFilter.setFileExt(fileExtension);
            fileFilter.setFileExtDesc(fileExtensionDescription);
            filechooser.setFileFilter(fileFilter);
        }
    }

    public File getSelectedFile()
    {
        File result = null;
        if (filetxt.getText() != null)
        {
            result = new File(filetxt.getText());
        }
        return result;
    }

    public boolean validateField()
    {
        String path = filetxt.getText();
        File file = field.getAbsoluteFile(path);
        filetxt.setText(file.getPath());
        return view.validate(filetxt.getText());
    }

    private void initialize()
    {
        int size = field.getSize() < 0 ? 0 : field.getSize();
        filetxt = new JTextField(field.getDefaultValue(), size);
        filetxt.setName(field.getVariable());
        filetxt.setCaretPosition(0);

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        GridBagConstraints fileTextConstraint = new GridBagConstraints();
        GridBagConstraints fileButtonConstraint = new GridBagConstraints();
        fileTextConstraint.gridx = 0;
        fileTextConstraint.gridy = 0;
        fileTextConstraint.anchor = GridBagConstraints.WEST;
        fileTextConstraint.insets = new Insets(0, 0, 0, 5);
        fileButtonConstraint.gridx = 1;
        fileButtonConstraint.gridy = 0;
        fileButtonConstraint.anchor = GridBagConstraints.WEST;

        // TODO: use separate key for button text
        browseBtn = ButtonFactory.createButton(installDataGUI.getMessages().get("UserInputPanel.search.browse"),
                                               installDataGUI.buttonsHColor);
        browseBtn.addActionListener(this);
        this.add(filetxt, fileTextConstraint);
        this.add(browseBtn, fileButtonConstraint);
    }

}
