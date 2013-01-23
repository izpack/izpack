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

    protected String emptyTargetMsg;

    protected String warnMsg;

    public boolean initI18n (AutomatedInstallData installData)
    {
        emptyTargetMsg = getI18nStringForClass("empty_target", "TargetPanel");
        warnMsg = getI18nStringForClass("warn", "TargetPanel");

        String introText = getI18nStringForClass("extendedIntro", "PathInputPanel");
        if (introText == null || introText.endsWith("extendedIntro")
                || introText.indexOf('$') > -1)
        {
            introText = getI18nStringForClass("intro", "PathInputPanel");
            if (introText == null || introText.endsWith("intro"))
            {
                introText = "";
            }
        }

    }


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

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated(String pstrPath, AutomatedInstallData idata)
    {
        String chosenPath = pstrPath;
        boolean ok = true;

        // We put a warning if the specified target is nameless
        if (chosenPath.length() == 0)
        {
            ok = emitWarning(idata.langpack.getString("installer.warning"), emptyTargetMsg);
        }
        if (!ok)
        {
            return ok;
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
        pathSelectionPanel.setPath(chosenPath);
        if (isMustExist())
        {
            if (!path.exists())
            {
                emitError(parent.langpack.getString("installer.error"), parent.langpack
                        .getString(getI18nStringForClass("required", "PathInputPanel")));
                return false;
            }
            if (!pathIsValid())
            {
                emitError(parent.langpack.getString("installer.error"), parent.langpack
                        .getString(getI18nStringForClass("notValid", "PathInputPanel")));
                return false;
            }
        }
        else
        {
            // We assume, that we would install something into this dir
            if (!isWriteable())
            {
                emitError(parent.langpack.getString("installer.error"), getI18nStringForClass(
                        "notwritable", "TargetPanel"));
                return false;
            }
            // We put a warning if the directory exists else we warn
            // that it will be created
            if (path.exists())
            {
                int res = askQuestion(parent.langpack.getString("installer.warning"), warnMsg,
                        AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES);
                ok = res == AbstractUIHandler.ANSWER_YES;
            }
            else
            {
                   //if 'ShowCreateDirectoryMessage' variable set to 'false'
                   // then don't show "directory will be created" dialog:
                final String vStr =
                            idata.getVariable("ShowCreateDirectoryMessage");
                if (vStr == null || Boolean.getBoolean(vStr))
                {
                    ok = this.emitNotificationFeedback(getI18nStringForClass(
                            "createdir", "TargetPanel") + "\n" + chosenPath);
                }
            }
        }
        return ok;
    }



}
