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
 *
 * This panel written by Hal Vaughan
 * http://thresholddigital.com
 * hal@thresholddigital.com
 *
 * And updated by Fabrice Mirabile
 * miraodb@hotmail.com
 */

package com.izforge.izpack.panels.datacheck;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

import java.util.logging.Logger;

/**
 * DataCheckPanel: Provide a lot of debugging information. Print a simple header of our
 * instance number and a line to separate output from other instances, then print all
 * the InstallData variables, all the packs (selected/unselected), and conditions. I hope
 * this will be expanded by others to provide needed debugging information by those
 * developing panels for IzPack.
 *
 * @author Hal Vaughan
 * @author Fabrice Mirabile
 */
public class DataCheckPanel extends IzPanel
{
    private static final long serialVersionUID = 3257848774955905587L;
    private static final Logger logger = Logger.getLogger(DataCheckPanel.class.getName());
    protected static int instanceCount = 0;
    protected String panelId;
    protected int instanceNumber;
    protected final JEditorPane panelBody;

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent frame
     * @param installData the installation data
     */
    public DataCheckPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources)
    {
        super(panel, parent, installData, resources);

        panelId = panel.getPanelId();
        instanceNumber = instanceCount++;

        BoxLayout bLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(bLayout);
        JLabel mainLabel = new JLabel(DataCheckCommon.getMainLabel(instanceNumber, panelId));
        add(mainLabel);
        JLabel subLabel = new JLabel(DataCheckCommon.SUB_LABEL);
        add(subLabel);
        panelBody = new JEditorPane();
        panelBody.setEditable(false);
        JScrollPane scrollText = new JScrollPane(panelBody);
        add(scrollText);
    }

    /**
     * When the panel is made active, display debug information.
     *
     * @see com.izforge.izpack.installer.gui.IzPanel#panelActivate()
     */
    public void panelActivate()
    {
        String packNames = DataCheckCommon.getPackNames(installData);
        String panelBodyText = DataCheckCommon.getInstallDataVariables(installData) + "\n" +
                (packNames == null ? "" : (packNames + "\n")) +
                DataCheckCommon.getConditions(installData);
        String infoToLog = DataCheckCommon.getMainLabelWithDashes(instanceNumber, panelId) + "\n" +
                DataCheckCommon.SUB_LABEL + "\n" +
                panelBodyText;

        logger.fine(infoToLog);
        panelBody.setText(panelBodyText);
        panelBody.setCaretPosition(0);
    }
}
