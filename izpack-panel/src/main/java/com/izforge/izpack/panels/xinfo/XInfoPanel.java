/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.panels.xinfo;

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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;

/**
 * The XInfo panel class - shows some adaptative text (ie by parsing for some variables.
 *
 * @author Julien Ponge
 */
public class XInfoPanel extends IzPanel
{
    private static final long serialVersionUID = 3257009856274970416L;

    /**
     * Resource name for panel content.
     */
    private final String panelResourceName;

    /**
     * The text area.
     */
    private JTextArea textArea;

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public XInfoPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        panelResourceName = PanelHelper.getPanelResourceName(panel, "info", resources);
        String panelName = PanelHelper.getPanelName(panel);

        // The info label.
        add(LabelFactory.create(getString(panelResourceName), parent.getIcons().get("edit"), LEADING), NEXT_LINE);
        // The text area which shows the info.
        textArea = new JTextArea();
        textArea.setName(panelName.equals("XInfoPanel") ? GuiId.XINFO_PANEL_TEXT_AREA.id : GuiId.INFO_PANEL_TEXT_AREA.id);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);
        String textAreaFont = installData.getVariable(panelName + ".font");
        if (textAreaFont != null && textAreaFont.length() > 0)
        {
            Font font = Font.decode(textAreaFont);
            textArea.setFont(font);
        }
        JScrollPane scroller = new JScrollPane(textArea);
        add(scroller, NEXT_LINE);
        // At end of layouting we should call the completeLayout method also they do nothing.
        getLayoutHelper().completeLayout();
    }

    /**
     * Loads the info text.
     */
    private String getInfoText()
    {
        String infoText = getResources().getString(panelResourceName, null, "Error : could not load the infoText text !");
        if (substituteVariables()) {
            // Parses the infoText text and substitute variables
            infoText = installData.getVariables().replace(infoText);
        }
        return infoText;
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        textArea.setText(getInfoText());
        textArea.setCaretPosition(0);
    }

    /**
     * Indicates whether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        return true;
    }

    /**
     * Returns true if variables are to be substituted in the text or else false.
     *
     * @return true, the default implementation
     */
    protected boolean substituteVariables() {
        return true;
    }
}
