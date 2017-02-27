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

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * DataCheckPanel: Provide a lot of debugging information. Print a simple header
 * of our instance number and a line to separate output from other instances,
 * then print all the GUIInstallData variables and list all the packs and
 * selected packs. I hope this will be expanded by others to provide needed
 * debugging information by those developing panels for IzPack.
 *
 * @author Hal Vaughan
 * @author Fabrice Mirabile
 */
public class DataCheckPanel extends IzPanel
{

    private static final long serialVersionUID = 3257848774955905587L;

    static int instanceCount = 0;

    protected int instanceNumber = 0;

    private GUIInstallData installDataGUI;

    JEditorPane staticText;

    /**
     * The constructor.
     *
     * @param panel the panel meta-data
     * @param parent the parent frame
     * @param installData the installation data
     */
    public DataCheckPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources)
    {
        super(panel, parent, installData, resources);

        installDataGUI = installData;
        instanceNumber = instanceCount++;

        String sInfo = "Debugging installDataGUI.  All GUIInstallData variables and all packs (selected packs are marked).";
        BoxLayout bLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(bLayout);
//        setLayout(new GridLayout(3,1));
        JLabel lInfo = new JLabel(sInfo);
        add(lInfo);
        staticText = new JEditorPane();
        staticText.setEditable(false);
        JScrollPane scrollText = new JScrollPane(staticText);
        add(new JLabel("  "));
        add(scrollText);

    }

    /**
     * When the panel is made active, call the printDebugInfo method.
     *
     * @see com.izforge.izpack.installer.gui.IzPanel#panelActivate()
     */
    public void panelActivate()
    {
        printDebugInfo();
    }

    /**
     * Get and return the list of pack names.
     *
     * @param packList
     * @return String
     */
    private String getPackNames(List<Pack> packList)
    {
        String pStatus;
        String sOutput = "";
        Pack iPack;
        for (int i = 0; i < packList.size(); i++)
        {
            iPack = packList.get(i);
            if (installDataGUI.getSelectedPacks().indexOf(iPack) != -1)
            {
                pStatus = "Selected";
            }
            else
            {
                pStatus = "Unselected";
            }
            sOutput = sOutput + "\t" + i + ": " + iPack.getName() + " (" + pStatus + ")\n";
        }
        return sOutput;
    }

    /**
     * Print list of variables names and value, as well as the list of packages
     * and their status (selected or not).
     */
    private void printDebugInfo()
    {
        int i = 0;
        String sInfo = "GUIInstallData Variables:\n";
        System.out.println("------------------------Data Check Panel Instance "
                + instanceNumber + "------------------------");
        System.out.println("GUIInstallData Variables:");
        Properties varList = installDataGUI.getVariables().getProperties();
        String[] alphaName = new String[varList.size()];
        Enumeration<String> varNames = (Enumeration<String>) varList.propertyNames();
        while (varNames.hasMoreElements())
        {
            alphaName[i++] = varNames.nextElement();
        }
        java.util.Arrays.sort(alphaName);
        for (i = 0; i < alphaName.length; i++)
        {
            sInfo = sInfo + "\tName: " + alphaName[i] + ", Value: " + varList.getProperty(alphaName[i]) + "\n";
        }
        sInfo = sInfo + "\nAvailable Packs: \n" + getPackNames(installDataGUI.getAllPacks()) + "\n";
        System.out.println(sInfo);
        staticText.setText(sInfo);
    }

    /**
     * By nature, always true.
     *
     * @return True
     */
    public boolean isValidated()
    {
        return true;
    }
}
