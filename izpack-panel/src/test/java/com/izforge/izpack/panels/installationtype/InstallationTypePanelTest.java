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

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.panels.defaulttarget.DefaultTargetPanel;
import com.izforge.izpack.panels.pdflicence.PDFLicencePanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;
import com.izforge.izpack.test.Container;
import org.fest.swing.fixture.FrameFixture;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.izforge.izpack.installer.util.InstallPathHelper.TARGET_PANEL_DIR;
import static com.izforge.izpack.util.Platform.Name.MAC_OSX;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link DefaultTargetPanel} class.
 *
 * @author Hitesh A. Bosamiya
 */
@Container(TestGUIPanelContainer.class)
public class InstallationTypePanelTest extends AbstractPanelTest
{
    /**
     * Constructs a {@code DefaultTargetPanelTest}.
     *
     * @param container           the panel container
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param factory             the panel factory
     * @param rules               the rules
     * @param icons               the icons
     * @param uninstallDataWriter the uninstallation data writer
     * @param locales             the locales
     */
    public InstallationTypePanelTest(TestGUIPanelContainer container, GUIInstallData installData,
                                     ResourceManager resourceManager,
                                     ObjectFactory factory, RulesEngine rules, IconsDatabase icons,
                                     UninstallDataWriter uninstallDataWriter, Locales locales)
    {
        super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter, locales);
    }

    @Test
    public void shouldSelectNormalInstallRadioByDefault()
    {
        // The variable "modify.izpack.install" is not set
        FrameFixture fixture = showInstallationTypePanel();
        fixture.radioButton(GuiId.INSTALLATION_TYPE_NORMAL.id).requireSelected();
        GUIInstallData installData = getInstallData();
        // The variable "modify.izpack.install" is set to false
        installData.setVariable(GUIInstallData.MODIFY_INSTALLATION, "false");
        fixture.radioButton(GuiId.INSTALLATION_TYPE_NORMAL.id).requireSelected();
    }

    @Test
    public void shouldSelectModifyInstallRadioByDefault()
    {
        GUIInstallData installData = getInstallData();
        // The variable "modify.izpack.install" is set to true
        installData.setVariable(GUIInstallData.MODIFY_INSTALLATION, "true");
        FrameFixture fixture = showInstallationTypePanel();
        fixture.radioButton(GuiId.INSTALLATION_TYPE_MODIFY.id).requireSelected();
    }

    /**
     * Verifies that the summary body for new installation
     */
    @Test
    public void testSummaryBodyForNewInstallation()
    {
        GUIInstallData installData = getInstallData();
        checkSummaryBody(installData.getMessages().get("InstallationTypePanel.normal"));
    }

    /**
     * Verifies that the summary body for modify installation
     */
    @Test
    public void testSummaryBodyForModifyInstallation()
    {
        GUIInstallData installData = getInstallData();
        installData.setVariable(GUIInstallData.MODIFY_INSTALLATION, "true");
        checkSummaryBody(installData.getMessages().get("InstallationTypePanel.modify"));
    }

    /**
     * Creates a fixture for a {@link InstallationTypePanel} and a {@link SimpleFinishPanel}.
     * <p>
     *     This method waits for the panel to become visible before it returns.
     * </p>
     *
     * @return A frame fixture for the created panel.
     */
    private FrameFixture showInstallationTypePanel()
    {
        IzPanelView view = createPanelView(InstallationTypePanel.class);
        view.getPanel().setPanelId("InstallationTypePanel_0");

        FrameFixture fixture = show(view, createPanelView(SimpleFinishPanel.class));
        waitForPanel(InstallationTypePanel.class);

        assertTrue(getPanels().getView() instanceof InstallationTypePanel);

        return fixture;
    }

    private void checkSummaryBody(String expected)
    {
        GUIInstallData installData = getInstallData();
        Panel panelMetadata = Mockito.mock(Panel.class);
        InstallerFrame parent = Mockito.mock(InstallerFrame.class);
        Resources resources = Mockito.mock(Resources.class);
        Log log = Mockito.mock(Log.class);
        IconsDatabase iconsDatabase = Mockito.mock(IconsDatabase.class);
        Mockito.when(parent.getIcons()).thenReturn(iconsDatabase);
        InstallationTypePanel panel = new InstallationTypePanel(panelMetadata, parent, installData, resources, log);
        String summaryBody = panel.getSummaryBody();
        assertEquals(expected, summaryBody);
    }
}
