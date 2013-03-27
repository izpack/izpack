/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

package com.izforge.izpack.panels.htmlinfo;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractTextConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of {@link HTMLInfoPanel}.
 *
 * @author Tim Anderson
 */
public class HTMLInfoConsolePanel extends AbstractTextConsolePanel
{
    private final Resources resources;
    private final String panelId;
    private final String resourcePrefix;

    public HTMLInfoConsolePanel(PanelView<Console> panel, Resources resources)
    {
        this(panel, resources, "HTMLInfoPanel");
    }

    public HTMLInfoConsolePanel(PanelView<Console> panel, Resources resources, String resourcePrefix)
    {
        super(panel);
        panelId = panel.getPanelId();
        this.resources = resources;
        this.resourcePrefix = resourcePrefix;
    }

    /**
     * Runs the panel using the specified console.
     * <p/>
     * If there is no text to display, the panel will return {@code false}.
     *
     * @param installData the installation data
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        console.println(installData.getMessages().get("InfoPanel.info"));
        return super.run(installData, console);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A {@code null} indicates failure
     */
    @Override
    protected String getText()
    {
        String result = null;
        if (panelId != null)
        {
            result = resources.getString(resourcePrefix + "." + panelId, null);
        }
        if (result == null)
        {
            result = resources.getString(resourcePrefix + ".info", null);
        }
        if (result != null)
        {
            result = removeHTML(result);
        }
        return result;
    }

}
