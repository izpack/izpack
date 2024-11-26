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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.unpacker.Unpacker;
import com.izforge.izpack.test.ContainerImport;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.test.junit.GuiceRunner;
import com.izforge.izpack.util.Platform.Name;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Verifies that executable files are correctly invoked during installation and uninstallation, based on their stage
 * configuration.
 * <br/>
 * Note that this test is limited to Windows and Unix based platforms.
 *
 * @author Tim Anderson
 */
@RunWith(GuiceRunner.class)
@ContainerImport(TestGUIInstallationContainer.class)
public class ExecutableFileTest extends AbstractDestroyerTest
{
    /**
     * The unpacker.
     */
    private final Unpacker unpacker;

    /**
     * The uninstall jar writer.
     */
    private final UninstallDataWriter uninstallDataWriter;


    /**
     * Constructs an <tt>UninstallerListenerTest</tt>.
     *
     * @param unpacker            the unpacker
     * @param uninstallDataWriter the uninstall jar writer
     * @param installData         the install data
     */
    public ExecutableFileTest(Unpacker unpacker, UninstallDataWriter uninstallDataWriter,
                              AutomatedInstallData installData)
    {
        super(installData);
        this.unpacker = unpacker;
        this.uninstallDataWriter = uninstallDataWriter;
    }

    /**
     * Verifies that executables marked:
     * <ul>
     * <li>"postinstall" - are executed after packages are unpacked</li>
     * <li>"uninstall" - are executed at uninstallation</li>
     * <li>"never" - are not executed</li>
     * </ul>
     *
     * @throws java.io.IOException if the jar cannot be read
     */
    @Test
    @InstallFile("samples/executables/executables.xml")
    @RunOn({Name.WINDOWS, Name.UNIX})
    public void testExecutables() throws Exception
    {
        // make sure variables are resolved.
        getInstallData().refreshVariables();

        // nothing should have executed yet
        checkNotExists("postinstall.log");
        checkNotExists("never.log");
        checkNotExists("uninstall.log");

        // perform installation and verify the postinstall.bat/postinstall.sh script runs
        unpacker.setProgressListener(new NoOpProgressHandler());
        unpacker.run();
        assertTrue(uninstallDataWriter.write());

        File file = checkContains("postinstall.log", "install");
        assertTrue(file.delete());
        checkNotExists("never.log");
        checkNotExists("uninstall.log");

        // now perform uninstallation and verify the uninstall.bat/uninstall.sh script runs
        File jar = getUninstallerJar();
        runDestroyer(jar);

        checkNotExists("postinstall.log");
        checkNotExists("never.log");
        checkContains("uninstall.log", "uninstall");
    }

    /**
     * Verifies that a file exists with the specified content.
     *
     * @param name    the file name
     * @param content the expected file content
     * @return the file
     * @throws IOException for any I/O error
     */
    private File checkContains(String name, String content) throws IOException
    {
        checkExists(name);
        File file = new File(temporaryFolder.getRoot(), name);
        List<String> fileContent = FileUtils.readLines(file, Charset.defaultCharset());
        assertEquals(1, fileContent.size());
        assertEquals(content, StringUtils.trim(fileContent.get(0)));
        return file;
    }

    /**
     * Verifies that a file exists.
     *
     * @param name the file name
     */
    private void checkExists(String name)
    {
        File file = new File(temporaryFolder.getRoot(), name);
        assertTrue(file.exists());
    }

    /**
     * Verifies that a file doesn't exist.
     *
     * @param name the file name
     */
    private void checkNotExists(String name)
    {
        File file = new File(temporaryFolder.getRoot(), name);
        assertFalse(file.exists());
    }

    /**
     * No-op implementation of {@link ProgressListener}. Can't use Mockito to mock this for some reason -
     * attempts to do so result in a ClassCastException - possibly because the same class has been mocked already,
     * but in an isolated class loader by the {@link ExecutableFileTest#runDestroyer(java.io.File)} method.
     */
    private static class NoOpProgressHandler implements ProgressListener
    {
        @Override
        public void startAction(String name, int no_of_steps)
        {
        }

        @Override
        public void stopAction()
        {
        }

        @Override
        public void nextStep(String step_name, int step_no, int no_of_substeps)
        {
        }

        @Override
        public void setSubStepNo(int no_of_substeps)
        {
        }

        @Override
        public void progress(int substep_no, String message)
        {
        }

        @Override
        public void progress(String message)
        {
        }

        @Override
        public void restartAction(String name, String overallMessage, String tip, int steps)
        {
        }
    }
}