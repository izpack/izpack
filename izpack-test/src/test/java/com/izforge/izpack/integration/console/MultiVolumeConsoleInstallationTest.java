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

import java.io.File;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.compiler.container.TestConsoleInstallerContainer;
import com.izforge.izpack.compiler.packager.impl.MultiVolumePackager;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpacker;
import com.izforge.izpack.integration.multivolume.AbstractMultiVolumeInstallationTest;
import com.izforge.izpack.test.util.TestConsole;


/**
 * Tests console installation when using the {@link MultiVolumePackager} and {@link MultiVolumeUnpacker}.
 *
 * @author Tim Anderson
 */
public class MultiVolumeConsoleInstallationTest extends AbstractMultiVolumeInstallationTest
{

    /**
     * Creates the installer container.
     *
     * @return a new installer container
     */
    @Override
    protected InstallerContainer createInstallerContainer()
    {
        return new TestConsoleInstallerContainer();
    }

    /**
     * Performs the installation.
     *
     * @param container   the installer container
     * @param installData the installation data
     * @param installPath the installation directory
     * @throws Exception for any error
     */
    @Override
    protected void install(InstallerContainer container, InstallData installData, File installPath) throws Exception
    {
        TestConsoleInstaller installer = container.getComponent(TestConsoleInstaller.class);
        TestConsole console = installer.getConsole();
        console.addScript("HelloPanel", "1");
        console.addScript("InfoPanel", "1");
        console.addScript("LicensePanel", "\n", "1");
        console.addScript("TargetPanel", "\n", "1");
        console.addScript("InstallPanel");
        console.addScript("FinishPanel");
        installer.run(Installer.CONSOLE_INSTALL, installPath.getPath(), new String[0]);
    }

}