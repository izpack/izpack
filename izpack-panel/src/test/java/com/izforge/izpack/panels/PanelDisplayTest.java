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
package com.izforge.izpack.panels;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.text.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.Help;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.panels.finish.FinishPanel;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel;
import com.izforge.izpack.panels.licence.LicencePanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;

/**
 * Manual test for finish panel
 */
public class PanelDisplayTest extends AbstractPanelTest
{

    public PanelDisplayTest(GUIInstallData guiInstallData, ResourceManager resourceManager,
                            UninstallDataWriter uninstallDataWriter, TestGUIPanelContainer container,
                            IconsDatabase icons, RulesEngine rules, ObjectFactory factory, Locales locales)
    {
        super(container, guiInstallData, resourceManager, factory, rules, icons, uninstallDataWriter, locales);
    }

    @Before
    public void setUp()
    {
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/panel/");
    }


    @Test
    public void htmlInfoPanelShouldDisplayText() throws Exception
    {
        FrameFixture frameFixture = show(HTMLInfoPanel.class);
        String textArea = frameFixture.textBox(GuiId.HTML_INFO_PANEL_TEXT.id).text();
        assertThat(textArea, StringContains.containsString("This is a test"));
    }

    @Test
    public void licencePanelShouldDisplayText() throws Exception
    {
        FrameFixture frameFixture = show(LicencePanel.class, HTMLInfoPanel.class);
        String textArea = frameFixture.textBox(GuiId.LICENCE_TEXT_AREA.id).text();
        assertThat(textArea, StringContains.containsString("This is a licenSe panel"));
    }

    @Test
    public void simpleFinishPanelShouldDisplayFinishingText() throws Exception
    {
        FrameFixture frameFixture = show(SimpleFinishPanel.class);
        String text = frameFixture.label(GuiId.SIMPLE_FINISH_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
    }

    @Test
    public void helloThenFinishPanelShouldDisplay() throws Exception
    {
        UninstallDataWriter uninstallDataWriter = getUninstallDataWriter();
        Mockito.when(uninstallDataWriter.isUninstallRequired()).thenReturn(true);
        FrameFixture frameFixture = show(HelloPanel.class, SimpleFinishPanel.class);
        String welcomeLabel = frameFixture.label(GuiId.HELLO_PANEL_LABEL.id).text();
        assertThat(welcomeLabel, StringContains.containsString("Welcome to the installation of"));
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        String uninstallLabel = frameFixture.label(GuiId.SIMPLE_FINISH_UNINSTALL_LABEL.id).text();
        assertThat(uninstallLabel, StringContains.containsString("An uninstaller program has been created in"));
    }

    @Test
    public void finishPanelShouldDisplay() throws Exception
    {
        FrameFixture frameFixture = show(FinishPanel.class);
        String text = frameFixture.label(GuiId.FINISH_PANEL_LABEL.id).text();
        assertThat(text, StringContains.containsString("Installation has completed"));
        // Is automatic installation xml button visible?
        frameFixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).requireVisible();
    }

    @Test
    public void helpShouldDisplay() throws Exception
    {
        Panel panel = new Panel();
        panel.setClassName(HelloPanel.class.getName());
        panel.setHelps(Arrays.asList(new Help("eng", "un.html")));
        IzPanelView panelView = createPanelView(panel);
        FrameFixture frameFixture = show(Collections.singletonList(panelView));
        frameFixture.button(GuiId.BUTTON_HELP.id).requireVisible();
        frameFixture.button(GuiId.BUTTON_HELP.id).click();
        DialogFixture dialogFixture = frameFixture.dialog(GuiId.HELP_WINDOWS.id);
        dialogFixture.requireVisible();
        assertThat(dialogFixture.textBox().text(), StringContains.containsString("toto"));
    }

}
