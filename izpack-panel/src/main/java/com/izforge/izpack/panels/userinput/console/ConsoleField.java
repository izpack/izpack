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

package com.izforge.izpack.panels.userinput.console;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.panels.userinput.field.AbstractFieldView;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.util.Console;


/**
 * Console presentation of an {@link Field}.
 *
 * @author Tim Anderson
 */
public abstract class ConsoleField extends AbstractFieldView
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs a {@code ConsoleField}.
     *
     * @param field   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleField(Field field, Console console, Prompt prompt)
    {
        super(field);
        this.console = console;
        this.prompt = prompt;
    }

    /**
     * Displays the field.
     * <p/>
     * For fields that update variables, this collects input and validates it.
     *
     * @return {@code true} if the field was displayed and validated successfully
     */
    public abstract boolean display();

    /**
     * Prints the field description, if one is available.
     */
    protected void printDescription()
    {
        String description = getField().getDescription();
        if (description != null)
        {
            println(description);
        }
    }

    /**
     * Prints a message to the console with a new line.
     *
     * @param message the message to print
     */
    protected void println(String message)
    {
        console.println(message);
    }

    /** TODO: Not needed here just leave in the Console class
     * Displays a prompt and waits for numeric input.
     *
     * @param prompt       the prompt to display
     * @param min          the minimum allowed value
     * @param max          the maximum allowed value
     * @param defaultValue the default value to use, if no input is entered. Use a value {@code < min} if there is no
     *                     default
     * @param eof          the value to return if end of stream is reached
     * @return a value in the range of <tt>from..to</tt>, or <tt>eof</tt> if the end of stream is reached
     */
    protected int prompt(String prompt, int min, int max, int defaultValue, int eof)
    {
        return console.prompt(prompt, min, max, defaultValue, eof);
    }

    /**
     * Displays an error message.
     *
     * @param message the message
     */
    protected void error(String message)
    {
        prompt.error(getMessage("UserInputPanel.error.caption"), message);
    }

    /**
     * Returns the console.
     *
     * @return the console
     */
    protected Console getConsole()
    {
        return console;
    }

    /**
     * Returns a localised message for the supplied message identifier.
     *
     * @param id the message identifier
     * @return the corresponding message, or {@code id} if the message does not exist
     */
    protected String getMessage(String id)
    {
        InstallData installData = getField().getInstallData();
        Messages messages = installData.getMessages();
        return messages.get(id);
    }

}
