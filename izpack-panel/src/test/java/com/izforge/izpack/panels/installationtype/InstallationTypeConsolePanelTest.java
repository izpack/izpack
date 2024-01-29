/*
 * IzPack - Copyright 2024 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2024 Hitesh A. Bosamiya
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
package com.izforge.izpack.panels.installationtype;

import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests the {@link InstallationTypePanel} class.
 *
 * @author Hitesh A. Bosamiya
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class InstallationTypeConsolePanelTest
{
    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The console.
     */
    private final TestConsole console;

    /**
     * Constructs a {@code InstallationTypeConsolePanelTest}.
     *
     * @param installData the installation data
     * @param console     the console
     */
    public InstallationTypeConsolePanelTest(InstallData installData, TestConsole console)
    {
        this.console = console;
        this.installData = installData;
    }

    /**
     * Verifies new installation, when "modify.izpack.install" is not set.
     */
    @Test
    public void testNewInstallation()
    {
        assertNull(installData.getVariable(InstallData.MODIFY_INSTALLATION));
        InstallationTypeConsolePanel panel = new InstallationTypeConsolePanel(null, installData);
        console.addScript("testNewInstallation", "0", "\n");
        assertTrue(panel.run(installData, console));
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies modify installation, when "modify.izpack.install" is not set.
     */
    @Test
    public void testModifyInstallation()
    {
        assertNull(installData.getVariable(InstallData.MODIFY_INSTALLATION));
        InstallationTypeConsolePanel panel = new InstallationTypeConsolePanel(null, installData);
        console.addScript("testNewInstallation", "1", "\n");
        assertTrue(panel.run(installData, console));
        assertEquals("true", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation, when "modify.izpack.install" is set to false.
     */
    @Test
    public void testNewInstallationWithVariable()
    {
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        InstallationTypeConsolePanel panel = new InstallationTypeConsolePanel(null, installData);
        console.addScript("testNewInstallation", "\n");
        assertTrue(panel.run(installData, console));
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation, when "modify.izpack.install" is set to true.
     */
    @Test
    public void testModifyInstallationWithVariable()
    {
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        InstallationTypeConsolePanel panel = new InstallationTypeConsolePanel(null, installData);
        console.addScript("testNewInstallation", "\n");
        assertTrue(panel.run(installData, console));
        assertEquals("true", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies new installation using properties.
     */
    @Test
    public void testNewInstallationFromProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(InstallData.MODIFY_INSTALLATION, "false");
        InstallationTypeConsolePanel panel = new InstallationTypeConsolePanel(null, installData);
        assertTrue(panel.run(installData, properties));
        assertEquals("false", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies modify installation using properties.
     */
    @Test
    public void testModifyInstallationFromProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(InstallData.MODIFY_INSTALLATION, "true");
        InstallationTypeConsolePanel panel = new InstallationTypeConsolePanel(null, installData);
        assertTrue(panel.run(installData, properties));
        assertEquals("true", installData.getVariable(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies generateOptions method when "modify.izpack.install" is not set.
     */
    @Test
    public void testGenerateOptionsForUnsetVariable()
    {
        Options options = new Options();
        @SuppressWarnings("unchecked")
        PanelView<ConsolePanel> panelView = Mockito.mock(PanelView.class);
        Panel panel = Mockito.mock(Panel.class);
        Mockito.when(panelView.getPanel()).thenReturn(panel);
        Mockito.when(panel.getPanelId()).thenReturn("InstallationTypePanel_0");
        InstallationTypeConsolePanel itcPanel = new InstallationTypeConsolePanel(panelView, installData);
        assertTrue(itcPanel.generateOptions(installData, options));
        assertTrue(options.containsKey(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies generateOptions method when "modify.izpack.install" is set to false (new installation).
     */
    @Test
    public void testGenerateOptionsForVariableSetToFalse()
    {
        Options options = new Options();
        @SuppressWarnings("unchecked")
        PanelView<ConsolePanel> panelView = Mockito.mock(PanelView.class);
        Panel panel = Mockito.mock(Panel.class);
        Mockito.when(panelView.getPanel()).thenReturn(panel);
        Mockito.when(panel.getPanelId()).thenReturn("InstallationTypePanel_0");
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        InstallationTypeConsolePanel itcPanel = new InstallationTypeConsolePanel(panelView, installData);
        assertTrue(itcPanel.generateOptions(installData, options));
        assertEquals("false", options.get(InstallData.MODIFY_INSTALLATION));
    }

    /**
     * Verifies generateOptions method when "modify.izpack.install" is set to true (modify installation).
     */
    @Test
    public void testGenerateOptionsForVariableSetToTrue()
    {
        Options options = new Options();
        @SuppressWarnings("unchecked")
        PanelView<ConsolePanel> panelView = Mockito.mock(PanelView.class);
        Panel panel = Mockito.mock(Panel.class);
        Mockito.when(panelView.getPanel()).thenReturn(panel);
        Mockito.when(panel.getPanelId()).thenReturn("InstallationTypePanel_0");
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        InstallationTypeConsolePanel itcPanel = new InstallationTypeConsolePanel(panelView, installData);
        assertTrue(itcPanel.generateOptions(installData, options));
        assertEquals("true", options.get(InstallData.MODIFY_INSTALLATION));
    }
}
