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

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.panels.target.TargetPanelHelper;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of the {@link DefaultTargetPanel}.
 *
 * @author Tim Anderson
 */
public class DefaultTargetConsolePanel extends AbstractConsolePanel
{

    /**
     * Generates a properties file for each input field or variable.
     *
     * @param installData the installation data
     * @param printWriter the properties file to write to
     * @return {@code true}
     */
    @Override
    public boolean generateProperties(InstallData installData, PrintWriter printWriter)
    {
        printWriter.println(InstallData.INSTALL_PATH + "=");
        return true;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return {@code true} if the installation is successful, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        String path = properties.getProperty(InstallData.INSTALL_PATH);
        path = installData.getVariables().replace(path);
        installData.setInstallPath(path);
        return true;
    }

    /**
     * Runs the panel in an interactive console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        String path = TargetPanelHelper.getPath(installData);
        installData.setInstallPath(path);
        return true;
    }
}
