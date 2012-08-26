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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.provider.JarFileProvider;
import com.izforge.izpack.test.util.ClassUtils;
import com.izforge.izpack.util.FileUtil;

/**
 * Container for compilation tests.
 *
 * @author Anthonin Bonnefoy
 */
public class TestCompilerContainer extends CompilerContainer
{

    public static final String APPNAME = "Test Installation";

    /**
     * The test class.
     */
    private Class<?> testClass;

    /**
     * The test method.
     */
    private FrameworkMethod testMethod;


    /**
     * Constructs a <tt>TestCompilerContainer</tt>.
     *
     * @param testClass  the test class
     * @param testMethod the test method
     */
    public TestCompilerContainer(Class<?> testClass, FrameworkMethod testMethod)
    {
        super(null);
        this.testClass = testClass;
        this.testMethod = testMethod;
        initialise();
    }

    public void launchCompilation()
    {
        try
        {
            CompilerConfig compilerConfig = getComponent(CompilerConfig.class);
            File out = getComponent(File.class);
            compilerConfig.executeCompiler();
            ClassUtils.loadJarInSystemClassLoader(out);
        }
        catch (Exception e)
        {
            throw new IzPackException(e);
        }
    }

    /**
     * Fills the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails, or the container has already been initialised
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        super.fillContainer(container);
        try
        {
            deleteLock();
        }
        catch (IOException exception)
        {
            throw new ContainerException(exception);
        }
        InstallFile installFile = testMethod.getAnnotation(InstallFile.class);
        if (installFile == null)
        {
            installFile = testClass.getAnnotation(InstallFile.class);
        }
        String installFileName = installFile.value();

        File installerFile = FileUtil.convertUrlToFile(getClass().getClassLoader().getResource(installFileName));
        File baseDir = installerFile.getParentFile();

        File out = new File(baseDir, "out" + Math.random() + ".jar");
        out.deleteOnExit();
        CompilerData data = new CompilerData(installerFile.getAbsolutePath(), baseDir.getAbsolutePath(),
                                             out.getAbsolutePath(), false);
        addComponent(CompilerData.class, data);
        addComponent(File.class, out);

        container.addConfig("installFile", installerFile.getAbsolutePath());
        container.addAdapter(new JarFileProvider());
    }

    private void deleteLock() throws IOException
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        FileUtils.deleteQuietly(file);
    }
}
