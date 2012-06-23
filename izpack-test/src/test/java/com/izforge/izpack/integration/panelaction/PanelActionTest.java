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

package com.izforge.izpack.integration.panelaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.timing.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.ActionStage;
import com.izforge.izpack.api.installer.DataValidator;
import com.izforge.izpack.compiler.container.TestInstallationContainer;
import com.izforge.izpack.data.PanelAction;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.integration.HelperTestMethod;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestHousekeeper;


/**
 * Tests that {@link PanelAction}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestInstallationContainer.class)
public class PanelActionTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Install data.
     */
    private final InstallData installData;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * The house-keeper.
     */
    private final TestHousekeeper housekeeper;

    /**
     * The panels.
     */
    private final IzPanels panels;

    /**
     * The frame fixture.
     */
    private FrameFixture frameFixture;

    /**
     * Constructs an <tt>PanelActionValidatorTest</tt>.
     *
     * @param installData the install data
     * @param frame       the installer frame
     * @param controller  the installer controller
     * @param housekeeper the house-keeper
     * @param panels      the panels
     */
    public PanelActionTest(InstallData installData, InstallerFrame frame, InstallerController controller,
                           TestHousekeeper housekeeper, IzPanels panels)
    {
        this.installData = installData;
        this.frame = frame;
        this.controller = controller;
        this.housekeeper = housekeeper;
        this.panels = panels;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp()
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = new File(temporaryFolder.getRoot(), "izpackTest");
        assertTrue(installPath.mkdirs());
        installData.setInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown()
    {
        if (frameFixture != null)
        {
            frameFixture.cleanUp();
        }
    }

    /**
     * Verifies that {@link DataValidator}s associated with panels are invoked.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/panelactions.xml")
    public void testPanelActions() throws Exception
    {
        assertEquals(2, panels.getPanels().size());
        Panel hello = panels.getPanels().get(0);
        // Panel finish = panels.getPanels().get(1);

        checkActions(hello);
        // checkActions(finish);
        // TODO - cant use the same action classes for different panels as the configuration is shared

        checkActionInvocations("HelloPanel", 0, 0, 0, 0, 0);

        frameFixture = HelperTestMethod.prepareFrameFixture(frame, controller);

        checkActionsConfiguration(hello);

        checkActionInvocations("HelloPanel", 1, 1, 0, 0, 0);

        // HelloPanel
        Thread.sleep(2000);
        checkCurrentPanel(HelloPanel.class);
        installData.setVariable("HelloPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("HelloPanel.error");

        checkActionInvocations("HelloPanel", 1, 1, 1, 1, 1);

        installData.setVariable("HelloPanel.status", "WARNING");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("HelloPanel.warning");

        checkActionInvocations("HelloPanel", 1, 1, 2, 2, 2);

        // SimpleFinishPanel
        Thread.sleep(1000);
        checkCurrentPanel(SimpleFinishPanel.class);

        // Validators not invoked on last panel, so this should be a no-op
        frameFixture.button(GuiId.BUTTON_QUIT.id).click();

        // verify the installer has terminated
        housekeeper.waitShutdown(2 * 60 * 1000);
        assertTrue(housekeeper.hasShutdown());
        assertEquals(0, housekeeper.getExitCode());
        assertFalse(housekeeper.getReboot());
    }

    /**
     * Verifies that the current panel is an instance of the specified type.
     *
     * @param type the expected panel type
     */
    private void checkCurrentPanel(Class<? extends IzPanel> type)
    {
        Panel panel = panels.getPanel();
        assertEquals(type.getName(), panel.getClassName());
    }

    /**
     * Verifies that actions have been invoked the expected no. of times for a given panel.
     *
     * @param panelId      the panel identifier
     * @param preConstruct the expected pre-construction action invocations
     * @param preActivate  the expected pre-activate action invocations
     * @param preValidate  the expected pre-validate action invocations
     * @param validate     the expected validate action invocations
     * @param postValidate the expected post-validate action invocations
     */
    private void checkActionInvocations(String panelId, int preConstruct, int preActivate, int preValidate,
                                        int validate, int postValidate)
    {
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException ignore)
        {
            // do nothing
        }
        assertEquals(preConstruct, TestPanelAction.getPreConstruct(panelId, installData));
        assertEquals(preActivate, TestPanelAction.getPreActivate(panelId, installData));
        assertEquals(preValidate, TestPanelAction.getPreValidate(panelId, installData));
        assertEquals(validate, TestPanelAction.getValidate(panelId, installData));
        assertEquals(postValidate, TestPanelAction.getPostValidate(panelId, installData));

    }

    /**
     * Verifies there is an action registered for each action stage.
     *
     * @param panel the panel to check
     */
    private void checkActions(Panel panel)
    {
        checkAction(panel.getPreConstructionActions(), PreConstructPanelAction.class);
        checkAction(panel.getPreActivationActions(), PreActivatePanelAction.class);
        checkAction(panel.getPreValidationActions(), PreValidatePanelAction.class);
        checkAction(panel.getPostValidationActions(), PostValidatePanelAction.class);
    }

    /**
     * Verifies there is a single action of the specified type.
     *
     * @param actions the actions
     * @param type    the expected type
     */
    private void checkAction(List<String> actions, Class<? extends PanelAction> type)
    {
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertEquals(type.getName(), actions.get(0)); // compiler emits fully qualified class names
    }

    /**
     * Verifies that the correct action params are registered for each action stage.
     *
     * @param panel the panel to check
     */
    private void checkActionsConfiguration(Panel panel)
    {
        checkActionConfiguration(panel.getPanelid(), ActionStage.PRECONSTRUCT);
        checkActionConfiguration(panel.getPanelid(), ActionStage.PREACTIVATE);
        checkActionConfiguration(panel.getPanelid(), ActionStage.PREVALIDATE, "prop1", "value1");
        checkActionConfiguration(panel.getPanelid(), ActionStage.POSTVALIDATE, "prop2", "value2", "prop3", "value3");
    }

    /**
     * Verifies that the specified params are registered for the action on the panel. Can also
     * verify that no params are registered, by specifying an empty array for {@code properties}.
     *
     * @param panelId    the panel to check
     * @param stage      the action stage to check
     * @param properties a list of key/value pairs to verify; if not left empty, must contain
     *                   an even number of strings (i.e. {@code key, value, key, value ...})
     */
    private void checkActionConfiguration(String panelId, ActionStage stage, String... properties)
    {
        String prefix = panelId + "." + stage.toString().toLowerCase() + ".config.";
        if (properties.length == 0)
        {
            Set<Object> keys = installData.getVariables().getProperties().keySet();
            for (Object key : keys)
            {
                assertFalse(key.toString().startsWith(prefix));
            }
        }
        else
        {
            for (int i = 0; i < properties.length; i++)
            {
                String name = prefix + properties[i];
                String value = properties[++i];
                assertEquals(value, installData.getVariable(name));
            }
        }
    }

    /**
     * Verifies that a dialog is being displayed with the specified text.
     * <p/>
     * This closes the dialog.
     *
     * @param text the expected text
     */
    private void checkDialog(String text)
    {
        DialogFixture dialog = frameFixture.dialog(Timeout.timeout(10000));
        assertEquals(text, dialog.label("OptionPane.label").text());
        dialog.button(JButtonMatcher.withText("OK")).click();
    }
}

