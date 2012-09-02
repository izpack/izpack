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

package com.izforge.izpack.integration;

import static com.izforge.izpack.test.util.TestHelper.assertFileExists;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.console.ConsoleUninstallerContainer;
import com.izforge.izpack.uninstaller.gui.GUIUninstallerContainer;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.file.FileUtils;


/**
 * Uninstallation helper.
 *
 * @author Tim Anderson
 */
public class UninstallHelper
{

    /**
     * Uninstalls an application.
     * <p/>
     * This copies the uninstaller jar so that the original can be removed.
     *
     * @param installData the installation data
     * @throws Exception for any error
     */
    public static void uninstall(InstallData installData) throws Exception
    {
        File uninstallerJar = getUninstallerJar(installData);
        assertFileExists(uninstallerJar);
        consoleUninstall(uninstallerJar);
    }

    /**
     * Returns the uninstaller jar file.
     *
     * @return the uninstaller jar file
     */
    public static File getUninstallerJar(InstallData installData)
    {
        Info info = installData.getInfo();
        String dir = IoHelper.translatePath(info.getUninstallerPath(), installData.getVariables());
        String path = dir + File.separator + info.getUninstallerName();
        return new File(path);
    }

    /**
     * Uninstalls the application at the specified path, by running the {@link Destroyer} in the supplied uninstall
     * jar, using the console uninstaller container.
     * <p/>
     * The Destroyer is launched in an isolated class loader as it locates resources using its class loader.
     * This also ensures it has all the classes it needs to run.
     *
     * @param uninstallJar the uninstall jar
     * @throws Exception for any error
     */
    public static void consoleUninstall(File uninstallJar) throws Exception
    {
        File copy = copy(uninstallJar);
        ClassLoader loader = getClassLoader(copy);

        // create the container
        Class containerClass = loader.loadClass(ConsoleUninstallerContainer.class.getName());
        Object container = containerClass.newInstance();

        runDestroyer(container, loader, copy);
    }

    /**
     * Uninstalls the application at the specified path, by running the GUI uninstaller.
     * <p/>
     * The uninstaller is launched in an isolated class loader as it locates resources using its class loader.
     * This also ensures it has all the classes it needs to run.
     *
     * @param uninstallJar the uninstall jar
     * @throws Exception for any error
     */
    public static void guiUninstall(File uninstallJar) throws Exception
    {
        File copy = copy(uninstallJar);
        ClassLoader loader = getClassLoader(copy);

        Class containerClass = loader.loadClass(GUIUninstallerContainer.class.getName());
        Object container = containerClass.newInstance();

        // now run the Destroyer. Can't do it via the UninstallerFrame as can't access the uninstall button
        runDestroyer(container, loader, copy);
    }

    /**
     * Helper to create an isolated class loader using only those classes in the specified uninstall jar.
     *
     * @param uninstallJar the uninstaller jar
     * @return a new class loader
     * @throws MalformedURLException
     */
    private static ClassLoader getClassLoader(File uninstallJar) throws MalformedURLException
    {
        // create an isolated class loader for loading classes and resources
        return new URLClassLoader(new URL[]{uninstallJar.toURI().toURL()}, null);
    }

    /**
     * Runs the destroyer obtained from the supplied container.
     *
     * @param container the container
     * @param loader    the isolated class loader to use to load classes
     * @throws Exception for any error
     */
    private static void runDestroyer(Object container, ClassLoader loader, File jar) throws Exception
    {
        Method getComponent = container.getClass().getMethod("getComponent", Class.class);

        // get the destroyer class
        Class destroyerClass = loader.loadClass(Destroyer.class.getName());
        Object destroyer = getComponent.invoke(container, destroyerClass);
        Method forceDelete = destroyerClass.getMethod("setForceDelete", boolean.class);
        forceDelete.invoke(destroyer, true);

        // create the Destroyer and run it
        Method run = destroyerClass.getMethod("run");
        run.invoke(destroyer);

        FileUtils.delete(jar); // probably won't delete as the class loader will still have a reference to it?

    }

    /**
     * Helper to make a temporary copy of a uninstaller jar.
     *
     * @param uninstallJar the jar to copy
     * @return the copy
     * @throws IOException for any I/O error
     */
    private static File copy(File uninstallJar) throws IOException
    {
        File copy = File.createTempFile("uninstaller", ".jar");
        copy.deleteOnExit();
        IoHelper.copyFile(uninstallJar, copy);
        return copy;
    }

}
