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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
/**
 * License Panel console helper
 *
 */
public class LicencePanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{   
    
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata)
    {

        String license = null;
        String resNamePrefix = "LicencePanel.licence";
        try
        {
            // We read it
             license = ResourceManager.getInstance().getTextResource(resNamePrefix);
        }
        catch (Exception err)
        {
            license = "Error : could not load the licence text for defined resource " + resNamePrefix;
            System.out.println(license);
            return false;
        }
        
        // controls # of lines to display at a time, to allow simulated scrolling down
        int lines=25;
        int l = 0;
        
        StringTokenizer st = new StringTokenizer(license, "\n");
        while (st.hasMoreTokens())
        {
             String token = st.nextToken();
             System.out.println(token);
             l++;
             if (l >= lines) {
                 String strIn = doContinue(); 
                 if ( strIn.equalsIgnoreCase("x") ) {
                     return false;
                 } else if ( strIn.equalsIgnoreCase("s") ) {
                     break;
                 }
                 l=0;
             }             
        }
   
        int i = askToAcceptLicense();

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
    
    private String doContinue()
    {
        try
        {
            System.out.println("\r");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println("press Enter to continue, S to skip or X to exit");
                String strIn = br.readLine();
                return strIn;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "X";
    }
    
    private int askToAcceptLicense()
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println("press 1 to accept, 2 to reject, 3 to redisplay");
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
