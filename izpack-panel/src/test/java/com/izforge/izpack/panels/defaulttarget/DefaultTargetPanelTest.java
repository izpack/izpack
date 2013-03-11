/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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
package com.izforge.izpack.panels.defaulttarget;

import static com.izforge.izpack.panels.target.TargetPanelHelper.TARGET_PANEL_DIR;
import static com.izforge.izpack.util.Platform.Name.MAC_OSX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
 * Tests the {@link DefaultTargetPanel} class.
 *
 * @author Tim Anderson
 */
@Container(TestGUIPanelContainer.class)
public class DefaultTargetPanelTest extends AbstractPanelTest
{

    /**
     * Constructs a {@code DefaultTargetPanelTest}.
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
    public DefaultTargetPanelTest(TestGUIPanelContainer container, GUIInstallData installData,
                                  ResourceManager resourceManager,
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
        String expectedPath = System.getProperty("user.dir");
        checkPath(expectedPath);
    }

    /**
     * Verifies that if the <em>TargetPanel.dir</em> property is set, it will be used.
     *
     * @throws Exception for any error
     */
    @Test
    public void testTargetPanelDirVariable() throws Exception
    {
        GUIInstallData installData = getInstallData();
        String expectedPath = "/foo/bar";
        installData.setVariable(TARGET_PANEL_DIR, expectedPath);
        checkPath(expectedPath);
    }

    /**
     * Verifies that if no <em>TargetPanel.dir</em> property is set, the default install path will be used.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDefaultInstallPath() throws Exception
    {
        GUIInstallData installData = getInstallData();
        String expectedPath = "/x/y";
        installData.setDefaultInstallPath(expectedPath);
        checkPath(expectedPath);
    }

    /**
     * Verifies that when the <em>TargetPanel.dir.&lt;platform&gt;</em> variable is set for the current platform,
     * it will be used in preference to any other.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPlatformSpecificInstallPath() throws Exception
    {
        GUIInstallData installData = getInstallData();
        assertTrue(installData.getPlatform().isA(MAC_OSX)); // hardcoded for test purposes

        installData.setDefaultInstallPath("/default/install/path");
        installData.setVariable(TARGET_PANEL_DIR, "/default/target/panel/dir");
        String expectedPath = "/mac_osx";
        installData.setVariable(TARGET_PANEL_DIR + ".mac_osx", expectedPath);
        checkPath(expectedPath);
    }

    /**
     * Verfiies that the installation path matches that expected, after the panel has been showh.
     *
     * @param expectedPath the expected installation path
     * @throws Exception for any error
     */
    private void checkPath(String expectedPath) throws Exception
    {
        GUIInstallData installData = getInstallData();
        show(DefaultTargetPanel.class, SimpleFinishPanel.class);
        Thread.sleep(1000);
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
        assertEquals(expectedPath, installData.getInstallPath());
    }

}
