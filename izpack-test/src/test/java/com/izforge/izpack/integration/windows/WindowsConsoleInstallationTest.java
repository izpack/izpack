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

package com.izforge.izpack.integration.windows;

import static com.izforge.izpack.integration.windows.WindowsHelper.registryDeleteUninstallKey;
import static com.izforge.izpack.integration.windows.WindowsHelper.registryKeyExists;
import static com.izforge.izpack.integration.windows.WindowsHelper.registryValueStringEquals;
import static com.izforge.izpack.util.Platform.Name.WINDOWS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.compiler.container.TestConsoleInstallationContainer;
import com.izforge.izpack.compiler.container.TestConsoleInstallerContainer;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.event.RegistryInstallerListener;
import com.izforge.izpack.event.RegistryUninstallerListener;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.integration.UninstallHelper;
import com.izforge.izpack.integration.console.AbstractConsoleInstallationTest;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;


/**
 * Test installation on Windows.
 * <p/>
 * Verifies that:
 * <ul>
 * <li>An <em>Uninstall</em> entry is added to the registry by {@link RegistryInstallerListener} during
 * installation</li>
 * <li>The <em>Uninstall</em> entry is removed at uninstallation by {@link RegistryUninstallerListener}</li>
 * </ul>
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@RunOn(WINDOWS)
@Container(TestConsoleInstallationContainer.class)
public class WindowsConsoleInstallationTest extends AbstractConsoleInstallationTest
{

    /**
     * The installer container.
     */
    private final TestConsoleInstallerContainer container;

    /**
     * The installer.
     */
    private final TestConsoleInstaller installer;

    /**
     * The registry handler.
     */
    private final RegistryDefaultHandler handler;

    /**
     * The app name.
     */
    private static final String APP_NAME = "IzPack Windows Installation Test 1.0";


    /**
     * Default uninstallation key.
     */
    private static final String DEFAULT_UNINSTALL_KEY = RegistryHandler.UNINSTALL_ROOT + APP_NAME;

    /**
     * Registry uninstallation key uninstall command value.
     */
    private static final String UNINSTALL_CMD_VALUE = "UninstallString";

    /**
     * Second installation uninstallation key.
     */
    private static final String UNINSTALL_KEY2 = RegistryHandler.UNINSTALL_ROOT + APP_NAME + "(1)";

    /**
     * Registry uninstallation keys. Hard-coded so we don't delete too much on cleanup if something unexpected happens.
     */
    private static final String[] UNINSTALL_KEYS = {DEFAULT_UNINSTALL_KEY, UNINSTALL_KEY2};


    /**
     * Constructs a <tt>WindowsConsoleInstallationTest</tt>
     *
     * @param container   the container
     * @param installer   the installer
     * @param installData the installation date
     * @param handler     the registry handler
     * @throws Exception for any error
     */
    public WindowsConsoleInstallationTest(TestConsoleInstallerContainer container, TestConsoleInstaller installer,
                                          AutomatedInstallData installData, RegistryDefaultHandler handler)
            throws Exception
    {
        super(installData);
        this.container = container;
        this.installer = installer;
        this.handler = handler;
    }


    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    public void setUp() throws Exception
    {
        assertFalse("This test must be run as administrator, or with Windows UAC turned off",
                    new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded());
        super.setUp();
        String appName = getInstallData().getInfo().getAppName();
        assertNotNull(appName);
        File file = FileUtil.getLockFile(appName);
        if (file.exists())
        {
            assertTrue(file.delete());
        }

        destroyRegistryEntries();
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @After
    public void tearDown() throws Exception
    {
        destroyRegistryEntries();
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/windows/install.xml")
    public void testInstallation() throws Exception
    {
        // run the install
        checkInstall(container, APP_NAME);
        assertTrue(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY));

        // run the uninstaller and verify that uninstall key is removed
        File uninstaller = getUninstallerJar();
        assertTrue(uninstaller.exists());
        UninstallHelper.consoleUninstall(uninstaller);

        assertFalse(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY));
    }

    /**
     * Runs the console installer twice, verifying that a second uninstall key is created.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/windows/install.xml")
    public void testMultipleInstallation() throws Exception
    {
        // run the install
        checkInstall(container, APP_NAME);

        // remove the lock file to enable second installation
        removeLock();

        // run the installation again
        ConsoleInstallerContainer container2 = new TestConsoleInstallerContainer();
        TestConsoleInstaller installer2 = container2.getComponent(TestConsoleInstaller.class);
        InstallData installData2 = container2.getComponent(InstallData.class);
        TestConsole console2 = installer2.getConsole();
        console2.addScript("CheckedHelloPanel", "y", "1");
        console2.addScript("TargetPanel", "\n", "1");
        console2.addScript("PacksPanel", "1");
        console2.addScript("ShortcutPanel", "N");

        assertFalse(registryKeyExists(handler, UNINSTALL_KEY2));
        checkInstall(installer2, installData2);

        // verify the UNINSTALL_NAME has been updated
        assertEquals(APP_NAME + "(1)", installData2.getVariable("UNINSTALL_NAME"));

        // verify a second key is created
        assertTrue(registryKeyExists(handler, UNINSTALL_KEY2));
    }

    /**
     * Runs the console installer twice, verifying that a second uninstall key is created.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/windows/install.xml")
    public void testRejectMultipleInstallation() throws Exception
    {
        checkInstall(container, APP_NAME);

        removeLock();

        ConsoleInstallerContainer container2 = new TestConsoleInstallerContainer();
        TestConsoleInstaller installer2 = container2.getComponent(TestConsoleInstaller.class);
        RegistryDefaultHandler handler2 = container2.getComponent(RegistryDefaultHandler.class);
        InstallData installData2 = container2.getComponent(InstallData.class);

        TestConsole console2 = installer2.getConsole();
        console2.addScript("CheckedHelloPanel", "n");

        assertFalse(registryKeyExists(handler2, UNINSTALL_KEY2));
        installer2.run(Installer.CONSOLE_INSTALL, null);

        // verify the installation thinks it was unsuccessful
        assertFalse(installData2.isInstallSuccess());

        // make sure the script has completed
        TestConsole console = installer2.getConsole();
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());

        // verify the second registry key hasn't been created
        assertFalse(registryKeyExists(handler2, UNINSTALL_KEY2));
    }

    /**
     * Runs the console installer against a script with an alternative uninstaller name and
     * path, verifying that the correct uninstall JAR and registry value are created.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/windows/consoleinstall_alt_uninstall.xml")
    public void testNonDefaultUninstaller() throws Exception
    {
        assertFalse(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY));

        TestConsole console = installer.getConsole();
        console.addScript("CheckedHelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("TargetPanel", "\n", "1");

        //run installer and check that default uninstaller doesn't exist
        InstallData installData = getInstallData();
        checkInstall(installer, installData, false);

        //check that uninstaller exists as specified in install spec
        String installPath = installData.getInstallPath();
        assertTrue(new File(installPath, "/uninstallme.jar").exists());

        //check that the registry key has the correct value
        assertTrue(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY));
        String command = "\"" + installData.getVariable("JAVA_HOME") + "\\bin\\javaw.exe\" -jar \"" + installPath
                + "\\uninstallme.jar\"";
        registryValueStringEquals(handler, DEFAULT_UNINSTALL_KEY, UNINSTALL_CMD_VALUE, command);
    }

    /**
     * Runs the installation, and verifies the uninstall key is created.
     *
     * @throws NativeLibException for any native library exception
     */
    private void checkInstall(TestConsoleInstallerContainer container, String uninstallName) throws NativeLibException
    {
        // UNINSTALL_NAME should be null prior to display of CheckedHelloPanel
        InstallData installData = container.getComponent(InstallData.class);
        TestConsoleInstaller installer = container.getComponent(TestConsoleInstaller.class);
        RegistryDefaultHandler handler = container.getComponent(RegistryDefaultHandler.class);

        assertNull(installData.getVariable("UNINSTALL_NAME"));

        assertFalse(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY));

        TestConsole console = installer.getConsole();
        console.addScript("CheckedHelloPanel", "1");
        console.addScript("TargetPanel", "\n", "1");
        console.addScript("PacksPanel", "1");
        console.addScript("ShortcutPanel", "N");

        checkInstall(installer, installData);

        // UNINSTALL_NAME should now be defined
        assertEquals(uninstallName, installData.getVariable("UNINSTALL_NAME"));

        assertTrue(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY));
    }

    /**
     * Removes the lock file (which normally gets removed on exit), to enable multiple installs.
     */
    private void removeLock()
    {
        String appName = getInstallData().getInfo().getAppName();
        File file = FileUtil.getLockFile(appName);
        if (file.exists())
        {
            assertTrue(file.delete());
        }
    }


    /**
     * Destroys registry entries that may not have been cleared out by a previous run.
     *
     * @throws NativeLibException if the entries cannot be removed
     */
    private void destroyRegistryEntries() throws NativeLibException
    {
        for (String key : UNINSTALL_KEYS)
        {
            registryDeleteUninstallKey(handler, key);
        }
    }
}

