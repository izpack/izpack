/*
 * IzPack - Copyright 2024 Hitesh A. Bosamiya, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2024 Hitesh A. Bosamiya
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
package com.izforge.izpack.panels.datacheck;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;

import java.util.*;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public class DataCheckCommon
{
    public static final String SUB_LABEL = "Debugging InstallData: InstallData variables, packs (selected packs are marked) and conditions.";

    public static String getMainLabelWithDashes(int instanceNumber, String panelId)
    {
        return "------------------------" + getMainLabel(instanceNumber, panelId) + "------------------------";
    }

    public static String getMainLabel(int instanceNumber, String panelId)
    {
        return "Data Check Panel, instance: " + instanceNumber + ", panel ID: " + panelId;
    }

    public static String getInstallDataVariables(InstallData installData)
    {
        Properties properties = installData.getVariables().getProperties();
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) Collections.list(installData.getVariables().getProperties().propertyNames());
        list.sort(CASE_INSENSITIVE_ORDER);
        StringBuilder output = new StringBuilder("InstallData Variables:\n");
        for (String varName : list)
        {
            output.append("\tName: ").append(varName)
                    .append(", Value: ").append(properties.getProperty(varName)).append('\n');
        }
        return output.toString();
    }

    public static String getPackNames(InstallData installData)
    {
        StringBuilder output = new StringBuilder("Available Packs:\n");
        int index = 0;
        List<Pack> allPacks = installData.getAllPacks();
        if (allPacks == null || allPacks.isEmpty())
        {
            return null;
        }
        for (Pack pack : allPacks)
        {
            String status = installData.getSelectedPacks().contains(pack) ? "Selected" : "Unselected";
            output.append('\t').append(index++).append(": ")
                    .append(pack.getName()).append(" (").append(status).append(")\n");
        }
        return output.toString();
    }

    public static String getConditions(InstallData installData)
    {
        StringBuilder output = new StringBuilder("Conditions:\n");
        RulesEngine rules = installData.getRules();
        List<String> conditionIds = new ArrayList<>(rules.getKnownConditionIds());
        conditionIds.sort(CASE_INSENSITIVE_ORDER);
        int index = 0;
        for (String conditionId : conditionIds) {
            Condition condition = rules.getCondition(conditionId);
            output.append('\t').append(index++).append(": ")
                    .append(condition.getId()).append(" is ").append(condition.isTrue()).append('\n');
        }
        return output.toString();
    }
}
