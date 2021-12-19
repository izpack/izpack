/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.htmlhello;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.htmlinfo.HTMLInfoPanel;

/**
 * Class HTMLHelloPanel is a version of the hello panel that displays
 * HTML content.
 */
public class HTMLHelloPanel extends HTMLInfoPanel
{
    private static final long serialVersionUID = -7671648991830935148L;

    /**
     * Constructs an <tt>HTMLHelloPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public HTMLHelloPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, false, resources, log);
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }
}
