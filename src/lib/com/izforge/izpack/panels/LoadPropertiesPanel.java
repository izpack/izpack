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

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;

/**
 * The panel that loads and sets variable values from a properties file.
 * 
 * @author Dasapich Thongnopnua
 */
public class LoadPropertiesPanel extends IzPanel
{

    private static final long serialVersionUID = 6910963095261854808L;

    private static final String PROPERTIES_FILE_VARIABLE = "load.properties.file";

    /**
     * The constructor. The panel loads values from a properties file specified by the variable
     * load.properties.file.
     * 
     * @param parent The parent window.
     * @param idata The installation data.
     */
    public LoadPropertiesPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        setHidden(true);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // Load the properties
        try
        {
            loadVariables();
        }
        catch (Exception e)
        {
            Debug.log(e);
        }
        // Then move on to the next panel
        parent.skipPanel();
    }

    /**
     * Set variables from the properties file.
     * 
     * @throws Exception
     */
    public void loadVariables() throws Exception
    {
        String loadFile = idata.getVariable(PROPERTIES_FILE_VARIABLE);
        if (loadFile == null)
        {
            Debug.log(PROPERTIES_FILE_VARIABLE + " not set--skipping.");
            return;
        }
        FileInputStream in = new FileInputStream(loadFile);
        Properties p = new Properties();
        try
        {
            p.load(in);
            Enumeration<?> pNames = p.propertyNames();
            while (pNames.hasMoreElements())
            {
                String strVariableName = (String) pNames.nextElement();
                String strVariableValue = p.getProperty(strVariableName);
                if (strVariableValue != null)
                {
                    idata.setVariable(strVariableName, strVariableValue);
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Indicates whether the panel has been validated or not.
     * 
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }

}
