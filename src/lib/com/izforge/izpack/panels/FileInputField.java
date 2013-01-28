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
package com.izforge.izpack.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;

public class FileInputField extends JPanel implements ActionListener
{

    private static final long serialVersionUID = 4673684743657328492L;

    InstallerFrame parentFrame;

    IzPanel parent;

    List<ValidatorContainer> validators;

    JTextField filetxt;

    JButton browseBtn;

    String set;

    int size;

    InstallData data;

    String fileExtension;

    String fileExtensionDescription;

    boolean allowEmpty;
    
    protected static final int INVALID = 0, EMPTY = 1; 

    public FileInputField(IzPanel parent, InstallData data, boolean directory, String set,
            int size, List<ValidatorContainer> validatorConfig)
    {
        this(parent, data, directory, set, size, validatorConfig, null, null);
    }

    public FileInputField(IzPanel parent, InstallData data, boolean directory, String set,
            int size, List<ValidatorContainer> validatorConfig, String fileExt, String fileExtDesc)
    {
        this.parent = parent;
        this.parentFrame = parent.getInstallerFrame();
        this.data = data;
        this.validators = validatorConfig;
        this.set = set;
        this.size = size;
        this.fileExtension = fileExt;
        this.fileExtensionDescription = fileExtDesc;
        this.initialize();
    }

    private void initialize()
    {
        //this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        filetxt = new JTextField(set, size);
        filetxt.setCaretPosition(0);
        
        // TODO: use separate key for button text
        browseBtn = ButtonFactory.createButton(data.langpack
                .getString("UserInputPanel.search.browse"), data.buttonsHColor);
        browseBtn.addActionListener(this);
        this.add(filetxt);
        this.add(browseBtn);
    }

    public void setFile(String filename)
    {
        filetxt.setText(filename);
    }

    public void actionPerformed(ActionEvent arg0)
    {
        if (arg0.getSource() == browseBtn)
        {
            Debug.trace("Show dirchooser");
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
                Debug.trace("Setting current file chooser directory to: " + selectedFile);
            }
        }
    }
    
    protected void prepareFileChooser(JFileChooser filechooser) {
        filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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

    protected void showMessage(int k)
    {
        if (k == INVALID)
        {
            showMessage("file.notfile");
        }
        else if (k == EMPTY)
        {
            showMessage("file.nofile");
        }
    }
    
    protected void showMessage(String messageType)
    {
        JOptionPane.showMessageDialog(parentFrame, parentFrame.langpack.getString("UserInputPanel."
                + messageType + ".message"), parentFrame.langpack.getString("UserInputPanel."
                + messageType + ".caption"), JOptionPane.WARNING_MESSAGE);
    }

    public boolean validateField()
    {
        boolean result = false;
        String input = filetxt.getText();
        
        if (allowEmpty && ((input == null) || (input.length() == 0)))
        {
            result = true;
        }
        else if (input != null)
        {
            // Expand unix home reference
            if (input.startsWith("~"))
            {
                String home = System.getProperty("user.home");
                input = home + input.substring(1);
            }

            // Normalize the path
            File file = new File(input).getAbsoluteFile();
            input = file.toString();

            filetxt.setText(input);
            
            if (!_validate(file))
            {
                result = false;
                showMessage(INVALID);
            }
            else
            {
                StringInputProcessingClient processingClient = new StringInputProcessingClient(
                        input, validators);
                boolean success = processingClient.validate();
                if (!success)
                {
                    JOptionPane
                            .showMessageDialog(parentFrame,
                                    processingClient.getValidationMessage(), parentFrame.langpack
                                            .getString("UserInputPanel.error.caption"),
                                    JOptionPane.WARNING_MESSAGE);
                }
                result = success;
            }
        }
        else
        {
            showMessage(EMPTY);
        }
        return result;
    }

    protected boolean _validate(File file)
    {
        return file.isFile();
    }

    public boolean isAllowEmptyInput()
    {
        return allowEmpty;
    }

    public void setAllowEmptyInput(boolean allowEmpty)
    {
        this.allowEmpty = allowEmpty;
    }
}
