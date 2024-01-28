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
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of {@link HTMLInfoPanel}.
 *
 * @author Tim Anderson
 */
public class HTMLInfoConsolePanel extends AbstractTextConsolePanel
{
    private final Resources resources;
    private final String panelResourceName;

    public HTMLInfoConsolePanel(PanelView<ConsolePanel> panel, Resources resources)
    {
        super(panel);
        this.resources = resources;
        panelResourceName = PanelHelper.getPanelResourceName(panel.getPanel(), "info", resources);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A {@code null} indicates failure
     */
    @Override
    protected String getText()
    {
        String text = resources.getString(panelResourceName, null);
        if (text != null)
        {
            return removeHTML(text);
        }
        return null;
    }
}
