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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.hamcrest.text.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

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
    private LookAndFeel lookAndFeel;


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
    @Before
    public void setUp()
    {
        lookAndFeel = UIManager.getLookAndFeel();
    }

    /**
     * Cleans up after the test case.
     *
     * @throws Exception for any error
     */
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        UIManager.setLookAndFeel(lookAndFeel);
    }

    /**
     * Tests a job with <em>executeclass</em> elements.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExecuteClass() throws Exception
    {
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/process/");
        Executable.init();
        Executable.setReturn(true);

        // show the panel
        FrameFixture fixture = show(ProcessPanel.class, SimpleFinishPanel.class);
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof ProcessPanel);

        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);

        // verify Executable was run the expected no. of times, with the expected arguments
        assertEquals(2, Executable.getInvocations());
        assertArrayEquals(Executable.getArgs(0), new String[]{"run0"});
        assertArrayEquals(Executable.getArgs(1), new String[]{"run1", "somearg"});
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
            UIManager.setLookAndFeel(lookAndFeel);
        }

        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/process/");
        Executable.init();
        Executable.setException(true);

        // show the panel
        FrameFixture fixture = show(ProcessPanel.class, SimpleFinishPanel.class);
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof ProcessPanel);

        // attempt to navigate to the next panel
        DialogFixture dialogFixture = fixture.dialog();
        dialogFixture.requireVisible();
        assertThat(dialogFixture.label("OptionPane.label").text(),
                   StringContains.containsString("Executable exception"));
        dialogFixture.button().click();

        Thread.sleep(2000);
        fixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();

        assertTrue(getPanels().getView() instanceof ProcessPanel);

        // verify Executable was run the expected no. of times, with the expected arguments
        assertEquals(1, Executable.getInvocations());
        assertArrayEquals(Executable.getArgs(0), new String[]{"run0"});
    }

}
