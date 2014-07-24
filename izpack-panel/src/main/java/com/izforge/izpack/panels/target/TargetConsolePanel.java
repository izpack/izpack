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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of the {@link TargetPanel}.
 *
 * @author Mounir El Hajj
 */
public class TargetConsolePanel extends AbstractConsolePanel implements ConsolePanel
{

    private final InstallData installData;
    /**
     * Constructs a {@code TargetConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public TargetConsolePanel(PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.installData = installData;
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
        File pathFile;
        String normalizedPath;
        String defaultPath = TargetPanelHelper.getPath(installData);
        PathInputBase.setInstallData(installData);

        if (defaultPath == null)
        {
            defaultPath = "";
        }

        String path = console.promptLocation("Select target path [" + defaultPath + "] ", defaultPath, null);
        if (path != null)
        {
            path = installData.getVariables().replace(path);
            normalizedPath = PathInputBase.normalizePath(path);
            pathFile = new File(normalizedPath);

            if (TargetPanelHelper.isIncompatibleInstallation(normalizedPath))
            {
                console.println(getIncompatibleInstallationMsg(installData));
                return run(installData, console);
            }
            else if (!PathInputBase.isWritable(normalizedPath))
            {
                console.println(installData.getMessages().get("UserPathPanel.notwritable"));
                return run(installData, console);
            }
            else if (!normalizedPath.isEmpty())
            {
                if (pathFile.isFile())
                {
                    console.println(installData.getMessages().get("PathInputPanel.isfile"));
                    return run(installData, console);
                }
                else if (pathFile.isDirectory() && pathFile.list().length > 0)
                {
                    console.println(installData.getMessages().get("TargetPanel.warn"));
                }
                else if(!installData.getPlatform().isValidDirectoryPath(pathFile))
                {
                    console.println(installData.getMessages().get("TargetPanel.syntax.error"));
                    return run(installData, console);
                }
                installData.setInstallPath(normalizedPath);
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

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new TargetPanelAutomation().createInstallationRecord(installData, panelRoot);
    }

}
