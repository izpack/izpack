/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.gui.GUIField;


/**
 * Multiple file field view.
 *
 * @author Tim Anderson
 */
public class GUIMultipleFileField extends GUIField
{

    /**
     * The view implementation.
     */
    private final MultipleFileInputField fileInput;


    /**
     * Constructs a {@code GUIMultipleFileField}.
     *
     * @param field       the field
     * @param installData the installation data
     * @param parent      the parent frame
     */
    public GUIMultipleFileField(MultipleFileField field, GUIInstallData installData, InstallerFrame parent)
    {
        super(field);
        fileInput = new MultipleFileInputField(field, parent, installData, false);
        addComponent(fileInput, new TwoColumnConstraints(TwoColumnConstraints.EAST));
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public MultipleFileField getField()
    {
        return (MultipleFileField) super.getField();
    }

    /**
     * Updates the field from the view.
     * <p/>
     * This implementation simply returns {@code true}.
     *
     * @param prompt the prompt to display messages
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField(Prompt prompt)
    {
        boolean result = fileInput.validateField();
        if (result)
        {
            getField().setValues(fileInput.getSelectedFiles());
        }
        return result;
    }

    /**
     * Updates the view from the field.
     *
     * @return {@code true} if the view was updated
     */
    @Override
    public boolean updateView()
    {
        boolean result = false;
        String value = getField().getValue();
        if (value != null)
        {
            fileInput.clearFiles();
            if (fileInput.isCreateMultipleVariables())
            {
                fileInput.addFile(value);
                // try to read more files
                String basevariable = getVariable();
                int index = 1;

                while (value != null)
                {
                    StringBuilder builder = new StringBuilder(basevariable);
                    builder.append("_");
                    builder.append(index++);
                    value = getInstallData().getVariable(builder.toString());
                    if (value != null)
                    {
                        fileInput.addFile(value);
                    }
                }
            }
            else
            {
                // split file string
                String[] files = value.split(";");
                for (String file : files)
                {
                    fileInput.addFile(file);
                }
            }
            result = true;
        }
        return result;
    }

}