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
package com.izforge.izpack.installer.util;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.util.Platforms;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Tests the {@link InstallPathHelper} class.
 *
 * @author Tim Anderson
 */
public class InstallPathHelperTest
{
	private String orgUserDir;
	
	@Before
	public void initialize() {
		orgUserDir = System.getProperty("user.dir");
	}
	
	@After
	public void cleanup() {
		System.setProperty("user.dir", orgUserDir);
	}
	
    /**
     * Tests the {@link InstallPathHelper#getPath(InstallData)} method.
     */
    @Test
    public void testGetPath()
    {
        Variables variables = new DefaultVariables();
        InstallData installData = new AutomatedInstallData(variables, Platforms.WINDOWS_7);

        // verify that the user dir is returned if no other variable is set
        System.setProperty("user.dir", "userdir");
        assertEquals("userdir", InstallPathHelper.getPath(installData));

        // verify that the DEFAULT_INSTALL_PATH overrides SYSTEM_user_dir
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertEquals("default", InstallPathHelper.getPath(installData));

        // verify that the TargetPanel.dir overrides DEFAULT_INSTALL_PATH
        variables.set("TargetPanel.dir", "override");
        assertEquals("override", InstallPathHelper.getPath(installData));
    }

    /**
     * Tests the {@link InstallPathHelper#getPath(InstallData)} method for Windows.
     */
    @Test
    public void testGetPathForWindows()
    {
        Variables variables = new DefaultVariables();
        InstallData installData = new AutomatedInstallData(variables, Platforms.WINDOWS_7);

        System.setProperty("user.dir", "userdir");
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertEquals("default", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.windows overrides DEFAULT_INSTALL_PATH and SYSTEM_user_dir
        variables.set("TargetPanel.dir.windows", "1");
        assertEquals("1", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.windows_7 overrides TargetPanel.dir.windows
        variables.set("TargetPanel.dir.windows_7", "2");
        assertEquals("2", InstallPathHelper.getPath(installData));
    }

    /**
     * Tests the {@link InstallPathHelper#getPath(InstallData)} method for Mac.
     * <p/>
     * Mac OSX has two parent platforms, Mac and UNIX. This verifies that Mac overrides Unix.
     */
    @Test
    public void testGetPathForMac()
    {
        Variables variables = new DefaultVariables();
        InstallData installData = new AutomatedInstallData(variables, Platforms.MAC_OSX);

        System.setProperty("user.dir", "userdir");
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertEquals("default", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.unix overrides DEFAULT_INSTALL_PATH and SYSTEM_user_dir
        variables.set("TargetPanel.dir.unix", "1");
        assertEquals("1", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.mac overrides TargetPanel.dir.unix
        variables.set("TargetPanel.dir.mac", "2");
        assertEquals("2", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.mac_osx overrides TargetPanel.dir.mac
        variables.set("TargetPanel.dir.mac_osx", "3");
        assertEquals("3", InstallPathHelper.getPath(installData));
    }

    /**
     * Tests the {@link InstallPathHelper#getPath(InstallData)} method for Fedora.
     */
    @Test
    public void testGetPathForFedora()
    {
        Variables variables = new DefaultVariables();
        InstallData installData = new AutomatedInstallData(variables, Platforms.FEDORA_LINUX);

        System.setProperty("user.dir", "userdir");
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertEquals("default", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.unix overrides DEFAULT_INSTALL_PATH and SYSTEM_user_dir
        variables.set("TargetPanel.dir.unix", "1");
        assertEquals("1", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.linux overrides TargetPanel.dir.unix
        variables.set("TargetPanel.dir.linux", "2");
        assertEquals("2", InstallPathHelper.getPath(installData));

        // verify TargetPanel.dir.fedora_linux overrides TargetPanel.dir.linux
        variables.set("TargetPanel.dir.fedora_linux", "3");
        assertEquals("3", InstallPathHelper.getPath(installData));
    }
}
