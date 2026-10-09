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

package com.izforge.izpack.integration.packvalidator;

import static com.izforge.izpack.integration.HelperTestMethod.prepareFrameFixture;
import static com.izforge.izpack.integration.packvalidator.TestPackValidator.setValid;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.compiler.container.TestGUIInstallationContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerController;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.panel.Panels;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.panels.install.InstallPanel;
import com.izforge.izpack.panels.packs.PacksPanel;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.util.TestHousekeeper;
import java.io.File;
import java.nio.file.Path;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that {@link PackValidator}s are invoked during installation.
 *
 * @author Tim Anderson
 */
@Container(TestGUIInstallationContainer.class)
public class PackValidatorTest
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
     * The house-keeper.
     */
    private final TestHousekeeper housekeeper;

    /**
     * The panels.
     */
    private final Panels panels;

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
    public PackValidatorTest(GUIInstallData installData, InstallerFrame frame, InstallerController controller,
                             TestHousekeeper housekeeper, Panels panels)
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
     * Verifies that registered {@link PackValidator}s are invoked by {@link PacksPanel}.
     *
     * @throws Exception for any error
     */
    @Test
    @InstallFile("samples/packvalidators.xml")
    public void testPackValidator() throws Exception
    {
        assertThat(panels.getPanels().size()).isEqualTo(4);

        frameFixture = prepareFrameFixture(frame, controller);

        // HelloPanel
        sleep(2000);
        checkCurrentPanel(HelloPanel.class);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // PacksPanel
        sleep(1000);
        checkCurrentPanel(PacksPanel.class);

        //set the Base pack as invalid, and verify clicking next has no effect
        setValid("Base", false, installData);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();
        sleep(1000);
        checkCurrentPanel(PacksPanel.class);

        // now set it valid. Should be able to go to next panel
        setValid("Base", true, installData);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // InstallPanel
        sleep(1000);
        checkCurrentPanel(InstallPanel.class);
        frameFixture.button(GuiId.BUTTON_NEXT.id).click();

        // SimpleFinishPanel
        sleep(1000);
        checkCurrentPanel(SimpleFinishPanel.class);
        frameFixture.button(GuiId.BUTTON_QUIT.id).click();

        // verify the installer has terminated
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

}

