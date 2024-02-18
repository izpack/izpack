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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.installer.automation.PanelAutomation;

import java.util.logging.Logger;

public class DataCheckPanelAutomation implements PanelAutomation
{
    private static final Logger logger = Logger.getLogger(DataCheckPanelAutomation.class.getName());
    private static int instanceCount = 0;
    private final String panelId;
    private final int instanceNumber;

    public DataCheckPanelAutomation(Panel panel)
    {
        panelId = panel.getPanelId();
        instanceNumber = instanceCount++;
    }

    /**
     * Creates an installation record for unattended installations and adds it to a XML root element.
     *
     * @param installData The installation data
     * @param panelRoot The root element to add panel-specific child elements to
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement panelRoot)
    {
        // we do nothing for the DataCheckPanel in unattended installations
    }

    /**
     * Makes the panel work in automated mode. Default is to do nothing, but any panel doing
     * something 'effective' during the installation process should implement this method.
     *
     * @param installData The installation data
     * @param panelRoot   The XML root element of the panels blackbox tree.
     * @throws com.izforge.izpack.api.exception.InstallerException
     *          if the automated work  failed critically.
     */

    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot)
    {
        String packNames = DataCheckCommon.getPackNames(installData);
        String infoToLog = DataCheckCommon.getMainLabelWithDashes(instanceNumber, panelId) + "\n" +
                DataCheckCommon.SUB_LABEL + "\n" +
                DataCheckCommon.getInstallDataVariables(installData) + "\n" +
                (packNames == null ? "" : (packNames + "\n")) +
                DataCheckCommon.getConditions(installData);
        logger.fine(infoToLog);
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides) {}
}
