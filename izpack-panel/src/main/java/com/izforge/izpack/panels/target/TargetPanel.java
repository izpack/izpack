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

package com.izforge.izpack.panels.target;

import java.io.File;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.path.PathInputPanel;

/**
 * The target directory selection panel.
 *
 * @author Julien Ponge
 */
public class TargetPanel extends PathInputPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 3256443616359429170L;

    /**
     * Constructs a <tt>TargetPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public TargetPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, resources, log);
    }

    /**
     * Called when the panel becomes active.
     */
    @Override
    public void panelActivate()
    {
       String path = TargetPanelHelper.getPath(installData);

        if (path != null)
        {
            pathSelectionPanel.setPath(path);
        }

        super.panelActivate();
    }

    @Override
    public void saveData()
    {
        String path = pathSelectionPanel.getPath();
        installData.setInstallPath(path);
    }

    @Override
    public boolean isValidated()
    {
        boolean result = false;
        File targetPathFile = new File(getPath());
        if (TargetPanelHelper.isIncompatibleInstallation(getPath()))
        {
            emitError(getString("installer.error"), getString("TargetPanel.incompatibleInstallation"));
        }
        else if (targetPathFile.isFile())
        {
            emitError(getString("installer.error"), getString(getI18nStringForClass("isfile", "PathInputPanel")));
            return false;
        }
        else if (super.isValidated())
        {
            String path = getPath();
            installData.setInstallPath(path);
            result = true;
        }
        return result;
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new TargetPanelAutomation().createInstallationRecord(installData, panelRoot);
    }

    @Override
    public String getSummaryBody()
    {
        return (installData.getInstallPath());
    }

}
