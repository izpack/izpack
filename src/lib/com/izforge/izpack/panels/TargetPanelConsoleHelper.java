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
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.VariableSubstitutor;
/**
 * The Target panel console helper class.
 * 
 * @author Mounir El Hajj
 */
public class TargetPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

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

        String strTargetPath = "";
//        String strDefaultPath = idata.getVariable("SYSTEM_user_dir"); // this is a special
//        // requirement to make the
//        // default path point to the
//        // current location
        String strDefaultPath = idata.getInstallPath();
        // requirement to make the
        // default path point to the
        // current location
        
        String path = TargetPanel.loadDefaultDirFromVariables(idata.getVariables());
        if (path != null) {
            strDefaultPath = path;
        }
        
        System.out.println("Select target path [" + strDefaultPath + "] ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            String strIn = br.readLine();
            if (!strIn.trim().equals(""))
            {
                strTargetPath = strIn;
            }
            else
            {
                strTargetPath = strDefaultPath;
            }
        }
        catch (IOException e)
        {

            e.printStackTrace();
        }

        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        strTargetPath = vs.substitute(strTargetPath, null);

        idata.setInstallPath(strTargetPath);
        
        if (strTargetPath != null && strTargetPath.length() > 0) {
            File selectedDir = new File(strTargetPath);
            if (selectedDir.exists() && selectedDir.isDirectory() && selectedDir.list().length > 0) {
                int answer = askNonEmptyDir();
                if (answer == 2)
                {
                    return false;
                }
                else if (answer == 3)
                {
                    return runConsole(idata);
                }
            }
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
    
    protected int askNonEmptyDir()
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println("The directory already exists and is not empty! Are you sure you want to install here and delete all existing files?\nPress 1 to continue, 2 to quit, 3 to redisplay");
                String strIn = br.readLine();
                if (strIn.equals("1"))
                {
                    return 1;
                }
                else if (strIn.equals("2"))
                {
                    return 2;
                }
                else if (strIn.equals("3")) { return 3; }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 2;
    }
    
}
