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


import static com.izforge.izpack.api.handler.Prompt.Option;
import static com.izforge.izpack.api.handler.Prompt.Option.CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Option.NO;
import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Option.YES;
import static com.izforge.izpack.api.handler.Prompt.Options.OK_CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO_CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Type.ERROR;
import static com.izforge.izpack.api.handler.Prompt.Type.INFORMATION;
import static com.izforge.izpack.api.handler.Prompt.Type.QUESTION;
import static com.izforge.izpack.api.handler.Prompt.Type.WARNING;

import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.handler.Prompt;


/**
 * An {@link AbstractUIHandler} implemented using {@link Prompt}.
 *
 * @author Tim Anderson
 */
public class PromptUIHandler implements AbstractUIHandler
{
    /**
     * The prompt.
     */
    private final Prompt prompt;


    /**
     * Constructs a {@code PromptUIHandler}.
     *
     * @param prompt the prompt
     */
    public PromptUIHandler(Prompt prompt)
    {
        this.prompt = prompt;
    }

    /**
     * Notify the user about something.
     * <p/>
     * The difference between notification and warning is that a notification should not need user
     * interaction and can safely be ignored.
     *
     * @param message The notification.
     */
    @Override
    public void emitNotification(String message)
    {
        prompt.message(INFORMATION, message);
    }

    /**
     * Warn the user about something.
     *
     * @param title   the message title (used for dialog name, might not be displayed)
     * @param message the warning message.
     * @return true if the user decided to continue
     */
    @Override
    public boolean emitWarning(String title, String message)
    {
        return prompt.confirm(WARNING, title, message, OK_CANCEL, OK) == OK;
    }

    /**
     * Notify the user of some error.
     *
     * @param title   the message title (used for dialog name, might not be displayed)
     * @param message the error message
     */
    @Override
    public void emitError(String title, String message)
    {
        prompt.message(ERROR, title, message);
    }

    /**
     * Notify the user of some error and block the next button.
     *
     * @param title   The message title (used for dialog name, might not be displayed)
     * @param message The error message.
     * @deprecated Inject the InstallerFrame to disable the next button
     */
    @Override
    public void emitErrorAndBlockNext(String title, String message)
    {
        emitError(title, message);
    }

    /**
     * Ask the user a question.
     *
     * @param title    Message title.
     * @param question The question.
     * @param choices  The set of choices to present.
     * @return The user's choice.
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#askQuestion(String, String, int)
     */
    @Override
    public int askQuestion(String title, String question, int choices)
    {
        return askQuestion(title, question, choices, -1);
    }

    /**
     * Ask the user a question.
     *
     * @param title          Message title.
     * @param question       The question.
     * @param choices        The set of choices to present.
     * @param default_choice The default choice. (-1 = no default choice)
     * @return The user's choice.
     * @see com.izforge.izpack.api.handler.AbstractUIHandler#askQuestion(String, String, int, int)
     */
    @Override
    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        int choice;

        if (choices == AbstractUIHandler.CHOICES_YES_NO)
        {
            Option defaultValue;
            switch (default_choice)
            {
                case ANSWER_YES:
                    defaultValue = YES;
                    break;
                case ANSWER_NO:
                    defaultValue = NO;
                    break;
                default:
                    defaultValue = null;
            }
            Option selected = prompt.confirm(QUESTION, title, question, YES_NO, defaultValue);
            choice = (selected == YES) ? AbstractUIHandler.ANSWER_YES : AbstractUIHandler.ANSWER_NO;
        }
        else
        {
            Prompt.Option defaultValue;
            switch (default_choice)
            {
                case ANSWER_YES:
                    defaultValue = YES;
                    break;
                case ANSWER_NO:
                    defaultValue = NO;
                    break;
                case ANSWER_CANCEL:
                    defaultValue = CANCEL;
                    break;
                default:
                    defaultValue = null;
            }
            Option selected = prompt.confirm(QUESTION, title, question, YES_NO_CANCEL, defaultValue);
            if (selected == YES)
            {
                choice = AbstractUIHandler.ANSWER_YES;
            }
            else if (selected == NO)
            {
                choice = AbstractUIHandler.ANSWER_NO;
            }
            else
            {
                choice = AbstractUIHandler.ANSWER_CANCEL;
            }
        }

        return choice;
    }

}
