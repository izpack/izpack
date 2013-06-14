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

package com.izforge.izpack.panels.treepacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Option;
import com.izforge.izpack.api.handler.Prompt.Options;
import com.izforge.izpack.api.handler.Prompt.Type;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

/**
 * Console implementation for the TreePacksPanel.
 * <p/>
 * Based on PacksConsolePanelHelper
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class TreePacksConsolePanel extends AbstractConsolePanel implements ConsolePanel
{

    private final Prompt prompt;

    private HashMap<String, Pack> idToPack;
    private HashMap<String, List<String>> treeData;

    private static final String REQUIRED = "required";
    private static final String NOT_SELECTED = "Not Selected";
    private static final String ALREADY_SELECTED = "Already Selected";
    private static final String DONE = "Done!";
    private static final String SPACE = " ";


    /**
     * Constructs a {@code TreePacksConsolePanel}.
     *
     * @param panel  the parent panel/view. May be {@code null}
     * @param prompt the prompt
     */
    public TreePacksConsolePanel(PanelView<Console> panel, Prompt prompt)
    {
        super(panel);
        this.prompt = prompt;
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt> if the installation is successful, otherwise <tt>false</tt>
     */
    public boolean run(InstallData installData, Properties properties)
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
    public boolean run(InstallData installData, Console console)
    {
        List<Pack> selectedPacks = new LinkedList<Pack>();
        createTreeData(installData);
        out(Type.INFORMATION, "");

        for (String key : treeData.keySet())
        {
            drawHelper(treeData, selectedPacks, installData, idToPack, key, true, "\t");
        }
        out(Type.INFORMATION, DONE);

        installData.setSelectedPacks(selectedPacks);

        if (selectedPacks.size() == 0)
        {
            out(Type.WARNING, "You have not selected any packs!");
            out(Type.WARNING, "Are you sure you want to continue?");
        }

        return promptEndPanel(installData, console);
    }

    private void createTreeData(InstallData installData)
    {
        treeData = new HashMap<String, List<String>>();
        idToPack = new HashMap<String, Pack>();

        for (Pack pack : getAvailableShowablePacks(installData))
        {
            idToPack.put(pack.getName(), pack);
            if (pack.getParent() != null)
            {
                List<String> kids;
                if (treeData.containsKey(pack.getParent()))
                {
                    kids = treeData.get(pack.getParent());
                }
                else
                {
                    kids = new ArrayList<String>();
                }
                kids.add(pack.getName());
                treeData.put(pack.getParent(), kids);
            }
        }
    }

    private void out(Type type, String message)
    {
        prompt.message(type, message);
    }


    /**
     * It is used to "draw" the appropriate tree-like structure of the packs and ask if you want to install
     * the pack. The pack will automatically be selected if it is required; otherwise you will be prompted if
     * you want to install that pack. If a pack is not selected, then their child packs won't be installed as
     * well and you won't be prompted to install them.
     *
     * @param treeData      - Map that contains information on the parent pack and its children
     * @param selectedPacks - the packs that are selected by the user are added there
     * @param installData   - Database of izpack
     * @param idToPack      - Map that mapds the id of the available packs to the actual Pack object
     * @param packParent    - The current "parent" pack to process
     * @param packMaster    - boolean to know if packParent is a top-level pack
     * @param indent        - String to know by how much the child packs should be indented
     */
    private void drawHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks,
                            final InstallData installData,
                            final Map<String, Pack> idToPack, final String packParent, boolean packMaster,
                            final String indent)
    {
        Pack p;

        /*
         * If that packParent contains children,
         * then run recursively and ask whether
         * you want to install the child packs·
         * too [if parent pack selected]
         */
        if (treeData.containsKey(packParent))
        {
            p = idToPack.get(packParent);

            // If the pack is a top-level pack and that top-level pack was not·
            // selected, then return. This will avoid prompting the user to
            // install the child packs.
            if (packMaster && !selectHelper(treeData, selectedPacks, installData, idToPack, p, packMaster, indent))
            {
                return;
            }
            // Now iterate through the child packs of the parent pack.
            for (String id : treeData.get(packParent))
            {
                p = idToPack.get(id);
                selectHelper(treeData, selectedPacks, installData, idToPack, p, false, indent);
            }
        }
    }

    private boolean selectHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks,
                                 final InstallData installData,
                                 final Map<String, Pack> idToPack, final Pack p, boolean packMaster,
                                 final String indent)
    {
        Boolean conditionSatisfied = checkCondition(installData, p);
        Boolean conditionExists = !(conditionSatisfied == null);
        String packName = p.getName();

        // If a condition is set to that pack
        if (conditionExists)
        {
            if (conditionSatisfied)
            {
                out(Type.INFORMATION, (packMaster ? "[" + packName + "]" : indent + packName) + SPACE
                        + ALREADY_SELECTED);

                selectedPacks.add(p);
                // we call drawHelper again to check if that pack has child packs
                // If that pack is a top-level pack, then don't run drawHelper as
                // it will create an infinite loop·
                if (!packMaster)
                {
                    drawHelper(treeData, selectedPacks, installData, idToPack, packName, packMaster,
                               indent + indent);
                }
                return true;
            }
            else
            {
                // condition says don't install!
                out(Type.INFORMATION, (packMaster ? "[" + packName + "]" : indent + packName) + SPACE
                        + NOT_SELECTED);
                return false;
            }
            // If no condition specified
        }
        else if (p.isRequired())
        {
            out(Type.INFORMATION, (packMaster ? "[" + packName + "]" : indent + packName)
                    + SPACE + REQUIRED);

            selectedPacks.add(p);
            if (!packMaster)
            {
                drawHelper(treeData, selectedPacks, installData, idToPack, packName, packMaster, indent + indent);
            }
            return true;
            // Prompt the user
        }
        else
        {

            if (askUser(packMaster ? "[" + packName + "] " : indent + packName))
            {
                selectedPacks.add(p);
                if (!packMaster)
                {
                    drawHelper(treeData, selectedPacks, installData, idToPack, packName, packMaster, indent + indent);
                }
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData - the data of izpack
     * @param pack        - the pack whose condition needs to be checked·
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */
    private Boolean checkCondition(InstallData installData, Pack pack)
    {
        if (pack.hasCondition())
        {
            return installData.getRules().isConditionTrue(pack.getCondition());
        }
        else
        {
            return null;
        }
    }

    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter>·
     *
     * @return boolean  - true if condition above satisfied. Otherwise false
     */
    private boolean askUser(String message)
    {
        return Option.YES == prompt.confirm(Type.QUESTION, message, Options.YES_NO);
    }

    private List<Pack> getAvailableShowablePacks(InstallData installData)
    {
        List<Pack> packs = new ArrayList<Pack>();
        List<Pack> availablePacks = installData.getAvailablePacks();

        for (Pack pack : availablePacks)
        {
            if (!pack.isHidden())
            {
                packs.add(pack);
            }
        }

        return packs;
    }
}
