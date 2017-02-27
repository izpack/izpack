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
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

public abstract class AbstractLicencePanel extends IzPanel
{

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractLicencePanel.class.getName());

    private static final String DEFAULT_SUFFIX = ".licence";
    private static final long serialVersionUID = 1483930095144726447L;

    public AbstractLicencePanel(Panel panel, InstallerFrame parent, GUIInstallData installData, LayoutManager2 layoutManager, Resources resources)
    {
        super(panel, parent, installData, layoutManager, resources);
    }

    /**
     * Loads the license document URL.
     *
     * @return The license text URL.
     */
    protected URL loadLicence()
    {
        final String resNamePrefix = getClass().getSimpleName();
        String resNameStr = resNamePrefix + DEFAULT_SUFFIX;

        Panel panel = getMetadata();
        Resources resources = getResources();
        URL url = null;
        if (panel != null)
        {
            String panelId = panel.getPanelId();
            if (panelId != null)
            {
                String panelSpecificResName = resNamePrefix + '.' + panelId;
                try
                {
                    url = resources.getURL(panelSpecificResName);
                }
                catch (ResourceNotFoundException e)
                {
                    try
                    {
                        url = resources.getURL(resNameStr);
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }
        }

        if (url == null)
        {
            String panelId = panel != null ? panel.getPanelId() : null;
            LOGGER.warning("Cannot open any of the possible license document resources ("
                    + (panelId != null ? resNamePrefix + '.' + panelId + ", " : "")
                    + resNamePrefix + DEFAULT_SUFFIX
                    + ") for panel type '" + resNamePrefix + "");
        }
        return url;
    }

    protected String loadLicenceAsString()
    {
        return loadLicenceAsString("UTF-8");
    }

    protected String loadLicenceAsString(final String encoding)
    {
        URL url = null;
        String result = null;
        try
        {
            url = loadLicence();

            InputStream in = url.openStream();
            try
            {
                result = IOUtils.toString(in, Charsets.toCharset(encoding));
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }
        }
        catch (IOException e)
        {
            LOGGER.warning("Cannot convert license document from resource " + url.getFile() + " to text: " + e.getMessage());
        }
        return result;
    }
}
