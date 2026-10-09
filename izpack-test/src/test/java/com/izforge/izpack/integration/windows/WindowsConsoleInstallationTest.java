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

import static com.izforge.izpack.integration.UninstallHelper.consoleUninstall;
import static com.izforge.izpack.integration.windows.WindowsHelper.*;
import static com.izforge.izpack.util.FileUtil.getLockFile;
import static java.util.logging.Logger.getLogger;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.compiler.container.TestConsoleInstallationContainer;
import com.izforge.izpack.compiler.container.TestConsoleInstallerContainer;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.console.ConsoleInstallerAction;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.integration.console.AbstractConsoleInstallationTest;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

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
@EnabledOnOs(WINDOWS)
@Container(TestConsoleInstallationContainer.class)
public class WindowsConsoleInstallationTest extends AbstractConsoleInstallationTest
{
    /**
     * The logger.
     */
    private static final Logger logger = getLogger(WindowsConsoleInstallationTest.class.getName());

    private final boolean skipTests = new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded();

    private final boolean isAdminUser = new PrivilegedRunner(Platforms.WINDOWS).isAdminUser();

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
        assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");

        super.setUp();
        String appName = getInstallData().getInfo().getAppName();
        assertThat(appName).isNotNull();
        File file = getLockFile(appName);
        if (file.exists()) {
            assertThat(file.delete()).isTrue();
        }

        destroyRegistryEntries();
    }

    /**
     * Cleans up after the test.
     *
     * @throws Exception for any error
     */
    @AfterEach
    public void tearDown() throws Exception
    {
        // don't use Assume in @AfterEach methods!

        if ((!skipTests && isAdminUser)) {
            destroyRegistryEntries();
        }

        if (getUninstallerJar() != null) {
            try {
                // remove the uninstaller dir
                deleteDirectory(getUninstallerJar().getParentFile());
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, "Delete uninstaller directory failed.", ex);
            }
        }
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
        assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");

        // run the install
        checkInstall(container, APP_NAME);
        assertThat(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY)).isTrue();

        // run the uninstaller and verify that uninstall key is removed
        File uninstaller = getUninstallerJar();
        assertThat(uninstaller).exists();
        consoleUninstall(uninstaller);

        assertThat(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY)).isFalse();
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
        assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");

        // run the install
        checkInstall(container, APP_NAME);

        // remove the lock file to enable second installation
        removeLock();

        // run the installation again
        ConsoleInstallerContainer container2 = new TestConsoleInstallerContainer();
        TestConsoleInstaller installer2 = container2.getComponent(TestConsoleInstaller.class);
        InstallData installData2 = container2.getComponent(InstallData.class);

        // copied from super.setUp()
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = temporaryFolder.resolve("izpackTest").toFile();
        installData2.setInstallPath(installPath.getAbsolutePath());
        installData2.setDefaultInstallPath(installPath.getAbsolutePath());

        TestConsole console2 = installer2.getConsole();
        console2.addScript("CheckedHelloPanel", "y", "1");
        console2.addScript("TargetPanel", "\n", "y", "1");
        console2.addScript("PacksPanel", "1");

        assertThat(registryKeyExists(handler, UNINSTALL_KEY2)).isFalse();
        checkInstall(installer2, installData2);

        // verify the UNINSTALL_NAME has been updated
        assertThat(installData2.getVariable("UNINSTALL_NAME")).isEqualTo(APP_NAME + "(1)");

        // verify a second key is created
        assertThat(registryKeyExists(handler, UNINSTALL_KEY2)).isTrue();
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
        assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");

        checkInstall(container, APP_NAME);

        removeLock();

        ConsoleInstallerContainer container2 = new TestConsoleInstallerContainer();
        TestConsoleInstaller installer2 = container2.getComponent(TestConsoleInstaller.class);
        RegistryDefaultHandler handler2 = container2.getComponent(RegistryDefaultHandler.class);
        InstallData installData2 = container2.getComponent(InstallData.class);

        TestConsole console2 = installer2.getConsole();
        console2.addScript("CheckedHelloPanel", "n");

        assertThat(registryKeyExists(handler2, UNINSTALL_KEY2)).isFalse();
        installer2.run(ConsoleInstallerAction.CONSOLE_INSTALL, null, new String[0]);

        // verify the installation thinks it was unsuccessful
        assertThat(installData2.isInstallSuccess()).isFalse();

        // make sure the script has completed
        TestConsole console = installer2.getConsole();
        assertThat(console.scriptCompleted()).as("Script still running panel: " + console.getScriptName()).isTrue();

        // verify the second registry key hasn't been created
        assertThat(registryKeyExists(handler2, UNINSTALL_KEY2)).isFalse();
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
        assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");

        assertThat(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY)).isFalse();

        TestConsole console = installer.getConsole();
        console.addScript("CheckedHelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("TargetPanel", "\n", "O", "1");

        //run installer and check that default uninstaller doesn't exist
        InstallData installData = getInstallData();
        checkInstall(installer, installData, false);

        //check that uninstaller exists as specified in install spec
        String installPath = installData.getInstallPath();
        assertThat(new File(installPath, "/uninstallme.jar")).exists();

        //check that the registry key has the correct value
        assertThat(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY)).isTrue();
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

        assertThat(installData.getVariable("UNINSTALL_NAME")).isNull();

        assertThat(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY)).isFalse();

        TestConsole console = installer.getConsole();
        console.addScript("CheckedHelloPanel", "1");
        console.addScript("TargetPanel", "\n", "O", "1");
        console.addScript("PacksPanel", "1");
        console.addScript("ShortcutPanel", "N");

        checkInstall(installer, installData);

        // UNINSTALL_NAME should now be defined
        assertThat(installData.getVariable("UNINSTALL_NAME")).isEqualTo(uninstallName);

        assertThat(registryKeyExists(handler, DEFAULT_UNINSTALL_KEY)).isTrue();
    }

    /**
     * Removes the lock file (which normally gets removed on exit), to enable multiple installs.
     */
    private void removeLock()
    {
        String appName = getInstallData().getInfo().getAppName();
        File file = getLockFile(appName);
        if (file.exists())
        {
            assertThat(file.delete()).isTrue();
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

