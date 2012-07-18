/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.packs;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.resource.Messages;

import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.AbstractPanelConsole;

import com.izforge.izpack.util.Console;

/**
 * Console implementation for the TreePacksPanel.
 *
 * Based on PacksPanelConsoleHelper
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class PacksPanelConsoleHelper extends AbstractPanelConsole implements PanelConsole
{
    /** Used to read input from the user */
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    private Messages messages;

    private String REQUIRED = "required";
    private String NOT_SELECTED = "Not Selected";
    private String ALREADY_SELECTED = "Already Selected";
    private String CONTINUE = "Continue?";
    private String NO_PACKS = "No packs selected.";
    private String DONE = "Done!";

    private String SPACE = " ";

    private HashMap<String, Pack> names;

    private HashMap<String, List<String>> treeData;
    private HashMap<String, Pack> idToPack;

    /** Are there dependencies in the packs */
    private boolean dependenciesExist = false;

    private void loadLangpack(InstallData installData)
    {
        messages = installData.getMessages();
    }

    /**
     * Generates a properties file for each input field or variable.
     *
     * @param installData the installation data
     * @param printWriter the properties file to write to
     * @return <tt>true</tt> if the generation is successful, otherwise <tt>false</tt>
     */
    public boolean runGeneratePropertiesFile(InstallData InstallData, PrintWriter printWriter)
    {
        return true;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt> if the installation is successful, otherwise <tt>false</tt>
     */
    public boolean runConsoleFromProperties(InstallData InstallData, Properties Properties)
    {
        return true;
    }

    /**
     * Runs the panel in interactive console mode.
     *
     * @param installData the installation data
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    public boolean runConsole(InstallData installData)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    public boolean runConsole(InstallData installData, Console console)
    {
        out("");
        List<String> kids;
        List<Pack> selectedPacks = new LinkedList<Pack>();
        loadLangpack(installData);
        computePacks(installData.getAvailablePacks());

        // private HashMap<String, Pack> names;
        for (String key: names.keySet())
        {
            drawHelper(key, selectedPacks, installData);
        }
        out(DONE);

        installData.setSelectedPacks(selectedPacks);

        if (selectedPacks.size() == 0)
        {
            out("You have not selected any packs!");
            out("Are you sure you want to continue?");
        }
        return promptEndPanel(installData, console);
    }

    private void out(String message)
    {
        System.out.println(message);
    }


    private String getTranslation(String id)
    {
        return messages.get(id);
    }

    /**
     * It is used to "draw" the appropriate tree-like structure of the packs and ask if you want to install
     * the pack. The pack will automatically be selected if it is required; otherwise you will be prompted if
     * you want to install that pack. If a pack is not selected, then their child packs won't be installed as
     * well and you won't be prompted to install them.
     *
     * @param pack              - the pack to install
     * @param selectedPacks     - the packs that are selected by the user are added there
     * @param installData       - Database of izpack
     *
     * @return void
     */
    private void drawHelper(final String pack, final List<Pack> selectedPacks, final InstallData installData)
    {
        Pack p                      = names.get(pack);
        Boolean conditionSatisfied  = checkCondition(installData, p);
        Boolean conditionExists     = !(conditionSatisfied == null);
        String packName             = p.getName();
        String id                   = p.getLangPackId();

        // If a condition is set to that pack
        if (conditionExists) {
            if (conditionSatisfied) {

                out(packName + SPACE + ALREADY_SELECTED);
                selectedPacks.add(p);

            } else {
                // condition says don't install!
                out(packName + SPACE + NOT_SELECTED);
            }
            // If no condition specified
        } else if (p.isRequired()) {
            out(packName + SPACE + REQUIRED);

            selectedPacks.add(p);
            // Prompt the user
        } else {
            System.out.print(packName + " [y/n] ");
            if (readPrompt()) {
                selectedPacks.add(p);
            }
        }
    }

    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData       - the data of izpack
     * @param pack              - the pack whose condition needs to be checked·
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */
    private Boolean checkCondition(InstallData installData, Pack pack)
    {
        if (pack.hasCondition()) {
            return installData.getRules().isConditionTrue(pack.getCondition());
        } else {
            return null;
        }
    }

    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter>·
     *
     * @return boolean  - true if condition above satisfied. Otherwise false
     */
    private boolean readPrompt()
    {
        String answer = "No";
        try {
            answer = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes") || answer.equals(""));
    }


    /**
     * Computes pack related installDataGUI like the names or the dependencies state.
     *
     * @param packs The list of packs.
     */
    private void computePacks(List<Pack> packs)
    {
        names = new HashMap<String, Pack>();
        dependenciesExist = false;
        for (Pack pack : packs)
        {
            names.put(pack.getName(), pack);
            if (pack.getDependencies() != null || pack.getExcludeGroup() != null)
            {
                dependenciesExist = true;
            }
        }
    }
}
