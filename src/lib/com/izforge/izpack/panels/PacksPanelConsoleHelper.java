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

package com.izforge.izpack.panels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.Debug;

/**
 * Console implementation for the PacksPanel.
 * 
 * @author Sergiy Shyrkov
 */
public class PacksPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    private static boolean askSelected(boolean defaultValue)
    {
        boolean selected = defaultValue;
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean bKeepAsking = true;

            while (bKeepAsking)
            {
                out("input 1 to select, 0 to deselect:");
                String strIn = br.readLine();
                // take default value if default value exists and no user input
                if (strIn.trim().equals(""))
                {
                    bKeepAsking = false;
                }
                int j = -1;
                try
                {
                    j = Integer.valueOf(strIn).intValue();
                }
                catch (Exception ex)
                {}
                // take user input if user input is valid
                if ((j == 0) || j == 1)
                {
                    selected = j == 1;
                    bKeepAsking = false;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return selected;
    }

    private static void out(String message)
    {
        System.out.println(message);
    }

    private String getI18n(LocaleDatabase langpack, String key, String defaultValue)
    {
        String text = langpack.getString(key);
        return text != null && !text.equals(key) ? text : defaultValue;
    }

    public boolean runConsole(AutomatedInstallData installData)
    {
        // load I18N
        LocaleDatabase langpack = installData.langpack;
        try
        {
            InputStream inputStream = ResourceManager.getInstance().getInputStream("packsLang.xml");
            langpack.add(inputStream);
        }
        catch (Exception e)
        {
            Debug.trace(e);
        }
        
        // initialize selection
        List<Pack> selectedPacks = new LinkedList<Pack>();

        out("");
        out(langpack.getString("PacksPanel.info"));
        out("");
        for (Pack pack : installData.availablePacks)
        {
            StringBuilder option = new StringBuilder(64);
            option.append("[")
                    .append(pack.required ? "<required>" : (pack.preselected ? "x" : " "))
                    .append("] ").append(getI18n(langpack, pack.id, pack.name));
            String descr = getI18n(langpack, pack.id + ".description", pack.description);
            if (descr != null && descr.length() > 0)
            {
                option.append(" (" + descr + ")");
            }
            out(option.toString());
            if (pack.required || askSelected(pack.preselected))
            {
                selectedPacks.add(pack);
            }
        }
        out("");
        out("...pack selection done.");

        installData.selectedPacks = selectedPacks;

        int i = askEndOfConsolePanel(installData);
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
            return runConsole(installData);
        }
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        // not implemented
        return false;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        // not implemented
        return false;
    }
}
