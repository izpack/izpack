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
package com.izforge.izpack.panels.datacheck;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;
import jakarta.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DataCheckConsolePanel} class.
 *
 * @author Hitesh A. Bosamiya
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class DataCheckConsolePanelTest
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
    @Inject
    public DataCheckConsolePanelTest(InstallData installData, TestConsole console)
    {
        this.console = console;
        this.installData = installData;
    }

    /**
     * Verifies data check console panel display with packs.
     */
    @Test
    public void testDataCheckConsolePanelWithPacks()
    {
        installData.setVariable("Variable1", "Value1");
        installData.setVariable("Variable2", "Value2");
        mockPacks();
        @SuppressWarnings("unchecked")
        PanelView<ConsolePanel> panelView = Mockito.mock(PanelView.class);
        Mockito.when(panelView.getPanelId()).thenReturn("DataCheckPanel_0");
        DataCheckConsolePanel dccPanel = new DataCheckConsolePanel(panelView);
        console.addScript("DataCheckPanel.1", "1");
        List<String> output = console.getOutput();
        assertTrue(dccPanel.run(installData, console));
        assertEquals(DataCheckCommon.SUB_LABEL, output.get(4));
        DataCheckCommonTest.verifyOutput(output.get(5), "InstallData Variables:", "Variable1", "Variable2");
        DataCheckCommonTest.verifyOutput(output.get(6), "Available Packs:", "Pack1 (Selected)", "Pack2 (Unselected)");
        DataCheckCommonTest.verifyOutput(output.get(7), "Conditions:", "izpack.windowsinstall", "izpack.linuxinstall");
    }

    /**
     * Verifies data check console panel display without packs.
     */
    @Test
    public void testDataCheckConsolePanelWithoutPacks()
    {
        installData.setVariable("Variable1", "Value1");
        installData.setVariable("Variable2", "Value2");
        @SuppressWarnings("unchecked")
        PanelView<ConsolePanel> panelView = Mockito.mock(PanelView.class);
        Mockito.when(panelView.getPanelId()).thenReturn("DataCheckPanel_0");
        DataCheckConsolePanel dccPanel = new DataCheckConsolePanel(panelView);
        console.addScript("DataCheckPanel.1", "1");
        List<String> output = console.getOutput();
        assertTrue(dccPanel.run(installData, console));
        assertEquals(DataCheckCommon.SUB_LABEL, output.get(4));
        DataCheckCommonTest.verifyOutput(output.get(5), "InstallData Variables:", "Variable1", "Variable2");
        DataCheckCommonTest.verifyOutput(output.get(6), "Conditions:", "izpack.windowsinstall", "izpack.linuxinstall");
    }

    private void mockPacks() {
        Pack pack1 = new Pack("Pack1", null, null, null, null, true, true, false, null, false, 0);
        Pack pack2 = new Pack("Pack2", null, null, null, null, false, true, false, null, false, 0);
        List<Pack> packList =  installData.getAllPacks();
        packList.add(pack1);
        packList.add(pack2);
        List<Pack> selectedPackList = installData.getSelectedPacks();
        selectedPackList.add(pack1);
    }
}
