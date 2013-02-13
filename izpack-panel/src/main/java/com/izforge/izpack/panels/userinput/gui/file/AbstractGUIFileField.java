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

import java.io.File;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.file.AbstractFileField;
import com.izforge.izpack.panels.userinput.gui.GUIField;


/**
 * Common functionality for file field views.
 *
 * @author Tim Anderson
 */
public abstract class AbstractGUIFileField extends GUIField
{
    /**
     * The field view implementation.
     */
    private FileInputField fileInput;

    /**
     * Constructs an {@code AbstractGUIFileField}.
     *
     * @param field the field
     */
    public AbstractGUIFileField(AbstractFileField field)
    {
        super(field);
    }

    /**
     * Updates the field from the view.
     *
     * @param prompt the prompt to display messages
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField(Prompt prompt)
    {
        boolean result = false;
        if (fileInput.validateField())
        {
            File selectedFile = fileInput.getSelectedFile();
            getField().setValue(selectedFile.getAbsolutePath());
            result = true;
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
            fileInput.setFile(value);
            result = true;
        }
        return result;
    }

    /**
     * Initialises the view with the view implementation.
     *
     * @param inputField the view implementation
     */
    protected void init(FileInputField inputField)
    {
        this.fileInput = inputField;
        if (getField().getLabel() != null)
        {
            addLabel();
            addComponent(inputField, new TwoColumnConstraints(TwoColumnConstraints.EAST));
        }
        else
        {
            addComponent(inputField, new TwoColumnConstraints(TwoColumnConstraints.BOTH));
        }
    }
}
