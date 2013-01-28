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
import java.util.ArrayList;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.VariableSubstitutor;
/**
 * The Target panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class TargetPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    protected String emptyTargetMsg = "";

    protected String warnMsg = "";

    protected String strTargetPath = "";


    public boolean initI18n (AutomatedInstallData installData)
    {
        emptyTargetMsg = installData.langpack.getString("TargetPanel.empty_target");
        warnMsg = installData.langpack.getString("TargetPanel.warn");

        String introText =  installData.langpack.getString( "PathInputPanel.extendedIntro");
        if (introText == null || introText.endsWith("extendedIntro")
                || introText.indexOf('$') > -1)
        {
            introText = installData.langpack.getString("PathInputPanel.intro");
            if (introText == null || introText.endsWith("intro"))
            {
                introText = "";
            }
        }

        return true;

    }


    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        initI18n ( installData);
        printWriter.println(ScriptParser.INSTALL_PATH + "=");
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        initI18n ( installData);
        strTargetPath = p.getProperty(ScriptParser.INSTALL_PATH);
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
        return runConsole( idata, true);
    }

    public boolean runConsole(AutomatedInstallData idata, boolean validateEnd)
    {
        initI18n ( idata);

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


        System.out.println(idata.langpack.getString("TargetPanel.info")+" [" + strDefaultPath + "] ");
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


        if (!isValidated(vs.substitute(strTargetPath, null), idata)) return false;


        if (validateEnd)
        {
            int i = askEndOfConsolePanel(idata);
            if (i == 1)
            {
                idata.setInstallPath(strTargetPath);
                
                String summaryCaption = idata.langpack.getString("TargetPanel.summaryCaption") ;
                ArrayList lstTarget = new ArrayList ();
                lstTarget.add(strTargetPath);
                
                idata.summaryText.put(summaryCaption, lstTarget);
                
                
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

        return true;

    }


    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated(String pstrPath, AutomatedInstallData idata)
    {
        String chosenPath = pstrPath;
        boolean ok = true;
        int nRet = 0;

        // We put a warning if the specified target is nameless
        if (chosenPath.length() == 0)
        {
            nRet = emitWarning(idata, idata.langpack.getString("installer.warning"), emptyTargetMsg);
            if ( nRet==3)
            {
                return false;
            }
            else if ( nRet==2)
            {
                return runConsole(idata, false);
            }
            nRet=0;
        }

        // Expand unix home reference
        if (chosenPath.startsWith("~"))
        {
            String home = System.getProperty("user.home");
            chosenPath = home + chosenPath.substring(1);
        }

        // Normalize the path
        File path = new File(chosenPath).getAbsoluteFile();
        chosenPath = path.toString();

        // We assume, that we would install something into this dir
        if (!isWriteable(chosenPath))
        {
            emitError(idata, idata.langpack.getString("installer.error"), idata.langpack.getString("TargetPanel.notwritable"));
            return runConsole(idata, false);
        }
        // We put a warning if the directory exists else we warn
        // that it will be created
        if (path.exists())
        {
            nRet = emitWarning(idata, idata.langpack.getString("installer.warning"), warnMsg);
            if ( nRet==3)
            {
                return false;
            }
            else if ( nRet==2)
            {
                return runConsole(idata, false);
            }
            nRet=0;
        }
        else
        {
               //if 'ShowCreateDirectoryMessage' variable set to 'false'
               // then don't show "directory will be created" dialog:
            final String vStr =
                        idata.getVariable("ShowCreateDirectoryMessage");
            if (vStr == null || Boolean.getBoolean(vStr))
            {
                nRet = emitWarning(idata, idata.langpack.getString("installer.warning"), idata.langpack.getString("TargetPanel.createdir")+ " " + chosenPath);
                if ( nRet==3)
                {
                    return false;
                }
                else if ( nRet==2)
                {
                    return runConsole(idata, false);
                }
                nRet=0;
            }
        }

        this.strTargetPath = chosenPath;

        return ok;
    }



}
