/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 JBoss Inc
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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;

/**
 * common utility methods for use by InstallationGroup related consoles/panels
 *
 */
public class InstallationGroups
{

    private static final transient Logger logger = Logger.getLogger(InstallationGroups.class.getName());

    public final static Comparator<GroupData> BY_SORT_KEY = new Comparator<GroupData>() {
        @Override
        public int compare(GroupData o1, GroupData o2) {
            return o1.sortKey.compareTo(o2.sortKey);
        }
    };

    /**
     * Build the set of unique installGroups installDataGUI. The GroupData description
     * is taken from the InstallationGroupPanel.description.[name] property
     * where [name] is the installGroup name. The GroupData size is built
     * from the Pack.size sum.
     *
     * @param automatedInstallData - the panel install installDataGUI
     * @return HashMap<String, GroupData> of unique install group names
     */
    public static HashMap<String, GroupData> getInstallGroups(AutomatedInstallData automatedInstallData)
    {
        /* First create a packsByName<String, Pack> of all packs and identify
        the unique install group names.
        */
        Map<String, Pack> packsByName = new HashMap<String, Pack>();
        HashMap<String, GroupData> installGroups = new HashMap<String, GroupData>();
        for (Pack pack : automatedInstallData.getAvailablePacks())
        {
            packsByName.put(pack.getName(), pack);
            Set<String> groups = pack.getInstallGroups();
            logger.fine("Pack: " + pack.getName() + ", installGroups: " + groups);
            for (String group : groups)
            {
                GroupData data = installGroups.get(group);
                if (data == null)
                {
                    String description = InstallationGroups.getGroupDescription(group, automatedInstallData);
                    String sortKey =  InstallationGroups.getGroupSortKey(group, automatedInstallData);
                    data = new GroupData(group, description, sortKey);
                    installGroups.put(group, data);
                }
            }
        }
        logger.fine("Found installGroups: " + installGroups.keySet());

        /* Build up a set of the packs to include in the installation by finding
        all packs in the selected group, and then include their dependencies.
        */
        for (GroupData data : installGroups.values())
        {
            logger.fine("Adding dependents for: " + data.name);
            for (Pack pack : automatedInstallData.getAvailablePacks())
            {
                Set<String> groups = pack.getInstallGroups();
                if (groups.size() == 0 || groups.contains(data.name))
                {
                    // The pack may have already been added while traversing dependencies
                    if (!data.packNames.contains(pack.getName()))
                    {
                        data.addDependents(pack, packsByName);
                    }
                }
            }
            logger.fine("Completed dependents for: " + data);
        }

        return installGroups;
    }

    /**
     * Look for a key = InstallationGroupPanel.sortKey.[group] entry:
     * by using installData.getVariable(key)
     * if this variable is not defined, defaults to group
     *
     * @param group - the installation group name
     * @param automatedInstallData - installation data
     * @return the group sortkey
     */
    public static String getGroupSortKey(String group, AutomatedInstallData automatedInstallData)
    {
        String key = "InstallationGroupPanel.sortKey." + group;
        String sortKey = automatedInstallData.getVariable(key);
        if (sortKey == null)
        {
            sortKey = group;
        }
        try
        {
            sortKey = URLDecoder.decode(sortKey, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.warning("Failed to convert sortKey" + e.getMessage());
        }

        return sortKey;
    }

    /**
     * Look for a key = InstallationGroupPanel.description.[group] entry:
     * first using installData.langpack.getString(key+".html")
     * next using installData.langpack.getString(key)
     * next using installData.getVariable(key)
     * lastly, defaulting to group + " installation"
     *
     * @param group - the installation group name
     * @param automatedInstallData - installation data
     * @return the group description
     */
    public static String getGroupDescription(String group, AutomatedInstallData automatedInstallData)
    {
        String description = null;
        String key = "InstallationGroupPanel.description." + group;
        String htmlKey = key + ".html";
        String html = getString(automatedInstallData, htmlKey);
        // This will equal the key if there is no entry
        if (htmlKey.equalsIgnoreCase(html))
        {
            description = getString(automatedInstallData, key);
        }
        else
        {
            description = html;
        }
        if (description == null || key.equalsIgnoreCase(description))
        {
            description = automatedInstallData.getVariable(key);
        }
        if (description == null)
        {
            description = group + " installation";
        }
        try
        {
            description = URLDecoder.decode(description, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.warning("Failed to convert description: "+e.getMessage());
        }

        return description;
    }

    /**
     * Look for a key = InstallationGroupPanel.group.[group] entry:
     * first using installData.langpackgetString(key+".html")
     * next using installData.langpack.getString(key)
     * next using installData.getVariable(key)
     * lastly, defaulting to group
     *
     * @param group - the installation group name
     * @param automatedInstallData - installation data
     * @return the localized group name
     */
    public static String getLocalizedGroupName(String group, AutomatedInstallData automatedInstallData)
    {
        String gname = null;
        String key = "InstallationGroupPanel.group." + group;
        String htmlKey = key + ".html";
        String html = getString(automatedInstallData, htmlKey);
        // This will equal the key if there is no entry
        if (htmlKey.equalsIgnoreCase(html))
        {
            gname = getString(automatedInstallData, key);
        }
        else
        {
            gname = html;
        }
        if (gname == null || key.equalsIgnoreCase(gname))
        {
            gname = automatedInstallData.getVariable(key);
        }
        if (gname == null)
        {
            gname = group;
        }
        try
        {
            gname = URLDecoder.decode(gname, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            logger.warning("Failed to convert localized group name: " + e.getMessage());
        }

        return gname;
    }

    protected static String getString(AutomatedInstallData automatedInstallData, String key)
    {
        return automatedInstallData.getMessages().get(key);
    }
}
