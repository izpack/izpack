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
package com.izforge.izpack.panels.target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Console;

/**
 * Tests the {@link TargetConsolePanel} class.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class TargetConsolePanelTest
{

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The console.
     */
    private final TestConsole console;

    /**
     * Constructs a {@code TargetConsolePanelTest}.
     *
     * @param installData the installation data
     * @param console     the console
     */
    public TargetConsolePanelTest(InstallData installData, TestConsole console)
    {
        this.console = console;
        this.installData = installData;
        installData.setInstallPath(null);
    }

    /**
     * Verifies that a directory containing an unrecognised .installationinformation file may not be selected to
     * install to, from {@link TargetConsolePanel#run(InstallData, Console)}.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRunConsoleIncompatibleInstallation() throws Exception
    {
        // set up two potential directories to install to, "badDir" and "goodDir"
        File root = temporaryFolder.getRoot();
        File badDir = new File(root, "badDir");
        assertTrue(badDir.mkdirs());
        File goodDir = new File(root, "goodDir");   // don't bother creating it
        installData.setDefaultInstallPath(badDir.getAbsolutePath());
        TargetPanelTestHelper.createBadInstallationInfo(badDir);

        // run the panel, selecting the default ("badDir")
        console.addScript("TargetPanel.1", "\n");
        TargetConsolePanel panel = new TargetConsolePanel();
        assertFalse(panel.run(installData, console));
        assertTrue(console.scriptCompleted());

        // verify that the install path wasn't set
        assertNull(installData.getInstallPath());

        // run the panel, selecting "goodDir"
        console.addScript("TargetPanel.2", goodDir.getAbsolutePath(), "1");
        assertTrue(panel.run(installData, console));
        assertTrue(console.scriptCompleted());

        // verify that the install path was updated
        assertEquals(goodDir.getAbsolutePath(), installData.getInstallPath());
    }

    /**
     * Verifies that a directory containing an unrecognised .installationinformation file may not be selected to
     * install to, from {@link TargetConsolePanel#run(InstallData, Properties)}.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testIncompatibleInstallationFromProperties() throws IOException
    {
        File root = temporaryFolder.getRoot();
        File badDir = new File(root, "badDir");
        assertTrue(badDir.mkdirs());
        TargetPanelTestHelper.createBadInstallationInfo(badDir);
        File goodDir = new File(root, "goodDir");   // don't bother creating it

        Properties properties = new Properties();
        properties.setProperty(InstallData.INSTALL_PATH, badDir.getAbsolutePath());

        TargetConsolePanel panel = new TargetConsolePanel();
        assertFalse(panel.run(installData, properties));

        properties.setProperty(InstallData.INSTALL_PATH, goodDir.getAbsolutePath());
        assertTrue(panel.run(installData, properties));
    }

}
