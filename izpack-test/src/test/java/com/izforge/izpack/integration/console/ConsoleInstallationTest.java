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

package com.izforge.izpack.integration.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.compiler.container.TestConsoleInstallationContainer;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;


/**
 * Tests the {@link ConsoleInstaller}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsoleInstallationContainer.class)
public class ConsoleInstallationTest extends AbstractConsoleInstallationTest
{

    /**
     * The installer.
     */
    private final TestConsoleInstaller installer;


    /**
     * Constructs a <tt>ConsoleInstallationTest</tt>
     *
     * @param installer   the installer
     * @param installData the installation data
     * @throws Exception for any error
     */
    public ConsoleInstallationTest(TestConsoleInstaller installer, AutomatedInstallData installData) throws Exception
    {
        super(installData);
        this.installer = installer;
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install_no_uninstall.xml")
    public void testInstallationWithDisabledUnInstaller() throws Exception
    {
        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");

        checkInstall(installer, getInstallData(), false);
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testInstallation() throws Exception
    {
        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");

        checkInstall(installer, getInstallData());
    }

    /**
     * Verifies that nothing is installed if the licence is rejected.
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testRejectLicence()
    {
        InstallData installData = getInstallData();

        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");

        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "2");

        installData.setInstallPath(installPath.getAbsolutePath());
        installer.run(Installer.CONSOLE_INSTALL, null, new String[0]);

        assertFalse(installData.isInstallSuccess());
        assertFalse(installPath.exists());

        // make sure the script has completed
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());
    }

    /**
     * Verifies that the licence can be redisplayed and accepted.
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testRedisplayAndAcceptLicence()
    {
        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "3", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");

        checkInstall(installer, getInstallData());
    }

    /**
     * Runs the console installer to generate properties.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testGenerateProperties() throws Exception
    {
        InstallData installData = getInstallData();

        File file = new File(temporaryFolder.getRoot(), "IZPackInstall.properties");
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());

        installer.run(Installer.CONSOLE_GEN_TEMPLATE, file.getPath(), new String[0]);

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // check the properties file matches that expected
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey(InstallData.INSTALL_PATH));
        assertEquals("", properties.getProperty(InstallData.INSTALL_PATH));
    }

    /**
     * Runs the console installer, installing from a properties file.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/install.xml")
    public void testInstallFromProperties() throws Exception
    {
        InstallData installData = getInstallData();

        File file = new File(temporaryFolder.getRoot(), "IzPackInstall.properties");
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        Properties properties = new Properties();
        properties.put(InstallData.INSTALL_PATH, installPath.getPath());
        properties.store(new FileOutputStream(file), "IzPack installation properties");

        TestConsole console = installer.getConsole();
        installer.run(Installer.CONSOLE_FROM_TEMPLATE, file.getPath(), new String[0]);

        // make sure there were no attempts to read from the console, as no prompting should occur
        assertEquals(0, console.getReads());

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "Licence.txt").exists());
        assertTrue(new File(installPath, "Readme.txt").exists());
        assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
    }

    /**
     * Verifies that an installer with panels that have no corresponding {@link ConsolePanel} doesn't install.
     */
    @Test
    @InstallFile("samples/console/dummy.xml")
    public void testUnsupportedInstaller()
    {
        InstallData installData = getInstallData();

        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());

        // verify installation isn't supported
        assertFalse(installer.canInstall());

        // try it anyway
        installer.run(Installer.CONSOLE_INSTALL, null, new String[0]);

        // verify installation failed
        assertFalse(installData.isInstallSuccess());
        assertFalse(installPath.exists());
    }

    /**
     * Verifies that console installation completes successfully.
     * \
     *
     * @param installer         the installer
     * @param installData       the installation data
     * @param expectUninstaller whether to expect an uninstaller to be created
     */
    @Override
    protected void checkInstall(TestConsoleInstaller installer, InstallData installData, boolean expectUninstaller)
    {
        super.checkInstall(installer, installData, expectUninstaller);

        String installPath = installData.getInstallPath();

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "Licence.txt").exists());
        assertTrue(new File(installPath, "Readme.txt").exists());
    }
}
