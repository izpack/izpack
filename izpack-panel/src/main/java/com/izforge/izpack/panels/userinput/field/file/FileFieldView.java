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

package com.izforge.izpack.panels.userinput.field.file;

import static com.izforge.izpack.api.handler.Prompt.Type.WARNING;

import java.io.File;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;


/**
 * Presentation helper for file fields.
 *
 * @author Tim Anderson
 */
public class FileFieldView
{

    /**
     * The file field.
     */
    private final AbstractFileField field;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs an {@code FileFieldView}.
     *
     * @param field  the field
     * @param prompt the prompt
     */
    public FileFieldView(AbstractFileField field, Prompt prompt)
    {
        this.field = field;
        this.prompt = prompt;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    public AbstractFileField getField()
    {
        return field;
    }

    /**
     * Validates a path.
     *
     * @param path the path to validate
     * @return {@code true} if the path is valid, {@code false if it is invalid}
     */
    public boolean validate(String path)
    {
        boolean result = false;
        boolean empty = (path == null) || (path.length() == 0);
        if (empty)
        {
            if (field.getAllowEmptyValue())
            {
                result = true;
            }
            else
            {
                emptyPathMessage();
            }
        }
        else
        {
            File file = field.getAbsoluteFile(path);
            path = file.toString();

            if (validate(file))
            {
                ValidationStatus status = field.validate(path);
                if (!status.isValid())
                {
                    prompt.message(WARNING, getMessage("UserInputPanel.error.caption"), status.getMessage());
                }
                else
                {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Returns the prompt.
     *
     * @return the prompt
     */
    protected Prompt getPrompt()
    {
        return prompt;
    }

    /**
     * Returns a localised message for the supplied message identifier.
     *
     * @param id the message identifier
     * @return the corresponding message, or {@code id} if the message does not exist
     */
    protected String getMessage(String id)
    {
        Messages messages = field.getInstallData().getMessages();
        return messages.get(id);
    }

    /**
     * Validates a value.
     *
     * @param value the value to validate
     * @return {@code true} if the value is valid, {@code false} if it is invalid
     */
    protected boolean validate(File value)
    {
        boolean result = false;
        if (!field.mustExist || value.isFile())
        {
            result = true;
        }
        else
        {
            warn(getMessage("UserInputPanel.file.notfile.caption"), getMessage("UserInputPanel.file.notfile.message"));
        }
        return result;
    }

    /**
     * Displays a warning message if the path is empty.
     */
    protected void emptyPathMessage()
    {
        warn(getMessage("UserInputPanel.file.nofile.caption"), getMessage("UserInputPanel.file.nofile.message"));
    }

    /**
     * Displays a warning message.
     *
     * @param title   the warning title
     * @param message the warning message
     */
    protected void warn(String title, String message)
    {
        prompt.warn(title, message);
    }

    /**
     * Displays an error message.
     *
     * @param title   the error title
     * @param message the error message
     */
    protected void error(String title, String message)
    {
        prompt.error(title, message);
    }

}
