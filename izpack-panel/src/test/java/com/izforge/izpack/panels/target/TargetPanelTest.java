/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JOptionPaneFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
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
     * @param locales             the locales
     */
    public TargetPanelTest(TestGUIPanelContainer container, GUIInstallData installData, ResourceManager resourceManager,
                           ObjectFactory factory, RulesEngine rules, IconsDatabase icons,
                           UninstallDataWriter uninstallDataWriter, Locales locales)
    {
        super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter, locales);
    }

    /**
     * Verifies that if no path is entered, it will default to that of the <em>user.dir</em> system property.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEmptyPath() throws Exception
    {
        File userDir = new File(System.getProperty("user.dir"));
        GUIInstallData installData = getInstallData();
        installData.setDefaultInstallPath("");

        FrameFixture fixture = show(TargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof TargetPanel);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        checkWarning(fixture, installData.getMessages().get("TargetPanel.empty_target"));
        Thread.sleep(1000);
        checkQuestionMessage(fixture, installData.getMessages().get("TargetPanel.warn"));

        Thread.sleep(1000);
        assertEquals(userDir.getAbsolutePath(), installData.getInstallPath());
    }

    /**
     * Verifies that a dialog is displayed if the directory will be created.
     *
     * @throws Exception for any error
     */
    @Test
    public void testShowCreateDirectoryMessage() throws Exception
    {
        GUIInstallData installData = getInstallData();
        File root = temporaryFolder.getRoot();
        File dir = new File(root, "install");
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = show(TargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof TargetPanel);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        String expectedMessage = installData.getMessages().get("TargetPanel.createdir") + "\n" + dir;
        checkWarning(fixture, expectedMessage);

        Thread.sleep(1000);
        assertEquals(dir.getAbsolutePath(), installData.getInstallPath());
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
    }

    /**
     * Verifies that a dialog is displayed if a directory is selected that already exists.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDirectoryExists() throws Exception
    {
        GUIInstallData installData = getInstallData();
        File root = temporaryFolder.getRoot();
        File dir = new File(root, "install");
        assertTrue(dir.mkdirs());
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = show(TargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof TargetPanel);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        checkQuestionMessage(fixture, installData.getMessages().get("TargetPanel.warn"));

        Thread.sleep(1000);
        assertEquals(dir.getAbsolutePath(), installData.getInstallPath());
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
    }

    /**
     * Verifies that a dialog is displayed if the target directory cannot be written to.
     *
     * @throws Exception for any error
     */
    @Test
    public void testNotWritable() throws Exception
    {
        File root = temporaryFolder.getRoot();
        File dir = new File(root, "install");

        GUIInstallData installData = getInstallData();
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = show(NotWritableTargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);

        checkErrorMessage(fixture, installData.getMessages().get("TargetPanel.notwritable"));
        assertNull(installData.getInstallPath());
    }

    /**
     * Verifies that when the "modify.izpack.install" variable is specified, the target directory must exist and
     * contain an <em>.installationinformation</em> file.
     *
     * @throws Exception for any error
     */
    @Test
    public void testModifyInstallation() throws Exception
    {
        GUIInstallData installData = getInstallData();
        Messages messages = installData.getMessages();
        installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");

        File root = temporaryFolder.getRoot();
        File dir = new File(root, "install");
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = show(TargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof TargetPanel);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        checkErrorMessage(fixture, messages.get("PathInputPanel.required"));

        Thread.sleep(1000);
        assertTrue(dir.mkdirs());

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);

        checkErrorMessage(fixture, messages.get("PathInputPanel.required.forModificationInstallation"));

        // create the .installinformationfile
        TargetPanelTestHelper.createInstallationInfo(dir);
        Thread.sleep(1000);
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);

        // navigation should now succeed.
        assertEquals(dir.getAbsolutePath(), installData.getInstallPath());
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
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
        Thread.sleep(1000);

        // panel should be the same and error should be displayed
        assertEquals(panel, getPanels().getView());
        checkErrorMessage(fixture, TargetPanelTestHelper.getIncompatibleInstallationMessage(installData));
        Thread.sleep(1000);

        // should still be on the TargetPanel
        assertEquals(panel, getPanels().getView());
        fixture.textBox().setText(goodDir.getAbsolutePath());

        // suppress dialog indicating that goodDir will be created
        installData.setVariable("ShowCreateDirectoryMessage", "false");

        // attempt to navigate to the next panel
        Thread.sleep(1000);
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1500);
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
    }

    /**
     * Verifies that when {@link TargetPanel#setExistFiles(String[])} is used, the specified files must exist
     * in order for the panel to be valid.
     *
     * @throws Exception for any error
     */
    @Test
    public void testFilesExist() throws Exception
    {
        String[] requiredFiles = {"a", "b"};

        GUIInstallData installData = getInstallData();
        Messages messages = installData.getMessages();
        File root = temporaryFolder.getRoot();
        File dir = new File(root, "install");
        assertTrue(dir.mkdirs());
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = show(TargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof TargetPanel);
        TargetPanel panel = (TargetPanel) getPanels().getView();
        panel.setMustExist(true); // to avoid popping up a Directory already exists dialog
        panel.setExistFiles(requiredFiles);

        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        checkErrorMessage(fixture, messages.get("PathInputPanel.notValid"));

        // create the required files
        for (String required : requiredFiles)
        {
            File file = new File(dir, required);
            FileUtils.touch(file);
        }
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
        assertEquals(dir.getAbsolutePath(), installData.getInstallPath());
    }

    /**
     * Verifies that a warning dialog is being displayed.
     *
     * @param frame    the parent frame
     * @param expected the expected warning message
     */
    private void checkWarning(FrameFixture frame, String expected)
    {
        JOptionPaneFixture warning = frame.optionPane().requireWarningMessage();
        warning.requireMessage(expected);
        warning.okButton().click();
    }

    /**
     * Verify that an error dialog is being displayed.
     *
     * @param frame    the parent frame
     * @param expected the expected error message
     */
    private void checkErrorMessage(FrameFixture frame, String expected)
    {
        JOptionPaneFixture error = frame.optionPane().requireErrorMessage();
        error.requireMessage(expected);
        error.okButton().click();
    }

    /**
     * Verify that a question dialog is being displayed.
     *
     * @param frame    the parent frame
     * @param expected the expected error message
     */
    private void checkQuestionMessage(FrameFixture frame, String expected)
    {
        JOptionPaneFixture question = frame.optionPane().requireQuestionMessage();
        question.requireMessage(expected);
        question.yesButton().click();
    }

    /**
     * Helper implementation of TargetPanel that simulates no permission to write to a directory.
     */
    public static class NotWritableTargetPanel extends TargetPanel
    {
        public NotWritableTargetPanel(Panel panel, InstallerFrame parent, GUIInstallData installData,
                                      Resources resources, Log log)
        {
            super(panel, parent, installData, resources, log);
        }
    }
}
