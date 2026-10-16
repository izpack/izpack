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

package com.izforge.izpack.integration.datavalidator;

import static com.izforge.izpack.integration.HelperTestMethod.prepareFrameFixture;
import static com.izforge.izpack.integration.datavalidator.TestDataValidator.getValidate;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.swing.core.matcher.JButtonMatcher.withText;
import static org.fest.swing.timing.Timeout.timeout;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.install.InstallPanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.util.TestHousekeeper;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that {@link DataValidator}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@Container(TestGUIInstallationContainer.class)
public class DataValidatorTest
{
    /**
     * Temporary folder to perform installations to.
     */
    @TempDir
    public Path temporaryFolder;

    /**
     * Install data.
     */
    private final GUIInstallData installData;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The installer controller.
     */
    private final InstallerController controller;

    /**
     * The panels.
     */
    private final IzPanels panels;

    /**
     * The house-keeper.
     */
    private final TestHousekeeper housekeeper;

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
     * @param panels      the panels
     * @param housekeeper the house-keeper
     */
    public DataValidatorTest(GUIInstallData installData, InstallerFrame frame, InstallerController controller,
                             IzPanels panels, TestHousekeeper housekeeper)
    {
        this.installData = installData;
        this.frame = frame;
        this.controller = controller;
        this.panels = panels;
        this.housekeeper = housekeeper;
    }

    /**
     * Sets up the test case.
     */
    @BeforeEach
    public void setUp()
    {
        // write to temporary folder so the test doesn't need to be run with elevated permissions
        File installPath = temporaryFolder.resolve("izpackTest").toFile();
        assertThat(installPath.mkdirs()).isTrue();
        installData.setInstallPath(installPath.getAbsolutePath());
    }

    /**
     * Tears down the test case.
     */
    @AfterEach
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
    @InstallFile("samples/datavalidators.xml")
    public void testDataValidators() throws Exception
    {
        List<Panel> list = panels.getPanels();
        assertThat(list).hasSize(3);
        Panel hello = list.get(0);
        Panel install = list.get(1);
        Panel finish = list.get(2);

        // verify that all class names are fully qualified
        assertThat(hello.getClassName()).isEqualTo(HelloPanel.class.getName());
        assertThat(hello.getValidators().iterator().next()).isEqualTo(TestDataValidator.class.getName());

        assertThat(install.getClassName()).isEqualTo(InstallPanel.class.getName());
        assertThat(install.getValidators().iterator().next()).isEqualTo(TestDataValidator.class.getName());

        assertThat(finish.getClassName()).isEqualTo(SimpleFinishPanel.class.getName());
        assertThat(finish.getValidators().iterator().next()).isEqualTo(TestDataValidator.class.getName());

        frameFixture = prepareFrameFixture(frame, controller);

        // HelloPanel
        sleep(2000);
        checkCurrentPanel(HelloPanel.class);
        installData.setVariable("HelloPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("HelloPanel.error");
        checkCurrentPanel(HelloPanel.class);

        installData.setVariable("HelloPanel.status", "WARNING");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("HelloPanel.warning");
        assertThat(getValidate("HelloPanel", installData)).isEqualTo(2);

        // InstallPanel
        sleep(1000);
        checkCurrentPanel(InstallPanel.class);
        installData.setVariable("InstallPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        checkDialog("InstallPanel.error");

        installData.setVariable("InstallPanel.status", "OK");
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        assertThat(getValidate("InstallPanel", installData)).isEqualTo(2);

        // SimpleFinishPanel
        sleep(1000);
        checkCurrentPanel(SimpleFinishPanel.class);

        // Validators not invoked on last panel, so this should be a no-op
        installData.setVariable("SimpleFinishPanel.status", "ERROR");
        frameFixture.button(GuiId.BUTTON_QUIT.id).click();
        assertThat(getValidate("SimpleFinishPanel", installData)).isEqualTo(0);

        // verify the installer has terminated, and an uninstaller has been written
        housekeeper.waitShutdown(2 * 60 * 1000);
        assertThat(housekeeper.hasShutdown()).isTrue();
        assertThat(housekeeper.getExitCode()).isEqualTo(0);
        assertThat(housekeeper.getReboot()).isFalse();
    }

    /**
     * Verifies that the current panel is an instance of the specified type.
     *
     * @param type the expected panel type
     */
    private void checkCurrentPanel(Class<? extends IzPanel> type)
    {
        Panel panel = panels.getPanel();
        assertThat(panel.getClassName()).isEqualTo(type.getName());
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
        DialogFixture dialog = frameFixture.dialog(timeout(10000));
        assertThat(dialog.label("OptionPane.label").text()).isEqualTo(text);
        dialog.button(withText("OK")).click();
    }
}
