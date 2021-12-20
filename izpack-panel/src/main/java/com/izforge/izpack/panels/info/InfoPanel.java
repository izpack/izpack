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

package com.izforge.izpack.panels.info;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.PanelHelper;

/**
 * The info panel class. Displays some raw-text informations.
 *
 * @author Julien Ponge
 */
public class InfoPanel extends IzPanel
{
    private static final long serialVersionUID = 3833748780590905399L;

    /**
     * Resource name for panel content.
     */
    private final String panelResourceName;

    /**
     * The info string.
     */
    private String info;

    /**
     * Constructs an <tt>InfoPanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public InfoPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        panelResourceName = PanelHelper.getPanelResourceName(panel, "info", resources);
        // We load the text.
        loadInfo();
        // The info label.
        add(LabelFactory.create(getString(panelResourceName), parent.getIcons().get("edit"), LEADING), NEXT_LINE);
        // The text area which shows the info.
        JTextArea textArea = new JTextArea(info);
        textArea.setName(GuiId.INFO_PANEL_TEXT_AREA.id);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        add(scroller, NEXT_LINE);
        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

    /**
     * Loads the info text.
     */
    private void loadInfo()
    {
        String defaultValue = "Error : could not load the info text !";
        info = getResources().getString(panelResourceName, defaultValue);
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
