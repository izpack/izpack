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

package com.izforge.izpack.panels.userinput.field.choice;

import java.util.List;

import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.TestFieldConfig;


/**
 * Implementation of {@link ChoiceFieldConfig} for testing purposes.
 *
 * @author Tim Anderson
 */
public class TestChoiceFieldConfig<T extends Choice> extends TestFieldConfig implements ChoiceFieldConfig<T>
{
    /**
     * The available choices.
     */
    private final List<T> choices;

    /**
     * The selected choice, or {@code -1} if no choice is selected.
     */
    private final int selected;

    /**
     * Constructs a {@code TestChoiceFieldConfig}.
     *
     * @param variable the variable
     * @param choices  the available choices
     * @param selected the initial selection, or {@code -1} if no choice is selected
     */
    public TestChoiceFieldConfig(String variable, List<T> choices, int selected)
    {
        super(variable);
        this.choices = choices;
        this.selected = selected;
    }

    /**
     * Returns the choices.
     *
     * @return the choices
     */
    @Override
    public List<T> getChoices(RulesEngine rules)
    {
        return choices;
    }

    /**
     * Returns the index of the selected choice.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    @Override
    public int getSelectedIndex()
    {
        return selected;
    }
}
