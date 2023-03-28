/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.userpath;

import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;

/**
 * The target directory selection panel.
 *
 * @author Julien Ponge
 * @author Jeff Gordon
 */
public class UserPathPanel extends UserPathInputPanel
{
    private static final long serialVersionUID = 3256443616359429170L;

    private static final Logger logger = Logger.getLogger(UserPathPanel.class.getName());

    public static final String PANEL_NAME = "UserPathPanel";
    private boolean skip = false;

    public static final String PATH_VARIABLE_NAME = "UserPathPanelVariable";
    public static final String PATH_DEPENDS_PACK_NAME = "UserPathPanelDependsName";
    public static final String PATH_ELEMENT_NAME = "UserPathPanelElement";

    /**
     * Constructs an <tt>UserPathPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public UserPathPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, PANEL_NAME, parent, installData, resources, log);
        // load the default directory info (if present)
        if (getDefaultDir() != null)
        {
            installData.setVariable(PATH_VARIABLE_NAME, getDefaultDir());
        }
    }

    @Override
    public void panelActivate()
    {
        skip = true;
        logger.fine("Looking for activation condition");
        // Need to have a way to suppress panel if not in selected packs.
        String dependsPackName = installData.getVariable(PATH_DEPENDS_PACK_NAME);
        if (dependsPackName != null && !dependsPackName.isEmpty())
        {
            logger.fine("Checking for pack dependency of " + dependsPackName);
            for (Pack pack : installData.getSelectedPacks())
            {
                logger.fine("- Checking if " + pack.getName() + " equals " + dependsPackName);
                if (pack.getName().equalsIgnoreCase(dependsPackName))
                {
                    skip = false;
                    logger.fine("-- Found " + dependsPackName + ", panel will be shown");
                    break;
                }
            }
        }
        else
        {
            logger.fine("Not Checking for a pack dependency, panel will be shown");
            skip = false;
        }
        if (skip)
        {
            logger.fine(PANEL_NAME + " will not be shown");
            parent.skipPanel();
            return;
        }
        super.panelActivate();
        Variables variables = installData.getVariables();
        // Set the default or old value to the path selection panel.
        String expandedPath = variables.get(PATH_VARIABLE_NAME);
        expandedPath = variables.replace(expandedPath);
        pathSelectionPanel.setPath(expandedPath);
    }

    @Override
    public boolean isValidated()
    {
        // Standard behavior of PathInputPanel.
        if (!super.isValidated())
        {
            return (false);
        }
        installData.setVariable(PATH_VARIABLE_NAME, pathSelectionPanel.getPath());
        return (true);
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        if (!(skip))
        {
            new UserPathPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
        }
    }

    @Override
    public String getSummaryBody()
    {
        if (skip)
        {
            return null;
        }
        else
        {
            return (installData.getVariable(PATH_VARIABLE_NAME));
        }
    }
}
