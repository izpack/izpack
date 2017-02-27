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
package com.izforge.izpack.panels.simplefinish;

import java.io.File;

import javax.swing.JLabel;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * The simple finish panel class.
 *
 * @author Julien Ponge
 */
public class SimpleFinishPanel extends IzPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 3689911781942572085L;
    private UninstallDataWriter uninstallDataWriter;

    /**
     * The log.
     */
    private final Log log;

    /**
     * Constructs a <tt>SimpleFinishPanel</tt>.
     *
     * @param panel the panel meta-data
     * @param parent the parent window
     * @param installData the installation data
     * @param resources the resources
     * @param uninstallDataWriter the uninstallation data writer
     * @param log the log
     */
    public SimpleFinishPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
            UninstallDataWriter uninstallDataWriter, Log log)
    {
        super(panel, parent, installData, new IzPanelLayout(log), resources);
        this.uninstallDataWriter = uninstallDataWriter;
        this.log = log;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the panel has been validated.
     */
    public boolean isValidated()
    {
        return true;
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        if (this.installData.isInstallSuccess())
        {

            // We set the information
            add(LabelFactory.create(parent.getIcons().get("check")));
            add(IzPanelLayout.createVerticalStrut(5));
            JLabel jLabel = LabelFactory.create(getString("FinishPanel.success"),
                    parent.getIcons().get("preferences"), LEADING);
            jLabel.setName(GuiId.SIMPLE_FINISH_LABEL.id);
            add(jLabel, NEXT_LINE);
            add(IzPanelLayout.createVerticalStrut(5));
            if (uninstallDataWriter.isUninstallRequired())
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath(installData.getInfo().getUninstallerPath());

                JLabel uninstallJLabel = LabelFactory.create(getString("FinishPanel.uninst.info"),
                        parent.getIcons().get("preferences"), LEADING);
                uninstallJLabel.setName(GuiId.SIMPLE_FINISH_UNINSTALL_LABEL.id);
                add(uninstallJLabel, NEXT_LINE);
                add(LabelFactory.create(path, parent.getIcons().get("empty"),
                        LEADING), NEXT_LINE);
            }
        }
        else
        {
            add(LabelFactory.create(getString("FinishPanel.fail"),
                    parent.getIcons().get("stop"), LEADING));
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        log.informUser();
    }

    /**
     * Translates a relative path to a local system path.
     *
     * @param destination The path to translate.
     * @return The translated path.
     */
    private String translatePath(String destination)
    {
        // Parse for variables
        destination = installData.getVariables().replace(destination);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
