/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.compiler;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.jar.JarFile;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Tests for correct order of dynamic variable computation
 *
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainer.class)
public class DynVariableOrderTest
{

    static final String XML_DIR = "samples/dynvars/";  // Where we find our installer definitions

    private CompilerConfig compilerConfig;
    private TestCompilerContainer testContainer;

    List<String> orderedVarnames;

    public DynVariableOrderTest(TestCompilerContainer container, CompilerConfig compilerConfig)
    {
        this.testContainer = container;
        this.compilerConfig = compilerConfig;
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception
    {
        compilerConfig.executeCompiler();
        JarFile jar = testContainer.getComponent(JarFile.class);
        InputStream ios = jar.getInputStream(jar.getEntry("resources/dynvariables"));
        Object object = new ObjectInputStream(ios).readObject();
        List<DynamicVariable> dynVars = (List<DynamicVariable>) object;
        orderedVarnames = new ArrayList<String>(dynVars.size());
        for (DynamicVariable var : dynVars)
        {
            orderedVarnames.add(var.getName());
        }
        StringBuffer sb = new StringBuffer(
                String.format("Installer built from '%s' has this ordering of variable computation:%n",
                        testContainer.getXmlFileName()));
        for (String name : orderedVarnames)
        {
            sb.append(name).append(", ");
        }
        System.out.println(sb);
    }

    /**
     * Check the default order of variables without dependencies
     */
    @Test
    @InstallFile(XML_DIR + "checkOrder.xml")
    public void testDefaultOrder()
    {
        // TODO
        // Actual there is no deterministic order of variables.
        // Because of this a passed test is NOT a guarantee for correct implementation.
        // The order of variable computation can be correct by random and may fail on other examples.
        //
        // Therefore a deterministic default ordering of variables without dependency would be
        // useful. Ideally in the order of definition in the installer.xml with static variables before
        // dynamic variables.

        // Until this, the test is skipped:
        //testOrder("static1", "static2", "static3", "static4", "static5", "dyn1", "dyn2", "dyn3", "dyn4", "dyn5");
    }

    /**
     * Test behaviour of cyclic dependencies
     */
    @Test
    @InstallFile(XML_DIR + "cyclicDependency.xml")
    public void testCyclicDependency()
    {
        // TODO
        // A cyclic variable definition should be evaluated in the order of install.xml
        // Because there is no deterministic ordering yet (see above), the ordering is not checked:
        // testOrder("dyn1", "dyn2");
        // testOrder("dyn10", "dyn11");
        // but at least the test should not run in an endless loop :-)
    }

    /**
     * Test behaviour of cyclic dependencies
     */
    @Test
    @InstallFile(XML_DIR + "selfReference.xml")
    public void testSelfReference()
    {
        testContained("dyn1");
        testContained("dyn2");
    }

    /**
     * Test a simple dependency: a dynamic variable depends on a static variable
     * and a second one standing alone
     */
    @Test
    @InstallFile(XML_DIR + "simpleDependency.xml")
    public void testSimpleDependency()
    {
        testOrder("static1", "dyn1");
        testContained("dyn2");
    }

    /**
     * Test a longer dependency of dynamic variables
     */
    @Test
    @InstallFile(XML_DIR + "deeperDependency.xml")
    public void testDeeperDependency()
    {
        testOrder("static1", "dyn1", "dyn2", "dyn3", "dyn4", "dyn5", "dyn6", "dyn7", "dyn8");
    }

    /**
     * Test a longer dependency of variables in reversed order
     */
    @Test
    @InstallFile(XML_DIR + "forwardDependency.xml")
    public void testforwardDependency()
    {
        testOrder("static1", "dyn8", "dyn7", "dyn6", "dyn5", "dyn4", "dyn3", "dyn2", "dyn1");
    }

    /**
     * Test a longer dependency of variables in mixed order
     */
    @Test
    @InstallFile(XML_DIR + "mixedDependency.xml")
    public void testMixedDependency()
    {
        testOrder("static1", "dyn1", "dyn5", "dyn4", "dyn6", "dyn3", "dyn7", "dyn2", "dyn8");
    }

    /**
     * Test two separate dependency sequences
     */
    @Test
    @InstallFile(XML_DIR + "separateDependency.xml")
    public void testSeparateDependency()
    {
        testOrder("dyn7", "dyn5", "dyn3", "dyn1");
        testOrder("dyn2", "dyn4", "dyn6", "dyn8");
    }

    /**
     * Test two separate dependency sequences merging together
     */
    @Test
    @InstallFile(XML_DIR + "parallelDependency.xml")
    public void testParallelDependency()
    {
        testOrder("dyn7", "dyn5", "dyn3", "dyn1", "dyn10");
        testOrder("dyn2", "dyn4", "dyn6", "dyn8", "dyn10");
    }

    /**
     * Test variables with values with indirect variable references
     */
    @Test
    @InstallFile(XML_DIR + "complexValueDependency.xml")
    public void testComplexValueDependency()
    {
        testOrder("file", "ini");
        testOrder("key", "ini");
        testOrder("section", "ini");
        testOrder("file", "opt");
        testOrder("key", "opt");
        testOrder("file", "xml");
        testOrder("key", "xml");

        testOrder("file", "jar_ini");
        testOrder("entry", "jar_ini");
        testOrder("key", "jar_ini");
        testOrder("section", "jar_ini");
        testOrder("file", "jar_opt");
        testOrder("entry", "jar_ini");
        testOrder("key", "jar_opt");
        testOrder("file", "jar_xml");
        testOrder("entry", "jar_ini");
        testOrder("key", "jar_xml");

        testOrder("file", "zip_ini");
        testOrder("entry", "jar_ini");
        testOrder("key", "zip_ini");
        testOrder("section", "zip_ini");
        testOrder("file", "zip_opt");
        testOrder("entry", "jar_ini");
        testOrder("key", "zip_opt");
        testOrder("file", "zip_xml");
        testOrder("entry", "jar_ini");
        testOrder("key", "zip_xml");

        testOrder("regkey", "reg");
        testOrder("regvalue", "reg");

        testOrder("executable", "exec");
        testOrder("dir", "exec");
        testOrder("arg", "exec");
    }

    /**
     * Test variables with values with indirect variable references
     */
    @Test
    @InstallFile(XML_DIR + "conditionDependency.xml")
    public void testConditionDependency()
    {
        // conditions with two arguments
        testOrder("arg1a", "var1");
        testOrder("arg1b", "var1");
        testOrder("arg2a", "var2");
        testOrder("arg2b", "var2");
        testOrder("arg3a", "var3");
        testOrder("arg3b", "var3");
        testOrder("arg4a", "var4"); // no var substitution: testOrder("arg4b", "var4");
        testOrder("arg5a", "var5"); // no var substitution: testOrder("arg5b", "var5");

        // conditions with a single argument
        testOrder("arg20", "var20");
        testOrder("arg21", "var21");
        testOrder("arg22", "var22");
        testOrder("arg23", "var23");

        // conditions, which directly refer to a variable (name)
        testOrder("arg50", "var50");
        testOrder("arg51", "var51");
        testOrder("arg52", "var52");
        testOrder("arg60a", "var60"); // no var substitution: testOrder("arg60b", "var60");

        // aggregate conditions
        testOrder("arg90a", "var90");
        testOrder("arg90b", "var90");
        testOrder("arg91a", "var91");
        testOrder("arg91b", "var91");
        testOrder("arg92a", "var92");
        testOrder("arg92b", "var92");
        testOrder("arg93", "var93");
        testOrder("arg94", "var94");
    }

    /**
     * Test variables with conditions in expression language
     */
    @Test
    @InstallFile(XML_DIR + "expressionLanguage.xml")
    public void testExpressionLanguageDependency()
    {
        testOrder("arg1a", "var1");
        testOrder("arg1b", "var1");
        testOrder("arg2a", "var2");
        testOrder("arg2b", "var2");
    }

    /**
     * Test case for IZPACK-1260
     *
     * @see
     * <a href="https://izpack.atlassian.net/browse/IZPACK-1260">IZPACK-1260</a>
     * @see
     * com.izforge.izpack.core.data.DefaultVariablesTest#testDynamicVariablesIZPACK1260()
     */
    @Test
    @InstallFile(XML_DIR + "IZPACK-1260.xml")
    public void testIZPACK1260()
    {
        testOrder("INSTALL_PATH", "previous.wrapper.conf1");
        testOrder("INSTALL_PATH", "previous.wrapper.conf1", "previous.wrapper.conf2");
    }

    private void testOrder(String... names)
    {
        String name1 = names[0];
        String name2;
        testContained(name1);
        for (int i = 1; i < names.length; i++)
        {
            name2 = names[i];
            testContained(name2);
            assertTrue(String.format("'%s' must come before '%s' in variables-list", name1, name2), seachInList(name1) < seachInList(name2));
            name1 = name2;
        }
    }

    private void testContained(String name)
    {
        assertTrue(String.format("variable '%s' must be contained in variables-list", name), seachInList(name) > -1);
    }

    private int seachInList(String name)
    {
        ListIterator<String> it = orderedVarnames.listIterator();
        while (it.hasNext())
        {
            if (it.next().equals(name))
            {
                return it.previousIndex();
            }
        }
        return -1;
    }

}
