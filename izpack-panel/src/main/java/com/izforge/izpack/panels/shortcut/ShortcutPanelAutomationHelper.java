/*
 * $Id: copyright-notice-template 1421 2006-03-12 16:32:32Z jponge $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Marc Eppelmann (marc.eppelmann&#064;gmx.de)
 * Copyright 2010 Florian Buehlmann
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
package com.izforge.izpack.panels.shortcut;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.TargetFactory;

/**
 * The ShortcutPanelAutomationHelper is responsible to create Shortcuts during the automated
 * installation.
 *
 * @author Marc Eppelmann (marc.eppelmann&#064;gmx.de)
 * @version $Revision: 1540 $
 */
public class ShortcutPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation
{
    private ShortcutPanelLogic shortcutPanelLogic;

    /**
     * Constructs a <tt>ShortcutPanel</tt>.
     *
     * @param installData   the installation data
     * @param resources     the resources
     * @param uninstallData the uninstallation data
     * @param housekeeper   the house keeper
     * @param factory       the factory for platform-specific implementations
     * @param matcher       the platform-model matcher
     * @throws Exception for any error
     */
    public ShortcutPanelAutomationHelper(AutomatedInstallData installData, Resources resources,
                                         UninstallData uninstallData, Housekeeper housekeeper, TargetFactory factory,
                                         InstallerListeners listeners, PlatformModelMatcher matcher) throws Exception
    {
        shortcutPanelLogic = new ShortcutPanelLogic(installData, resources, uninstallData,
                                                    housekeeper, factory, listeners, matcher);
    }
    public ShortcutPanelAutomationHelper(ShortcutPanelLogic shortcutPanelLogic) throws Exception
    {
        this.shortcutPanelLogic = shortcutPanelLogic;
    }

    /**
     * Create the xml configuration content for automatic installation. Normally this method is not
     * used because we are in an automatic installation step.
     *
     * @param installData Installation data
     * @param panelRoot   panel specific data for autoinstall.xml
     */
    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement panelRoot)
    {
        for (IXMLElement element : shortcutPanelLogic.getAutoinstallXMLData(panelRoot))
        {
            panelRoot.addChild(element);
        }
    }

    /**
     * Implementation of the Shortcut specific automation code.
     *
     * @param installData Installation data
     * @param panelRoot   panel specific data from autoinstall.xml
     */
    @Override
    public void runAutomated(InstallData installData, IXMLElement panelRoot)
    {
        shortcutPanelLogic.setAutoinstallXMLData(panelRoot);
        if (shortcutPanelLogic.isCreateShortcutsImmediately())
        {
            try
            {
                shortcutPanelLogic.refreshShortcutData();
                shortcutPanelLogic.createAndRegisterShortcuts();
            }
            catch (Exception e)
            {
                // ignore exception
            }
        }
    }
}
