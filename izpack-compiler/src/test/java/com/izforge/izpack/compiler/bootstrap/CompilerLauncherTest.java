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

package com.izforge.izpack.compiler.bootstrap;

import com.google.inject.Inject;
import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.packager.impl.AbstractPackagerTest;
import com.izforge.izpack.test.ContainerImport;
import com.izforge.izpack.test.junit.GuiceRunner;
import org.hamcrest.core.IsNull;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;
import java.util.jar.JarOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test compiler bindings
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(GuiceRunner.class)
@ContainerImport(TestCompilerLauncherContainer.class)
public class CompilerLauncherTest
{
    private final Container compilerContainer;

    @Inject
    public CompilerLauncherTest(Container compilerContainer)
    {
        this.compilerContainer = compilerContainer;
    }

    @After
    public void tearDown()
    {
        TestCompilerDataProvider.reset();
    }

    @Test
    public void testPropertiesBinding() throws Exception
    {
        Properties properties = compilerContainer.getComponent(Properties.class);
        assertThat(properties, IsNull.notNullValue());
    }

    @Test
    public void testJarOutputStream() throws Exception
    {
        String baseDir = AbstractPackagerTest.getBaseDir().getPath();
        TestCompilerDataProvider.compilerData = new CompilerData(
                        baseDir + "src/test/resources/bindingTest.xml",
                        "",
                        baseDir + "/target/output.jar",
                        true);
        JarOutputStream jarOutputStream = compilerContainer.getComponent(JarOutputStream.class);
        assertThat(jarOutputStream, IsNull.notNullValue());
    }

    @Test
    public void testCompilerBinding() throws Exception
    {
        TestCompilerDataProvider.args = new String[]{"bindingTest.xml"};

        Compiler compiler = compilerContainer.getComponent(Compiler.class);
        assertThat(compiler, IsNull.notNullValue());
    }

    @Test
    public void testCompilerDataBinding()
    {
        String baseDir = AbstractPackagerTest.getBaseDir().getPath();

        TestCompilerDataProvider.compilerData = new CompilerData(
                        baseDir + "src/test/resources/bindingTest.xml",
                        "",
                        baseDir + "/target/output.jar",
                        false);
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
    }

    @Test
    public void testCompilerConfigBinding() throws Exception
    {
        TestCompilerDataProvider.args = new String[]{"bindingTest.xml"};
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
        CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
        assertThat(compiler, IsNull.notNullValue());
    }
}
