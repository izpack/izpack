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

import static org.fest.swing.timing.Timeout.timeout;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
     * Situation: Empty path is entered during target panel
     *   This is very similar to the testDirectoryExists test, since the current user directory (where JVM was started)
     *   is most likley to exist. How would you start the JVM from a non-existential location?
     *
     * 1. Emit a warning in the form of a question
     * 2. Ensure target panel warning is shown in the warning question prompt
     * 3. Ensure that the installation path is set to the 'user.dir' system property.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEmptyPath() throws Exception
    {
        File userDir = new File(System.getProperty("user.dir"));
        GUIInstallData installData = getInstallData();
        installData.setDefaultInstallPath("");

        FrameFixture fixture = showTargetPanel();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        checkWarningQuestion(fixture, installData.getMessages().get("TargetPanel.exists_warn"));

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
        FrameFixture fixture = showTargetPanel();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        String expectedMessage = installData.getMessages().get("TargetPanel.createdir") + "\n" + dir;
        checkWarning(fixture, expectedMessage);

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
        File dir = temporaryFolder.getRoot();
        temporaryFolder.newFile("warning-is-only-triggered-for-non-empty-directory.txt");

        GUIInstallData installData = getInstallData();
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = showTargetPanel();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        checkWarningQuestion(fixture, installData.getMessages().get("TargetPanel.exists_warn"));

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
        File dir = temporaryFolder.newFolder("install");

        GUIInstallData installData = getInstallData();
        installData.setDefaultInstallPath(dir.getAbsolutePath());

        // show the panel
        FrameFixture fixture = showTargetPanel(NotWritableTargetPanel.class);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();

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
        FrameFixture fixture = showTargetPanel();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        checkErrorMessage(fixture, messages.get("TargetPanel.required"));

        assertTrue(dir.mkdirs());

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();

        checkErrorMessage(fixture, messages.get("TargetPanel.required.forModificationInstallation"));

        // create the .installinformationfile
        TargetPanelTestHelper.createInstallationInfo(dir);

        // navigation should now succeed.
        checkNavigateNext(fixture);

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

        File badDir = temporaryFolder.newFolder("badDir");
        File goodDir = temporaryFolder.newFolder("goodDir");

        installData.setDefaultInstallPath(badDir.getAbsolutePath());

        // create an invalid "badDir/.installationinformation" to simulate incompatible data
        TargetPanelTestHelper.createBadInstallationInfo(badDir);

        // show the panel
        FrameFixture fixture = showTargetPanel();
        TargetPanel panel = (TargetPanel) getPanels().getView();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();

        // panel should be the same and error should be displayed
        checkErrorMessage(fixture, TargetPanelTestHelper.getIncompatibleInstallationMessage(installData));

        // should still be on the TargetPanel
        assertEquals(panel, getPanels().getView());
        fixture.textBox(GuiId.PATH_SELECTION_PANEL_PATH_TEXT_FIELD.id).focus();
        fixture.textBox(GuiId.PATH_SELECTION_PANEL_PATH_TEXT_FIELD.id).setText(goodDir.getAbsolutePath());

        // suppress dialog indicating that goodDir will be created
        installData.setVariable("ShowCreateDirectoryMessage", "false");

        // attempt to navigate to the next panel
        checkNavigateNext(fixture);
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
        FrameFixture fixture = showTargetPanel();

        TargetPanel panel = (TargetPanel) getPanels().getView();
        panel.setMustExist(true); // to avoid popping up a Directory already exists dialog
        panel.setExistFiles(requiredFiles);

        fixture.button(GuiId.BUTTON_NEXT.id).click();
        checkErrorMessage(fixture, messages.get("TargetPanel.notValid"));

        // create the required files
        for (String required : requiredFiles)
        {
            File file = new File(dir, required);
            FileUtils.touch(file);
        }

        checkNavigateNext(fixture);
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

    private void checkWarningQuestion(FrameFixture frame, String expected)
    {
        JOptionPaneFixture warningQuestion = frame.optionPane(timeout(2000)).requireWarningMessage();
        warningQuestion.requireMessage(expected);
        warningQuestion.yesButton().click();
    }

    /**
     * Verify that an error dialog is being displayed.
     *
     * @param frame    the parent frame
     * @param expected the expected error message
     */
    private void checkErrorMessage(FrameFixture frame, String expected)
    {
        JOptionPaneFixture error = frame.optionPane(timeout(2000)).requireErrorMessage();
        // Can't use error.requireMessage due to custom JPanel message in GUIPrompt
        assertThat(error.label("OptionPane.label").text(), equalTo(expected));
        error.button().click();
    }

    /**
     * Verify that a question dialog is being displayed.
     *
     * @param frame    the parent frame
     * @param expected the expected error message
     */
    private void checkQuestionMessage(FrameFixture frame, String expected)
    {
        JOptionPaneFixture question = frame.optionPane(timeout(2000)).requireQuestionMessage();
        question.requireMessage(expected);
        question.yesButton().click();
    }

    /**
     * Verifies that the next panel can be navigated to.
     *
     * @param frame the frame
     * @throws InterruptedException if interrupted waiting for the panel to change
     */
    private void checkNavigateNext(FrameFixture frame) throws InterruptedException
    {
        // attempt to navigate to the next panel
        frame.button(GuiId.BUTTON_NEXT.id).click();

        waitForPanel(SimpleFinishPanel.class);

        assertThat(getPanels().getView(), instanceOf(SimpleFinishPanel.class));
    }

    /**
     * Creates and waits for a {@link TargetPanel}.
     *
     * @return The frame fixture for the target panel.
     */
    private FrameFixture showTargetPanel()
    {
        return showTargetPanel(TargetPanel.class);
    }

    /**
     * Creates and waits for a specific target panel.
     *
     * @return The frame fixture for the target panel.
     */
    private FrameFixture showTargetPanel(Class<? extends TargetPanel> clazz)
    {
        FrameFixture fixture = show(clazz, SimpleFinishPanel.class);
        waitForPanel(clazz);
        assertThat(getPanels().getView(), instanceOf(clazz));
        return fixture;
    }

    /**
     * Helper implementation of TargetPanel that simulates no permission to write to a directory.
     */
    public static class NotWritableTargetPanel extends TargetPanel
    {
        private static final long serialVersionUID = -1516699768499683236L;

        public NotWritableTargetPanel(Panel panel, InstallerFrame parent, GUIInstallData installData,
                                      Resources resources, Log log)
        {
            super(panel, parent, installData, resources, log);
        }

        /**
         * This implementation always returns false.
         *
         * @param path The path which is to be checked.
         * @return Always false.
         */
        @Override
        protected boolean isWritable(File path) {

            return false;
        }
    }
}
