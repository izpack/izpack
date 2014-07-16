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

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Platform;

/**
 * The Target panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class JDKPathConsolePanel extends AbstractConsolePanel
{
    private String detectedVersion;
    private InstallData installData;
    private final VariableSubstitutor variableSubstitutor;
    private final RegistryDefaultHandler handler;
    private final static String JDK_VAR_NAME = "jdkVarName";
    private final static String JDK_PATH = "jdkPath";

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
        this.installData = installData;
        this.variableSubstitutor = variableSubstitutor;
        this.handler = handler;
    }

    public boolean generateProperties(InstallData installData, PrintWriter printWriter)
    {
        printWriter.println(InstallData.INSTALL_PATH + "=");
        return true;
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
        String minVersion = installData.getVariable("JDKPathPanel.minVersion");
        String maxVersion = installData.getVariable("JDKPathPanel.maxVersion");
        String variableName = JDK_PATH;
        installData.setVariable(JDK_VAR_NAME, variableName);

        String strPath;
        String strDefaultPath = installData.getVariable(variableName);
        if (strDefaultPath == null)
        {
            if (OsVersion.IS_OSX)
            {
                strDefaultPath = JDKPathPanel.OSX_JDK_HOME;
            }
            else
            {
                // Try the JAVA_HOME as child dir of the jdk path
                strDefaultPath = (new File(installData.getVariable("JAVA_HOME"))).getParent();
            }
        }

        Platform platform = installData.getPlatform();
        if (!pathIsValid(strDefaultPath) || !verifyVersion(minVersion, maxVersion, strDefaultPath, platform))
        {
            strDefaultPath = resolveInRegistry(minVersion, maxVersion);
            if (!pathIsValid(strDefaultPath) || !verifyVersion(minVersion, maxVersion, strDefaultPath, platform))
            {
                strDefaultPath = "";
            }
        }

        boolean bKeepAsking = true;

        while (bKeepAsking)
        {
            strPath = console.prompt("Select JDK path [" + strDefaultPath + "] ", null);
            if (strPath == null)
            {
                // end of stream
                return false;
            }
            strPath = strPath.trim();
            if (strPath.equals(""))
            {
                strPath = strDefaultPath;
            }
            if (!pathIsValid(strPath))
            {
                console.println("Path " + strPath + " is not valid.");
            }
            else if (!verifyVersion(minVersion, maxVersion, strPath, installData.getPlatform()))
            {
                String message = "The chosen JDK has the wrong version (available: " + detectedVersion + " required: "
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

    /**
     * Returns whether the chosen path is true or not. If existFiles are not null, the existence of
     * it under the chosen path are detected. This method can be also implemented in derived
     * classes to handle special verification of the path.
     *
     * @return true if existFiles are exist or not defined, else false
     */
    private static boolean pathIsValid(String strPath)
    {
        for (String existFile : JDKPathPanel.testFiles)
        {
            File path = new File(strPath, existFile).getAbsoluteFile();
            if (!path.exists())
            {
                return false;
            }
        }
        return true;
    }

    private boolean verifyVersion(String min, String max, String path, Platform platform)
    {
        boolean retval = true;
        // No min and max, version always ok.
        if (min == null && max == null)
        {
            return (true);
        }
        // Now get the version ...
        // We cannot look to the version of this vm because we should
        // test the given JDK VM.
        String[] params;
        if (platform.isA(Platform.Name.WINDOWS))
        {
            params = new String[]{
                    "cmd",
                    "/c",
                    path + File.separator + "bin" + File.separator + "java",
                    "-version"
            };
        }
        else
        {
            params = new String[]{
                    path + File.separator + "bin" + File.separator + "java",
                    "-version"
            };
        }
        String[] output = new String[2];
        FileExecutor fe = new FileExecutor();
        fe.executeCommand(params, output);
        // "My" VM writes the version on stderr :-(
        String vs = (output[0].length() > 0) ? output[0] : output[1];
        if (min != null)
        {
            if (!compareVersions(vs, min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
            {
                retval = false;
            }
        }
        if (max != null)
        {
            if (!compareVersions(vs, max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
            {
                retval = false;
            }
        }
        return retval;
    }

    private boolean compareVersions(String in, String template, boolean isMin,
                                    int assumedPlace, int halfRange, String useNotIdentifier)
    {
        StringTokenizer tokenizer = new StringTokenizer(in, " \t\n\r\f\"");
        int i;
        int currentRange = 0;
        String[] interestedEntries = new String[halfRange + halfRange];
        for (i = 0; i < assumedPlace - halfRange; ++i)
        {
            if (tokenizer.hasMoreTokens())
            {
                tokenizer.nextToken(); // Forget this entries.
            }
        }

        for (i = 0; i < halfRange + halfRange; ++i)
        { // Put the interesting Strings into an intermediaer array.
            if (tokenizer.hasMoreTokens())
            {
                interestedEntries[i] = tokenizer.nextToken();
                currentRange++;
            }
        }

        for (i = 0; i < currentRange; ++i)
        {
            if (useNotIdentifier != null && interestedEntries[i].contains(useNotIdentifier))
            {
                continue;
            }
            if (Character.getType(interestedEntries[i].charAt(0)) != Character.DECIMAL_DIGIT_NUMBER)
            {
                continue;
            }
            break;
        }
        if (i == currentRange)
        {
            detectedVersion = "<not found>";
            return (false);
        }
        detectedVersion = interestedEntries[i];
        StringTokenizer currentTokenizer = new StringTokenizer(interestedEntries[i], "._-");
        StringTokenizer neededTokenizer = new StringTokenizer(template, "._-");
        while (neededTokenizer.hasMoreTokens())
        {
            // Current can have no more tokens if needed has more
            // and if a privious token was not accepted as good version.
            // e.g. 1.4.2_02 needed, 1.4.2 current. The false return
            // will be right here. Only if e.g. needed is 1.4.2_00 the
            // return value will be false, but zero should not b e used
            // at the last version part.
            if (!currentTokenizer.hasMoreTokens())
            {
                return (false);
            }
            String current = currentTokenizer.nextToken();
            String needed = neededTokenizer.nextToken();
            int currentValue;
            int neededValue;
            try
            {
                currentValue = Integer.parseInt(current);
                neededValue = Integer.parseInt(needed);
            }
            catch (NumberFormatException nfe)
            { // A number format exception will be raised if
                // there is a non numeric part in the version,
                // e.g. 1.5.0_beta. The verification runs only into
                // this deep area of version number (fourth sub place)
                // if all other are equal to the given limit. Then
                // it is right to return false because e.g.
                // the minimal needed version will be 1.5.0.2.
                return (false);
            }
            if (currentValue < neededValue)
            {
                if (isMin)
                {
                    return (false);
                }
                return (true);
            }
            if (currentValue > neededValue)
            {
                if (isMin)
                {
                    return (true);
                }
                return (false);
            }
        }
        return (true);
    }

    /**
     * Returns the path to the needed JDK if found in the registry. If there are more than one JDKs
     * registered, that one with the highest allowd version will be returned. Works only on windows.
     * On Unix an empty string returns.
     *
     * @return the path to the needed JDK if found in the windows registry
     */

    private String resolveInRegistry(String min, String max)
    {
        String retval = "";
        int oldVal = 0;
        RegistryHandler registryHandler = null;
        Set<String> badRegEntries = new HashSet<String>();
        try
        {
            // Get the default registry handler.
            registryHandler = handler.getInstance();
            if (registryHandler == null)
            // We are on a os which has no registry or the
            // needed dll was not bound to this installation. In
            // both cases we forget the try to get the JDK path from registry.
            {
                return (retval);
            }
            oldVal = registryHandler.getRoot(); // Only for security...
            registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
            String[] keys = registryHandler.getSubkeys(JDKPathPanel.JDK_ROOT_KEY);
            if (keys == null || keys.length == 0)
            {
                return (retval);
            }
            Arrays.sort(keys);
            int i = keys.length - 1;
            // We search for the highest allowed version, therefore retrograde
            while (i > 0)
            {
                if (max == null || compareVersions(keys[i], max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
                { // First allowed version found, now we have to test that the min value
                    // also allows this version.
                    if (min == null || compareVersions(keys[i], min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
                    {
                        String cv = JDKPathPanel.JDK_ROOT_KEY + "\\" + keys[i];
                        String path = registryHandler.getValue(cv, JDKPathPanel.JDK_VALUE_NAME).getStringData();
                        // Use it only if the path is valid.
                        // Set the path for method pathIsValid ...
                        if (!pathIsValid(path))
                        {
                            badRegEntries.add(keys[i]);
                        }
                        else if ("".equals(retval))
                        {
                            retval = path;
                        }
                    }
                }
                i--;
            }
        }
        catch (Exception e)
        { // Will only be happen if registry handler is good, but an
            // exception at performing was thrown. This is an error...
            e.printStackTrace();
        }
        finally
        {
            if (registryHandler != null && oldVal != 0)
            {
                try
                {
                    registryHandler.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
                }
                catch (NativeLibException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return (retval);
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot) {
        new JDKPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }
}
