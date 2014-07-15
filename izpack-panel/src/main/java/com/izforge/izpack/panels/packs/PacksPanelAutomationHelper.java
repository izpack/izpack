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

package com.izforge.izpack.panels.packs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.automation.PanelAutomation;

/**
 * Functions to support automated usage of the PacksPanel
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 */
public class PacksPanelAutomationHelper implements PanelAutomation
{
    private static final Logger logger = Logger.getLogger(PacksPanelAutomationHelper.class.getName());

    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement panelRoot)
    {
        // We add each pack to the panelRoot element
        for (int i = 0; i < installData.getAvailablePacks().size(); i++)
        {
            Pack pack = installData.getAvailablePacks().get(i);
            IXMLElement packElement = new XMLElementImpl("pack", panelRoot);
            packElement.setAttribute("index", Integer.toString(i));
            packElement.setAttribute("name", pack.getName());
            Boolean selected = installData.getSelectedPacks().contains(pack);
            packElement.setAttribute("selected", selected.toString());

            panelRoot.addChild(packElement);
        }
    }

    /**
     * Asks to run in the automated mode.
     *
     * @param idata     The installation installDataGUI.
     * @param panelRoot The root of the panel installDataGUI.
     */
    @Override
    public void runAutomated(InstallData idata, IXMLElement panelRoot)
    {
        final class PInfo
        {

            private boolean _selected;

            private int _index;

            private String _name = "";

            PInfo(boolean selected, String index, String name)
            {
                _selected = selected;
                try
                {
                    _index = Integer.valueOf(index);
                }
                catch (NumberFormatException e)
                {
                    _index = -100;
                }
                if (name != null)
                {
                    _name = name;
                }
            }

            public boolean isSelected()
            {
                return _selected;
            }

            public boolean equals(int index)
            {
                return _index == index && _name.equals("");
            }

            public boolean equals(String name)
            {
                return _name.equals(name);
            }

            @Override
            public String toString()
            {
                String retVal = "";
                if (!_name.equals(""))
                {
                    retVal = "Name: " + _name + " and ";
                }
                retVal += "Index: " + String.valueOf(_index);
                return retVal;
            }
        }

        List<PInfo> autoinstallPackInfoList = new ArrayList<PInfo>();

        // We get the packs markups
        List<IXMLElement> packList = panelRoot.getChildrenNamed("pack");

        // Read all packs from the xml and remember them to merge it with the selected packs from
        // install installDataGUI
        logger.fine("Read pack list from xml definition.");
        for (IXMLElement pack : packList)
        {
            String index = pack.getAttribute("index");
            String name = pack.getAttribute("name");
            final String selectedString = pack.getAttribute("selected");
            boolean selected = selectedString.equalsIgnoreCase("true")
                    || selectedString.equalsIgnoreCase("on");
            final PInfo packInfo = new PInfo(selected, index, name);
            autoinstallPackInfoList.add(packInfo);
            logger.fine("Try to " + (selected ? "add to" : "remove from") + " selection ["
                                + packInfo.toString() + "]");
        }

        // Now merge the selected pack from automated install installDataGUI with the selected packs form
        // autoinstall.xml
        logger.fine("Modify pack selection");
        for (Pack pack : idata.getAvailablePacks())
        {
            // Check if the pack is in the List of autoinstall.xml (search by name and index)
            final int indexOfAvailablePack = idata.getAvailablePacks().indexOf(pack);
            for (PInfo packInfo : autoinstallPackInfoList)
            {
                // Check if we have a pack available that is referenced in autoinstall.xml
                if ((packInfo.equals(pack.getName())) || (packInfo.equals(indexOfAvailablePack)))
                {
                    if (pack.isRequired())
                    {
                        // Do not modify required packs
                        if (!packInfo.isSelected())
                        {
                            logger.warning("Pack [" + packInfo.toString()
                                                   + "] must be installed because it is required");
                        }
                    }
                    else
                    {
                        if (packInfo.isSelected())
                        {
                            // Check if the conditions allow to select the pack
                            RulesEngine rules = idata.getRules();
                            if (idata.getSelectedPacks().indexOf(pack) < 0
                                    && rules.canInstallPack(pack.getName(), idata.getVariables()))
                            {
                                idata.getSelectedPacks().add(pack);
                                logger.fine("Pack [" + packInfo.toString() + "] added to selection.");
                            }
                        }
                        else
                        {
                            // Pack can be removed from selection because it is not required
                            idata.getSelectedPacks().remove(pack);
                            logger.fine("Pack [" + packInfo.toString() + "] removed from selection.");

                        }
                    }
                    break;
                }
            }
        }
        // Update panelRoot to reflect the changes made by the automation helper, panel validate or panel action
        for (int counter = panelRoot.getChildrenCount(); counter > 0; counter--)
        {
            panelRoot.removeChild(panelRoot.getChildAtIndex(0));
        }
        createInstallationRecord(idata, panelRoot);
    }
}
