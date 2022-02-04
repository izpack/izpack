/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.PanelHelper;

import java.awt.LayoutManager2;
import java.net.URL;
import java.util.logging.Logger;

public abstract class AbstractLicencePanel extends IzPanel
{
    private static final long serialVersionUID = 1483930095144726447L;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractLicencePanel.class.getName());

    /**
     * The shared licence loader.
     */
    private final transient LicenceLoader licenceLoader;

    protected final String titleMessageKey;

    public AbstractLicencePanel(Panel panel, InstallerFrame parent,
                                GUIInstallData installData, LayoutManager2 layoutManager,
                                Resources resources)
    {
        super(panel, parent, installData, layoutManager, resources);
        licenceLoader = new LicenceLoader(panel, resources);
        titleMessageKey = PanelHelper.getPanelTitleMessageKey(panel, "info", installData);
    }

    /**
     * Loads the license document URL.
     *
     * @return The license text URL.
     */
    protected URL loadLicence()
    {
        try
        {
            return licenceLoader.asURL();
        }
        catch (ResourceException e)
        {
            logger.warning(e.getMessage());
            return null;
        }
    }

    protected String loadLicenceAsString()
    {
        return licenceLoader.asString();
    }
}
