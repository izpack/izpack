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

package com.izforge.izpack.integration.multivolume;

import static com.izforge.izpack.test.util.TestHelper.assertFileEquals;
import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static com.izforge.izpack.test.util.TestHelper.assertFileNotExists;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.compiler.container.TestCompilationContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.integration.UninstallHelper;
import com.izforge.izpack.test.util.TestHelper;


/**
 * Base class for multi-volume installation tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMultiVolumeInstallationTest
{
    /**
     * Temporary directory for installing to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    /**
     * Packages an multi-volume installer and installs it.
     *
     * @throws Exception for any error
     */
    @Test
    public void testMultiVolume() throws Exception
    {
        // compiler output goes to targetDir
        File targetDir = new File(temporaryFolder.getRoot(), "target");
        assertTrue(targetDir.mkdir());
        TestCompilationContainer compiler = new TestCompilationContainer("samples/multivolume/multivolume.xml",
                                                                         targetDir);

        // create the pack files. These correspond to those in multivolume.xml - created here so they don't need to be
        // committed.
        File baseDir = compiler.getBaseDir();
        File file1 = TestHelper.createFile(baseDir, "file1.dat", 10000);
        File file2 = TestHelper.createFile(baseDir, "file2.dat", 20000);
        File file3 = TestHelper.createFile(baseDir, "file3.dat", 30000);
        File file4 = TestHelper.createFile(baseDir, "file4.dat", 40000);
        File file5 = TestHelper.createFile(baseDir, "file5.dat", 50000);
        File file6 = TestHelper.createFile(baseDir, "file6.dat", 60000);

        // run the compiler
        compiler.launchCompilation();

        // verify loose packed files have been copied to the target dir
        assertFileExists(targetDir, file5.getName());
        assertFileExists(targetDir, file6.getName());

        // verify no other source exists in the target dir
        assertFileNotExists(targetDir, file1.getName());
        assertFileNotExists(targetDir, file2.getName());
        assertFileNotExists(targetDir, file3.getName());
        assertFileNotExists(targetDir, file4.getName());

        // now run the installer
        InstallerContainer installer = createInstallerContainer();
        InstallData installData = installer.getComponent(InstallData.class);

        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        installData.setInstallPath(installPath.getAbsolutePath());
        installData.setDefaultInstallPath(installPath.getAbsolutePath());
        installData.setMediaPath(targetDir.getPath());

        install(installer, installData, installPath);

        // verify expected files exist
        assertFileEquals(file1, installPath, file1.getName());
        assertFileEquals(file2, installPath, file2.getName());
        assertFileEquals(file5, installPath, file5.getName());
        assertFileEquals(file6, installPath, file6.getName());

        // verify skipped pack1 files not present
        assertFileNotExists(installPath, file3.getName());
        assertFileNotExists(installPath, file4.getName());

        // now uninstall it
        UninstallHelper.uninstall(installData);

        // verify the installed files no longer exist
        assertFileNotExists(installPath, file1.getName());
        assertFileNotExists(installPath, file2.getName());
        assertFileNotExists(installPath, file5.getName());
        assertFileNotExists(installPath, file6.getName());

        // in fact, entire installation dir should now be gone
        assertFileNotExists(installPath);
    }

    /**
     * Creates the installer container.
     *
     * @return a new installer container
     */
    protected abstract InstallerContainer createInstallerContainer();

    /**
     * Performs the installation.
     *
     * @param container   the installer container
     * @param installData the installation data
     * @param installPath the installation directory
     * @throws Exception for any error
     */
    protected abstract void install(InstallerContainer container, InstallData installData, File installPath)
            throws Exception;
}
