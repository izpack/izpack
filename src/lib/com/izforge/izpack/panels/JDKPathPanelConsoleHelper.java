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
package com.izforge.izpack.panels;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.coi.tools.os.win.MSWinConstants;
import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
/**
 * The Target panel console helper class.
 * 
 * @author Mounir El Hajj
 */
public class JDKPathPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    private String minVersion;
    private String maxVersion;
    private String variableName;
    private String detectedVersion;
    
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        printWriter.println(ScriptParser.INSTALL_PATH + "=");
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        String strTargetPath = p.getProperty(ScriptParser.INSTALL_PATH);
        if (strTargetPath == null || "".equals(strTargetPath.trim()))
        {
            System.err.println("Inputting the target path is mandatory!!!!");
            return false;
        }
        else
        {
            VariableSubstitutor vs = new VariableSubstitutor(installData.getVariables());
            strTargetPath = vs.substitute(strTargetPath, null);
            installData.setInstallPath(strTargetPath);
            return true;
        }
    }

    public boolean runConsole(AutomatedInstallData idata)
    {
        minVersion = idata.getVariable("JDKPathPanel.minVersion");
        maxVersion = idata.getVariable("JDKPathPanel.maxVersion");
        variableName = "JDKPath";

        String strPath = "";
        String strDefaultPath = idata.getVariable(variableName);
        if ( strDefaultPath == null ) {
            if (OsVersion.IS_OSX)
            {
                strDefaultPath = JDKPathPanel.OSX_JDK_HOME;
            }
            else
            {
                // Try the JAVA_HOME as child dir of the jdk path
                strDefaultPath = (new File(idata.getVariable("JAVA_HOME"))).getParent();
            }
        }

        if (!pathIsValid(strDefaultPath) || !verifyVersion(minVersion, maxVersion, strDefaultPath))
        {
            strDefaultPath = resolveInRegistry(minVersion, maxVersion);
            if (!pathIsValid(strDefaultPath) || !verifyVersion(minVersion, maxVersion, strDefaultPath))
            {
                strDefaultPath = "";
            }
        }       
        
        boolean bKeepAsking = true;

        while (bKeepAsking)
        {
            System.out.println("Select JDK path [" + strDefaultPath + "] ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try
            {
                String strIn = br.readLine();
                if (!strIn.trim().equals(""))
                {
                    strPath = strIn.trim();
                }
                else
                {
                    strPath = strDefaultPath;
                }
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
            if ( !pathIsValid(strPath) ) {
                System.out.println("Path "+strPath+" is not valid.");
            } else if ( !verifyVersion(minVersion, maxVersion, strPath) ) {
                System.out.println("The chosen JDK has the wrong version (available: "+detectedVersion+" required: "+minVersion+" - "+maxVersion+").");
                System.out.println("Continue anyway? [no]");
                br = new BufferedReader(new InputStreamReader(System.in));
                try
                {
                    String strIn = br.readLine();
                    if ( strIn.trim().toLowerCase().equals("y") || strIn.trim().toLowerCase().equals("yes") )
                    {
                        bKeepAsking = false;
                    }
                }
                catch (IOException e)
                {

                    e.printStackTrace();
                }
            } else {
                bKeepAsking = false;
            }
            idata.setVariable(variableName, strPath);
        }
        
        int i = askEndOfConsolePanel(idata);
        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(idata);
        }

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
 
    private boolean verifyVersion(String min, String max, String path)
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
        if ( System.getProperty("os.name").indexOf("Windows") >= 0 ) {
            String[] paramsp = {
                "cmd",
                "/c",
                path + File.separator + "bin" + File.separator + "java",
                "-version"
            };
            params=paramsp;
        } else {
            String[] paramsp = {
                path + File.separator + "bin" + File.separator + "java",
                "-version"
            };
            params=paramsp;
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
        StringTokenizer st = new StringTokenizer(in, " \t\n\r\f\"");
        int i;
        int currentRange = 0;
        String[] interestedEntries = new String[halfRange + halfRange];
        for (i = 0; i < assumedPlace - halfRange; ++i)
        {
            if (st.hasMoreTokens())
            {
                st.nextToken(); // Forget this entries.
            }
        }

        for (i = 0; i < halfRange + halfRange; ++i)
        { // Put the interesting Strings into an intermediaer array.
            if (st.hasMoreTokens())
            {
                interestedEntries[i] = st.nextToken();
                currentRange++;
            }
        }

        for (i = 0; i < currentRange; ++i)
        {
            if (useNotIdentifier != null && interestedEntries[i].indexOf(useNotIdentifier) > -1)
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
        StringTokenizer current = new StringTokenizer(interestedEntries[i], "._-");
        StringTokenizer needed = new StringTokenizer(template, "._-");
        while (needed.hasMoreTokens())
        {
            // Current can have no more tokens if needed has more
            // and if a privious token was not accepted as good version.
            // e.g. 1.4.2_02 needed, 1.4.2 current. The false return
            // will be right here. Only if e.g. needed is 1.4.2_00 the
            // return value will be false, but zero should not b e used
            // at the last version part.
            if (!current.hasMoreTokens())
            {
                return (false);
            }
            String cur = current.nextToken();
            String nee = needed.nextToken();
            int curVal = 0;
            int neededVal = 0;
            try
            {
                curVal = Integer.parseInt(cur);
                neededVal = Integer.parseInt(nee);
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
            if (curVal < neededVal)
            {
                if (isMin)
                {
                    return (false);
                }
                return (true);
            }
            if (curVal > neededVal)
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
        RegistryHandler rh = null;
        Set<String> badRegEntries = new HashSet<String>();
        try
        {
            // Get the default registry handler.
            rh = RegistryDefaultHandler.getInstance();
            if (rh == null)
            // We are on a os which has no registry or the
            // needed dll was not bound to this installation. In
            // both cases we forget the try to get the JDK path from registry.
            {
                return (retval);
            }
            oldVal = rh.getRoot(); // Only for security...
            rh.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
            String[] keys = rh.getSubkeys(JDKPathPanel.JDK_ROOT_KEY);
            if (keys == null || keys.length == 0)
            {
                return (retval);
            }
            Arrays.sort(keys);
            int i = keys.length - 1;
            // We search for the highest allowd version, therefore retrograde
            while (i > 0)
            {
                if ( max == null || compareVersions(keys[i], max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
                { // First allowed version found, now we have to test that the min value
                    // also allows this version.
                    if ( min == null || compareVersions(keys[i], min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
                    {
                        String cv = JDKPathPanel.JDK_ROOT_KEY + "\\" + keys[i];
                        String path = rh.getValue(cv, JDKPathPanel.JDK_VALUE_NAME).getStringData();
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
            if (rh != null && oldVal != 0)
            {
                try
                {
                    rh.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
                }
                catch (NativeLibException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return (retval);
    }
}
