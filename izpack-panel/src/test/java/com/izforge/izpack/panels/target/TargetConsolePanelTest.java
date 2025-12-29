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

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanelView;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.util.Console;
import jakarta.inject.Inject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

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
     * The factory for creating panels.
     */
    private final ObjectFactory factory;

    /**
     * The console.
     */
    private final TestConsole console;
    
    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs a {@code TargetConsolePanelTest}.
     *
     * @param installData the installation data
     * @param console     the console
     */
    @Inject
    public TargetConsolePanelTest(InstallData installData, ObjectFactory factory, TestConsole console, Prompt prompt)
    {
        this.console = console;
        this.factory = factory;
        this.installData = installData;
        this.prompt = prompt;
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
        TargetConsolePanel panel = new TargetConsolePanel(
                createPanelView(TargetPanel.class, "panel.install_path"),
                installData, prompt);

        TargetPanelTestHelper.createBadInstallationInfo(badDir);

        // run the panel, selecting the default ("badDir")
        System.out.println();
        System.out.println("Test part 1 ...");
        console.addScript("TargetPanel.1", "\n");
        assertFalse(panel.run(installData, console));
        assertTrue(console.scriptCompleted());

        // verify that the install path wasn't set
        assertNull(installData.getInstallPath());

        // run the panel, selecting "goodDir"
        System.out.println();
        System.out.println("Test part 2 ...");
        console.addScript("TargetPanel.2", goodDir.getAbsolutePath(), "O", "1");
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

        TargetConsolePanel panel = new TargetConsolePanel(
                createPanelView(TargetPanel.class, "panel.install_path"),
                installData, prompt);
        assertFalse(panel.run(installData, properties));

        properties.setProperty(InstallData.INSTALL_PATH, goodDir.getAbsolutePath());
        assertTrue(panel.run(installData, properties));
    }

    /**
     * Creates a {@code ConsolePanels} containing an instance of the console version of the supplied panel
     * implementation.
     *
     * @param panelClass the panel class
     * @param id         the panel identifier
     * @return a new {@code ConsolePanels}
     */
    private PanelView<ConsolePanel> createPanelView(Class<TargetPanel> panelClass, String id)
    {
        Panel panel = new Panel();
        panel.setClassName(panelClass.getName());
        panel.setPanelId(id);
        return new ConsolePanelView(panel, factory, installData, console);
    }

}
