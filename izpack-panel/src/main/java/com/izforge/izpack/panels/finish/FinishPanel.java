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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.AutomatedInstallScriptFilter;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
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
                       UninstallDataWriter uninstallDataWriter, Log log)
    {
        super(panel, parent, installData, new GridBagLayout(), resources);
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
            addLaunchApplicationCheckbox(constraints);
        }
        else
        {
            add(LabelFactory.create(getString("FinishPanel.fail"), parent.getIcons().get("stop"), LEADING),
                constraints);
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        log.informUser();
    }

    private boolean launchOnFinish = true;
    
    private void addLaunchApplicationCheckbox(GridBagConstraints constraints) {
    	if (getExecutionCommand().length == 0) return;
    	
    	JCheckBox check = new JCheckBox(getString("FinishPanel.launchAfterInstall.combo.text"));
    	check.setSelected(launchOnFinish);
    	check.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				launchOnFinish = e.getStateChange() == ItemEvent.SELECTED;
			}
		});
    	constraints.gridx = 0;
    	constraints.insets = new Insets(50, 0, 0, 0);
    	add(check, constraints);
    	Runtime.getRuntime().addShutdownHook(new Thread("Application launcher thread") {
    		@Override
    		public void run() {
    			if (launchOnFinish) {
    				runApp();
    			} else {
    				log.addDebugMessage("Skipping application launch", new String[0], Log.PANEL_TRACE, null);
    			}
    		}
    	});
	}
    
    protected void runApp() {
    	String[] cmd = getExecutionCommand();
    	String execDirVariable = installData.getVariable("FinishPanel.executableDir");
    	File execDir = execDirVariable == null ? new File("") : new File(execDirVariable);
    	try {
    		log.addDebugMessage("Executing {0} in {1}", new String[]{Arrays.toString(cmd), execDir.toString()}, Log.PANEL_TRACE, null);
	    	Runtime.getRuntime().exec(cmd, null, execDir);
    	} catch (IOException e) {
    		throw new RuntimeException("Failed to launch "+Arrays.toString(cmd)+" in "+execDir, e);
    	}
	}
    
	protected String[] getExecutionCommand() {
		Variables variables = installData.getVariables();
		SortedMap<String, String> commands = new TreeMap<String, String>();
		Set<Entry<Object, Object>> allVariables = variables.getProperties().entrySet();
		for (Entry<Object, Object> entry : allVariables) {
			if(entry.getKey().toString().startsWith("FinishPanel.executableCmd")) {
				commands.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return commands.values().toArray(new String[0]);
	}

	/**
     * Actions-handling method.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        // Prepares the file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setName(GuiId.FINISH_PANEL_FILE_CHOOSER.id);
        fileChooser.setCurrentDirectory(new File(this.installData.getInstallPath()));
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new AutomatedInstallScriptFilter(installData.getMessages()));
        // fileChooser.setCurrentDirectory(new File("."));

        // Shows it
        try
        {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                // We handle the xml installDataGUI writing
                File file = fileChooser.getSelectedFile();
                FileOutputStream out = new FileOutputStream(file);
                BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                parent.writeXMLTree(this.installData.getXmlData(), outBuff);
                outBuff.flush();
                outBuff.close();

                autoButton.setEnabled(false);
            }
        }
        catch (Exception err)
        {
            err.printStackTrace();
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
