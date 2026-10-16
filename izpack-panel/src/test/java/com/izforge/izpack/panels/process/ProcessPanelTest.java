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
package com.izforge.izpack.panels.process;

import static com.izforge.izpack.panels.process.Executable.getArgs;
import static com.izforge.izpack.panels.process.Executable.getInvocations;
import static com.izforge.izpack.panels.process.Executable.init;
import static com.izforge.izpack.panels.process.Executable.setException;
import static com.izforge.izpack.panels.process.Executable.setReturn;
import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.UIManager.getLookAndFeel;
import static javax.swing.UIManager.setLookAndFeel;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;
import com.izforge.izpack.test.Container;
import javax.swing.LookAndFeel;
import javax.swing.UnsupportedLookAndFeelException;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

/**
 * Tests the {@link ProcessPanel}.
 * TODO - this only covers a fraction of ProcessPanel functionality.
 *
 * @author Tim Anderson
 */
@Container(TestGUIPanelContainer.class)
public class ProcessPanelTest extends AbstractPanelTest
{

    /**
     * Saves the look & feel.
     */
    private LookAndFeel savedLookAndFeel;


    /**
     * Constructs a {@code ProcessPanelTest}.
     *
     * @param container           the test container
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param factory             the panel factory
     * @param rules               the rules
     * @param icons               the icons
     * @param uninstallDataWriter the uninstallation data writer
     * @param locales             the locales
     */
    public ProcessPanelTest(TestGUIPanelContainer container, GUIInstallData installData,
                            ResourceManager resourceManager, ObjectFactory factory, RulesEngine rules,
                            IconsDatabase icons, UninstallDataWriter uninstallDataWriter, Locales locales)
    {
        super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter, locales);
    }

    /**
     * Sets up the test.
     */
    @BeforeEach
    public void setUp()
    {
        savedLookAndFeel = getLookAndFeel();
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/process/");
    }

    /**
     * Cleans up after the test case.
     *
     * @throws Exception for any error
     */
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        invokeAndWait(()-> {
            try {
                setLookAndFeel(savedLookAndFeel);
            } catch (UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Tests a job with <em>executeclass</em> elements.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExecuteClass() throws Exception
    {
        init();
        setReturn(true);

        // show the panel
        FrameFixture fixture = showProcessPanel();

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        waitForPanel(SimpleFinishPanel.class);

        // verify Executable was run the expected no. of times, with the expected arguments
        assertThat(getInvocations()).isEqualTo(2);
        assertThat(new String[]{"run0"}).isEqualTo(getArgs(0));
        assertThat(new String[]{"run1", "somearg"}).isEqualTo(getArgs(1));
    }

    /**
     * Verifies that a dialog is displayed if the specified <em>executeclass</em> throws an exception.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExecuteClassException() throws Exception
    {
        SubstanceBusinessLookAndFeel lookAndFeel = new SubstanceBusinessLookAndFeel();
        if (lookAndFeel.isSupportedLookAndFeel())
        {
            // Substances checks that UI elements are created within the event dispatcher thread.
            invokeAndWait(() -> {
                try {
                    setLookAndFeel(lookAndFeel);
                } catch (UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        init();
        setException(true);

        // show the panel
        FrameFixture fixture = showProcessPanel();

        // attempt to navigate to the next panel
        DialogFixture dialogFixture = fixture.dialog();
        dialogFixture.requireVisible();
        assertThat(dialogFixture.label("OptionPane.label").text()).contains("Executable exception");
        dialogFixture.button().click();

        fixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();

        assertThat(getPanels().getView() instanceof ProcessPanel).isTrue();

        // verify Executable was run the expected no. of times, with the expected arguments
        assertThat(getInvocations()).isEqualTo(1);
        assertThat(new String[]{"run0"}).isEqualTo(getArgs(0));
    }

    /**
     * Creates and waits for a process panel.
     *
     * @return The frame fixture for the process panel.
     */
    private FrameFixture showProcessPanel()
    {
        FrameFixture fixture = show(ProcessPanel.class, SimpleFinishPanel.class);
        waitForPanel(ProcessPanel.class);
        assertThat(getPanels().getView()).isInstanceOf(ProcessPanel.class);
        return fixture;
    }
}
