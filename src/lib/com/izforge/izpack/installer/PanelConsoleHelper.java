/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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
package com.izforge.izpack.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Abstract class implementing basic functions needed by all panel console helpers.
 * 
 * @author Mounir El Hajj
 */
abstract public class PanelConsoleHelper 
{

    /**
     * This method determines whether the chosen dir is writeable or not.
     *
     * @return whether the chosen dir is writeable or not
     */
    public boolean isWriteable(String pstrPath)
    {
        File existParent = IoHelper.existingParent(new File(pstrPath));
        if (existParent == null)
        {
            return false;
        }
        // On windows we cannot use canWrite because
        // it looks to the dos flags which are not valid
        // on NT or 2k XP or ...
        if (OsVersion.IS_WINDOWS)
        {
            File tmpFile;
            try
            {
                tmpFile = File.createTempFile("izWrTe", ".tmp", existParent);
                tmpFile.deleteOnExit();
            }
            catch (IOException e)
            {
                Debug.trace(e.toString());
                return false;
            }
            return true;
        }
        return existParent.canWrite();
    }
    
    /**
     * Notify the user about something.
     * 
     * @param message The notification.
     */
    public void emitNotification(AutomatedInstallData idata, String message)
    {
        System.out.println(message);
    }

    /**
     * Warn the user about something.
     * 
     * @param message The warning message.
     */
    public int emitWarning(AutomatedInstallData idata, String title, String message)
    {
        System.out.println (idata.langpack.getString("installer.warning") + " " + message);
        
        return askQuestion(idata, idata.langpack.getString("consolehelper.askyesno"), 3);
    }

    /**
     * Notify the user of some error.
     * 
     * @param message The error message.
     */
    public void emitError(AutomatedInstallData idata, String title, String message)
    {
        System.out.println (idata.langpack.getString("installer.error") + " " + message);
    }
    
    public int doContinue(AutomatedInstallData idata)
    {
        return askQuestion(idata, idata.langpack.getString("consolehelper.askcontinue"), 3);
    }

    public int askToAccept(AutomatedInstallData idata)
    {
        return askQuestion(idata, idata.langpack.getString("consolehelper.askaccept"), 2);
    }

    public int askEndOfConsolePanel(AutomatedInstallData idata)
    {
        return askQuestion(idata, idata.langpack.getString("consolehelper.askredisplay"), 2);
    }
    
    public int askQuestion (AutomatedInstallData idata, String pstrQuestion, int pDef)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println(pstrQuestion);
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
        
        return pDef;
    }

}
