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

package com.izforge.izpack.panels.userinput.console.password;

import java.util.List;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.password.PasswordField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.gui.password.PasswordGroup;
import com.izforge.izpack.util.Console;


/**
 * Console presentation of {@link PasswordGroupField}.
 *
 * @author Tim Anderson
 */
public class ConsolePasswordGroupField extends ConsoleField
{

    /**
     * Constructs a {@link ConsolePasswordGroupField}.
     *
     * @param field   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsolePasswordGroupField(PasswordGroupField field, Console console, Prompt prompt)
    {
        super(field, console, prompt);
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public PasswordGroupField getField()
    {
        return (PasswordGroupField) super.getField();
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
        String[] passwords = getPasswords();
        if (passwords != null)
        {
            PasswordGroupField field = getField();
            ValidationStatus status = field.validate(new PasswordGroup(passwords));
            if (status.isValid())
            {
                try
                {
                    String value = field.process(passwords);
                    field.setValue(value);
                    result = true;
                }
                catch (Throwable exception)
                {
                    error(exception.getMessage());
                }
            }
            else
            {
                error(status.getMessage());
            }
        }
        return result;
    }

    /**
     * Returns the passwords.
     *
     * @return the passwords
     */
    private String[] getPasswords()
    {
        List<PasswordField> fields = getField().getPasswordFields();
        Console console = getConsole();
        String[] values = new String[fields.size()];
        for (int i = 0; i < fields.size(); ++i)
        {
            String value = console.prompt(fields.get(i).getLabel(), null);
            if (value == null)
            {
                return null;
            }
            values[i] = value;
        }
        return values;
    }
}
