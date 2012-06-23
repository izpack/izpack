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
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.runners.model.FrameworkMethod;
import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.provider.JarFileProvider;
import com.izforge.izpack.util.ClassUtils;
import com.izforge.izpack.util.FileUtil;

/**
 * Container for compilation test.
 * <p/>
 * TODO - merge with TestCompilerContainer from izpack-compiler
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
public class TestCompilationContainer extends CompilerContainer
{

    public static final String APPNAME = "Test Installation";

    /**
     * The install file path.
     */
    private final String installFile;

    /**
     * The directory to locate files.
     */
    private File baseDir;

    /**
     * The directory to write the compile targets to.
     */
    private File targetDir;


    /**
     * Constructs a <tt>TestCompilationContainer</tt>.
     *
     * @param installFile the install file path
     * @param targetDir   the directory to write the compile targets to
     */
    public TestCompilationContainer(String installFile, File targetDir)
    {
        super(null);
        this.installFile = installFile;
        this.targetDir = targetDir;
        initialise();
    }

    /**
     * Constructs a <tt>TestCompilationContainer</tt> for a specific test.
     *
     * @param testClass the test class
     * @param method    the test method
     */
    public TestCompilationContainer(Class<?> testClass, FrameworkMethod method)
    {
        super(null);
        InstallFile installFile = method.getAnnotation(InstallFile.class);
        if (installFile == null)
        {
            installFile = testClass.getAnnotation(InstallFile.class);
        }
        this.installFile = installFile.value();
        initialise();
    }

    /**
     * Returns the directory to locate source files relative to.
     *
     * @return the base directory
     */
    public File getBaseDir()
    {
        return baseDir;
    }

    /**
     * Launches compilation.
     */
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
        deleteLock();
        URL resource = getClass().getClassLoader().getResource(installFile);
        if (resource == null)
        {
            throw new IllegalStateException("Cannot find install file: " + installFile);
        }
        File file = FileUtil.convertUrlToFile(resource);
        baseDir = file.getParentFile();

        if (targetDir == null)
        {
            targetDir = baseDir;
        }

        File out = new File(targetDir, "out" + Math.random() + ".jar");
        out.deleteOnExit();
        CompilerData data = new CompilerData(file.getAbsolutePath(), baseDir.getAbsolutePath(), out.getAbsolutePath(),
                                             false);
        container.addConfig("installFile", file.getAbsolutePath());
        container.addComponent(CompilerData.class, data);
        container.addComponent(File.class, out);
        container.addAdapter(new JarFileProvider());
    }

    /**
     * Deletes the lock file.
     */
    private void deleteLock()
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "iz-" + APPNAME + ".tmp");
        FileUtils.deleteQuietly(file);
    }
}
