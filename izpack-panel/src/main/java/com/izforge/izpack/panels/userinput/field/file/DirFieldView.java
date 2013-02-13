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

import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Options.OK_CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Type.WARNING;

import java.io.File;

import com.izforge.izpack.api.handler.Prompt;


/**
 * Presentation helper for directory fields.
 *
 * @author Tim Anderson
 */
public class DirFieldView extends FileFieldView
{

    /**
     * Constructs a {@code DirFieldView}.
     *
     * @param field  the directory field
     * @param prompt the prompt
     */
    public DirFieldView(DirField field, Prompt prompt)
    {
        super(field, prompt);
    }

    /**
     * Validates a value.
     *
     * @param value the value to validate
     * @return {@code true} if the value is valid, {@code false} if it is invalid
     */
    @Override
    protected boolean validate(File value)
    {
        boolean result;
        DirField field = getField();
        if (value.isDirectory())
        {
            result = true;
        }
        else if (value.isFile() || field.getMustExist())
        {
            error(getMessage("UserInputPanel.dir.notdirectory.caption"),
                  getMessage("UserInputPanel.dir.notdirectory.message"));
            result = false;
        }
        else
        {
            result = !field.getCreate() || initDir(value);
        }
        return result;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public DirField getField()
    {
        return (DirField) super.getField();
    }

    /**
     * Displays a warning message if the path is empty.
     */
    @Override
    protected void emptyPathMessage()
    {
        warn(getMessage("UserInputPanel.dir.nodirectory.caption"),
             getMessage("UserInputPanel.dir.nodirectory.message"));
    }

    /**
     * Sets up a directory. This creates it if it doesn't exist, and verifies it can be written to.
     *
     * @param path the directory path
     * @return {@code true} if the directory was created, {@code false} creation was vetoed by the user or creation
     *         failed
     */
    private boolean initDir(File path)
    {
        DirField field = getField();
        Prompt prompt = getPrompt();
        if (!path.exists())
        {
            String title = getMessage("installer.Message");
            String message = getMessage("UserPathPanel.createdir") + "\n" + path.getAbsolutePath();
            if (prompt.confirm(WARNING, title, message, OK_CANCEL, OK) != OK)
            {
                return false;
            }
            if (!path.mkdirs())
            {
                error(getMessage("installer.error"), getMessage("UserPathPanel.notwritable"));
                return false;
            }
        }

        // check that the directory is writable
        if (!field.isWritable(path))
        {
            error(getMessage("installer.error"), getMessage("UserPathPanel.notwritable"));
            return false;
        }
        return true;
    }

}
