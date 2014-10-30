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
package com.izforge.izpack.panels;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.adaptator.IXMLElement;

/**
 * The taget directory selection panel.
 *
 * @author Julien Ponge
 */
public class TargetPanel extends PathInputPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 3256443616359429170L;
    private boolean noWhitespaces;
    
    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public TargetPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        // load and save default directory from resource
        super.loadDefaultInstallDir(parent, idata);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // load the default directory info (if present)
        String path = TargetPanelConsoleHelper.loadDefaultInstallDirFromVariables(idata.getVariables());
        // If no default directory in variables, load from resource
        if (path==null) {
            path = defaultInstallDir;
        }
        if(path!=null)
    {
            setDefaultInstallDir(path);
            idata.setInstallPath(getDefaultInstallDir());
            pathSelectionPanel.setPath(idata.getInstallPath());
        }
        
        super.panelActivate();
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        if (noWhitespaces && pathSelectionPanel.getPath() != null && pathSelectionPanel.getPath().length() > 0
                && pathSelectionPanel.getPath().contains(" "))
        {
            emitError(parent.langpack.getString("installer.error"),
                    parent.langpack.getString("PathInputPanel.noWhitespaces"));

            return false;
        }
        
        // Standard behavior of PathInputPanel.
        if (!super.isValidated())
        {
            return (false);
        }
        idata.setInstallPath(pathSelectionPanel.getPath());
        return (true);
    }

    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        new TargetPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */
    public String getSummaryBody()
    {
        return (idata.getInstallPath());
    }
}
