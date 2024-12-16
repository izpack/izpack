/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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

import com.google.inject.Inject;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.AbstractPrompt;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
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
     * The install data.
     */
    private final InstallData installData;

    /**
     * Constructs a {@code ConsolePrompt}.
     *
     * @param console  the console
     * @param installData
     */
    @Inject
    public ConsolePrompt(Console console, InstallData installData)
    {
        this.console = console;
        this.installData = installData;
    }

    @Override
    public void message(Type type, String title, String message, Throwable throwable)
    {
        if (title != null)
        {
            console.println(title + ":");
        }
        console.println(message);
        if (throwable != null)
        {
            console.println(getDetails(throwable));
        }
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
        Messages messages = installData.getMessages();
        String okCancelPrompt = messages.get("ConsolePrompt.okCancel");
        String yesNoPrompt = messages.get("ConsolePrompt.yesNo");
        String yesNoCancelPrompt = messages.get("ConsolePrompt.yesNoCancel");
        String ok = messages.get("ConsolePrompt.ok");
        String cancel = messages.get("ConsolePrompt.cancel");
        String yes = messages.get("ConsolePrompt.yes");
        String no = messages.get("ConsolePrompt.no");

        console.printMessageBox(title, message);
        if (options == Options.OK_CANCEL)
        {
            String defaultValue = (defaultOption != null && defaultOption == Option.OK) ? ok : cancel;
            String selected = console.prompt(okCancelPrompt, new String[]{ok, cancel}, defaultValue);
            if (ok.equals(selected))
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
            String defaultValue = cancel;
            if (defaultOption != null)
            {
                if (defaultOption == Option.YES)
                {
                    defaultValue = yes;
                }
                else if (defaultOption == Option.NO)
                {
                    defaultValue = no;
                }
            }
            String selected = console.prompt(yesNoCancelPrompt, new String[]{yes, no, cancel}, defaultValue);
            if (yes.equals(selected))
            {
                result = Option.YES;
            }
            else if (no.equals(selected))
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
            String defaultValue = no;
            if (defaultOption != null && defaultOption == Option.YES)
            {
                defaultValue = yes;
            }
            String selected = console.prompt(yesNoPrompt, new String[]{yes, no}, defaultValue);
            if (yes.equals(selected))
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
