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

import java.util.List;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceField;
import com.izforge.izpack.util.Console;


/**
 * Console presentation of {@link ChoiceField}.
 *
 * @author Tim Anderson
 */
public abstract class ConsoleChoiceField<T extends Choice> extends ConsoleField
{
    /**
     * Constructs a {@link ConsoleChoiceField}.
     *
     * @param field   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleChoiceField(ChoiceField<T> field, Console console, Prompt prompt)
    {
        super(field, console, prompt);
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    @SuppressWarnings("unchecked")
    public ChoiceField<T> getField()
    {
        return (ChoiceField<T>) super.getField();
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
        ChoiceField<T> field = getField();
        printDescription();

        List<T> choices = field.getChoices();
        listChoices(choices, field.getSelectedIndex());

        int selected = getConsole().prompt("input selection: ", 0, choices.size() - 1, field.getSelectedIndex(), -1);
        if (selected == -1)
        {
            return false;
        }
        field.setValue(choices.get(selected).getKey());
        return true;
    }

    /**
     * Displays the choices.
     *
     * @param choices  the choices
     * @param selected the selected choice, or {@code -1} if no choice is selected
     */
    protected void listChoices(List<T> choices, int selected)
    {
        for (int i = 0; i < choices.size(); ++i)
        {
            Choice choice = choices.get(i);
            println(i + "  [" + (i == selected ? "x" : " ") + "] " + choice.getValue());
        }
    }

}
