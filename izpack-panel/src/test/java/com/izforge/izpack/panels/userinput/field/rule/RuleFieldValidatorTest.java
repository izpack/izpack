/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 René Krell
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

package com.izforge.izpack.panels.userinput.field.rule;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.validator.HostAddressValidator;
import com.izforge.izpack.panels.userinput.validator.RegularExpressionValidator;
import com.izforge.izpack.util.Platforms;


/**
 * Tests the field validation of {@link RuleField} class instances.
 *
 * @author René Krell
 */
public class RuleFieldValidatorTest
{
    /**
     * The install data.
     */
    private AutomatedInstallData installData;

    /**
     * The object factory.
     */
    private ObjectFactory factory;


    /**
     * Default constructor.
     */
    public RuleFieldValidatorTest()
    {
        installData = new AutomatedInstallData(new DefaultVariables(), Platforms.LINUX);
        RulesEngine rules = new RulesEngineImpl(new ConditionContainer(new DefaultContainer()),
                                                installData.getPlatform());
        installData.setRules(rules);
        factory = new DefaultObjectFactory(new DefaultContainer());
    }

    @Test
    public void testRegexpValidation()
    {
        String layout = "O:15:U : N:5:5"; // host : port format
        String variable = "server.address";
        String separator = null;
        String defaultValue = "0: 1:";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setDefaultValue(defaultValue);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(RegularExpressionValidator.PATTERN_PARAM, "\\b.*\\:(6553[0-5]|655[0-2]\\d|65[0-4]\\d{2}|6[0-4]\\d{3}|[1-5]\\d{4}|[1-9]\\d{0,3})\\b");
        FieldValidator fieldValidator = new FieldValidator( RegularExpressionValidator.class.getName(), parameters, "Regex validation failed", factory);
        config.addValidator(fieldValidator);
        RuleField model = new RuleField(config, installData, factory);
        ValidationStatus status = model.validate(new String[] {"127.0.0.1", "1234"});
        assertTrue(status.isValid());
    }

    @Test
    public void testHostIpValidation()
    {
        String layout = "O:15:U : N:5:5"; // host : port format
        String variable = "server.address";
        String separator = null;
        String defaultValue = "0: 1:";

        TestRuleFieldConfig config = new TestRuleFieldConfig(variable, layout, separator, RuleFormat.DISPLAY_FORMAT);
        config.setDefaultValue(defaultValue);
        FieldValidator fieldValidator = new FieldValidator( HostAddressValidator.class, "Host address validation failed", factory);
        config.addValidator(fieldValidator);
        RuleField model = new RuleField(config, installData, factory);
        ValidationStatus status = model.validate(new String[] {"127.0.0.1", "1234"});
        assertTrue(status.isValid());
    }

}
