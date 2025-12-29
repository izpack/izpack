/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.installationgroup;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;

import java.util.*;
import java.util.logging.Logger;

/**
 * Console implementation for the InstallationGroupPanel.
 *
 * @author radai.rosenblatt@gmail.com
 */
public class InstallationGroupConsolePanel extends AbstractConsolePanel implements ConsolePanel
{
    private static final transient Logger logger = Logger.getLogger(InstallationGroupPanel.class.getName());

    private static final String NOT_SELECTED = "Not Selected";
    private static final String DONE = "Done!";
    private static final String SPACE = " ";

    private final Prompt prompt;
    private final InstallData installData;
    private final PlatformModelMatcher matcher;
    
    private GroupData[] rows;

    @SuppressWarnings("UnusedDeclaration")
    public InstallationGroupConsolePanel(PanelView<ConsolePanel> panel, Prompt prompt,
                                         InstallData installData, PlatformModelMatcher matcher)
    {
        super(panel);
        this.prompt = prompt;
        this.installData = installData;
        this.matcher = matcher;
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        //scripted run - no interaction
        return true;
    }

    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);

        // Set/restore availablePacks from allPacks; consider OS constraints
        this.installData.updateAvailablePacks(pack -> matcher.matchesCurrentPlatform(pack.getOsConstraints()));

        // If there are no groups, skip this panel
        Map<String, GroupData> installGroups = InstallationGroups.getInstallGroups(this.installData);
        if (installGroups.size() == 0)
        {
            console.prompt("Skip InstallGroup selection", new String[]{"Yes", "No"}, "Yes");
            return false;
        }

        List<GroupData> sortedGroups = new ArrayList<GroupData>(installGroups.values());
        Collections.sort(sortedGroups, InstallationGroups.BY_SORT_KEY);

        GroupData selected = selectGroup(sortedGroups);
        while (selected==null) {
            out(Prompt.Type.ERROR, "Must select an option");
            selected = selectGroup(sortedGroups);
        }

        this.installData.setVariable("INSTALL_GROUP", selected.name);
        logger.fine("Added variable INSTALL_GROUP=" + selected.name);

        setSelectedPacksBySelectedGroup(selected);

        rows = new GroupData[installGroups.size()];
        int count = 0;
        for (GroupData gd : sortedGroups) {
          rows[count] = gd;
          count++;
        }

        out(Prompt.Type.INFORMATION, DONE);
        return promptEndPanel(installData, console);
    }
    
    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        InstallationGroupPanelAutomationHelper helper = new InstallationGroupPanelAutomationHelper();
        this.installData.setAttribute("GroupData", rows);
        helper.createInstallationRecord(this.installData, panelRoot);
    }

    protected void setSelectedPacksBySelectedGroup(GroupData selected)
    {
        logger.fine("data=" + selected.name);

        // Now remove the packs not in groupPackNames
        Iterator<Pack> iter = installData.getAvailablePacks().iterator();
        while (iter.hasNext())
        {
            Pack pack = iter.next();

            //reverse dependencies must be reset in case the user is going
            //back and forth between the group selection panel and the packs selection panel
            pack.setDependants(null);

            if (!selected.packNames.contains(pack.getName()))
            {
                iter.remove();
                logger.fine("Removed available pack: " + pack.getName());
            }
        }

        this.installData.getSelectedPacks().clear();
        this.installData.getSelectedPacks().addAll(this.installData.getAvailablePacks());

    }

    private GroupData selectGroup(List<GroupData> options)
    {
        GroupData selected = null;
        for (GroupData groupData : options) {
            if (selected!=null) {
                out(Prompt.Type.INFORMATION, groupData.name + SPACE + NOT_SELECTED);
                continue;
            }
            if (askUser(groupData.name)) {
                selected = groupData;
                continue;
            }
        }
        return selected;
    }

    private void out(Prompt.Type type, String message)
    {
        prompt.message(type, message);
    }

    private boolean askUser(String message)
    {
        return Prompt.Option.YES == prompt.confirm(Prompt.Type.QUESTION, message, Prompt.Options.YES_NO);
    }
}
