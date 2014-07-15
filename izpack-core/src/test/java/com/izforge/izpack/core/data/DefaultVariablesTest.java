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

package com.izforge.izpack.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.rules.process.VariableCondition;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.util.Platforms;


/**
 * Tests the {@link DefaultVariables} class.
 *
 * @author Tim Anderson
 */
public class DefaultVariablesTest
{

    /**
     * The variables.
     */
    private final Variables variables = new DefaultVariables();


    /**
     * Tests the {@link Variables#set(String, String)}, {@link Variables#get(String)} and
     * {@link Variables#get(String, String)} methods.
     */
    @Test
    public void testStringVariables()
    {
        // test basic set/get
        variables.set("var1", "value1");
        assertEquals(variables.get("var1"), "value1");

        // test nulls
        variables.set("null", null);
        assertNull(variables.get("null"));
        assertEquals("default", variables.get("null", "default"));

        // test where no variable set
        assertNull(variables.get("nonExistingVariable"));
        assertEquals("default", variables.get("nonExistingVariable", "default"));
    }

    /**
     * Tests the {@link Variables#getBoolean(String)} and {@link Variables#getBoolean(String, boolean)} methods.
     */
    @Test
    public void testBooleanVariables()
    {
        // test basic set/get
        variables.set("var1", "true");
        variables.set("var2", "false");
        assertEquals(true, variables.getBoolean("var1"));
        assertEquals(false, variables.getBoolean("var2"));

        // test null
        variables.set("null", null);
        assertEquals(false, variables.getBoolean("null"));
        assertEquals(true, variables.getBoolean("null", true));

        // test where no variable set
        assertEquals(false, variables.getBoolean("nonExistingVariable"));
        assertEquals(true, variables.getBoolean("nonExistingVariable", true));

        // test when the value is not a boolean value
        variables.set("notABoolean", "yes");
        assertEquals(false, variables.getBoolean("notABoolean"));
        assertEquals(true, variables.getBoolean("notABoolean", true));
    }

    /**
     * Tests the {@link Variables#getInt(String)} and {@link Variables#getInt(String, int)} methods.
     */
    @Test
    public void testIntVariables()
    {
        // check basic set, get
        variables.set("var1", "0");
        variables.set("var2", Integer.toString(Integer.MIN_VALUE));
        variables.set("var3", Integer.toString(Integer.MAX_VALUE));
        assertEquals(0, variables.getInt("var1"));
        assertEquals(Integer.MIN_VALUE, variables.getInt("var2"));
        assertEquals(Integer.MAX_VALUE, variables.getInt("var3"));

        // check when the variable is null
        variables.set("null", null);
        assertEquals(-1, variables.getInt("null"));
        assertEquals(9999, variables.getInt("null", 9999));

        // check when the variable doesn't exist
        assertEquals(-1, variables.getInt("nonExistingVariable"));
        assertEquals(9999, variables.getInt("nonExistingVariable", 9999));

        // check when the variable is not an integer value
        variables.set("notAnInt", "abcdef");
        assertEquals(-1, variables.getInt("notAnInt"));
        assertEquals(9999, variables.getInt("notAnInt", 9999));

        // check behaviour when value < Integer.MIN_VALUE or > Integer.MAX_VALUE
        variables.set("exceed1", Long.toString(Long.MIN_VALUE));
        variables.set("exceed2", Long.toString(Long.MAX_VALUE));

        assertEquals(-1, variables.getInt("exceed1"));
        assertEquals(9999, variables.getInt("exceed1", 9999));
        assertEquals(-1, variables.getInt("exceed2"));
        assertEquals(9999, variables.getInt("exceed2", 9999));
    }

    /**
     * Tests the {@link Variables#getLong(String)} and {@link Variables#getLong(String, long)} methods.
     */
    @Test
    public void testLongVariables()
    {
        // check basic set, get
        variables.set("var1", "0");
        assertEquals(0, variables.getLong("var1"));

        // check when the variable is null
        variables.set("null", null);
        assertEquals(-1, variables.getLong("null"));
        assertEquals(9999, variables.getLong("null", 9999));

        // check when the variable doesn't exist
        assertEquals(-1, variables.getLong("nonExistingVariable"));
        assertEquals(9999, variables.getLong("nonExistingVariable", 9999));

        // check when the variable is not an integer value
        variables.set("notALong", "abcdef");
        assertEquals(-1, variables.getLong("notALong"));
        assertEquals(9999, variables.getLong("notALong", 9999));
    }

    /**
     * Tests the {@link Variables#replace(String)} method.
     */
    @Test
    public void testReplace()
    {
        variables.set("var1", "Hello");
        variables.set("var2", "world");

        assertEquals("Hello world", variables.replace("$var1 $var2"));
        assertEquals("Hello world", variables.replace("${var1} ${var2}"));

        // check non-existent variable
        assertEquals("Hello $var3", variables.replace("$var1 $var3"));
        assertEquals("Hello ${var3}", variables.replace("${var1} ${var3}"));

        // check malformed variable
        assertEquals("Hello ${var3", variables.replace("$var1 ${var3"));

        // check null
        assertNull(variables.replace(null));
    }

    /**
     * Tests simple dynamic variables.
     */
    @Test
    public void testDynamicVariables()
    {
        variables.add(createDynamic("var1", "$INSTALL_PATH"));
        variables.set("INSTALL_PATH", "a");

        assertNull(variables.get("var1"));  // not created till variables refreshed
        variables.refresh();
        assertEquals("a", variables.get("var1"));
    }

    /**
     * Tests conditional dynamic variables.
     */
    @Test
    public void testConditionalDynamicVariables()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("cond1", new VariableCondition("os", "windows")); // true when os = windows
        conditions.put("cond2", new VariableCondition("os", "unix"));    // true when os = unix

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.FREEBSD);
        RulesEngineImpl rules = new RulesEngineImpl(installData, new ConditionContainer(new DefaultContainer()),
                                                    installData.getPlatform());
        rules.readConditionMap(conditions);
        ((DefaultVariables) variables).setRules(rules);

        // add dynamic variables
        variables.add(createDynamic("INSTALL_PATH", "c:\\Program Files", "cond1")); // evaluated when os = windows
        variables.add(createDynamic("INSTALL_PATH", "/usr/local/bin", "cond2"));    // evaluated when os = unix

        // check when cond1 is true
        variables.set("os", "windows");
        variables.refresh();
        assertEquals("c:\\Program Files", variables.get("INSTALL_PATH"));

        // check when cond2 is true
        variables.set("os", "unix");
        variables.refresh();
        assertEquals("/usr/local/bin", variables.get("INSTALL_PATH"));
    }

    /**
     * Tests simple dynamic variables.
     */
    @Test
    public void testDynamicVariablesUnset()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("cond1", new VariableCondition("condvar1", "x"));
        conditions.put("cond2", new VariableCondition("condvar2", "y"));

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, new ConditionContainer(new DefaultContainer()),
                                                    installData.getPlatform());
        rules.readConditionMap(conditions);
        ((DefaultVariables) variables).setRules(rules);

        DynamicVariable variable1 = createDynamic("unset1", "a", "cond1+cond2");
        variable1.setCheckonce(true);
        variables.add(variable1);
        DynamicVariable variable2 = createDynamic("unset1", "b", "cond1+!cond2");
        variable2.setCheckonce(true);
        variables.add(variable2);

        // !cond1+!cond2
        variables.refresh();
        assertNull(variables.get("unset1"));

        variables.set("condvar1", "x");
        // cond1+!cond2
        variables.refresh();
        assertEquals("b", variables.get("unset1"));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("b", variables.get("unset1"));

        variables.set("unset1", "anothervalue");
        variables.refresh(); // Check keep overload from another source like a user input panel
        assertEquals("anothervalue", variables.get("unset1"));

        variables.set("condvar2", "y");
        // cond1+cond2 - override the previous value
        variables.refresh();
        assertEquals("a", variables.get("unset1"));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("a", variables.get("unset1"));
    }


    /**
     * Creates a dynamic variable.
     *
     * @param name  the variable name
     * @param value the variable value
     * @return a new variable
     */
    private DynamicVariable createDynamic(String name, String value)
    {
        return createDynamic(name, value, null);
    }

    /**
     * Creates a dynamic variable with a condition.
     *
     * @param name        the variable name
     * @param value       the variable value
     * @param conditionId the condition identifier. May be {@code null}
     * @return a new variable
     */
    private DynamicVariable createDynamic(String name, String value, String conditionId)
    {
        DynamicVariableImpl result = new DynamicVariableImpl();
        result.setName(name);
        result.setValue(new PlainValue(value));
        result.setConditionid(conditionId);
        return result;
    }
}

