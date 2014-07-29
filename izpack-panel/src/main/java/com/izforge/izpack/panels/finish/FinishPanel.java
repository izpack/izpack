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

package com.izforge.izpack.panels.finish;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.AutomatedInstallScriptFilter;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * The finish panel class.
 *
 * @author Julien Ponge
 */
public class FinishPanel extends IzPanel implements ActionListener
{

    private static final long serialVersionUID = 3257282535107998009L;

    /**
     * The automated installers generation button.
     */
    protected JButton autoButton;

    private UninstallDataWriter uninstallDataWriter;

    private UninstallData uninstallData;

    /**
     * The log.
     */
    private final Log log;

    /**
     * Constructs a <tt>FinishPanel</tt>.
     *
     * @param panel               the panel meta-data
     * @param parent              the parent window
     * @param installData         the installation data
     * @param resources           the resources
     * @param uninstallDataWriter the uninstallation data writer
     * @param log                 the log
     */
    public FinishPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                       UninstallDataWriter uninstallDataWriter, UninstallData uninstallData, Log log)
    {
        super(panel, parent, installData, new GridBagLayout(), resources);
        this.uninstallData = uninstallData;
        this.uninstallDataWriter = uninstallDataWriter;
        this.log = log;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the panel has been validated.
     */
    @Override
    public boolean isValidated()
    {
        return true;
    }

    @Override
    public void panelActivate()
    {
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        Insets inset = new Insets(10, 20, 2, 2);
        GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
                                                                GridBagConstraints.CENTER, inset, 0, 0);
        if (this.installData.isInstallSuccess())
        {
            // We set the information
            JLabel jLabel = LabelFactory.create(getString("FinishPanel.success"), parent.getIcons().get("preferences"),
                                                LEADING);
            jLabel.setName(GuiId.FINISH_PANEL_LABEL.id);
            add(jLabel, constraints);
            constraints.gridy++;
            if (uninstallDataWriter.isUninstallRequired())
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath("$INSTALL_PATH") + File.separator + "Uninstaller";

                add(LabelFactory.create(getString("FinishPanel.uninst.info"), parent.getIcons()
                        .get("preferences"), LEADING), constraints);
                constraints.gridy++;
                add(LabelFactory.create(path, parent.getIcons().get("empty"),
                                        LEADING), constraints);
                constraints.gridy++;
            }
            // We add the autoButton
            autoButton = ButtonFactory.createButton(getString("FinishPanel.auto"),
                                                    parent.getIcons().get("edit"), this.installData.buttonsHColor);
            autoButton.setName(GuiId.FINISH_PANEL_AUTO_BUTTON.id);
            autoButton.setToolTipText(getString("FinishPanel.auto.tip"));
            autoButton.addActionListener(this);
            add(autoButton, constraints);
            constraints.gridy++;
        }
        else
        {
            add(LabelFactory.create(getString("FinishPanel.fail"), parent.getIcons().get("stop"), LEADING),
                constraints);
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        log.informUser();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Prepares the file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName(GuiId.FINISH_PANEL_FILE_CHOOSER.id);
        fileChooser.setCurrentDirectory(new File(this.installData.getInstallPath()));
        fileChooser.setSelectedFile(new File(this.installData.getInstallPath(), "auto-install.xml"));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new AutomatedInstallScriptFilter(installData.getMessages()));

        try
        {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                // We handle the xml installDataGUI writing
                File file = fileChooser.getSelectedFile();
                parent.writeInstallationRecord(file, uninstallData);
                autoButton.setEnabled(false);
            }
        }
        catch (Exception err)
        {
            JOptionPane.showMessageDialog(this, err.toString(), getString("installer.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Translates a relative path to a local system path.
     *
     * @param destination The path to translate.
     * @return The translated path.
     */
    protected String translatePath(String destination)
    {
        // Parse for variables
        destination = installData.getVariables().replace(destination);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
