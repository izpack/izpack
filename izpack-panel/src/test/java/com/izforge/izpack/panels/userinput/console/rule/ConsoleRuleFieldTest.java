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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFormat;
import com.izforge.izpack.panels.userinput.field.rule.TestRuleFieldConfig;

/**
 * Tests the {@link ConsoleRuleField}.
 *
 * @author Tim Anderson
 */
public class ConsoleRuleFieldTest extends AbstractConsoleFieldTest
{

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        String layout = "N:3:3 . N:3:3 . N:3:3 . N:3:3"; // IP address format
        String initialValue = "192.168.0.1";
        String separator = null;
        String variable = "variable1";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setInitialValue(initialValue);

        RuleField model = new RuleField(config, installData);

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
        String initialValue = "192.168.0.1";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setInitialValue(initialValue);
        RuleField model = new RuleField(config, installData);

        ConsoleRuleField field = new ConsoleRuleField(model, console, prompt);
        console.addScript("Set value", "127.0.0.1");
        assertTrue(field.display());

        assertEquals("127.0.0.1", installData.getVariable(variable));
    }
}
