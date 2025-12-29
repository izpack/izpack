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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PrivilegedRunner;
import jakarta.enterprise.inject.Vetoed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

/**
 * Runs the install process in text only (no GUI) mode.
 *
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
@Vetoed
public class AutomatedInstaller implements InstallerBase
{

    /**
     * The panels.
     */
    private final AutomatedPanels panels;

    /**
     * The automated installation data.
     */
    private final InstallData installData;

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
    public AutomatedInstaller(AutomatedPanels panels, InstallData installData, Locales locales,
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
    public void init(String inputFilename, String mediaPath, String[] args) throws Exception
    {
        PrivilegedRunner runner = new PrivilegedRunner(installData.getPlatform());
        if (!runner.hasCorrectPermissions(installData.getInfo(), installData.getRules()))
        {
            try
            {
                runner.relaunchWithElevatedRights(args);
            }
            catch (Exception e)
            {
                System.out.println(installData.getMessages().get("AutomatedInstaller.permissionError"));
            }
            System.exit(0);
        }
        if (inputFilename != null)
        {
            File input = new File(inputFilename);
            IXMLElement installRecord = getXMLData(input);
            installData.setInstallationRecord(installRecord);
            String code = installRecord.getAttribute("langpack", "eng");
            locales.setLocale(code);
        }

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
            IXMLElement installationRecord = installData.getInstallationRecord();
            if (installationRecord != null && installationRecord.hasChildren())
            {
                List<IXMLElement> panelRoots = installationRecord.getChildren();
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
            }
            else
            {
                //List<AutomatedPanelView> panelViews = panels.getPanelViews();
                while (panels.hasNext())
                {
                    success = panels.next();
                    if (!success)
                    {
                        break;
                    }
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
        IXMLElement rtn;
        FileInputStream in = new FileInputStream(input);
        try
        {
            // Initialises the parser
            // TODO: Create an XSD for auto-install files and activate validation here
            XMLParser parser = new XMLParser(false);
            InputStream xsl = getClass().getResourceAsStream("/resources/AutomatedInstallation.xsl");
            if (xsl == null)
            {
                rtn = parser.parse(in, input.getAbsolutePath());
            }
            else
            {
                rtn = parser.parse(in, input.getAbsolutePath(), new StreamSource(xsl));
            }
        }
        finally
        {
            in.close();
        }
        return rtn;
    }

    @Override
    public void writeInstallationRecord(File file, UninstallData uninstallData) throws Exception
    {
        panels.writeInstallationRecord(file, uninstallData);
    }
}
