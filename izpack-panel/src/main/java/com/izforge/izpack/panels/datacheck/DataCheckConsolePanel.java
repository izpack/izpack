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
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public class DataCheckConsolePanel extends AbstractConsolePanel
{
    private final InstallData installData;
    private String panelId;

    /**
     * Constructs an {@code DefaultTargetConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     * @param installData the installation data
     */
    public DataCheckConsolePanel(PanelView<ConsolePanel> panel, InstallData installData)
    {
        super(panel);
        this.installData = installData;
        panelId = panel.getPanelId();
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        return true;
    }

    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);
        console.println("------------------------Data Check Panel - " + panelId + "------------------------");
        console.println("Debugging InstallData. All InstallData variables and all packs (selected packs are marked).");
        console.println(getInstallDataVariables());
        console.print(getPackNames());

        return promptEndPanel(installData, console);
    }

    private String getInstallDataVariables()
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

    private String getPackNames()
    {
        StringBuilder output = new StringBuilder("Available Packs:\n");
        int index = 0;
        List<Pack> allPacks = installData.getAllPacks();
        if (allPacks == null)
        {
            return "";
        }
        for (Pack pack : allPacks)
        {
            String status = installData.getSelectedPacks().contains(pack) ? "Selected" : "Unselected";
            output.append('\t').append(index++).append(": ")
                    .append(pack.getName()).append('(').append(status).append(")\n");
        }
        return output.toString();
    }
}
