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
package com.izforge.izpack.panels.selectprinter;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.installer.automation.PanelAutomation;

public class SelectPrinterPanelAutomation implements PanelAutomation
{
    public SelectPrinterPanelAutomation()
    {
    }

    /**
     * Creates an installation record for unattended installations and adds it to a XML root element.
     *
     * @param installData The installation data
     * @param rootElement The root element to add panel-specific child elements to
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
        IXMLElement element = new XMLElementImpl("selectedPrinter", rootElement);
        String selectedPrinter = installData.getVariable("SELECTED_PRINTER");
        element.setContent(String.valueOf(Boolean.parseBoolean(selectedPrinter)));
        rootElement.addChild(element);
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
        IXMLElement element = panelRoot.getFirstChildNamed("selectedPrinter");
        // installation record of prior (to this change) versions will not have this element
        if (element != null)
        {
            installData.setVariable("SELECTED_PRINTER", element.getContent());
        }
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
        String selectedPrinter = overrides.fetch("SELECTED_PRINTER");
        // if not specified, we don't want to override existing value
        if (selectedPrinter != null)
        {
            installData.setVariable("SELECTED_PRINTER", selectedPrinter);
        }
    }
}
