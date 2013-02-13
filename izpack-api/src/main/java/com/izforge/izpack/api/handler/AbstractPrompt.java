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
package com.izforge.izpack.api.handler;

/**
 * Abstract implementation of {@link Prompt}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPrompt implements Prompt
{

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    @Override
    public void message(Type type, String message)
    {
        message(type, null, message);
    }

    /**
     * Displays a warning message.
     *
     * @param message the message to display
     */
    @Override
    public void warn(String message)
    {
        warn(null, message);
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
        message(Type.WARNING, title, message);
    }

    /**
     * Displays an error message.
     *
     * @param message the message to display
     */
    @Override
    public void error(String message)
    {
        error(null, message);
    }

    /**
     * Displays an error message.
     *
     * @param title   the message title. May be {@code null}
     * @param message the message to display
     */
    @Override
    public void error(String title, String message)
    {
        message(Type.ERROR, title, message);
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
        return confirm(type, message, options, null);
    }

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select. May be {@code null}
     * @return the selected option
     */
    @Override
    public Option confirm(Type type, String message, Options options, Option defaultOption)
    {
        return confirm(type, null, message, options, defaultOption);
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
        return confirm(type, title, message, options, null);
    }

}
