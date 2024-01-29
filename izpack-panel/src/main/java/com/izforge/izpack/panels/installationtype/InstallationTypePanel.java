/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

package com.izforge.izpack.panels.installationtype;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class InstallationTypePanel extends IzPanel implements ActionListener
{
    private static final long serialVersionUID = -8178770882900584122L;

    private static final Logger logger = Logger.getLogger(InstallationTypePanel.class.getName());

    private JRadioButton normalInstall;
    private JRadioButton modifyInstall;


    /**
     * Constructs an <tt>InstallationTypePanel</tt>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param log         the log
     */
    public InstallationTypePanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                                 Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        buildGUI();
    }

    private void buildGUI()
    {
        // We put our components

        add(LabelFactory.create(getString("InstallationTypePanel.info"), parent.getIcons().get("history"), LEADING),
            NEXT_LINE);


        ButtonGroup group = new ButtonGroup();

        boolean modifyInstallation = Boolean.parseBoolean(installData.getVariable(InstallData.MODIFY_INSTALLATION));

        normalInstall = new JRadioButton(getString("InstallationTypePanel.normal"), !modifyInstallation);
        normalInstall.setName(GuiId.INSTALLATION_TYPE_NORMAL.id);
        normalInstall.addActionListener(this);
        group.add(normalInstall);
        add(normalInstall, NEXT_LINE);

        modifyInstall = new JRadioButton(getString("InstallationTypePanel.modify"), modifyInstallation);
        modifyInstall.setName(GuiId.INSTALLATION_TYPE_MODIFY.id);
        modifyInstall.addActionListener(this);
        group.add(modifyInstall);
        add(modifyInstall, NEXT_LINE);

        setInitialFocus(normalInstall);
        getLayoutHelper().completeLayout();
    }

    /* (non-Javadoc)
    * @see com.izforge.izpack.installer.IzPanel#panelActivate()
    */
    @Override
    public void panelActivate()
    {
        boolean modifyInstallation = Boolean.parseBoolean(
                this.installData.getVariable(InstallData.MODIFY_INSTALLATION));
        if (modifyInstallation)
        {
            modifyInstall.setSelected(true);
        }
        else
        {
            normalInstall.setSelected(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == normalInstall)
        {
            logger.fine("Installation type: Normal installation");
            this.installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else
        {
            logger.fine("Installation type: Modification installation");
            this.installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        }
    }

    @Override
    public void createInstallationRecord(IXMLElement rootElement) {
        new InstallationTypePanelAutomation().createInstallationRecord(installData, rootElement);
    }

    @Override
    public String getSummaryBody()
    {
        boolean modifyInstallation = Boolean.parseBoolean(
                this.installData.getVariable(InstallData.MODIFY_INSTALLATION));
        return modifyInstallation ? getString("InstallationTypePanel.modify") : getString("InstallationTypePanel.normal");
    }
}
