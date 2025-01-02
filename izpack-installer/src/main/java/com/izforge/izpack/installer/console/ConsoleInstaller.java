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

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.UserInterruptException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.base.InstallerBase;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PrivilegedRunner;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs the console installer.
 *
 * @author Mounir el hajj
 * @author Tim Anderson
 */
public class ConsoleInstaller implements InstallerBase
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ConsoleInstaller.class.getName());

    /**
     * The panels.
     */
    private final ConsolePanels panels;

    /**
     * The installation data.
     */
    private ConsoleInstallData installData;

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
     * Whether the console installation has been aborted by the user
     */
    private boolean interrupted = false;


    /**
     * Constructs a <tt>ConsoleInstaller</tt>
     *
     * @param panels              the panels
     * @param installData         the installation data
     * @param uninstallDataWriter the uninstallation data writer
     * @param console             the console
     * @param housekeeper         the house-keeper
     * @throws IzPackException for any IzPack error
     */
    public ConsoleInstaller(ConsolePanels panels, ConsoleInstallData installData,
            UninstallDataWriter uninstallDataWriter, Console console, Housekeeper housekeeper)
    {
        this.panels = panels;
        this.installData = installData;
        this.uninstallDataWriter = uninstallDataWriter;
        this.console = console;
        this.housekeeper = housekeeper;
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
     * @param type the type of the console action to perform
     * @param path the path to use for the action. May be <tt>null</tt>
     */
    public void run(ConsoleInstallerAction type, String path, String[] args)
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

        panels.initialise();

        try
        {
            action = createConsoleAction(type, path, console);
            panels.setAction(action);
            while (panels.hasNext())
            {
                success = panels.next(action.isValidating());
                if (!success)
                {
                    break;
                }
                if (action.isValidating())
                {
                    success = panels.getView().handlePanelValidationResult(success);
                    if (!success)
                    {
                        break;
                    }
                }
            }
            if (success)
            {
                if (action.isValidating())
                {
                    // last panel needs to be validated
                    success = panels.getView().handlePanelValidationResult(panels.isValid());
                }
                if (success)
                {
                    success = action.complete();
                }
            }
        }
        catch (UserInterruptException uie)
        {
            interrupted = true;
            success = false;
            console.println(uie.getMessage());
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
        final Messages messages = installData.getMessages();
        if (installData.isRebootNecessary())
        {
            console.println("[ " + messages.get("ConsoleInstaller.shutdown.pendingFileOperations") + " ]");
            switch (installData.getInfo().getRebootAction())
            {
                case Info.REBOOT_ACTION_ALWAYS:
                    reboot = true;
            }
            if (reboot)
            {
                console.println("[ " + messages.get("ConsoleInstaller.shutdown.rebootingNow") + " ]");
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
        final Messages messages = installData.getMessages();
        if (exitSuccess && !installData.isInstallSuccess())
        {
            logger.severe("Expected successful exit status, but installation data is reporting failure");
            exitSuccess = false;
        }
        installData.setInstallSuccess(exitSuccess);
        if (exitSuccess)
        {
            console.println("[ " + messages.get("ConsoleInstaller.shutdown.done") + " ]");
        }
        else
        {
            if (interrupted)
            {
                console.println("[ " + messages.get("ConsoleInstaller.shutdown.aborted") + " ]");
            }
            else
            {
                console.println("[ " + messages.get("ConsoleInstaller.shutdown.failed") + " ]");
            }
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
    private ConsoleAction createConsoleAction(ConsoleInstallerAction type, String path, Console console) throws IOException
    {
        ConsoleAction action;
        switch (type)
        {
            case CONSOLE_GEN_TEMPLATE:
                action = createGeneratePropertiesAction(path);
                break;

            case CONSOLE_FROM_TEMPLATE:
                action = createInstallFromPropertiesFileAction(path);
                break;

            case CONSOLE_FROM_SYSTEMPROPERTIES:
                action = new PropertyInstallAction(installData, uninstallDataWriter, System.getProperties());
                break;

            case CONSOLE_FROM_SYSTEMPROPERTIESMERGE:
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
            IOUtils.closeQuietly(in);
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
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public void writeInstallationRecord(File file, UninstallData uninstallData) throws Exception
    {
        panels.writeInstallationRecord(file, uninstallData);
    }
}
