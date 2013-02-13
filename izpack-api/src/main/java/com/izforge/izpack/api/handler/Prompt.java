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

package com.izforge.izpack.api.handler;

/**
 * Prompts users for a value or informs them of something.
 *
 * @author Tim Anderson
 */
public interface Prompt
{
    /**
     * The prompt type.
     */
    enum Type
    {
        INFORMATION, QUESTION, WARNING, ERROR
    }

    /**
     * A prompt option.
     */
    enum Option
    {
        OK, YES, NO, CANCEL
    }

    /**
     * Predefined options.
     */
    enum Options
    {
        OK_CANCEL, YES_NO, YES_NO_CANCEL
    }

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param message the message to display
     */
    void message(Type type, String message);

    /**
     * Displays a message.
     *
     * @param type    the type of the message
     * @param title   the message title. If {@code null}, the title will be determined from the type
     * @param message the message to display
     */
    void message(Type type, String title, String message);

    /**
     * Displays a warning message.
     *
     * @param message the message to display
     */
    void warn(String message);

    /**
     * Displays a warning message.
     *
     * @param title   the message title. May be {@code null}
     * @param message the message to display
     */
    void warn(String title, String message);

    /**
     * Displays an error message.
     *
     * @param message the message to display
     */
    void error(String message);

    /**
     * Displays an error message.
     *
     * @param title   the message title. May be {@code null}
     * @param message the message display
     */
    void error(String title, String message);

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    Option confirm(Type type, String message, Options options);

    /**
     * Displays a confirmation message.
     *
     * @param type          the type of the message
     * @param message       the message
     * @param options       the options which may be selected
     * @param defaultOption the default option to select
     * @return the selected option
     */
    Option confirm(Type type, String message, Options options, Option defaultOption);

    /**
     * Displays a confirmation message.
     *
     * @param type    the type of the message
     * @param title   the message title. May be {@code null}
     * @param message the message
     * @param options the options which may be selected
     * @return the selected option
     */
    Option confirm(Type type, String title, String message, Options options);

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
    Option confirm(Type type, String title, String message, Options options, Option defaultOption);

}