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

package com.izforge.izpack.installer.console;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.requirement.RequirementsChecker;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.util.file.FileUtils;

/**
 * Runs the console installer.
 *
 * @author Mounir el hajj
 * @author Tim Anderson
 */
public class ConsoleInstaller implements InstallerBase
{

    /**
     * The panels.
     */
    private final ConsolePanels panels;

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * Verifies the installation requirements.
     */
    private final RequirementsChecker requirements;

    /**
     * The uninstallation data writer.
     */
    private UninstallDataWriter uninstallDataWriter;

    /**
     * The console.
     */
    private Console console;

    /**
     * The house-keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ConsoleInstaller.class.getName());


    /**
     * Constructs a <tt>ConsoleInstaller</tt>
     *
     * @param panels              the panels
     * @param installData         the installation data
     * @param requirements        the installation requirements
     * @param uninstallDataWriter the uninstallation data writer
     * @param console             the console
     * @param housekeeper         the house-keeper
     * @throws IzPackException for any IzPack error
     */
    public ConsoleInstaller(ConsolePanels panels, AutomatedInstallData installData, RequirementsChecker requirements,
                            UninstallDataWriter uninstallDataWriter, Console console, Housekeeper housekeeper)
    {
        this.panels = panels;
        this.installData = installData;
        this.requirements = requirements;
        this.uninstallDataWriter = uninstallDataWriter;
        this.console = console;
        this.housekeeper = housekeeper;
    }

    /**
     * Determines if console installation is supported.
     *
     * @return <tt>true</tt> if there are {@link ConsolePanel} implementations for each panel
     */
    public boolean canInstall()
    {
        boolean success = true;
        for (ConsolePanelView panel : panels.getPanelViews())
        {
            if (panel.getViewClass() == null)
            {
                success = false;
                logger.warning("No console implementation of panel: " + panel.getPanel().getClassName());
            }
        }
        return success;
    }

    /**
     * Sets the media path for multi-volume installations.
     *
     * @param path the media path. May be <tt>null</tt>
     */
    public void setMediaPath(String path)
    {
        installData.setMediaPath(path);
    }

    /**
     * Runs the installation.
     * <p/>
     * This method does not return - it invokes {@code System.exit(0)} on successful installation, or
     * {@code System.exit(1)} on failure.
     *
     * @param type the type of the action to perform
     * @param path the path to use for the action. May be <tt>null</tt>
     */
    public void run(int type, String path, String[] args)
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
                console.println(installData.getMessages().get("ConsoleInstaller.permissionError"));
            }
            System.exit(0);
        }

        boolean success = false;
        ConsoleAction action = null;
        if (!canInstall())
        {
            console.println("Console installation is not supported by this installer");
            shutdown(false, false);
        }
        else
        {
            try
            {
                // refresh variables so they may be used by
                if (requirements.check())
                {
                    action = createConsoleAction(type, path, console);
                    panels.setAction(action);
                    while (panels.hasNext())
                    {
                        success = panels.next();
                        if (!success)
                        {
                            break;
                        }
                    }
                    if (success)
                    {
                        success = panels.isValid(); // last panel needs to be validated
                        if (success)
                        {
                            success = action.complete();
                        }
                    }
                }
            }
            catch (Throwable t)
            {
                success = false;
                logger.log(Level.SEVERE, t.getMessage(), t);
            }
            finally
            {
                if (action != null && action.isInstall())
                {
                    shutdown(success, console);
                }
                else
                {
                    shutdown(success, false);
                }
            }
        }
    }

    /**
     * Shuts down the installer, rebooting if necessary.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param console     the console
     */
    protected void shutdown(boolean exitSuccess, Console console)
    {
        // TODO - fix reboot handling
        boolean reboot = false;
        if (installData.isRebootNecessary())
        {
            console.println("[ There are file operations pending after reboot ]");
            switch (installData.getInfo().getRebootAction())
            {
                case Info.REBOOT_ACTION_ALWAYS:
                    reboot = true;
            }
            if (reboot)
            {
                console.println("[ Rebooting now automatically ]");
            }
        }
        shutdown(exitSuccess, reboot);
    }

    /**
     * Shuts down the installer.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param reboot      if <tt>true</tt> perform a reboot
     */
    protected void shutdown(boolean exitSuccess, boolean reboot)
    {
        if (exitSuccess && !installData.isInstallSuccess())
        {
            logger.severe("Expected successful exit status, but installation data is reporting failure");
            exitSuccess = false;
        }
        installData.setInstallSuccess(exitSuccess);
        if (exitSuccess)
        {
            console.println("[ Console installation done ]");
        }
        else
        {
            console.println("[ Console installation FAILED! ]");
        }

        terminate(exitSuccess, reboot);
    }

    /**
     * Terminates the installation process.
     *
     * @param exitSuccess if <tt>true</tt>, exits with a <tt>0</tt> exit code, else exits with a <tt>1</tt> exit code
     * @param reboot      if <tt>true</tt> perform a reboot
     */
    protected void terminate(boolean exitSuccess, boolean reboot)
    {
        housekeeper.shutDown(exitSuccess ? 0 : 1, reboot);
    }

    /**
     * Returns the console.
     *
     * @return the console
     */
    protected Console getConsole()
    {
        return console;
    }

    /**
     * Creates a new console action.
     *
     * @param type    the type of the action to perform
     * @param path    the path to use for the action. May be <tt>null</tt>
     * @param console the console
     * @return a new {@link ConsoleAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createConsoleAction(int type, String path, Console console) throws IOException
    {
        ConsoleAction action;
        switch (type)
        {
            case Installer.CONSOLE_GEN_TEMPLATE:
                action = createGeneratePropertiesAction(path);
                break;

            case Installer.CONSOLE_FROM_TEMPLATE:
                action = createInstallFromPropertiesFileAction(path);
                break;

            case Installer.CONSOLE_FROM_SYSTEMPROPERTIES:
                action = new PropertyInstallAction(installData, uninstallDataWriter, System.getProperties());
                break;

            case Installer.CONSOLE_FROM_SYSTEMPROPERTIESMERGE:
                action = createInstallFromSystemPropertiesMergeAction(path, console);
                break;

            default:
                action = createInstallAction();
        }
        return action;
    }

    /**
     * Creates a new action to perform installation.
     *
     * @return a new {@link ConsoleInstallAction}
     */
    private ConsoleAction createInstallAction()
    {
        return new ConsoleInstallAction(console, installData, uninstallDataWriter);
    }

    /**
     * Creates a new action to generate installation properties.
     *
     * @param path the property file path
     * @return a new {@link GeneratePropertiesAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createGeneratePropertiesAction(String path) throws IOException
    {
        return new GeneratePropertiesAction(installData, path);
    }

    /**
     * Creates a new action to perform installation from a properties file.
     *
     * @param path the property file path
     * @return a new {@link PropertyInstallAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createInstallFromPropertiesFileAction(String path) throws IOException
    {
        FileInputStream in = new FileInputStream(path);
        try
        {
            Properties properties = new Properties();
            properties.load(in);
            return new PropertyInstallAction(installData, uninstallDataWriter, properties);
        }
        finally
        {
            FileUtils.close(in);
        }
    }

    /**
     * Creates a new action to perform installation from a properties file.
     *
     * @param path    the property file path
     * @param console the console
     * @return a new {@link PropertyInstallAction}
     * @throws IOException for any I/O error
     */
    private ConsoleAction createInstallFromSystemPropertiesMergeAction(String path, Console console) throws IOException
    {
        FileInputStream in = new FileInputStream(path);
        try
        {
            Properties properties = new Properties();
            properties.load(in);
            Properties systemProperties = System.getProperties();
            Enumeration<?> e = systemProperties.propertyNames();
            while (e.hasMoreElements())
            {
                String key = (String) e.nextElement();
                String newValue = systemProperties.getProperty(key);
                String oldValue = (String) properties.setProperty(key, newValue);
                if (oldValue != null)
                {
                    console.println("Warning: Property " + key + " overwritten: '"
                                            + oldValue + "' --> '" + newValue + "'");
                }
            }
            return new PropertyInstallAction(installData, uninstallDataWriter, properties);
        }
        finally
        {
            FileUtils.close(in);
        }
    }

    @Override
    public void writeInstallationRecord(File file) throws Exception
    {
        panels.writeInstallationRecord(file);
    }
}
