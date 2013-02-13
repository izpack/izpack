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

package com.izforge.izpack.core.handler;

import com.izforge.izpack.api.handler.AbstractPrompt;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of {@link Prompt}.
 *
 * @author Tim Anderson
 */
public class ConsolePrompt extends AbstractPrompt
{
    /**
     * The console.
     */
    private final Console console;

    /**
     * Constructs a <tt>ConsolePrompt</tt>.
     *
     * @param console the console
     */
    public ConsolePrompt(Console console)
    {
        this.console = console;
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param title   the message title. If {@code null}, the title will be determined from the type
     * @param message the message to display
     */
    @Override
    public void message(Type type, String title, String message)
    {
        console.println(message);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param title         the message title. May be {@code null}
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select. May be {@code null}
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String title, String message, Options options, Option defaultOption)
    {
        Option result;
        console.println(message);
        if (options == Options.OK_CANCEL)
        {
            String defaultValue = (defaultOption != null && defaultOption == Option.OK) ? "O" : "C";
            String selected = console.prompt("Enter O for OK, C to Cancel", new String[]{"O", "C"}, defaultValue);
            if ("O".equals(selected))
            {
                result = Option.OK;
            }
            else
            {
                result = Option.CANCEL;
            }
        }
        else if (options == Options.YES_NO_CANCEL)
        {
            String defaultValue = "C";
            if (defaultOption != null)
            {
                if (defaultOption == Option.YES)
                {
                    defaultValue = "Y";
                }
                else if (defaultOption == Option.NO)
                {
                    defaultValue = "N";
                }
            }
            String selected = console.prompt("Enter Y for Yes, N for No, or C to Cancel",
                                             new String[]{"Y", "N", "C"}, defaultValue);
            if ("Y".equals(selected))
            {
                result = Option.YES;
            }
            else if ("N".equals(selected))
            {
                result = Option.NO;
            }
            else
            {
                result = Option.CANCEL;
            }
        }
        else
        {
            String defaultValue = "N";
            if (defaultOption != null && defaultOption == Option.YES)
            {
                defaultValue = "Y";
            }
            String selected = console.prompt("Enter Y for Yes or N for No", new String[]{"Y", "N"}, defaultValue);
            if ("Y".equals(selected))
            {
                result = Option.YES;
            }
            else
            {
                result = Option.NO;
            }
        }
        return result;
    }
}
