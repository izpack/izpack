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

package com.izforge.izpack.compiler.container;

import java.util.jar.JarFile;

import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.test.junit.UnloadJarRule;

/**
 * Abstract implementation of a container for testing purposes.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
public abstract class AbstractTestInstallationContainer extends AbstractContainer
{
    protected Class klass;
    protected FrameworkMethod frameworkMethod;
    @Rule
    public UnloadJarRule unloadJarRule = new UnloadJarRule();

    public AbstractTestInstallationContainer(Class klass, FrameworkMethod frameworkMethod)
    {
        this.klass = klass;
        this.frameworkMethod = frameworkMethod;
    }

    @Override
    protected void fillContainer(MutablePicoContainer picoContainer)
    {
        TestCompilationContainer compiler = new TestCompilationContainer(klass, frameworkMethod);
        compiler.launchCompilation();

        // propagate compilation objects to the installer container so the installation test can use them
        CompilerData data = compiler.getComponent(CompilerData.class);
        JarFile installer = compiler.getComponent(JarFile.class);
        picoContainer.addComponent(data);
        picoContainer.addComponent(installer);

        fillInstallerContainer(picoContainer);
    }

    protected abstract InstallerContainer fillInstallerContainer(MutablePicoContainer picoContainer);
}
