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

import static org.assertj.core.api.Assertions.assertThat;

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
import com.izforge.izpack.util.Platforms;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link GUIRuleField}.
 *
 * @author Tim Anderson
 */
public class GUIRuleFieldTest
{

    /**
     * The install data.
     */
    private GUIInstallData installData;


    /**
     * Default constructor.
     */
    public GUIRuleFieldTest()
    {
        installData = new GUIInstallData(new DefaultVariables(), Platforms.HP_UX);
        RulesEngine rules = new RulesEngineImpl(new ConditionContainer(new DefaultContainer()),
                                                installData.getPlatform());
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
        assertThat(field.updateView()).isTrue(); // Update: Empty field -> initial value
        assertThat(field.updateView()).isFalse(); // should be nothing to update

        // check default value
        assertThat(field.getValue()).isEqualTo("192.168.0.1");

        String[] values = field.getValues();
        assertThat(values.length).isEqualTo(4);
        assertThat(values[0]).isEqualTo("192");
        assertThat(values[1]).isEqualTo("168");
        assertThat(values[2]).isEqualTo("0");
        assertThat(values[3]).isEqualTo("1");

        assertThat(field.updateField(LoggingPrompt.INSTANCE)).isTrue();

        assertThat(installData.getVariable(variable)).isEqualTo("192.168.0.1");

        field.setValues("127", "0", "0", "1");
        assertThat(field.updateField(LoggingPrompt.INSTANCE)).isTrue();
        assertThat(installData.getVariable(variable)).isEqualTo("127.0.0.1");

        // the following is a bit ridiculous but highlights that a minimum length can't be specified for a field
        field.setValues("", "", "", "");
        assertThat(field.updateField(LoggingPrompt.INSTANCE)).isTrue();
        assertThat(installData.getVariable(variable)).isEqualTo("...");
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
        assertThat(field.updateView()).isTrue(); // Update: Empty field -> initial value
        assertThat(field.updateView()).isFalse(); // should be nothing to update
        assertThat(field.getValue()).isEqualTo("localhost");

        assertThat(installData.getVariable("variable1")).isNull();
        assertThat(field.updateField(LoggingPrompt.INSTANCE)).isTrue();
        assertThat(installData.getVariable(variable)).isEqualTo("localhost");
    }
}

