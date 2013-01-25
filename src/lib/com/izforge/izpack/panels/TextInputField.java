/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2008 Piotr Skowronek
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

import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.util.List;
import java.util.Map;

/*---------------------------------------------------------------------------*/
/**
 * This class is a wrapper for JTextField to allow field validation.
 * Based on RuleInputField.
 *
 * @author Piotr Skowronek
 */
/*---------------------------------------------------------------------------*/
public class TextInputField extends JComponent 
{

    /**
     *
     */
    private static final long serialVersionUID = 8611515659787697087L;

    /**
     * This composite can only contain one component ie JTextField
     */
    private JTextComponent field;

    IzPanel parent;
    List<ValidatorContainer> validators;
    InstallerFrame parentFrame;
    
    
    /*--------------------------------------------------------------------------*/
    /**
     * Constructs a text input field.
     *
     * @param set       A default value for field.
     * @param size      The size of the field.
     * @param rows      The number of rows in case this is a text area field
     * @param validator A string that specifies a class to perform validation services. The string
     *                  must completely identify the class, so that it can be instantiated. The class must implement
     *                  the <code>RuleValidator</code> interface. If an attempt to instantiate this class fails, no
     *                  validation will be performed.
     * @param validatorParams validator parameters.
     */
    /*--------------------------------------------------------------------------*/
    public TextInputField(IzPanel parent, String set, int size, int rows, List<ValidatorContainer> validatorConfig)
    {
        this.parent = parent;
        this.parentFrame = parent.getInstallerFrame();
        this.validators = validatorConfig;

        com.izforge.izpack.gui.FlowLayout layout = new com.izforge.izpack.gui.FlowLayout();
        layout.setAlignment(com.izforge.izpack.gui.FlowLayout.LEADING);
        layout.setVgap(0);
        setLayout(layout);

        // ----------------------------------------------------
        // construct the UI element and add it to the composite
        // ----------------------------------------------------
        if (rows > 1) {
            JTextArea area = new JTextArea(set, rows, size);
            area.setCaretPosition(0);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            
            field = area;
            add(new JScrollPane(area));
        } else {
            field = new JTextField(set, size);
            field.setCaretPosition(0);
            add(field);
        }
    }

    /*---------------------------------------------------------------------------*/
    /**
     * Returns the field contents, assembled acording to the encryption and separator rules.
     *
     * @return the field contents
     */
    /*--------------------------------------------------------------------------*/
    public String getText()
    {
        return (field.getText());
    }

    // javadoc inherited
    public void setText(String value)
    {
        field.setText(value);
    }

    // javadoc inherited
    public String getFieldContents(int index)
    {
        return field.getText();
    }

    // javadoc inherited
    public int getNumFields()
    {
        // We've got only one field
        return 1;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method validates the field content. Validating is performed through a user supplied
     * service class that provides the validation rules.
     *
     * @return <code>true</code> if the validation passes or no implementation of a validation
     *         rule exists. Otherwise <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    public boolean validateContents()
    {
        String input = field.getText();
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
        return success;
        
//        if (validationService != null)
//        {
//            Debug.trace("Validating contents");
//            return (validationService.validate(this));
//        }
//        else
//        {
//            Debug.trace("Not validating contents");
//            return (true);
//        }
    }

    // javadoc inherited
    // ----------------------------------------------------------------------------
}
/*---------------------------------------------------------------------------*/
