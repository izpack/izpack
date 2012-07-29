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
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.fest.swing.fixture.FrameFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;
import com.izforge.izpack.test.Container;

/**
 * Tests the {@link TargetPanel} class.
 *
 * @author Tim Anderson
 */
@Container(TestGUIPanelContainer.class)
public class TargetPanelTest extends AbstractPanelTest
{

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Constructs a {@code TargetPanelTest}.
     *
     * @param container           the panel container
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param factory             the panel factory
     * @param rules               the rules
     * @param icons               the icons
     * @param uninstallDataWriter the uninstallation data writer
     */
    public TargetPanelTest(TestGUIPanelContainer container, GUIInstallData installData, ResourceManager resourceManager,
                           ObjectFactory factory, RulesEngine rules, IconsDatabase icons,
                           UninstallDataWriter uninstallDataWriter)
    {
        super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter);
    }

    /**
     * Verifies that the <em>TargetPanel.incompatibleInstallation</em> message is displayed if the selected
     * directory contains an unrecognised .installationinformation file.
     *
     * @throws Exception for any error
     */
    @Test
    public void testIncompatibleInstallation() throws Exception
    {
        GUIInstallData installData = getInstallData();

        // get the expected error message for the locale
        String expectedMessage = TargetPanelTestHelper.getIncompatibleInstallationMessage(installData);

        // set up two potential directories to install to, "badDir" and "goodDir"
        File root = temporaryFolder.getRoot();
        File badDir = new File(root, "badDir");
        assertTrue(badDir.mkdirs());
        File goodDir = new File(root, "goodDir");   // don't bother creating it
        installData.setDefaultInstallPath(badDir.getAbsolutePath());


        // create an invalid "badDir/.installationinformation" to simulate incompatible data
        TargetPanelTestHelper.createBadInstallationInfo(badDir);

        // show the panel
        FrameFixture fixture = show(TargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof TargetPanel);
        TargetPanel panel = (TargetPanel) getPanels().getView();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(2000);

        // panel should be the same and error should be displayed
        assertEquals(panel, getPanels().getView());
        fixture.optionPane().requireErrorMessage().requireMessage(expectedMessage);
        Thread.sleep(2000);
        fixture.optionPane().okButton().click();

        // should still be on the TargetPanel
        assertEquals(panel, getPanels().getView());
        fixture.textBox().setText(goodDir.getAbsolutePath());

        // suppress dialog indicating that goodDir will be created
        installData.setVariable("ShowCreateDirectoryMessage", "false");

        // attempt to navigate to the next panel
        Thread.sleep(2000);
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
    }

}
