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

import static com.izforge.izpack.panels.target.TargetPanelTestHelper.createBadInstallationInfo;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanelView;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.util.TestConsole;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the {@link TargetConsolePanel} class.
 *
 * @author Tim Anderson
 */
@Container(TestConsolePanelContainer.class)
public class TargetConsolePanelTest
{

    /**
     * Temporary folder.
     */
    @TempDir
    public Path temporaryFolder;

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
        File root = temporaryFolder.toFile();
        File badDir = new File(root, "badDir");
        assertThat(badDir.mkdirs()).isTrue();
        File goodDir = new File(root, "goodDir");   // don't bother creating it
        installData.setDefaultInstallPath(badDir.getAbsolutePath());
        TargetConsolePanel panel = new TargetConsolePanel(
                createPanelView(TargetPanel.class, "panel.install_path"),
                installData, prompt);

        createBadInstallationInfo(badDir);

        // run the panel, selecting the default ("badDir")
        System.out.println();
        System.out.println("Test part 1 ...");
        console.addScript("TargetPanel.1", "\n");
        assertThat(panel.run(installData, console)).isFalse();
        assertThat(console.scriptCompleted()).isTrue();

        // verify that the install path wasn't set
        assertThat(installData.getInstallPath()).isNull();

        // run the panel, selecting "goodDir"
        System.out.println();
        System.out.println("Test part 2 ...");
        console.addScript("TargetPanel.2", goodDir.getAbsolutePath(), "O", "1");
        assertThat(panel.run(installData, console)).isTrue();
        assertThat(console.scriptCompleted()).isTrue();

        // verify that the install path was updated
        assertThat(installData.getInstallPath()).isEqualTo(goodDir.getAbsolutePath());
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
        File root = temporaryFolder.toFile();
        File badDir = new File(root, "badDir");
        assertThat(badDir.mkdirs()).isTrue();
        createBadInstallationInfo(badDir);
        File goodDir = new File(root, "goodDir");   // don't bother creating it

        Properties properties = new Properties();
        properties.setProperty(InstallData.INSTALL_PATH, badDir.getAbsolutePath());

        TargetConsolePanel panel = new TargetConsolePanel(
                createPanelView(TargetPanel.class, "panel.install_path"),
                installData, prompt);
        assertThat(panel.run(installData, properties)).isFalse();

        properties.setProperty(InstallData.INSTALL_PATH, goodDir.getAbsolutePath());
        assertThat(panel.run(installData, properties)).isTrue();
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
