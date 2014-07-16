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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.test.TestConsolePanelContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.test.util.TestConsole;

/**
 * Tests the {@link DefaultTargetConsolePanel} class.
 *
 * @author Tim Anderson
 */
@RunWith(PicoRunner.class)
@Container(TestConsolePanelContainer.class)
public class DefaultTargetConsolePanelTest
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The console.
     */
    private final TestConsole console;

    /**
     * Constructs a {@code DefaultTargetConsolePanelTest}.
     *
     * @param installData the installation data
     * @param console     the console
     */
    public DefaultTargetConsolePanelTest(InstallData installData, TestConsole console)
    {
        this.console = console;
        this.installData = installData;
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
        checkInstallPath(expectedPath);
    }

    /**
     * Verifies that if the <em>TargetPanel.dir</em> property is set, it will be used.
     *
     * @throws Exception for any error
     */
    @Test
    public void testTargetPanelDirVariable() throws Exception
    {
        String expectedPath = "/foo/bar";
        installData.setVariable(TARGET_PANEL_DIR, expectedPath);
        checkInstallPath(expectedPath);
    }

    /**
     * Verifies that if no <em>TargetPanel.dir</em> property is set, the default install path will be used.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDefaultInstallPath() throws Exception
    {
        String expectedPath = "/x/y";
        installData.setDefaultInstallPath(expectedPath);
        checkInstallPath(expectedPath);
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
        assertTrue(installData.getPlatform().isA(MAC_OSX)); // hardcoded for test purposes

        installData.setDefaultInstallPath("/default/install/path");
        installData.setVariable(TARGET_PANEL_DIR, "/default/target/panel/dir");
        String expectedPath = "/mac_osx";
        installData.setVariable(TARGET_PANEL_DIR + ".mac_osx", expectedPath);
        checkInstallPath(expectedPath);
    }

    /**
     * Verifies the install path matches that expected after the panel is run.
     *
     * @param expectedPath the expected install path
     */
    private void checkInstallPath(String expectedPath)
    {
        assertNull(installData.getInstallPath());
        DefaultTargetConsolePanel panel = new DefaultTargetConsolePanel(null,installData);
        assertTrue(panel.run(installData, console));
        assertEquals(expectedPath, installData.getInstallPath());
    }
}
