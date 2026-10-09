/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
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
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.util.Platforms;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ContainsConditionTest
{

    /**
     * Checks conditions for strings read from the test <em>contains_in_string.xml</em> file.
     */
    @Test
    public void testInString()
    {
        doTests("contains_in_string.xml");
    }

    /**
     * Checks conditions for file read from the test <em>contains_in_file.xml</em> file.
     */
    @Test
    public void testInFile()
    {
        Map<String,Boolean> additional = new HashMap<String, Boolean>();
        additional.put("regex_multiple_lines_dotall", true);
        additional.put("regex_multiple_lines", false);
        doTests("contains_in_file.xml", additional);
    }

    /**
     * Checks conditions for variables read from the test <em>contains_in_variable.xml</em> file.
     */
    @Test
    public void testInVariable()
    {
        Variables variables = new DefaultVariables();
        variables.set("text", "This is A line of text");
        doTests(variables, "contains_in_variable.xml");
    }

    /**
     * Run defined set of tests
     *
     * @param resource      the name of the <conditions> xml
     */
    private void doTests(String resource)
    {
        doTests(resource, null);
    }

    /**
     * Run defined set of tests and additional tests
     *
     * @param resource      the name of the <conditions> xml
     * @param additional    additional tests to be run
     */
    private void doTests(String resource, Map<String, Boolean> additional)
    {
        doTests(new DefaultVariables(), resource, additional);
    }

    /**
     * Run defined set of tests with variables set
     *
     * @param variables     defined variables for test
     * @param resource      the name of the <conditions> xml
     */
    private void doTests(Variables variables, String resource)
    {
        doTests(variables, resource, null);
    }

    /**
     * Run defined set of tests and additional tests with variables set
     *
     * @param variables     defined variables for test
     * @param resource      the name of the <conditions> xml
     * @param additional    additional tests to be run
     */
    private void doTests(Variables variables, String resource, Map<String, Boolean> additional)
    {
        RulesEngine rules = createRulesEngine(new AutomatedInstallData(variables, Platforms.UNIX));
        IXMLParser parser = new XMLParser();
        IXMLElement conditions = parser.parse(getClass().getResourceAsStream(resource));
        rules.analyzeXml(conditions);

        assertThat(rules.isConditionTrue("value_found")).isTrue();               // a simple substring
        assertThat(rules.isConditionTrue("value_not_found1")).isFalse();         // not found because string is uppercase
        assertThat(rules.isConditionTrue("value_not_found2")).isFalse();         // not found because value is uppercase
        assertThat(rules.isConditionTrue("value_found_ignore_case")).isTrue();   // different case

        assertThat(rules.isConditionTrue("trivial_regex")).isTrue();             // a simple substring
        assertThat(rules.isConditionTrue("regex_with_wildcard")).isTrue();       // a regex with wildcards substring
        assertThat(rules.isConditionTrue("regex_whole_line")).isTrue();          // a regex matching the whole line
        assertThat(rules.isConditionTrue("regex_not_whole_line")).isFalse();     // a regex not matching the whole line
        assertThat(rules.isConditionTrue("regex_not_found1")).isFalse();         // not found because string is uppercase
        assertThat(rules.isConditionTrue("regex_not_found2")).isFalse();         // not found because value is uppercase

        if (additional!=null) {
            for (Entry<String, Boolean> test : additional.entrySet())
            {
                String cond = test.getKey();
                Boolean expected = test.getValue();
                assertThat(rules.isConditionTrue(cond)).as("condition '" + cond + "':").isEqualTo(expected);
            }
        }
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
