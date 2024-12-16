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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.RulesEngineImpl;
import com.izforge.izpack.core.rules.process.ExistsCondition;
import com.izforge.izpack.core.rules.process.VariableCondition;
import com.izforge.izpack.core.variable.ConfigFileValue;
import com.izforge.izpack.core.variable.PlainConfigFileValue;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.Platforms;

import jakarta.inject.Inject;


/**
 * Tests the {@link DefaultVariables} class.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(DefaultContainer.class)
public class DefaultVariablesTest
{
    @Rule
    public TemporaryFolder rootFolder = new TemporaryFolder();


    /**
     * The variables.
     */
    private final DefaultVariables variables = new DefaultVariables();

    @Inject
    ConditionContainer conditionContainer;

    
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
        Map<String, Condition> conditions = new HashMap<>();
        conditions.put("cond1", new VariableCondition("os", "windows")); // true when os = windows
        conditions.put("cond2", new VariableCondition("os", "unix"));    // true when os = unix

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.FREEBSD);
        RulesEngineImpl rules = new RulesEngineImpl(installData, conditionContainer);
        rules.readConditionMap(conditions);
        variables.setRules(rules);

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
     * Tests for IZPACK-1260
     */
    @Test
    public void testDynamicVariablesIZPACK1260()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<>();
        conditions.put("haveInstallPath", new ExistsCondition(ExistsCondition.ContentType.VARIABLE, "INSTALL_PATH"));
        conditions.put("previous.wrapper.conf.exists", new ExistsCondition(ExistsCondition.ContentType.FILE, "${previous.wrapper.conf}"));

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, conditionContainer);
        rules.readConditionMap(conditions);
        variables.setRules(rules);

        variables.add(createDynamicCheckonce("previous.wrapper.conf", "${INSTALL_PATH}/conf/wrapper.conf".replace("/", File.separator), "haveInstallPath"));
        variables.add(createDynamicCheckonce("previous.wrapper.conf", "${INSTALL_PATH}/wrapper.conf".replace("/", File.separator), "haveInstallPath+!previous.wrapper.conf.exists"));

        File installPath = null;
        File confFile = null;
        try
        {
            installPath = rootFolder.newFolder("myapp");
            File confPath = new File(installPath, "conf");
            confPath.mkdirs();
            confFile = new File(confPath, "wrapper.conf");
            FileUtils.touch(confFile);
        }
        catch (IOException e)
        {
            fail("File operation failed.");
        }

        variables.set("INSTALL_PATH", installPath.getAbsolutePath());

        variables.refresh();

        assertEquals(confFile.getAbsolutePath(), variables.get("previous.wrapper.conf"));
    }

        /**
         * Tests simple dynamic variables.
         */
    @Test
    public void testDynamicVariablesWithUserInput()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<>();
        conditions.put("cond1", new VariableCondition("condvar1", "x"));
        conditions.put("cond2", new VariableCondition("condvar2", "y"));

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, conditionContainer);
        rules.readConditionMap(conditions);
        variables.setRules(rules);

        variables.add(createDynamicCheckonce("var", "a", "cond1+cond2"));
        variables.add(createDynamicCheckonce("var", "b", "cond1+!cond2"));

        // !cond1+!cond2
        variables.refresh();
        assertNull(variables.get("var"));

        variables.set("condvar1", "x");
        // cond1+!cond2
        variables.refresh();
        assertEquals("b", variables.get("var"));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("b", variables.get("var"));

        Set<String> blocked = new HashSet<>();
        // we now do overwrite the variable on a UserInputPanel
        blocked.add("var");
        variables.set("var", "anothervalue");
        variables.registerBlockedVariableNames(blocked, this);
        variables.refresh(); // user input is stronger than other definitions
        assertEquals("anothervalue", variables.get("var"));

        variables.set("condvar2", "y"); // a conditions changes and wants to overwrite the variable
        variables.refresh();            // but user input still must survive
        assertEquals("anothervalue", variables.get("var"));

        // now the user goes back to the previous panel
        variables.unregisterBlockedVariableNames(blocked, this);
        variables.refresh(); // value must be changed as defined for cond1+cond2
        assertEquals("a", variables.get("var"));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("a", variables.get("var"));
    }

    /**
     * Tests dynamic variables with a deeper dependency
     * Before IZPACK-1406 the recursion was done by iterations of variables.refresh().
     * Since IZPACK-1406 the variables are preordered by PackagerBase.buildVariableList().
     * Therefore we have to provide the variables here in the correct order.
     * @see com.izforge.izpack.compiler.DynVariableOrderTest for ordering tests
     */
    @Test
    public void testDependentDynamicVariables()
    {
        variables.set("depVar1", "depValue");
        variables.add(createDynamic("depVar2", "${depVar1}"));
        variables.add(createDynamic("depVar3", "${depVar2}"));

        assertNull(variables.get("depVar3")); // not created till variables refreshed
        variables.refresh();
        assertEquals("check dependent variable", "depValue", variables.get("depVar3"));
    }

    /**
     * Tests dynamic variables with a deeper dependency
     */
    @Test
    public void testDependentDynamicVariables2()
    {
        variables.add(createDynamic("name1", "someValue"));
        variables.add(createDynamic("name2", "${name1}"));
        variables.add(createDynamic("name3", "${name2}"));
        assertNull(variables.get("name3")); // not created till variables refreshed
        variables.refresh();
        assertEquals("check dependent variable", "someValue", variables.get("name3"));
    }

    /**
     * Tests dynamic variables with a deeper dependency and checkonce==true
     */
    @Test
    public void testDependentDynamicVariablesWithCheckOnce()
    {
        variables.set("depVar1", "depValue");
        variables.add(createDynamic("depVar2", "${depVar1}"));
        variables.add(createDynamic("depVar3", "${depVar2}"));

        variables.add(createDynamicCheckonce("checkonceVar", "${depVar3}"));

        variables.refresh();
        assertEquals("check dependent variable", "depValue", variables.get("depVar3"));
        assertEquals("check variable with checkonce=true", "depValue", variables.get("checkonceVar"));

        variables.set("depVar1", "newValue");
        variables.refresh();
        assertEquals("recheck dependent variable", "newValue", variables.get("depVar3")); // should be changed
        assertEquals("recheck variable with checkonce=true", "depValue", variables.get("checkonceVar")); // should not change any more
    }

    /**
     * Tests dynamic variables with and without conditions
     * <p>
     * This test is for the definition
     * <dynamicvariables>
     * <variable name="thechoice" value="fallback value" />
     * <variable name="thechoice" value="choice1" condition="cond1" />
     * <variable name="thechoice" value="choice2" condition="cond2" />
     * </dynamicvariables>
     */
    @Test
    public void testMixedDynamicVariables()
    {
        final String observedVar = "thechoice";

        // set up conditions
        Map<String, Condition> conditions = new HashMap<>();
        conditions.put("cond1", new VariableCondition("condvar1", "1"));
        conditions.put("cond2", new VariableCondition("condvar2", "1"));

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, conditionContainer);
        rules.readConditionMap(conditions);
        variables.setRules(rules);

        variables.add(createDynamic(observedVar, "fallback value", null));
        variables.add(createDynamic(observedVar, "choice1", "cond1"));
        variables.add(createDynamic(observedVar, "choice2", "cond2"));

        assertNull(variables.get(observedVar));

        // !cond1+!cond2
        variables.refresh();
        assertEquals("fallback value", variables.get(observedVar));

        // cond1+!cond2
        variables.set("condvar1", "1");
        variables.refresh();
        assertEquals("choice1", variables.get(observedVar));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("choice1", variables.get(observedVar));

        // cond1+cond2
        variables.set("condvar2", "1");
        variables.refresh();
        assertEquals("choice2", variables.get(observedVar));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("choice2", variables.get(observedVar));

        // !cond1+cond2
        variables.set("condvar1", "0");
        variables.refresh();
        assertEquals("choice2", variables.get(observedVar));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("choice2", variables.get(observedVar));

        // !cond1+!cond2
        variables.set("condvar2", "0");
        variables.refresh();
        assertEquals("fallback value", variables.get(observedVar));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("fallback value", variables.get(observedVar));

        // !cond1+cond2
        variables.set("condvar2", "1");
        variables.refresh();
        assertEquals("choice2", variables.get(observedVar));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("choice2", variables.get(observedVar));

        // cond1+cond2
        variables.set("condvar1", "1");
        variables.refresh(); // cond2 takes precedence because of ordering
        assertEquals("choice2", variables.get(observedVar));
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertEquals("choice2", variables.get(observedVar));
    }

    /**
     * Tests dynamic variables with cyclic reference
     * <dynamicvariables>
     * <variable name="a" value="${b}" />
     * <variable name="b" value="${a}" />
     * </dynamicvariables>
     * <p>
     * This example is not useful, but should not create a loop.
     * Resolving is done in the best way it can be done. No error is thrown, but variable "a" is simply not resolved.
     */
    @Test
    public void testCyclicReference()
    {
        variables.add(createDynamic("a", "${b}"));
        variables.add(createDynamic("b", "${a}"));
        variables.refresh();

        assertEquals("${b}", variables.get("a"));
        assertEquals("${b}", variables.get("b"));
    }

    /**
     * Test loop detection with no dynamic variables at all
     * Ensure, that no exception is thrown
     */
    @Test
    public void testNoDynamicVariables()
    {
        boolean catched = false;
        try
        {
            variables.refresh();
        }
        catch (InstallerException e)
        {
            catched = true;
        }
        assertFalse("empty <dynamicVariables> must not throw an exception", catched);
    }

    /**
     * Tests a variable defined both static (<variables>) and dynamic (<dynamicvariables>)
     */
    @Test
    public void testMixedVariables()
    {
        variables.set("var1", "value1");
        variables.add(createDynamic("var1", "dynValue"));

        assertEquals("static value", "value1", variables.get("var1"));       // dynamic variable not resolved till variables refreshed
        variables.refresh();
        assertEquals("dynamic overwrite", "dynValue", variables.get("var1")); // static variable is overwritten by dynamic variable

        variables.add(createDynamic("var1", "${var2}"));                     // var2 is not defined yet
        variables.refresh();
        assertEquals("expect unresolved reference", "${var2}", variables.get("var1"));

        variables.set("var2", "value2");                                     // define var2
        variables.refresh();
        assertEquals("expect reference resolved", "value2", variables.get("var1"));
    }

    /**
     * Tests a variable defined both static (<variables>) and dynamic (<dynamicvariables>) with reference to ini file
     */
    @Test
    public void testMixedVariablesFromIniFileUnsetTrue()
    {
        testMixedVariablesFromIniFileUnset(true);
        // variable "var6" is static defined, but dynamic reference is not found in ini file.
        // with unset="true" (Default) the static variable is overwritten with null
        assertNull("undefined reference gives <null>", variables.get("var6"));
    }

    /**
     * Tests a variable defined both static (<variables>) and dynamic (<dynamicvariables>) with reference to ini file
     */
    @Test
    public void testMixedVariablesFromIniFileUnsetFalse()
    {
        testMixedVariablesFromIniFileUnset(false);
        // variable "var6" is static defined, but dynamic reference is not found in ini file.
        // with unset="false" the static variable is conserved and can be used as default
        assertEquals("undefined reference gives static value", "static", variables.get("var6"));
    }

    private void testMixedVariablesFromIniFileUnset(boolean unset)
    {
        // setup for testMixedVariablesFromIniFileUnsetTrue and testMixedVariablesFromIniFileUnsetFalse
        variables.set("var1", "static");
        variables.set("var2", "static");
        variables.set("var3", "static");
        variables.set("var4", "static");
        variables.set("var5", "static");
        variables.set("var6", "static");
        variables.add(createDynamicFromIni("found", unset));
        variables.add(createDynamicFromIni("var1", unset));
        variables.add(createDynamicFromIni("var2", unset));
        variables.add(createDynamicFromIni("var3", unset));
        variables.add(createDynamicFromIni("var4", unset));
        variables.add(createDynamicFromIni("var5", unset));
        variables.add(createDynamicFromIni("var6", unset));

        // common tests for testMixedVariablesFromIniFileUnsetTrue and testMixedVariablesFromIniFileUnsetFalse
        assertEquals("static value", "static", variables.get("var1"));       // dynamic variable not resolved till variables refreshed
        variables.refresh();
        assertEquals("ini not found", "true", variables.get("found"));      // check, whether ini was found at all

        // static variable replaced by dynamic value from ini
        assertEquals("value from ini", "ini1", variables.get("var1"));
        assertEquals("value from ini with spaces", "ini2", variables.get("var2"));
        assertEquals("value from ini with spaces in value", "ini with spaces", variables.get("var3"));
        assertEquals("empty value from ini", "", variables.get("var4"));
        assertEquals("empty value with spaces in ini", "", variables.get("var5"));
    }

    /**
     * Test for blocking of dynamic variables
     */
    @Test
    public void testBlockedDynamicVariables()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<>();
        conditions.put("cond1", new VariableCondition("condvar1", "1"));
        variables.set("condvar1", "0");

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, conditionContainer);
        rules.readConditionMap(conditions);
        variables.setRules(rules);

        String blockedVar = "a";
        variables.add(createDynamic(blockedVar, "oldValue"));
        variables.add(createDynamic(blockedVar, "newValue", "cond1"));

        variables.refresh();
        assertEquals("oldValue", variables.get(blockedVar));

        // block variable
        Set<String> blockedVars = new HashSet<>();
        blockedVars.add(blockedVar);
        Panel blocker = new Panel();
        variables.registerBlockedVariableNames(blockedVars, blocker);
        variables.refresh();
        assertEquals("oldValue", variables.get(blockedVar));

        // condition becomes true, but variable is still blocked
        variables.set("condvar1", "1");
        variables.refresh();
        assertEquals("oldValue", variables.get(blockedVar));

        // unblock variable, so value should change
        variables.unregisterBlockedVariableNames(blockedVars, blocker);
        variables.refresh();
        assertEquals("newValue", variables.get(blockedVar));
    }

    /**
     * Creates a dynamic variable with Checkonce set.
     *
     * @param name  the variable name
     * @param value the variable value
     * @return a new variable
     */
    private DynamicVariable createDynamicCheckonce(String name, String value)
    {
        return createDynamicCheckonce(name, value, null);
    }

    /**
     * Creates a dynamic variable with a condition and Checkonce set.
     *
     * @param name        the variable name
     * @param value       the variable value
     * @param conditionId the condition identifier. May be {@code null}
     * @return a new variable
     */
    private DynamicVariable createDynamicCheckonce(String name, String value, String conditionId)
    {
        DynamicVariable var = createDynamic(name, value, conditionId);
        var.setCheckonce(true);
        return var;
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

    /**
     * Creates a dynamic variable from the ini file "src/test/resources/com/izforge/izpack/core/variable/test.ini".
     *
     * @param name      the variable name
     * @param autounset whether to unset, when undefined
     * @return a new variable
     */
    private DynamicVariable createDynamicFromIni(String name, boolean autounset)
    {
        return createDynamicFromIni(name, "src/test/resources/com/izforge/izpack/core/variable/test.ini", "test", name, autounset);
    }

    /**
     * Creates a dynamic variable from a ini file.
     *
     * @param name        the variable name
     * @param file        the test ini file
     * @param filesection the section in the ini file
     * @param filekey     the key in the ini file
     * @param autounset   whether to unset, when undefined
     * @return a new variable
     */
    private DynamicVariable createDynamicFromIni(String name, String file, String filesection, String filekey, boolean autounset)
    {
        DynamicVariableImpl result = new DynamicVariableImpl();
        result.setName(name);
        boolean escape = true;
        result.setValue(new PlainConfigFileValue(file, ConfigFileValue.CONFIGFILE_TYPE_INI, filesection, filekey, escape));
        result.setAutoUnset(autounset);
        return result;
    }

    /**
     * Tests variable overrides to be passed to the installer
     */
    @Test
    public void testOverrides()
    {
        File file = new File("src/test/resources/com/izforge/izpack/core/data/test.defaults");
        assertTrue("File " + file + " not found", file.exists());
        Overrides overrides = null;
        try
        {
            overrides = new DefaultOverrides(file);
            overrides.load();
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
        variables.add(createDynamic("var1", "dynamic_definition"));
        variables.setOverrides(overrides);
        variables.refresh();
        assertEquals("Wrong override after refresh without explicitly setting the variable",
                "OVERRIDE1", variables.get("var1"));
        variables.set("var1", "explicit_value");
        assertEquals("Explicitly set variable value not present not present after refresh"
                        + "- user input will not override the defaults contents",
                "explicit_value", variables.get("var1"));
    }

}