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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.handler.AbstractPrompt;
import com.izforge.izpack.api.handler.Prompt;

/**
 * An {@link Prompt} that simply logs messages.
 *
 * @author Tim Anderson
 */
public class LoggingPrompt extends AbstractPrompt
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(LoggingPrompt.class.getName());

    /**
     * Singleton instance.
     */
    public static final Prompt INSTANCE = new LoggingPrompt();

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
        Level level;
        switch (type)
        {
            case ERROR:
                level = Level.SEVERE;
                break;
            case WARNING:
                level = Level.WARNING;
                break;
            default:
                level = Level.INFO;
        }
        logger.log(level, message);
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
        message(type, message);
        return defaultOption;
    }
}
