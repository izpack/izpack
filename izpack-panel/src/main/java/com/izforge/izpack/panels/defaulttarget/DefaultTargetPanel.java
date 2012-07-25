/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.panels.defaulttarget;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.path.PathInputPanel;

/**
 * The target directory selection panel.
 *
 * @author Julien Ponge
 * @author Jeff Gordon
 */
public class DefaultTargetPanel extends PathInputPanel
{

    private static final long serialVersionUID = 3256443616359329170L;
    private PanelAutomation defaultTargetPanelAutomationHelper;

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param helper      the automation helper
     * @param log         the log
     */
    public DefaultTargetPanel(Panel panel, InstallerFrame parent, GUIInstallData installData,
                              Resources resources, DefaultTargetPanelAutomationHelper helper, Log log)
    {
        super(panel, parent, installData, resources, log);
        this.defaultTargetPanelAutomationHelper = helper;
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // Resolve the default for chosenPath
//        super.panelActivate();
        // Set the default or old value to the path selection panel.
        pathSelectionPanel.setPath(this.installData.getDefaultInstallPath());
        parent.skipPanel();
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Whether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        // Standard behavior of PathInputPanel.
        //if (!super.isValidated()) return (false);
        //installData.setInstallPath(pathSelectionPanel.getPath());
        return (true);
    }

    /**
     * Asks to make the XML panel installDataGUI.
     *
     * @param panelRoot The tree to put the installDataGUI in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        defaultTargetPanelAutomationHelper.makeXMLData(this.installData, panelRoot);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */

    public String getSummaryBody()
    {
        return (this.installData.getInstallPath());
    }

}
