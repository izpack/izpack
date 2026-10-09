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

import static com.izforge.izpack.installer.util.InstallPathHelper.getPath;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.util.Platforms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link InstallPathHelper} class.
 *
 * @author Tim Anderson
 */
public class InstallPathHelperTest
{
    private String orgUserDir;

    @BeforeEach
    public void initialize() {
        orgUserDir = getProperty("user.dir");
    }

    @AfterEach
    public void cleanup() {
        setProperty("user.dir", orgUserDir);
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
        setProperty("user.dir", "userdir");
        assertThat(getPath(installData)).isEqualTo("userdir");

        // verify that the DEFAULT_INSTALL_PATH overrides SYSTEM_user_dir
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertThat(getPath(installData)).isEqualTo("default");

        // verify that the TargetPanel.dir overrides DEFAULT_INSTALL_PATH
        variables.set("TargetPanel.dir", "override");
        assertThat(getPath(installData)).isEqualTo("override");
    }

    /**
     * Tests the {@link InstallPathHelper#getPath(InstallData)} method for Windows.
     */
    @Test
    public void testGetPathForWindows()
    {
        Variables variables = new DefaultVariables();
        InstallData installData = new AutomatedInstallData(variables, Platforms.WINDOWS_7);

        setProperty("user.dir", "userdir");
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertThat(getPath(installData)).isEqualTo("default");

        // verify TargetPanel.dir.windows overrides DEFAULT_INSTALL_PATH and SYSTEM_user_dir
        variables.set("TargetPanel.dir.windows", "1");
        assertThat(getPath(installData)).isEqualTo("1");

        // verify TargetPanel.dir.windows_7 overrides TargetPanel.dir.windows
        variables.set("TargetPanel.dir.windows_7", "2");
        assertThat(getPath(installData)).isEqualTo("2");
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

        setProperty("user.dir", "userdir");
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertThat(getPath(installData)).isEqualTo("default");

        // verify TargetPanel.dir.unix overrides DEFAULT_INSTALL_PATH and SYSTEM_user_dir
        variables.set("TargetPanel.dir.unix", "1");
        assertThat(getPath(installData)).isEqualTo("1");

        // verify TargetPanel.dir.mac overrides TargetPanel.dir.unix
        variables.set("TargetPanel.dir.mac", "2");
        assertThat(getPath(installData)).isEqualTo("2");

        // verify TargetPanel.dir.mac_osx overrides TargetPanel.dir.mac
        variables.set("TargetPanel.dir.mac_osx", "3");
        assertThat(getPath(installData)).isEqualTo("3");
    }

    /**
     * Tests the {@link InstallPathHelper#getPath(InstallData)} method for Fedora.
     */
    @Test
    public void testGetPathForFedora()
    {
        Variables variables = new DefaultVariables();
        InstallData installData = new AutomatedInstallData(variables, Platforms.FEDORA_LINUX);

        setProperty("user.dir", "userdir");
        variables.set("DEFAULT_INSTALL_PATH", "default");
        assertThat(getPath(installData)).isEqualTo("default");

        // verify TargetPanel.dir.unix overrides DEFAULT_INSTALL_PATH and SYSTEM_user_dir
        variables.set("TargetPanel.dir.unix", "1");
        assertThat(getPath(installData)).isEqualTo("1");

        // verify TargetPanel.dir.linux overrides TargetPanel.dir.unix
        variables.set("TargetPanel.dir.linux", "2");
        assertThat(getPath(installData)).isEqualTo("2");

        // verify TargetPanel.dir.fedora_linux overrides TargetPanel.dir.linux
        variables.set("TargetPanel.dir.fedora_linux", "3");
        assertThat(getPath(installData)).isEqualTo("3");
    }
}
