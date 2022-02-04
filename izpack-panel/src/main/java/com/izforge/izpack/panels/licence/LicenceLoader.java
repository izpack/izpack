/*
 * IzPack - Copyright 2001-2017 The IzPack project team.
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
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.util.PanelHelper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Provides shared licence loading logic between console and GUI panels.
 * <p>
 *     A licence resource file is identified by its resource name plus a suffix.
 *     The resource name is built from the simple class name of the IzPanel class
 *     related to the given {@code panelClass}. The suffix is built from the
 *     identifier of the given {@code panel}, if available. Otherwise a default
 *     suffix of {@code "licence"} is used.
 * </p>
 *
 * @author Michael Aichler
 */
class LicenceLoader {
    private final Panel panel;
    private final Resources resources;

    /**
     * Resource name for panel content.
     */
    private final String panelResourceName;

    /**
     * Creates a new licence loader.
     *
     * @param panel The panel metadata (needed for the panel identifier).
     * @param resources The resource locator.
     */
    LicenceLoader(Panel panel, Resources resources) {
        this.panel = panel;
        this.resources = resources;
        panelResourceName = PanelHelper.getPanelResourceName(panel, "licence", resources);
    }

    /**
     * Loads the licence as a URL.
     *
     * @return The URL to the resource.
     * @throws ResourceException If the related IzPanel or the licence resource
     *      cannot be found. The generated error message is ready to be logged.
     */
    URL asURL() throws ResourceException
    {
        try
        {
            return resources.getURL(panelResourceName);
        }
        catch (ResourceNotFoundException ignored)
        {
            String panelClassName = panel.getClassName();
            String panelName = panelClassName.substring(panelClassName.lastIndexOf('.') + 1);
            String message = "Could not open license document for the resource id " + panelResourceName +
                    " of the panel " + panelName;
            throw new ResourceNotFoundException(message);
        }
    }

    /**
     * Loads the licence into a string using UTF-8 as encoding.
     *
     * @return A string representation of the licence.
     */
    String asString()
    {
        return resources.getString(panelResourceName,
                "Could not open license document for the resource id " + panelResourceName);
    }
}
