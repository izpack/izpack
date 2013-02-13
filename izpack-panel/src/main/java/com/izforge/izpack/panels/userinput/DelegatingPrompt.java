/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.api.handler.Prompt;

/**
 * Implementation of {@link Prompt} that delegates all requests to another prompt.
 * The underlying prompt may be switched.
 *
 * @author Tim Anderson
 */
public class DelegatingPrompt implements Prompt
{

    /**
     * The prompt to delegate to.
     */
    private Prompt prompt;

    /**
     * Constructs a {@code DelegatingPrompt}.
     *
     * @param prompt the prompt to delegate to
     */
    public DelegatingPrompt(Prompt prompt)
    {
        this.prompt = prompt;
    }

    /**
     * Registers the prompt to delegate to.
     *
     * @param prompt the prompt
     */
    public void setPrompt(Prompt prompt)
    {
        this.prompt = prompt;
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    @Override
    public void message(Type type, String message)
    {
        prompt.message(type, message);
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
        prompt.message(type, title, message);
    }

    /**
     * Displays a warning message.
     *
     * @param message the message to display
     */
    @Override
    public void warn(String message)
    {
        prompt.warn(message);
    }

    /**
     * Displays a warning message.
     *
     * @param title   the message title. May be {@code null}
     * @param message the message to display
     */
    @Override
    public void warn(String title, String message)
    {
        prompt.warn(title, message);
    }

    /**
     * Displays an error message.
     *
     * @param message the message to display
     */
    @Override
    public void error(String message)
    {
        prompt.error(message);
    }

    /**
     * Displays an error message.
     *
     * @param title   the message title. May be {@code null}
     * @param message the message display
     */
    @Override
    public void error(String title, String message)
    {
        prompt.error(title, message);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options)
    {
        return prompt.confirm(type, message, options);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options, Option defaultOption)
    {
        return prompt.confirm(type, message, options, defaultOption);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param title   the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String title, String message, Options options)
    {
        return prompt.confirm(type, title, message, options);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param title         the message title. May be {@code null}
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String title, String message, Options options, Option defaultOption)
    {
        return prompt.confirm(type, title, message, options, defaultOption);
    }
}
