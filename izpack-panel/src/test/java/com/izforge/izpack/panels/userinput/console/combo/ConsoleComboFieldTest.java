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

package com.izforge.izpack.panels.userinput.console.combo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.choice.TestChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;


/**
 * Tests the {@link ConsoleComboField}.
 *
 * @author Tim Anderson
 */
public class ConsoleComboFieldTest extends AbstractConsoleFieldTest
{
    private final RulesEngine rules = installData.getRules();

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        rules.addCondition(new BooleanCondition("showCondChoice", true, installData));
        ConsoleComboField field = createField(1);
        checkValid(field, "\n");
        assertEquals("B", installData.getVariable("combo"));
    }

    /**
     * Tests selection of a particular value.
     */
    @Test
    public void testSetValue()
    {
        rules.addCondition(new BooleanCondition("showCondChoice", true, installData));
        ConsoleComboField field = createField(-1);
        checkValid(field, "2");
        assertEquals("C", installData.getVariable("combo"));
    }

    /**
     * Tests choice not available due to false condition
     */
    @Test
    public void testConditionalValueFalse()
    {
        rules.addCondition(new BooleanCondition("showCondChoice", false, installData));
        ConsoleComboField field = createField(1);
        checkValid(field, "3");
        assertEquals("D", installData.getVariable("combo"));
    }

    /**
     * Tests choice available due to true condition
     */
    @Test
    public void testConditionalValueTrue()
    {
        rules.addCondition(new BooleanCondition("showCondChoice", true, installData));
        ConsoleComboField field = createField(1);
        checkValid(field, "3");
        assertEquals("X", installData.getVariable("combo"));
    }

    /**
     * Creates a new {@link ConsoleComboField} that updates the "radio" variable.
     *
     * @param selected the initial selection
     * @return a new field
     */
    private ConsoleComboField createField(int selected)
    {
        List<Choice> choices = new ArrayList<Choice>();
        choices.add(new Choice("A", "A Choice"));
        choices.add(new Choice("B", "B Choice"));
        choices.add(new Choice("C", "C Choice"));
        Choice conditionalChoice = new Choice("X", "X Choice", "showCondChoice");
        choices.add(conditionalChoice);
        choices.add(new Choice("D", "D Choice"));
        ChoiceFieldConfig config = new TestChoiceFieldConfig<Choice>("combo", choices, selected);

        ComboField model = new ComboField(config, installData);
        return new ConsoleComboField(model, console, prompt);
    }

    private static class BooleanCondition extends Condition
    {
        private static final long serialVersionUID = 2558942007157683355L;
        private final boolean value;

        BooleanCondition(String id, boolean value, InstallData installData)
        {
            this.value = value;
            super.setId(id);
            super.setInstallData(installData);
        }
        @Override
        public void readFromXML(IXMLElement xmlcondition) throws Exception {}
        @Override
        public void makeXMLData(IXMLElement conditionRoot) {}
        @Override
        public boolean isTrue() { return this.value; }

        @Override
        public Set<String> getNeededVariableNames()
        {
            return new HashSet<String>(1);
        }
    }
}
