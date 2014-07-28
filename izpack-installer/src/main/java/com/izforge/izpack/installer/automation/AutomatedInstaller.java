/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.installer.automation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PrivilegedRunner;

/**
 * Runs the install process in text only (no GUI) mode.
 *
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstaller implements InstallerBase
{

    /**
     * The panels.
     */
    private final AutomatedPanels panels;

    /**
     * The automated installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * Installation requirements.
     */
    private RequirementsChecker requirements;

    /**
     * Manager for writing uninstall data
     */
    private UninstallDataWriter uninstallDataWriter;

    /**
     * The supported locales.
     */
    private final Locales locales;

    /**
     * The house-keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * Constructs an <tt>AutomatedInstaller</tt>.
     *
     * @param panels              the panels
     * @param installData         the installation data
     * @param locales             the supported locales
     * @param requirements        the installation requirements checker
     * @param uninstallDataWriter the uninstallation data writer
     * @param housekeeper         the house-keeper
     */
    public AutomatedInstaller(AutomatedPanels panels, AutomatedInstallData installData, Locales locales,
                              RequirementsChecker requirements, UninstallDataWriter uninstallDataWriter,
                              Housekeeper housekeeper)
    {
        this.panels = panels;
        this.installData = installData;
        this.locales = locales;
        this.requirements = requirements;
        this.uninstallDataWriter = uninstallDataWriter;
        this.housekeeper = housekeeper;
    }

    /**
     * Initialize the automated installer.
     *
     * @param inputFilename the name of the file containing the installation data
     * @param mediaPath     the multi-volume media directory. May be <tt>null</tt>
     * @throws Exception
     */
    public void init(String inputFilename, String mediaPath) throws Exception
    {
        File input = new File(inputFilename);
        IXMLElement installRecord = getXMLData(input);
        installData.setInstallationRecord(installRecord);
        String code = installRecord.getAttribute("langpack", "eng");
        locales.setLocale(code);
        installData.setMessages(locales.getMessages());
        installData.setLocale(locales.getLocale(), locales.getISOCode());
        installData.setMediaPath(mediaPath);
    }

    /**
     * Runs the automated installation logic for each panel in turn.
     *
     * @throws Exception
     */
    public void doInstall() throws Exception
    {
        PrivilegedRunner runner = new PrivilegedRunner(installData.getPlatform());
        if (!runner.hasCorrectPermissions(installData.getInfo(), installData.getRules()))
        {
            System.out.println(installData.getMessages().get("AutomatedInstaller.permissionError"));
            System.exit(0);
        }

        boolean success = false;
        // check installer conditions
        if (!requirements.check())
        {
            System.out.println("[ Automated installation FAILED! ]");
            System.exit(-1);
            return;
        }

        // TODO: i18n
        System.out.println("[ Starting automated installation ]");

        try
        {
            List<IXMLElement> panelRoots = installData.getInstallationRecord().getChildren();
            for (IXMLElement panelRoot : panelRoots)
            {
                String panelId = panelRoot.getAttribute(AutomatedInstallData.AUTOINSTALL_PANELROOT_ATTR_ID);
                for (AutomatedPanelView panelView : panels.getPanelViews())
                {
                    if (panelView.getPanelId().equals(panelId))
                    {
                        success = panels.switchPanel(panelView.getIndex(), true);
                        break;
                    }
                }
                if (!success)
                {
                    break;
                }
            }

            if (success)
            {
                success = panels.isValid();// last panel needs to be validated
                if (uninstallDataWriter.isUninstallRequired())
                {
                    success = uninstallDataWriter.write();
                }
            }
        }
        catch (Exception e)
        {
            success = false;
            System.err.println(e.toString());
            e.printStackTrace();
        }
        finally
        {
            if (success)
            {
                System.out.println("[ Automated installation done ]");
            }
            else
            {
                System.out.println("[ Automated installation FAILED! ]");
            }

            // Bye
            // FIXME !!! Reboot handling
            boolean reboot = false;
            if (installData.isRebootNecessary())
            {
                System.out.println("[ There are file operations pending after reboot ]");
                switch (installData.getInfo().getRebootAction())
                {
                    case Info.REBOOT_ACTION_ALWAYS:
                        reboot = true;
                }
                if (reboot)
                {
                    System.out.println("[ Rebooting now automatically ]");
                }
            }
            housekeeper.shutDown(success ? 0 : 1, reboot);
        }
    }

    /**
     * Loads the xml data for the automated mode.
     *
     * @param input The file containing the installation data.
     * @return The root of the XML file.
     * @throws IOException thrown if there are problems reading the file.
     */
    private IXMLElement getXMLData(File input) throws IOException
    {
        FileInputStream in = new FileInputStream(input);

        // Initialises the parser
        IXMLParser parser = new XMLParser();
        IXMLElement rtn = parser.parse(in, input.getAbsolutePath());
        in.close();

        return rtn;
    }

    @Override
    public void writeInstallationRecord(File file) throws Exception
    {
        panels.writeInstallationRecord(file);
    }
}
