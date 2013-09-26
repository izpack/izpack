/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.installer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Defines the Interface that must be implemented for running Panels in console mode.
 * <p/>
 * Implementing classes MUST NOT link against awt/swing classes. Thus the Panels cannot implement
 * this interface directly, they should use e.g. helper classes instead.
 * 
 * @author Mounir El Hajj
 * 
 */

public interface PanelConsole
{

    /**
     * Asks the panel to return all inputed fields/variables in a string with a properties file
     * style
     * 
     * @param installData The installation data
     */
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter);

    /**
     * Asks the panel to run and do its work, given a set of properties to use as variables
     * 
     * @param installData The installation data
     * @param p The the properties
     */
    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p);

    /**
     * Asks the panel to run in interactive console mode
     * 
     * @param installData The installation data *
     */
    public boolean runConsole(AutomatedInstallData installData);
    
    public void addPreActivationAction(PanelAction preActivateAction);

    public void addPreValidationAction(PanelAction preValidateAction);

    public void addPostValidationAction(PanelAction postValidateAction);
    
    public void executePreActivationActions();

    public void executePreValidationActions();

    public void executePostValidationActions();

    public void setValidationService(DataValidator validationService);

    public void setAutomatedInstallData (AutomatedInstallData idata);
    

}
