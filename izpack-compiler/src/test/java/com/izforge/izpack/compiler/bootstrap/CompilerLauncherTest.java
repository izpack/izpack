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

import static com.izforge.izpack.compiler.packager.impl.AbstractPackagerTest.getBaseDir;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.compiler.Compiler;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.logging.MavenStyleLogFormatter;
import com.izforge.izpack.test.Container;
import java.util.Properties;
import java.util.jar.JarOutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;

/**
 * Test compiler bindings
 *
 * @author Anthonin Bonnefoy
 */
@Container(CompilerContainer.class)
public class CompilerLauncherTest
{
    private final CompilerContainer compilerContainer;

    public CompilerLauncherTest(CompilerContainer compilerContainer)
    {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new MavenStyleLogFormatter());
        compilerContainer.addComponent(Handler.class, consoleHandler);

        this.compilerContainer = compilerContainer;

    }

    @Test
    public void testPropertiesBinding() throws Exception
    {
        Properties properties = compilerContainer.getComponent(Properties.class);
        assertThat(properties).isNotNull();
    }

    @Test
    public void testJarOutputStream() throws Exception
    {
        String baseDir = getBaseDir().getPath();
        compilerContainer.addComponent(CompilerData.class,
                new CompilerData(
                        baseDir + "src/test/resources/bindingTest.xml",
                        "",
                        baseDir + "/target/output.jar",
                        true)
        );
        JarOutputStream jarOutputStream = compilerContainer.getComponent(JarOutputStream.class);
        assertThat(jarOutputStream).isNotNull();
    }

    @Test
    public void testCompilerBinding() throws Exception
    {
        compilerContainer.processCompileDataFromArgs(new String[]{"bindingTest.xml"});
        Compiler compiler = compilerContainer.getComponent(Compiler.class);
        assertThat(compiler).isNotNull();
    }

    @Test
    public void testCompilerDataBinding()
    {
        String baseDir = getBaseDir().getPath();

        compilerContainer.addComponent(CompilerData.class,
                new CompilerData(
                        baseDir + "src/test/resources/bindingTest.xml",
                        "",
                        baseDir + "/target/output.jar",
                        false)
        );
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data).isNotNull();
    }

    @Test
    public void testCompilerConfigBinding() throws Exception
    {
        compilerContainer.processCompileDataFromArgs(new String[]{"bindingTest.xml"});
        CompilerData data = compilerContainer.getComponent(CompilerData.class);
        assertThat(data).isNotNull();
        CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
        assertThat(compiler).isNotNull();
    }
}
