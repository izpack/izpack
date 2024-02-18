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
    private static int instanceCount = 0;
    private final String panelId;
    private final int instanceNumber;

    /**
     * Constructs an {@code DefaultTargetConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public DataCheckConsolePanel(PanelView<ConsolePanel> panel)
    {
        super(panel);
        panelId = panel.getPanelId();
        instanceNumber = instanceCount++;
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
        console.println(DataCheckCommon.getMainLabelWithDashes(instanceNumber, panelId));
        console.println(DataCheckCommon.SUB_LABEL);
        console.println(DataCheckCommon.getInstallDataVariables(installData));
        String packNames = DataCheckCommon.getPackNames(installData);
        if (packNames != null)
        {
            console.println(packNames);
        }
        console.print(DataCheckCommon.getConditions(installData));

        return promptEndPanel(installData, console);
    }
}
