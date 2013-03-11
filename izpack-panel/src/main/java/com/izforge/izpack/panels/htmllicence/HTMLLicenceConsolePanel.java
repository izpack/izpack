/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.htmllicence;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.panels.licence.AbstractLicenseConsolePanel;

/**
 * HTML License Panel console helper
 */
public class HTMLLicenceConsolePanel extends AbstractLicenseConsolePanel
{

    /**
     * Constructs an <tt>HTMLLicenceConsolePanel</tt>.
     *
     * @param resources the resources
     */
    public HTMLLicenceConsolePanel(Resources resources)
    {
        super(resources);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A <tt>null</tt> indicates failure
     */
    @Override
    protected String getText()
    {
        String text = getText("HTMLLicencePanel.licence");
        if (text != null)
        {
            text = removeHTML(text);
        }
        return text;
    }

}
