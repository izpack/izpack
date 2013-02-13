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

package com.izforge.izpack.panels.userinput.console.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFormat;
import com.izforge.izpack.panels.userinput.field.rule.TestDefaultIPProcessor;
import com.izforge.izpack.panels.userinput.field.rule.TestRuleFieldConfig;
import com.izforge.izpack.panels.userinput.processor.Processor;


/**
 * Tests the {@link ConsoleRuleField}.
 *
 * @author Tim Anderson
 */
public class ConsoleRuleFieldTest extends AbstractConsoleFieldTest
{

    /**
     * The object factory.
     */
    private final ObjectFactory factory;

    /**
     * Default constructor.
     */
    public ConsoleRuleFieldTest()
    {
        factory = new DefaultObjectFactory(new DefaultContainer());
    }

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String defaultValue = "0:192 1:168 2:0 3:1";
        String separator = null;
        String variable = "variable1";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setDefaultValue(defaultValue);

        RuleField model = new RuleField(config, installData, factory);

        ConsoleRuleField field = new ConsoleRuleField(model, console, prompt);
        console.addScript("Select default", "\n");
        assertTrue(field.display());

        assertEquals("192.168.0.1", installData.getVariable(variable));
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
        String defaultValue = "0:192 1:168 2:0 3:1";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setDefaultValue(defaultValue);
        RuleField model = new RuleField(config, installData, factory);

        ConsoleRuleField field = new ConsoleRuleField(model, console, prompt);
        console.addScript("Set value", "127.0.0.1");
        assertTrue(field.display());

        assertEquals("127.0.0.1", installData.getVariable(variable));
    }

    /**
     * Tests the specification of a {@link Processor} as part of the 'set' attribute.
     */
    @Test
    public void testDefaultValueProcessor()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String variable = "variable1";
        String separator = null;
        String defaultValue = "0::" + TestDefaultIPProcessor.class.getName();
        // The processor will be run for the first field

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setDefaultValue(defaultValue);
        RuleField model = new RuleField(config, installData, factory);
        ConsoleRuleField field = new ConsoleRuleField(model, console, prompt);

        assertNull(installData.getVariable("variable1"));
        console.addScript("Select default", "\n");
        assertTrue(field.display());
        assertEquals("192.168.0.1", installData.getVariable(variable));
    }
}
