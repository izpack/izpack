/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Wolschon
 * Copyright 2002 Jan Blok
 * Copyright 2004 Gaganis Giorgos
 * Copyright 2006,2007 Dennis Reil
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

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.data.GUIInstallData;

public class PacksModelGUI extends PacksModel
{
    GUIInstallData GuiInstallData;
    private PacksPanelInterface panel;

    public PacksModelGUI(PacksPanelInterface panel, GUIInstallData GuiInstallData, RulesEngine rules)
    {
        super(GuiInstallData);
        this.GuiInstallData = GuiInstallData;
        this.panel = panel;

    }

    @Override
    public void setValueAt(Object checkValue, int rowIndex, int columnIndex)
    {
        if (columnIndex != 0 || !(checkValue instanceof Integer))
        {
            return;
        }
        else
        {
            super.setValueAt(checkValue, rowIndex, columnIndex);
            updatePacksToInstall();
            updateBytes();
            fireTableDataChanged();
            panel.showSpaceRequired();

        }
    }

    private void updateBytes()
    {
        long bytes = 0;
        for (int q = 0; q < packs.size(); q++)
        {
            if (Math.abs(checkValues[q]) == 1)
            {
                Pack pack = packs.get(q);
                bytes += pack.getSize();
            }
        }

        // add selected hidden bytes
        for (Pack hidden : this.hiddenPacks)
        {
            if (this.rules.canInstallPack(hidden.getName(), variables))
            {
                bytes += hidden.getSize();
            }
        }
        panel.setBytes(bytes);
    }

    @Override
    public int getColumnCount()
    {
        boolean doNotShowPackSize = Boolean.parseBoolean(GuiInstallData.guiPrefs.modifier.get("doNotShowPackSizeColumn"));

        int result;
        if (!doNotShowPackSize)
        {
            result = 3;
        }
        else
        {
            result = 2;
        }
        return result;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }
}
