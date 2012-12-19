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

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Properties;

import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.packager.impl.AbstractPackagerTest;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test compiler bindings
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(CompilerContainer.class)
public class CompilerLauncherTest
{
    private CompilerContainer compilerContainer;

    public CompilerLauncherTest(CompilerContainer compilerContainer)
    {
        this.compilerContainer = compilerContainer;
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
        compilerContainer.addComponent(CompilerData.class,
                new CompilerData(
                        baseDir + "src/test/resources/bindingTest.xml",
                        "",
                        baseDir + "/target/output.jar",
                        false)
        );
        JarOutputStream jarOutputStream = compilerContainer.getComponent(JarOutputStream.class);
        assertThat(jarOutputStream, IsNull.notNullValue());
    }

    @Test
    public void testCompilerBinding() throws Exception
    {
        compilerContainer.processCompileDataFromArgs(new String[]{"bindingTest.xml"});
        Compiler compiler = compilerContainer.getComponent(Compiler.class);
        assertThat(compiler, IsNull.notNullValue());
    }

    @Test
    public void testCompilerDataBinding()
    {
        String baseDir = AbstractPackagerTest.getBaseDir().getPath();

        compilerContainer.addComponent(CompilerData.class,
                new CompilerData(
                        baseDir + "src/test/resources/bindingTest.xml",
                        "",
                        baseDir + "/target/output.jar",
                        false)
        );
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
    }

    @Test
    public void testCompilerConfigBinding() throws Exception
    {
        compilerContainer.processCompileDataFromArgs(new String[]{"bindingTest.xml"});
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data, IsNull.notNullValue());
        CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
        assertThat(compiler, IsNull.notNullValue());
    }
}
