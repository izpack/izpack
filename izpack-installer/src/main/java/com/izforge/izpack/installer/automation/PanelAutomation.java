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

package com.izforge.izpack.installer.automation;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.InstallerException;

/**
 * Defines the Interface that must be implemented for running Panels in automated (or "silent",
 * "headless") install mode.
 * <p/>
 * Implementing classes MUST NOT link against awt/swing classes. Thus the Panels cannot implement
 * this interface directly, they should use e.g. helper classes instead.
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 * @see AutomatedInstaller
 */
public interface PanelAutomation
{

    /**
     * Creates an installation record for unattended installations and adds it to a XML root element.
     *
     * @param installData The installation data
     * @param rootElement The root element to add panel-specific child elements to
     */
    void createInstallationRecord(InstallData installData, IXMLElement rootElement);

    /**
     * Makes the panel work in automated mode. Default is to do nothing, but any panel doing
     * something 'effective' during the installation process should implement this method.
     *
     * @param installData The installation data
     * @param panelRoot   The XML root element of the panels blackbox tree.
     * @throws com.izforge.izpack.api.exception.InstallerException
     *          if the automated work  failed critically.
     */
    void runAutomated(InstallData installData, IXMLElement panelRoot) throws InstallerException;
}
