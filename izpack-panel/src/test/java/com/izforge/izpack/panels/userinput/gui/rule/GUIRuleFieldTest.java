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

package com.izforge.izpack.panels.userinput.gui.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.userinput.LoggingPrompt;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFormat;
import com.izforge.izpack.panels.userinput.field.rule.TestRuleFieldConfig;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.Platforms;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;


/**
 * Tests the {@link GUIRuleField}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(DefaultContainer.class)
public class GUIRuleFieldTest
{
    @Inject
    private ConditionContainer conditionContainer;

    /**
     * The install data.
     */
    private GUIInstallData installData;


    /**
     * Default constructor.
     */
    @PostConstruct
    public void postConstruct()
    {
        installData = new GUIInstallData(new DefaultVariables(), Platforms.HP_UX);
        RulesEngine rules = new RulesEngineImpl(installData, conditionContainer);
        installData.setRules(rules);
    }

    /**
     * Tests support for entering IP addresses.
     */
    @Test
    public void testIPAddress()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String separator = null;
        String variable = "variable1";
        String initialValue = "192.168.0.1";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setInitialValue(initialValue);

        RuleField model = new RuleField(config, installData);

        GUIRuleField field = new GUIRuleField(model);
        assertTrue(field.updateView()); // Update: Empty field -> initial value
        assertFalse(field.updateView()); // should be nothing to update

        // check default value
        assertEquals("192.168.0.1", field.getValue());

        String[] values = field.getValues();
        assertEquals(4, values.length);
        assertEquals("192", values[0]);
        assertEquals("168", values[1]);
        assertEquals("0", values[2]);
        assertEquals("1", values[3]);

        assertTrue(field.updateField(LoggingPrompt.INSTANCE));

        assertEquals("192.168.0.1", installData.getVariable(variable));

        field.setValues("127", "0", "0", "1");
        assertTrue(field.updateField(LoggingPrompt.INSTANCE));
        assertEquals("127.0.0.1", installData.getVariable(variable));

        // the following is a bit ridiculous but highlights that a minimum length can't be specified for a field
        field.setValues("", "", "", "");
        assertTrue(field.updateField(LoggingPrompt.INSTANCE));
        assertEquals("...", installData.getVariable(variable));
    }

    /**
     * Tests the specification of a {@link Processor} as part of the 'set' attribute.
     */
    @Test
    public void testDefaultValueProcessorOnePart()
    {
        String layout = "O:25:U";
        String variable = "variable1";
        String separator = null;
        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setInitialValue("localhost");
        RuleField model = new RuleField(config, installData);

        GUIRuleField field = new GUIRuleField(model);
        assertTrue(field.updateView()); // Update: Empty field -> initial value
        assertFalse(field.updateView()); // should be nothing to update
        assertEquals("localhost", field.getValue());

        assertNull(installData.getVariable("variable1"));
        assertTrue(field.updateField(LoggingPrompt.INSTANCE));
        assertEquals("localhost", installData.getVariable(variable));
    }
}

