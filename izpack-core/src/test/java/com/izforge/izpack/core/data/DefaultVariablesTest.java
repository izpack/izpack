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

import static java.nio.file.Files.createDirectories;
import static org.apache.commons.io.FileUtils.touch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.izforge.izpack.api.data.*;
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
import com.izforge.izpack.util.Platforms;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the {@link DefaultVariables} class.
 *
 * @author Tim Anderson
 */
public class DefaultVariablesTest
{
    @TempDir
    public Path rootFolder;


    /**
     * The variables.
     */
    private final DefaultVariables variables = new DefaultVariables();


    /**
     * Tests the {@link Variables#set(String, String)}, {@link Variables#get(String)} and
     * {@link Variables#get(String, String)} methods.
     */
    @Test
    public void testStringVariables()
    {
        // test basic set/get
        variables.set("var1", "value1");
        assertThat(variables.get("var1")).isEqualTo("value1");

        // test nulls
        variables.set("null", null);
        assertThat(variables.get("null")).isNull();
        assertThat(variables.get("null", "default")).isEqualTo("default");

        // test where no variable set
        assertThat(variables.get("nonExistingVariable")).isNull();
        assertThat(variables.get("nonExistingVariable", "default")).isEqualTo("default");
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
        assertThat(variables.getBoolean("var1")).isTrue();
        assertThat(variables.getBoolean("var2")).isFalse();

        // test null
        variables.set("null", null);
        assertThat(variables.getBoolean("null")).isFalse();
        assertThat(variables.getBoolean("null", true)).isTrue();

        // test where no variable set
        assertThat(variables.getBoolean("nonExistingVariable")).isFalse();
        assertThat(variables.getBoolean("nonExistingVariable", true)).isTrue();

        // test when the value is not a boolean value
        variables.set("notABoolean", "yes");
        assertThat(variables.getBoolean("notABoolean")).isFalse();
        assertThat(variables.getBoolean("notABoolean", true)).isTrue();
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
        assertThat(variables.getInt("var1")).isEqualTo(0);
        assertThat(variables.getInt("var2")).isEqualTo(Integer.MIN_VALUE);
        assertThat(variables.getInt("var3")).isEqualTo(Integer.MAX_VALUE);

        // check when the variable is null
        variables.set("null", null);
        assertThat(variables.getInt("null")).isEqualTo(-1);
        assertThat(variables.getInt("null", 9999)).isEqualTo(9999);

        // check when the variable doesn't exist
        assertThat(variables.getInt("nonExistingVariable")).isEqualTo(-1);
        assertThat(variables.getInt("nonExistingVariable", 9999)).isEqualTo(9999);

        // check when the variable is not an integer value
        variables.set("notAnInt", "abcdef");
        assertThat(variables.getInt("notAnInt")).isEqualTo(-1);
        assertThat(variables.getInt("notAnInt", 9999)).isEqualTo(9999);

        // check behaviour when value < Integer.MIN_VALUE or > Integer.MAX_VALUE
        variables.set("exceed1", Long.toString(Long.MIN_VALUE));
        variables.set("exceed2", Long.toString(Long.MAX_VALUE));

        assertThat(variables.getInt("exceed1")).isEqualTo(-1);
        assertThat(variables.getInt("exceed1", 9999)).isEqualTo(9999);
        assertThat(variables.getInt("exceed2")).isEqualTo(-1);
        assertThat(variables.getInt("exceed2", 9999)).isEqualTo(9999);
    }

    /**
     * Tests the {@link Variables#getLong(String)} and {@link Variables#getLong(String, long)} methods.
     */
    @Test
    public void testLongVariables()
    {
        // check basic set, get
        variables.set("var1", "0");
        assertThat(variables.getLong("var1")).isEqualTo(0);

        // check when the variable is null
        variables.set("null", null);
        assertThat(variables.getLong("null")).isEqualTo(-1);
        assertThat(variables.getLong("null", 9999)).isEqualTo(9999);

        // check when the variable doesn't exist
        assertThat(variables.getLong("nonExistingVariable")).isEqualTo(-1);
        assertThat(variables.getLong("nonExistingVariable", 9999)).isEqualTo(9999);

        // check when the variable is not an integer value
        variables.set("notALong", "abcdef");
        assertThat(variables.getLong("notALong")).isEqualTo(-1);
        assertThat(variables.getLong("notALong", 9999)).isEqualTo(9999);
    }

    /**
     * Tests the {@link Variables#replace(String)} method.
     */
    @Test
    public void testReplace()
    {
        variables.set("var1", "Hello");
        variables.set("var2", "world");

        assertThat(variables.replace("$var1 $var2")).isEqualTo("Hello world");
        assertThat(variables.replace("${var1} ${var2}")).isEqualTo("Hello world");

        // check non-existent variable
        assertThat(variables.replace("$var1 $var3")).isEqualTo("Hello $var3");
        assertThat(variables.replace("${var1} ${var3}")).isEqualTo("Hello ${var3}");

        // check malformed variable
        assertThat(variables.replace("$var1 ${var3")).isEqualTo("Hello ${var3");

        // check null
        assertThat(variables.replace(null)).isNull();
    }

    /**
     * Tests simple dynamic variables.
     */
    @Test
    public void testDynamicVariables()
    {
        variables.add(createDynamic("var1", "$INSTALL_PATH"));
        variables.set("INSTALL_PATH", "a");

        assertThat(variables.get("var1")).isNull();  // not created till variables refreshed
        variables.refresh();
        assertThat(variables.get("var1")).isEqualTo("a");
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
        assertThat(variables.get("INSTALL_PATH")).isEqualTo("c:\\Program Files");

        // check when cond2 is true
        variables.set("os", "unix");
        variables.refresh();
        assertThat(variables.get("INSTALL_PATH")).isEqualTo("/usr/local/bin");
    }

    /**
     * Tests for IZPACK-1260
     */
    @Test
    public void testDynamicVariablesIZPACK1260()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("haveInstallPath", new ExistsCondition(ExistsCondition.ContentType.VARIABLE, "INSTALL_PATH"));
        conditions.put("previous.wrapper.conf.exists", new ExistsCondition(ExistsCondition.ContentType.FILE, "${previous.wrapper.conf}"));

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, new ConditionContainer(new DefaultContainer()),
                installData.getPlatform());
        rules.readConditionMap(conditions);
        ((DefaultVariables) variables).setRules(rules);

        variables.add(createDynamicCheckonce("previous.wrapper.conf", "${INSTALL_PATH}/conf/wrapper.conf".replace("/", File.separator), "haveInstallPath"));
        variables.add(createDynamicCheckonce("previous.wrapper.conf", "${INSTALL_PATH}/wrapper.conf".replace("/", File.separator), "haveInstallPath+!previous.wrapper.conf.exists"));

        File installPath = null;
        File confFile = null;
        try
        {
            installPath = createDirectories(rootFolder.resolve("myapp")).toFile();
            File confPath = new File(installPath, "conf");
            confPath.mkdirs();
            confFile = new File(confPath, "wrapper.conf");
            touch(confFile);
        }
        catch (IOException e)
        {
            fail("File operation failed.");
        }

        variables.set("INSTALL_PATH", installPath.getAbsolutePath());

        variables.refresh();

        assertThat(variables.get("previous.wrapper.conf")).isEqualTo(confFile.getAbsolutePath());
    }

        /**
         * Tests simple dynamic variables.
         */
    @Test
    public void testDynamicVariablesWithUserInput()
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

        variables.add(createDynamicCheckonce("var", "a", "cond1+cond2"));
        variables.add(createDynamicCheckonce("var", "b", "cond1+!cond2"));

        // !cond1+!cond2
        variables.refresh();
        assertThat(variables.get("var")).isNull();

        variables.set("condvar1", "x");
        // cond1+!cond2
        variables.refresh();
        assertThat(variables.get("var")).isEqualTo("b");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get("var")).isEqualTo("b");

        Set<String> blocked = new HashSet<String>();
        // we now do overwrite the variable on a UserInputPanel
        blocked.add("var");
        variables.set("var", "anothervalue");
        variables.registerBlockedVariableNames(blocked, this);
        variables.refresh(); // user input is stronger than other definitions
        assertThat(variables.get("var")).isEqualTo("anothervalue");

        variables.set("condvar2", "y"); // a conditions changes and wants to overwrite the variable
        variables.refresh();            // but user input still must survive
        assertThat(variables.get("var")).isEqualTo("anothervalue");

        // now the user goes back to the previous panel
        variables.unregisterBlockedVariableNames(blocked, this);
        variables.refresh(); // value must be changed as defined for cond1+cond2
        assertThat(variables.get("var")).isEqualTo("a");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get("var")).isEqualTo("a");
    }

    /**
     * Tests dynamic variables with a deeper dependency
     * Before IZPACK-1406 the recursion was done by iterations of variables.refresh().
     * Since IZPACK-1406 the variables are preordered by PackagerBase.buildVariableList().
     * Therefore we have to provide the variables here in the correct order.
     * @see com.izforge.izpack.compiler.DynVariableOrderTest for ordering tests
     */
    @SuppressWarnings("JavadocReference")
    @Test
    public void testDependentDynamicVariables()
    {
        variables.set("depVar1", "depValue");
        variables.add(createDynamic("depVar2", "${depVar1}"));
        variables.add(createDynamic("depVar3", "${depVar2}"));

        assertThat(variables.get("depVar3")).isNull(); // not created till variables refreshed
        variables.refresh();
        assertThat(variables.get("depVar3")).as("check dependent variable").isEqualTo("depValue");
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
        assertThat(variables.get("name3")).isNull(); // not created till variables refreshed
        variables.refresh();
        assertThat(variables.get("name3")).as("check dependent variable").isEqualTo("someValue");
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
        assertThat(variables.get("depVar3")).as("check dependent variable").isEqualTo("depValue");
        assertThat(variables.get("checkonceVar")).as("check variable with checkonce=true").isEqualTo("depValue");

        variables.set("depVar1", "newValue");
        variables.refresh();
        assertThat(variables.get("depVar3")).as("recheck dependent variable").isEqualTo("newValue"); // should be changed
        assertThat(variables.get("checkonceVar")).as("recheck variable with checkonce=true").isEqualTo("depValue"); // should not change any more
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
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("cond1", new VariableCondition("condvar1", "1"));
        conditions.put("cond2", new VariableCondition("condvar2", "1"));

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, new ConditionContainer(new DefaultContainer()),
                installData.getPlatform());
        rules.readConditionMap(conditions);
        ((DefaultVariables) variables).setRules(rules);

        variables.add(createDynamic(observedVar, "fallback value", null));
        variables.add(createDynamic(observedVar, "choice1", "cond1"));
        variables.add(createDynamic(observedVar, "choice2", "cond2"));

        assertThat(variables.get(observedVar)).isNull();

        // !cond1+!cond2
        variables.refresh();
        assertThat(variables.get(observedVar)).isEqualTo("fallback value");

        // cond1+!cond2
        variables.set("condvar1", "1");
        variables.refresh();
        assertThat(variables.get(observedVar)).isEqualTo("choice1");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get(observedVar)).isEqualTo("choice1");

        // cond1+cond2
        variables.set("condvar2", "1");
        variables.refresh();
        assertThat(variables.get(observedVar)).isEqualTo("choice2");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get(observedVar)).isEqualTo("choice2");

        // !cond1+cond2
        variables.set("condvar1", "0");
        variables.refresh();
        assertThat(variables.get(observedVar)).isEqualTo("choice2");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get(observedVar)).isEqualTo("choice2");

        // !cond1+!cond2
        variables.set("condvar2", "0");
        variables.refresh();
        assertThat(variables.get(observedVar)).isEqualTo("fallback value");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get(observedVar)).isEqualTo("fallback value");

        // !cond1+cond2
        variables.set("condvar2", "1");
        variables.refresh();
        assertThat(variables.get(observedVar)).isEqualTo("choice2");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get(observedVar)).isEqualTo("choice2");

        // cond1+cond2
        variables.set("condvar1", "1");
        variables.refresh(); // cond2 takes precedence because of ordering
        assertThat(variables.get(observedVar)).isEqualTo("choice2");
        variables.refresh(); // Double check whether it is a stable state (for instance on panel change)
        assertThat(variables.get(observedVar)).isEqualTo("choice2");
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

        assertThat(variables.get("a")).isEqualTo("${b}");
        assertThat(variables.get("b")).isEqualTo("${b}");
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
        assertThat(catched).as("empty <dynamicVariables> must not throw an exception").isFalse();
    }

    /**
     * Tests a variable defined both static (<variables>) and dynamic (<dynamicvariables>)
     */
    @Test
    public void testMixedVariables()
    {
        variables.set("var1", "value1");
        variables.add(createDynamic("var1", "dynValue"));

        assertThat(variables.get("var1")).as("static value").isEqualTo("value1");       // dynamic variable not resolved till variables refreshed
        variables.refresh();
        assertThat(variables.get("var1")).as("dynamic overwrite").isEqualTo("dynValue"); // static variable is overwritten by dynamic variable

        variables.add(createDynamic("var1", "${var2}"));                     // var2 is not defined yet
        variables.refresh();
        assertThat(variables.get("var1")).as("expect unresolved reference").isEqualTo("${var2}");

        variables.set("var2", "value2");                                     // define var2
        variables.refresh();
        assertThat(variables.get("var1")).as("expect reference resolved").isEqualTo("value2");
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
        assertThat(variables.get("var6")).as("undefined reference gives <null>").isNull();
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
        assertThat(variables.get("var6")).as("undefined reference gives static value").isEqualTo("static");
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
        assertThat(variables.get("var1")).as("static value").isEqualTo("static");       // dynamic variable not resolved till variables refreshed
        variables.refresh();
        assertThat(variables.get("found")).as("ini not found").isEqualTo("true");      // check, whether ini was found at all

        // static variable replaced by dynamic value from ini
        assertThat(variables.get("var1")).as("value from ini").isEqualTo("ini1");
        assertThat(variables.get("var2")).as("value from ini with spaces").isEqualTo("ini2");
        assertThat(variables.get("var3")).as("value from ini with spaces in value").isEqualTo("ini with spaces");
        assertThat(variables.get("var4")).as("empty value from ini").isEmpty();
        assertThat(variables.get("var5")).as("empty value with spaces in ini").isEmpty();
    }

    /**
     * Test for blocking of dynamic variables
     */
    @Test
    public void testBlockedDynamicVariables()
    {
        // set up conditions
        Map<String, Condition> conditions = new HashMap<String, Condition>();
        conditions.put("cond1", new VariableCondition("condvar1", "1"));
        variables.set("condvar1", "0");

        // set up the rules
        AutomatedInstallData installData = new AutomatedInstallData(variables, Platforms.LINUX);
        RulesEngineImpl rules = new RulesEngineImpl(installData, new ConditionContainer(new DefaultContainer()),
                installData.getPlatform());
        rules.readConditionMap(conditions);
        ((DefaultVariables) variables).setRules(rules);

        String blockedVar = "a";
        variables.add(createDynamic(blockedVar, "oldValue"));
        variables.add(createDynamic(blockedVar, "newValue", "cond1"));

        variables.refresh();
        assertThat(variables.get(blockedVar)).isEqualTo("oldValue");

        // block variable
        Set<String> blockedVars = new HashSet<String>();
        blockedVars.add(blockedVar);
        Panel blocker = new Panel();
        variables.registerBlockedVariableNames(blockedVars, blocker);
        variables.refresh();
        assertThat(variables.get(blockedVar)).isEqualTo("oldValue");

        // condition becomes true, but variable is still blocked
        variables.set("condvar1", "1");
        variables.refresh();
        assertThat(variables.get(blockedVar)).isEqualTo("oldValue");

        // unblock variable, so value should change
        variables.unregisterBlockedVariableNames(blockedVars, blocker);
        variables.refresh();
        assertThat(variables.get(blockedVar)).isEqualTo("newValue");
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
        assertThat(file).as("File " + file + " not found").exists();
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
        assertThat(variables.get("var1")).as("Wrong override after refresh without explicitly setting the variable").isEqualTo("OVERRIDE1");
        variables.set("var1", "explicit_value");
        assertThat(variables.get("var1")).as("Explicitly set variable value not present not present after refresh"
                        + "- user input will not override the defaults contents").isEqualTo("explicit_value");
    }

}