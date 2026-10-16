/*
 * IzPack - Copyright 2001-2020 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.core.rules.process;

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.util.Platforms;
import org.junit.jupiter.api.Test;


public class JavaConditionTest {
    public static final boolean CONSTANT_VALUE = true;
    public static final Boolean CONSTANT_OBJECT_VALUE = Boolean.TRUE;

    public static void conditionMethodWithArgument(String someArgument) {
    }

    public static String conditionMethodWithNonBooleanResult() {
        return "true";
    }

    public static boolean conditionMethodFailing() {
        throw new RuntimeException();
    }

    public static boolean conditionMethodPrimitiveResult() {
        return true;
    }

    public static Boolean conditionMethod() {
        return Boolean.TRUE;
    }

    @Test
    public void testJavaConditions()
    {
        RulesEngine rules = createRulesEngine(new AutomatedInstallData(new DefaultVariables(), Platforms.UNIX));
        IXMLParser parser = new XMLParser();
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream("javaconditions.xml"));
        rules.analyzeXml(conditions);

        assertThat(rules.isConditionTrue("java0")).isFalse();  // class does not exist
        assertThat(rules.isConditionTrue("java1")).isFalse();  // field does not exist
        assertThat(rules.isConditionTrue("java2")).isTrue();   // access to simple boolean field
        assertThat(rules.isConditionTrue("java3")).isTrue();   // access to boolean object field
        assertThat(rules.isConditionTrue("java4")).isFalse();  // method does not exist
        assertThat(rules.isConditionTrue("java5")).isFalse();  // method has arguments
        assertThat(rules.isConditionTrue("java6")).isFalse();  // method has non boolean return type
        assertThat(rules.isConditionTrue("java7")).isFalse();  // method invocation fails
        assertThat(rules.isConditionTrue("java8")).isTrue();   // simple boolean return value
        assertThat(rules.isConditionTrue("java9")).isTrue();   // boolean object return value
    }

    /**
     * Creates a new {@link RulesEngine}.
     *
     * @param installData the installation data
     * @return a new rules engine
     */
    private RulesEngine createRulesEngine(InstallData installData)
    {
        DefaultContainer parent = new DefaultContainer();
        RulesEngine rules = new RulesEngineImpl(installData, new ConditionContainer(parent), installData.getPlatform());
        parent.addComponent(RulesEngine.class, rules);
        return rules;
    }
}
