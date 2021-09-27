/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jan Blok
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

package com.izforge.izpack.panels.sudo;

import static com.izforge.izpack.util.Platform.Name.UNIX;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * The packs selection panel class.
 *
 * @author Jan Blok - Pier Paolo Ucchino
 * @since November 27, 2003 - November 29, 2019
 */
public class SudoPanel extends IzPanel implements ActionListener
{
	/**
     * The logger.
     */
    private static Logger logger = Logger.getLogger(SudoPanel.class.getName());

    /**
     *
     */
    private static final long serialVersionUID = 3689628116465561651L;

    private final JTextField passwordField;

    private boolean isValid = false;

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window.
     * @param installData the installation data
     * @param resources   the resources
     * @param replacer    the variable replacer
     * @param matcher     the platform-model matcher
     */
    public SudoPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                     VariableSubstitutor replacer, PlatformModelMatcher matcher)
    {
        super(panel, parent, installData, resources);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(LabelFactory.create(getString("SudoPanel.info"),JLabel.TRAILING));

        add(Box.createRigidArea(new Dimension(0, 5)));

        add(LabelFactory.create(getString("SudoPanel.tip"),parent.getIcons().get("tip"), JLabel.TRAILING));

        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel spacePanel = new JPanel();
        spacePanel.setAlignmentX(LEFT_ALIGNMENT);
        spacePanel.setAlignmentY(CENTER_ALIGNMENT);
        spacePanel.setBorder(BorderFactory.createEmptyBorder(80, 30, 0, 50));
        spacePanel.setLayout(new BorderLayout(5, 5));
        spacePanel.add(LabelFactory.create(getString("SudoPanel.specifyAdminPassword")),BorderLayout.NORTH);
        passwordField = new JTextField();
        passwordField.addActionListener(this);
        JPanel space2Panel = new JPanel();
        space2Panel.setLayout(new BorderLayout());
        space2Panel.add(passwordField, BorderLayout.NORTH);
        space2Panel.add(Box.createRigidArea(new Dimension(0, 5)), BorderLayout.CENTER);
        spacePanel.add(space2Panel, BorderLayout.CENTER);
        add(spacePanel);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        passwordField.requestFocus();
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        doSudoCmd();
    }

    // check if sudo password is correct (so sudo can be used in all other
    // scripts, even without password, lasts for 5 minutes)

    private void doSudoCmd()
    {
        Platform platform = installData.getPlatform();
        
        String pass = passwordField.getText();

        isValid = false;
        
        try
        {
            if(platform.isA(UNIX)) 
            {
                isValid = checkUnixRootPassByCommandline(pass);

                if (isValid)
                {
                    installData.setVariable("password", pass);
                }
            }
        }
        catch (Exception e)
        {
        	logger.log(Level.WARNING, e.getMessage(), e);
            isValid = false;
        }
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Always true.
     */
    public boolean isValidated()
    {
        if (!isValid)
        {
            doSudoCmd();
        }
        if (!isValid)
        {
            JOptionPane.showMessageDialog(this,getString("SudoPanel.invalidPassword"),"Error",JOptionPane.ERROR_MESSAGE);
        }
        return isValid;
    }
    
    private static List<String> getSudoCommandline(String password, String command) {
        if(password == null) password = "";
        if(command == null) command = "";

        List<String> cmd = new ArrayList<String>();
        
        cmd.add("/bin/bash");
        cmd.add("-c");
        cmd.add("echo " + password + " | sudo -S " + command.trim());
        
        return cmd;
    }

    public static void execCmdAuthByCommandline(String password,String commandString) throws Exception {
        List<String> command = getSudoCommandline(password,commandString);

        get(command);
    }

    public static boolean checkUnixRootPassByCommandline(String password) throws Exception {
        boolean root = false;

        String dir = "/test_root_" + new Date().getTime();

        execCmdAuthByCommandline(password, "mkdir " + dir);

        if (new File(dir).exists() == true) {
            execCmdAuthByCommandline(password, "rm -rf " + dir);
            root = true;
        }

        return root;
    }   

    public static int get(List<String> command) throws Exception {

        int result = -1;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();

            result = processBuilder.start().waitFor();
        } catch (Exception reason) {
            throw new Exception("Problem executing command",reason);
        }
        return result;
    }     
}
