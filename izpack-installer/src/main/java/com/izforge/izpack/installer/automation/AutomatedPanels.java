/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.installer.automation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.installer.panel.AbstractPanels;
import com.izforge.izpack.installer.panel.Panels;


/**
 * Implementation of {@link Panels} for {@link AutomatedPanelView}.
 *
 * @author Tim Anderson
 */
public class AutomatedPanels extends AbstractPanels<AutomatedPanelView, PanelAutomation>
{

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AutomatedPanels.class.getName());


    /**
     * Constructs an {@code AutomatedPanels}.
     *
     * @param panels      the panels
     * @param installData the installation data
     */
    public AutomatedPanels(List<AutomatedPanelView> panels, AutomatedInstallData installData)
    {
        super(panels, installData);
        this.installData = installData;
    }

    /**
     * Switches panels.
     *
     * @param newPanel the panel to switch to
     * @param oldPanel the panel to switch from, or {@code null} if there was no prior panel
     * @return {@code true} if the switch was successful
     */
    @Override
    protected boolean switchPanel(AutomatedPanelView newPanel, AutomatedPanelView oldPanel)
    {
        boolean result;
        if (newPanel.getViewClass() == null)
        {
            // panel has no view. This is apparently OK - not all panels have/need automation support.
            logger.warning("AutomationHelper class not found for panel class: " + newPanel.getPanel().getClassName());
            result = executeValidationActions(newPanel, true);
        }
        else
        {
            newPanel.executePreActivationActions();
            PanelAutomation view = newPanel.getView();

            IXMLElement xml = installData.getInstallationRecordPanelRoot(newPanel.getPanelId());
            if (xml != null)
            {
                view.runAutomated(installData, xml);
                result = true;
            }
            else
            {
                logger.log(Level.SEVERE, "No configuration for panel: " + newPanel.getPanel().getPanelId());
                result = false;
            }
        }
        return result;
    }
}
