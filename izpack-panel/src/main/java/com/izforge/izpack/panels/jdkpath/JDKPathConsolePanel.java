/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.jdkpath;

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.path.PathInputBase;
import com.izforge.izpack.util.Console;

/**
 * The JDKPathPanel panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class JDKPathConsolePanel extends AbstractConsolePanel
{
    private InstallData installData;
    private final VariableSubstitutor variableSubstitutor;
    private final RegistryDefaultHandler handler;
    private final static String JDK_PATH = "jdkPath";
    private final static String JDK_VAR_NAME = "jdkVarName";


    /**
     * Constructs a <tt>JDKPathConsolePanelHelper</tt>.
     *
     * @param variableSubstitutor the variable substituter
     * @param handler             the registry handler
     * @param panel               the parent panel/view. May be {@code null}
     */
    public JDKPathConsolePanel(VariableSubstitutor variableSubstitutor, RegistryDefaultHandler handler,
                               PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.handler = handler;
        this.installData = installData;
        this.variableSubstitutor = variableSubstitutor;
        JDKPathPanelHelper.initialize(installData);
    }

    public boolean run(InstallData installData, Properties properties)
    {
        String strTargetPath = properties.getProperty(InstallData.INSTALL_PATH);
        if (strTargetPath == null || "".equals(strTargetPath.trim()))
        {
            System.err.println("Missing mandatory target path!");
            return false;
        }
        else
        {
            try
            {
                strTargetPath = variableSubstitutor.substitute(strTargetPath);
            }
            catch (Exception e)
            {
                // ignore
            }
            installData.setInstallPath(strTargetPath);
            return true;
        }
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
        String detectedJavaVersion = "";
        String minVersion = installData.getVariable("JDKPathPanel.minVersion");
        String maxVersion = installData.getVariable("JDKPathPanel.maxVersion");

        String variableName = JDK_PATH;
        String defaultValue = JDKPathPanelHelper.getDefaultJavaPath(installData, handler);

        if(JDKPathPanelHelper.skipPanel(installData, defaultValue))
        {
            return true;
        }
        String strPath;
        boolean bKeepAsking = true;
        while (bKeepAsking)
        {
            strPath = console.promptLocation("Select JDK path [" + defaultValue + "] ", null);
            if (strPath == null)
            {
                return false;
            }
            strPath = strPath.trim();
            if (strPath.equals(""))
            {
                strPath = defaultValue;
            }
            strPath = PathInputBase.normalizePath(strPath);
            detectedJavaVersion = JDKPathPanelHelper.getCurrentJavaVersion(strPath, installData.getPlatform());

            if (!JDKPathPanelHelper.pathIsValid(strPath))
            {
                console.println("Path " + strPath + " is not valid.");
            }
            else if (!JDKPathPanelHelper.verifyVersion(detectedJavaVersion))
            {
                String message = "The chosen JDK has the wrong version (available: "
                        + detectedJavaVersion
                        + " required: "
                        + minVersion + " - " + maxVersion + ").";
                message += "\nContinue anyway? [no]";
                String strIn = console.prompt(message, null);
                if (strIn == null)
                {
                    // end of stream
                    return false;
                }
                strIn = strIn.toLowerCase();
                if (strIn != null && (strIn.equals("y") || strIn.equals("yes")))
                {
                    bKeepAsking = false;
                }
            }
            else
            {
                bKeepAsking = false;
            }
            installData.setVariable(variableName, strPath);
        }

        return promptEndPanel(installData, console);
    }

    @Override
    public boolean generateProperties(InstallData installData, PrintWriter printWriter)
    {
        printWriter.println(InstallData.INSTALL_PATH + "=");
        return true;
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new JDKPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }
}
