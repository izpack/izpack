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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.integration.AbstractDestroyerTest;
import com.izforge.izpack.test.util.TestConsole;


/**
 * Base class for {@link ConsoleInstaller} test cases.
 *
 * @author Tim Anderson
 */
public class AbstractConsoleInstallationTest extends AbstractDestroyerTest
{

    /**
     * Constructs an <tt>AbstractConsoleInstallationTest</tt>.
     *
     * @param installData the installation date
     * @throws Exception for any error
     */
    public AbstractConsoleInstallationTest(InstallData installData) throws Exception
    {
        super(installData);
    }

    /**
     * Verifies that console installation completes successfully.
     *
     * @param installer   the installer
     * @param installData the installation data
     */
    protected void checkInstall(TestConsoleInstaller installer, InstallData installData)
    {
        checkInstall(installer, installData, true);
    }

    /**
     * Verifies that console installation completes successfully.
     *
     * @param installer         the installer
     * @param installData       the installation data
     * @param expectUninstaller whether to expect an uninstaller to be created
     */
    protected void checkInstall(TestConsoleInstaller installer, InstallData installData, boolean expectUninstaller)
    {
        installer.run(Installer.CONSOLE_INSTALL, null, new String[0]);

        // verify the installation thinks it was successful
        assertTrue(installData.isInstallSuccess());

        // make sure the script has completed
        TestConsole console = installer.getConsole();
        assertTrue("Script still running panel: " + console.getScriptName(), console.scriptCompleted());

        String installPath = installData.getInstallPath();

        if (expectUninstaller)
        {
            assertTrue(new File(installPath, "Uninstaller/uninstaller.jar").exists());
        }
        else
        {
            assertFalse(new File(installPath, "Uninstaller/uninstaller.jar").exists());
        }
    }
}
