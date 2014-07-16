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

package com.izforge.izpack.panels.userinput.console.file;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.file.AbstractFileField;
import com.izforge.izpack.panels.userinput.field.file.FileFieldView;
import com.izforge.izpack.util.Console;

/**
 * Base class for console presentations of file fields.
 *
 * @author Tim Anderson
 */
public class AbstractConsoleFileField extends ConsoleField
{

    /**
     * The file field view helper.
     */
    private final FileFieldView view;


    /**
     * Constructs a {@link AbstractConsoleFileField}.
     *
     * @param view    the field view
     * @param console the console
     * @param prompt  the prompt
     */
    public AbstractConsoleFileField(FileFieldView view, Console console, Prompt prompt)
    {
        super(view.getField(), console, prompt);
        this.view = view;
    }

    /**
     * Displays the field.
     * <p/>
     * For fields that update variables, this collects input and validates it.
     *
     * @return {@code true} if the field was displayed and validated successfully
     */
    @Override
    public boolean display()
    {
        boolean result = false;
        printDescription();
        AbstractFileField field = getField();
        String initialValue = field.getInitialValue();
        String prompt = field.getLabel() + "[" + ((initialValue != null) ? initialValue : "") + "] ";
        String path = getConsole().promptLocation(prompt, null);
        if (path != null)
        {
            path = path.trim();
            if ("".equals(path))
            {
                path = initialValue;
            }
            if (path != null && !"".equals(path))
            {
                path = field.getAbsoluteFile(path).toString();
            }
            if (view.validate(path))
            {
                field.setValue(path);
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public AbstractFileField getField()
    {
        return (AbstractFileField) super.getField();
    }
}
