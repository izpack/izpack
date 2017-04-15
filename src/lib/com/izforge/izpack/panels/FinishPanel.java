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

package com.izforge.izpack.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.izforge.izpack.gui.AutomatedInstallScriptFilter;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.IzPanel.Filler;
import com.izforge.izpack.util.Log;
import com.izforge.izpack.util.VariableSubstitutor;

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

    /**
     * The variables substitutor.
     */
    protected VariableSubstitutor vs;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation data.
     */
    public FinishPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new GridBagLayout());

        vs = new VariableSubstitutor(idata.getVariables());
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
        parent.setQuitButtonText(parent.langpack.getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 1;
        constraints.gridheight = 4;
        if (idata.installSuccess)
        {
            // We set the information
            JLabel jLabel = LabelFactory.create(idata.langpack.getString("FinishPanel.success"),
                    parent.icons.getImageIcon("preferences"), LEADING);
            
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.gridx = 0;
            
            Filler dummy = new Filler();
            add(dummy, constraints);
            
            add(jLabel, constraints);
            
            
            if (idata.uninstallOutJar != null)
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath(idata.info.getUninstallerPath());
               
           
                constraints.gridx = 0;
                add(LabelFactory.create(parent.langpack
                        .getString("FinishPanel.uninst.info"), parent.icons
                        .getImageIcon("preferences"), LEADING), constraints);
                

            
                constraints.gridx = 0;
                add(LabelFactory.create("  " + path, parent.icons.getImageIcon("empty"),
                        LEADING), constraints);
            }

            // We add the autoButton
            autoButton = ButtonFactory.createButton(parent.langpack.getString("FinishPanel.auto"),
                    parent.icons.getImageIcon("edit"), idata.buttonsHColor);
            autoButton.setToolTipText(parent.langpack.getString("FinishPanel.auto.tip"));
            autoButton.addActionListener(this);
            
            constraints.weighty = 1.0;   //request any extra vertical space
            constraints.insets = new Insets(40,0,0,0);  //top padding
            constraints.gridx = 0;
            add(autoButton, constraints);
 
            JCheckBox check = new JCheckBox(
                    parent.langpack.getString("FinishPanel.launchAfterInstall"));
            check.setSelected(false);
            idata.setVariable("launch_app_after_install", "off");
            check.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        idata.setVariable("launch_app_after_install", "on");
                    }
                    else
                    {
                        idata.setVariable("launch_app_after_install", "off");
                    }
                }
            });
            constraints.gridx = 0;
            constraints.insets = new Insets(50, 0, 0, 0); // add padding between the launch
                                                          // application checkbox and the
                                                          // previous
                                                          // element
            add(check, constraints);
        }
        else
        {
            add(LabelFactory.create(parent.langpack.getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("stop"), LEADING), NEXT_LINE);
            constraints.gridy++;
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        Log.getInstance().informUser();
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        // Prepares the file chooser
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(idata.getInstallPath()));
        fc.setMultiSelectionEnabled(false);
        fc.addChoosableFileFilter(new AutomatedInstallScriptFilter());
        // fc.setCurrentDirectory(new File("."));

        // Shows it
        try
        {
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                // We handle the xml data writing
                File file = fc.getSelectedFile();
                FileOutputStream out = new FileOutputStream(file);
                BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                parent.writeXMLTree(idata.xmlData, outBuff);
                outBuff.flush();
                outBuff.close();

                autoButton.setEnabled(false);
            }
        }
        catch (Exception err)
        {
            err.printStackTrace();
            JOptionPane.showMessageDialog(this, err.toString(), parent.langpack
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
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
        destination = vs.substitute(destination, null);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
