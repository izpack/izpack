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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.listener.TestUninstallerListener;
import com.izforge.izpack.uninstaller.Destroyer;


/**
 * Tests that {@link com.izforge.izpack.api.event.UninstallerListener}s are invoked by the {@link Destroyer}.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class UninstallerListenerTest extends AbstractDestroyerTest
{
    /**
     * The uninstall jar writer.
     */
    private final UninstallDataWriter uninstallDataWriter;

    /**
     * Uninstall data.
     */
    private final UninstallData uninstallData;


    /**
     * Constructs an <tt>UninstallerListenerTest</tt>.
     *
     * @param uninstallDataWriter the uninstall jar writer
     * @param installData         the install data
     * @param uninstallData       the uninstall data
     */
    public UninstallerListenerTest(UninstallDataWriter uninstallDataWriter, AutomatedInstallData installData,
                                   UninstallData uninstallData)
    {
        super(installData);
        this.uninstallDataWriter = uninstallDataWriter;
        this.uninstallData = uninstallData;
    }

    /**
     * Sets up the test case.
     *
     * @throws IOException if the install directory cannot be created
     * @throws Exception   for any other error
     */
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        // clean up any existing state file
        removeState();
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown()
    {
        removeState();
    }

    /**
     * Verifies that the uninstaller jar is written, and contains key classes and files.
     *
     * @throws java.io.IOException if the jar cannot be read
     */
    @Test
    @InstallFile("samples/event/customlisteners.xml")
    public void testUninstallerListenerInvocation() throws Exception
    {
        String installPath = getInstallPath();
        File installDir = new File(installPath);
        if (!installDir.exists())
        {
            assertTrue(installDir.mkdirs());
        }

        // add some files to the installation.
        int files = 3;
        for (int i = 0; i < files; ++i)
        {
            File file = new File(installPath, "file" + i);
            assertTrue(file.createNewFile());
            uninstallData.addFile(file.getAbsolutePath(), true);
        }

        // write the uninstaller and verify it contains the listeners
        assertTrue(uninstallDataWriter.write());

        File file = getUninstallerJar();
        JarFile uninstallJar = new JarFile(file);

        assertThat(uninstallJar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/api/event/UninstallerListener.class",
                "com/izforge/izpack/test/listener/TestUninstallerListener.class"));

        // perform uninstallation.
        runDestroyer(file);

        // verify the listener methods have been invoked the expected no. of times
        String path = TestUninstallerListener.getStatePath(installPath);
        TestUninstallerListener.State state = TestUninstallerListener.readState(path);
        assertEquals(1, state.initialiseCount);
        assertEquals(1, state.beforeListDeleteCount);
        assertEquals(files + 1, state.beforeDeleteCount); // 3 files + 1 for the uninstaller jar

        assertEquals(state.beforeListDeleteCount, state.afterListDeleteCount);
        assertEquals(state.beforeDeleteCount, state.afterDeleteCount);
    }

    /**
     * Removes any {@link TestUninstallerListener.State} file.
     */
    private void removeState()
    {
        String path = TestUninstallerListener.getStatePath(getInstallPath());
        File file = new File(path);
        if (file.exists())
        {
            assertTrue(file.delete());
        }
    }

}