/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.panels.userinput;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;

/**
 * Functions to support automated usage of the UserInputPanel
 *
 * @author Jonathan Halliday
 * @author Elmar Grom
 */
public class UserInputPanelAutomationHelper implements PanelAutomation
{
    private static final Logger logger = Logger.getLogger(UserInputPanelAutomationHelper.class.getName());

    // ------------------------------------------------------
    // automatic script section keys
    // ------------------------------------------------------
    private static final String AUTO_KEY_ENTRY = "entry";

    // ------------------------------------------------------
    // automatic script keys attributes
    // ------------------------------------------------------
    private static final String AUTO_ATTRIBUTE_KEY = "key";

    private static final String AUTO_ATTRIBUTE_VALUE = "value";

    // ------------------------------------------------------
    // String-String key-value pairs
    // ------------------------------------------------------
    private Map<String, String> entries;

    /**
     * Default constructor, used during automated installation.
     */
    public UserInputPanelAutomationHelper()
    {
        this.entries = null;
    }

    /**
     * @param entries String-String key-value pairs representing the state of the Panel
     */
    public UserInputPanelAutomationHelper(Map<String, String> entries)
    {
        this.entries = entries;
    }

    /**
     * Serialize state to XML and insert under panelRoot.
     *
     * @param installData The installation installData GUI.
     * @param rootElement The XML root element of the panels blackbox tree.
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
        IXMLElement dataElement;

        // ----------------------------------------------------
        // add all entries
        // ----------------------------------------------------
        for (String key : this.entries.keySet())
        {
            String value = this.entries.get(key);
            dataElement = new XMLElementImpl(AUTO_KEY_ENTRY, rootElement);
            dataElement.setAttribute(AUTO_ATTRIBUTE_KEY, key);
            dataElement.setAttribute(AUTO_ATTRIBUTE_VALUE, value);

            rootElement.addChild(dataElement);
        }
    }

    /**
     * Deserialize state from panelRoot and set installData variables accordingly.
     *
     *
     * @param idata     The installation installDataGUI.
     * @param panelRoot The XML root element of the panels blackbox tree.
     * @throws InstallerException if some elements are missing.
     */
    @Override
    public void runAutomated(InstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        String variable;
        String value;

        List<IXMLElement> userEntries = panelRoot.getChildrenNamed(AUTO_KEY_ENTRY);

        // ----------------------------------------------------
        // retieve each entry and substitute the associated
        // variable
        // ----------------------------------------------------
        Variables variables = idata.getVariables();
        for (IXMLElement dataElement : userEntries)
        {
            variable = dataElement.getAttribute(AUTO_ATTRIBUTE_KEY);

            // Substitute variable used in the 'value' field
            value = dataElement.getAttribute(AUTO_ATTRIBUTE_VALUE);
            value = variables.replace(value);

            logger.fine("Setting variable " + variable + " to " + value);
            idata.setVariable(variable, value);
        }
    }
}
