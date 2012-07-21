/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.test.panel;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.text.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.DefaultNavigator;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.container.TestPanelContainer;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platforms;

/**
 * Manual test for finish panel
 */
@RunWith(PicoRunner.class)
@Container(TestPanelContainer.class)
public class PanelDisplayTest
{

    private GUIInstallData guiInstallData;
    private FrameFixture frameFixture;
    private ResourceManager resourceManager;
    private UninstallDataWriter uninstallDataWriter;
    private final TestPanelContainer container;
    private final IconsDatabase icons;
    private final RulesEngine rules;
    private final ObjectFactory factory;

    public PanelDisplayTest(GUIInstallData guiInstallData, ResourceManager resourceManager,
                            UninstallDataWriter uninstallDataWriter, TestPanelContainer container,
                            IconsDatabase icons, RulesEngine rules, ObjectFactory factory)
    {
        this.guiInstallData = guiInstallData;
        this.resourceManager = resourceManager;
        this.uninstallDataWriter = uninstallDataWriter;
        this.container = container;
        this.icons = icons;
        this.rules = rules;
        this.factory = factory;
    }

    @Before
    public void setUp()
    {
        resourceManager.setResourceBasePath("/com/izforge/izpack/test/panel/");
    }


    @After
    public void after()
    {
        frameFixture.cleanUp();
    }


    @Test
    public void htmlInfoPanelShouldDisplayText() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel");
        String textArea = frameFixture.textBox(GuiId.HTML_INFO_PANEL_TEXT.id).text();
        assertThat(textArea, StringContains.containsString("This is a test"));
    }

    @Test
    public void licencePanelShouldDisplayText() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.licence.LicencePanel");
        String textArea = frameFixture.textBox(GuiId.LICENCE_TEXT_AREA.id).text();
        assertThat(textArea, StringContains.containsString("This is a licenSe panel"));
    }

    @Test
    public void simpleFinishPanelShouldDisplayFinishingText() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.simplefinish.SimpleFinishPanel");
        String text = frameFixture.label(GuiId.SIMPLE_FINISH_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
    }

    @Test
    public void helloThenFinishPanelShouldDisplay() throws Exception
    {
        Mockito.when(uninstallDataWriter.isUninstallRequired()).thenReturn(true);
        addPanelAndShow("com.izforge.izpack.panels.hello.HelloPanel",
                        "com.izforge.izpack.panels.simplefinish.SimpleFinishPanel");
        String welcomLabel = frameFixture.label(GuiId.HELLO_PANEL_LABEL.id).text();
        assertThat(welcomLabel, StringContains.containsString("Welcome to the installation of"));
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        String uninstallLabel = frameFixture.label(GuiId.SIMPLE_FINISH_UNINSTALL_LABEL.id).text();
        assertThat(uninstallLabel, StringContains.containsString("An uninstaller program has been created in"));
    }

    @Test
    public void finishPanelShouldDisplay() throws Exception
    {
        addPanelAndShow("com.izforge.izpack.panels.finish.FinishPanel");
        String text = frameFixture.label(GuiId.FINISH_PANEL_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
        // Is automatic installation xml button visible?
        frameFixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).requireVisible();
    }

    @Test
    public void helpShouldDisplay() throws Exception
    {
        Panel panel = new Panel();
        panel.setClassName("com.izforge.izpack.panels.hello.HelloPanel");
        panel.setHelps(Arrays.asList(new Help("eng", "un.html")));
        IzPanelView panelView = new IzPanelView(panel, factory, guiInstallData);
        addPanelAndShow(Collections.singletonList(panelView));
        frameFixture.button(GuiId.BUTTON_HELP.id).requireVisible();
        frameFixture.button(GuiId.BUTTON_HELP.id).click();
        DialogFixture dialogFixture = frameFixture.dialog(GuiId.HELP_WINDOWS.id);
        dialogFixture.requireVisible();
        assertThat(dialogFixture.textBox().text(), StringContains.containsString("toto"));
    }

    private void addPanelAndShow(String... classNames)
            throws Exception
    {
        List<IzPanelView> panelList = new ArrayList<IzPanelView>();
        for (String className : classNames)
        {
            Panel panel = new Panel();
            panel.setClassName(className);
            IzPanelView panelView = new IzPanelView(panel, factory, guiInstallData);
            panelList.add(panelView);
        }
        addPanelAndShow(panelList);
    }

    private void addPanelAndShow(List<IzPanelView> panelList) throws Exception
    {
        IzPanels panels = new IzPanels(panelList, container, guiInstallData);
        DefaultNavigator navigator = new DefaultNavigator(panels, icons, guiInstallData);
        InstallerFrame frame = new InstallerFrame("A title", guiInstallData, rules,
                                                  icons, panels, uninstallDataWriter, resourceManager,
                                                  Mockito.mock(UninstallData.class),
                                                  Mockito.mock(Housekeeper.class), navigator,
                                                  Mockito.mock(Log.class));
        container.getContainer().addComponent(frame);
        InstallDataConfiguratorWithRules configuratorWithRules = new InstallDataConfiguratorWithRules(
                guiInstallData, rules, Platforms.UNIX);
        InstallerController controller = new InstallerController(configuratorWithRules, frame);
        controller.buildInstallation();
        controller.launchInstallation();
        frameFixture = new FrameFixture(frame);
    }

}
