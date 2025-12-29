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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.compiler.container.TestConsoleInstallationContainer;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertTrue;


/**
 * Tests pack-specific settings with the {@link ConsoleInstaller}.
 *
 * @author René Krell
 */
@RunWith(PicoRunner.class)
@Container(TestConsoleInstallationContainer.class)
public class PacksConsoleInstallationTest extends AbstractConsoleInstallationTest
{

    /**
     * The installer.
     */
    private final TestConsoleInstaller installer;
    private InstallData installData;

    /**
     * Constructs a <tt>PacksConsoleInstallationTest</tt>
     *
     * @param installer   the installer
     * @param installData the installation data
     * @throws Exception for any error
     */
    public PacksConsoleInstallationTest(TestConsoleInstaller installer, InstallData installData) throws Exception
    {
        super(installData);
        this.installer = installer;
        this.installData = installData;
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/packs/install.xml")
    public void testInstallationNotPack2() throws Exception
    {
        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("TargetPanel", "\n", "O", "1");
        console.addScript("PacksPanel",installData.getMessages().get("ConsolePrompt.no") , installData.getMessages().get("ConsolePrompt.no"), "1");
        console.addScript("UserInputPanel", "\n", "1");

        checkInstall(installer, getInstallData(), false, false);
    }

    /**
     * Runs the console installer against a script, and verifies expected files are installed.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/console/packs/install.xml")
    public void testInstallationPack2() throws Exception
    {
        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("TargetPanel", "\n", "O", "1");
        console.addScript("PacksPanel", installData.getMessages().get("ConsolePrompt.yes"), "\n", "1");
        console.addScript("UserInputPanel", "1");
        console.addScript("UserInputPanel", "xyz", "\n", "1");

        checkInstall(installer, getInstallData(), false, true);
    }

    /**
     * Verifies that console installation completes successfully.
     * \
     *
     * @param installer         the installer
     * @param installData       the installation data
     * @param expectUninstaller whether to expect an uninstaller to be created
     * @param expectPack2       whether to expect file_2.txt to be installed along with "Pack 2"
     */
    protected void checkInstall(TestConsoleInstaller installer, InstallData installData, boolean expectUninstaller, boolean expectPack2)
    {
        checkInstall(installer, installData, expectUninstaller);

        String installPath = installData.getInstallPath();

        // make sure some of the expected files are installed
        assertTrue(new File(installPath, "file_1.txt").exists());
        assertTrue(!expectPack2 || new File(installPath, "file_2.txt").exists());
    }

}
