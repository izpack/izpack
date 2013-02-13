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
package com.izforge.izpack.panels.userinput.console.radio;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.ChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.choice.TestChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.radio.RadioChoice;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;


/**
 * Tests the {@link ConsoleRadioField}.
 *
 * @author Tim Anderson
 */
public class ConsoleRadioFieldTest extends AbstractConsoleFieldTest
{

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        ConsoleRadioField field = createField(1);
        checkValid(field, "\n");
        assertEquals("B", installData.getVariable("radio"));
    }


    /**
     * Tests selection of a particular value.
     */
    @Test
    public void testSetValue()
    {
        ConsoleRadioField field = createField(-1);
        checkValid(field, "2");
        assertEquals("C", installData.getVariable("radio"));
    }

    /**
     * Creates a new {@link ConsoleRadioField} that updates the "radio" variable.
     *
     * @param selected the initial selection
     * @return a new field
     */
    private ConsoleRadioField createField(int selected)
    {
        List<RadioChoice> choices = new ArrayList<RadioChoice>();
        choices.add(new RadioChoice("A", "A Choice", false));
        choices.add(new RadioChoice("B", "B Choice", false));
        choices.add(new RadioChoice("C", "C Choice", false));
        ChoiceFieldConfig<RadioChoice> config = new TestChoiceFieldConfig<RadioChoice>("radio", choices, selected);

        RadioField model = new RadioField(config, installData);
        return new ConsoleRadioField(model, console, prompt);
    }
}
