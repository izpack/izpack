/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.target;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of the {@link TargetPanel}.
 *
 * @author Mounir El Hajj
 */
public class TargetConsolePanel extends AbstractConsolePanel implements ConsolePanel
{

    /**
     * Constructs a <tt>TargetConsolePanelHelper</tt>.
     */
    public TargetConsolePanel()
    {
        super();
    }

    public boolean generateProperties(InstallData installData, PrintWriter printWriter)
    {
        printWriter.println(InstallData.INSTALL_PATH + "=");
        return true;
    }

    public boolean run(InstallData installData, Properties properties)
    {
        boolean result = false;
        String path = properties.getProperty(InstallData.INSTALL_PATH);
        if (path == null || "".equals(path.trim()))
        {
            System.err.println("Missing mandatory target path!");
        }
        else if (TargetPanelHelper.isIncompatibleInstallation(path))
        {
            System.err.println(getIncompatibleInstallationMsg(installData));
        }
        else
        {
            path = installData.getVariables().replace(path);
            installData.setInstallPath(path);
            result = true;
        }
        return result;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        String defaultPath = TargetPanelHelper.getPath(installData);
        if (defaultPath == null)
        {
            defaultPath = "";
        }

        String path = console.prompt("Select target path [" + defaultPath + "] ", null);
        if (path != null)
        {
            path = path.trim();
            if (path.isEmpty())
            {
                path = defaultPath;
            }
            path = installData.getVariables().replace(path);

            if (TargetPanelHelper.isIncompatibleInstallation(path))
            {
                console.println(getIncompatibleInstallationMsg(installData));
                return run(installData, console);
            }
            else if (!path.isEmpty())
            {
                File selectedDir = new File(path);
                if (selectedDir.exists() && selectedDir.isDirectory() && selectedDir.list().length > 0)
                {
                    console.println(installData.getMessages().get("TargetPanel.warn"));
                }
                installData.setInstallPath(path);
                return promptEndPanel(installData, console);
            }
            return run(installData, console);
        }
        else
        {
            return false;
        }
    }

    private String getIncompatibleInstallationMsg(InstallData installData)
    {
        return installData.getMessages().get("TargetPanel.incompatibleInstallation");
    }

}
