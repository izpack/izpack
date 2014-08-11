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


/**
 * Configuration for fields that have a set of pre-defined choices.
 *
 * @author Tim Anderson
 */
public interface ChoiceFieldConfig extends FieldConfig
{
    /**
     * Returns the choices.
     *
     * @return the choices
     */
    List<Choice> getChoices();

    /**
     * Returns the index of the selected choice.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    int getSelectedIndex(String variable);
}
