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

package com.izforge.izpack.installer.data;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.util.Platform;

/**
 * Encloses information about the install process.
 *
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class GUIInstallData extends InstallData implements Serializable
{

    private static final long serialVersionUID = 4048793450990024505L;

    /**
     * The GUI preferences.
     */
    public GUIPrefs guiPrefs;

    /**
     * The buttons highlighting color.
     */
    public Color buttonsHColor = new Color(230, 230, 230);

    /**
     * The panels list.
     */
    private List<IzPanel> panels = new ArrayList<IzPanel>();


    public GUIInstallData(Variables variables, Platform platform)
    {
        super(variables, platform);
    }

    public void configureGuiButtons()
    {
        Messages messages = getMessages();
        UIManager.put("OptionPane.yesButtonText", messages.get("installer.yes"));
        UIManager.put("OptionPane.noButtonText", messages.get("installer.no"));
        UIManager.put("OptionPane.cancelButtonText", messages.get("installer.cancel"));
    }

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    public List<IzPanel> getPanels()
    {
        return panels;
    }
}
