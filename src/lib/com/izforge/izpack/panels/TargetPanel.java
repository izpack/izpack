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

import java.util.Properties;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.VariableSubstitutor;
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
    
    public static String loadDefaultDirFromVariables(Properties vars)
    {
        String os = System.getProperty("os.name").replace(' ', '_').toLowerCase();
        
        String path = vars.getProperty("TargetPanel.dir.".concat(os));
        
        if (path == null) {
            path = vars.getProperty("TargetPanel.dir." + (OsVersion.IS_WINDOWS ? "windows" : (OsVersion.IS_OSX ? "macosx" : "unix")));
            if (path == null) {
                path = vars.getProperty("TargetPanel.dir");
            }
        }
        if (path != null) {
            path = new VariableSubstitutor(vars).substitute(path, null);
        }
        
        return path;
    }

    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public TargetPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        // load the default directory info (if present)
        loadDefaultDir();
        String defDir = getDefaultDir();
        if (defDir != null)
        {
            // override the system default that uses app name (which is set in
            // the Installer class)
            idata.setInstallPath(defDir);
        }
        noWhitespaces = Boolean.valueOf(idata.getVariable("TargetPanel.noWhitespaces"));
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // Resolve the default for chosenPath
        super.panelActivate();

        // load the default directory info (if present)
        loadDefaultDir();
        String defDir = getDefaultDir();
        if (defDir != null)
        {
            // override the system default that uses app name (which is set in
            // the Installer class)
            idata.setInstallPath(defDir);
        }
        
        // Set the default or old value to the path selection panel.
        pathSelectionPanel.setPath(idata.getInstallPath());
    }

    /**
     * This method simple delegates to <code>PathInputPanel.loadDefaultInstallDir</code> with the
     * current parent as installer frame.
     */
    public void loadDefaultDir()
    {
        String path = loadDefaultDirFromVariables(idata.getVariables());
        
        if (path != null) {
            System.out.println("Found default install dir in variables: " + path);
            setDefaultInstallDir(path);
            return;
        }
        
        super.loadDefaultInstallDir(parent, idata);
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
     * Returns the default install directory. This is equal to
     * <code>PathInputPanel.getDefaultInstallDir</code>
     *
     * @return the default install directory
     */
    public String getDefaultDir()
    {
        return getDefaultInstallDir();
    }

    /**
     * Sets the default install directory to the given String. This is equal to
     * <code>PathInputPanel.setDefaultInstallDir</code>
     *
     * @param defaultDir path to be used for the install directory
     */
    public void setDefaultDir(String defaultDir)
    {
        setDefaultInstallDir(defaultDir);
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
