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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.junit.runners.model.FrameworkMethod;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.logging.MavenStyleLogFormatter;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.provider.JarFileProvider;
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
        super(false);
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
        super(false);
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
            Thread currentThread = Thread.currentThread();
            currentThread.setContextClassLoader(new URLClassLoader(new URL[] {out.toURI().toURL()}, currentThread.getContextClassLoader()));
        }
        catch (Exception e)
        {
            throw new IzPackException(e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Fills the container.
     *
     * @throws ContainerException if initialisation fails, or the container has already been initialised
     */
    @Override
    protected void fillContainer()
    {
        super.fillContainer();
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


        File jarFile = new File(targetDir, "out" + Math.random() + ".jar");
        jarFile.deleteOnExit();
        CompilerData data = new CompilerData(file.getAbsolutePath(), baseDir.getAbsolutePath(), jarFile.getAbsolutePath(),
                false);
        addConfig("installFile", file.getAbsolutePath());
        addComponent(CompilerData.class, data);
        addComponent(File.class, jarFile);

        try {
            JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile));
            jos.putNextEntry(new ZipEntry("test"));
            jos.closeEntry();
            jos.finish();
            jos.close();

            addComponent(JarFile.class, new JarFile(jarFile));
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot create jar file", ex);
        }

        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new MavenStyleLogFormatter());
        addComponent(Handler.class, consoleHandler);
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
