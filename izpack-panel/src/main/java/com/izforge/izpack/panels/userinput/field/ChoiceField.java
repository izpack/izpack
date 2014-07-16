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

package com.izforge.izpack.panels.userinput.field;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;


/**
 * A field with a number of pre-defined choices.
 *
 * @author Tim Anderson
 */
public abstract class ChoiceField extends Field
{
    /**
     * The available choices.
     */
    private final List<Choice> choices;

    private final ChoiceFieldConfig config;

    /**
     * Constructs a {@code ChoiceField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     */
    public ChoiceField(ChoiceFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.config = config;
        this.choices = config.getChoices();
    }

    /**
     * Returns the choices.
     *
     * @return the choices. The key is the choice identifier, the value, the display text
     */
    public List<Choice> getChoices()
    {
        return choices;
    }

    /**
     * Returns the selected choice.
     *
     * @return the selected choice, or {@code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return config.getSelectedIndex(getVariable());
    }
}
