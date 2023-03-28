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

package com.izforge.izpack.panels.userpath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.installer.automation.PanelAutomation;

import static com.izforge.izpack.panels.userpath.UserPathPanel.PATH_ELEMENT_NAME;
import static com.izforge.izpack.panels.userpath.UserPathPanel.PATH_VARIABLE_NAME;

/**
 * Functions to support automated usage of the UserPathPanel
 *
 * @author Jonathan Halliday
 * @author Julien Ponge
 * @author Jeff Gordon
 */
public class UserPathPanelAutomationHelper implements PanelAutomation
{
    public UserPathPanelAutomationHelper()
    {
    }

    @Override
    public void createInstallationRecord(InstallData idata, IXMLElement panelRoot)
    {
        // Installation path markup
        IXMLElement ipath = new XMLElementImpl(PATH_ELEMENT_NAME, panelRoot);
        // check this writes even if value is the default,
        // because without the constructor, default does not get set.
        ipath.setContent(idata.getVariable(PATH_VARIABLE_NAME));

        // Checkings to fix bug #1864
        IXMLElement prev = panelRoot.getFirstChildNamed(PATH_ELEMENT_NAME);
        if (prev != null)
        {
            panelRoot.removeChild(prev);
        }
        panelRoot.addChild(ipath);
    }

    @Override
    public void runAutomated(InstallData idata, IXMLElement panelRoot)
    {
        // We set the installation path
        IXMLElement ipath = panelRoot.getFirstChildNamed(PATH_ELEMENT_NAME);

        // Allow for variable substitution of the installpath value
        if (ipath != null)
        {
            String path = ipath.getContent();
            path = idata.getVariables().replace(path);
            idata.setVariable(PATH_VARIABLE_NAME, path);
        }
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides) {}
}
