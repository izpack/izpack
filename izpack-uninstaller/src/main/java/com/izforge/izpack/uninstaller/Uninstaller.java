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

package com.izforge.izpack.uninstaller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.izforge.izpack.core.resource.DefaultResources;
import com.izforge.izpack.uninstaller.console.ConsoleUninstaller;
import com.izforge.izpack.uninstaller.console.ConsoleUninstallerContainer;
import com.izforge.izpack.uninstaller.container.UninstallerContainer;
import com.izforge.izpack.uninstaller.gui.GUIUninstallerContainer;
import com.izforge.izpack.uninstaller.gui.UninstallerFrame;
import com.izforge.izpack.uninstaller.resource.InstallLog;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.util.SelfModifier;

/**
 * The uninstaller class.
 *
 * @author Julien Ponge
 * @author Tim Anderson
 */
public class Uninstaller
{
    /**
     * The exec-admin resource path.
     */
    private static final String EXEC_ADMIN = "/exec-admin";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Uninstaller.class.getName());


    /**
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command line.
     */
    public static void main(String[] args)
    {
        // relaunch the uninstaller with elevated permissions if required
        Platform platform = new Platforms().getCurrentPlatform();

        try
        {
            if (!PrivilegedRunner.isPrivilegedMode() && isElevationRequired(platform))
            {
                if (relaunchWithElevatedRights(platform, args))
                {
                    System.exit(0);
                }
            }
        }
        catch (IOException exception)
        {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            System.exit(1);
        }

        boolean console = false;
        for (String arg : args)
        {
            if (arg.equals("-c"))
            {
                console = true;
            }
        }
        if (console)
        {
            System.out.println("Command line uninstaller.\n");
        }
        try
        {
            Class<Uninstaller> clazz = Uninstaller.class;
            Method target;
            if (console)
            {
                target = clazz.getMethod("consoleUninstall", new Class[]{String[].class});
            }
            else
            {
                target = clazz.getMethod("uninstall", new Class[]{String[].class});
            }
            new SelfModifier(target).invoke(args);
        }
        catch (Exception ioeOrTypo)
        {
            System.err.println(ioeOrTypo.getMessage());
            ioeOrTypo.printStackTrace();
            System.err.println("Unable to exec java as a subprocess.");
            System.err.println("The uninstall may not fully complete.");
            uninstall(args);
        }
    }

    /**
     * Runs uninstallation via the console.
     *
     * @param args the command line arguments
     */
    public static void consoleUninstall(String[] args)
    {
        UninstallerContainer container = new ConsoleUninstallerContainer();
        try
        {
            ConsoleUninstaller uninstaller = container.getComponent(ConsoleUninstaller.class);
            boolean force = false;
            for (String arg : args)
            {
                if (arg.equals("-f"))
                {
                    force = true;
                }
            }
            uninstaller.uninstall(force);
        }
        catch (Exception err)
        {
            shutdown(container, err);
        }
    }

    public static void uninstall(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                UninstallerContainer container = new GUIUninstallerContainer();
                try
                {
                    boolean displayForceOption = true;
                    boolean forceOptionState = false;

                    for (String arg : args)
                    {
                        if (arg.equals("-f"))
                        {
                            forceOptionState = true;
                        }
                        else if (arg.equals("-x"))
                        {
                            displayForceOption = false;
                        }
                    }

                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    UninstallerFrame uninstaller = container.getComponent(UninstallerFrame.class);
                    uninstaller.init(displayForceOption, forceOptionState);
                }
                catch (Exception err)
                {
                    shutdown(container, err);
                }
            }
        });
    }

    private static void shutdown(UninstallerContainer container, Exception error)
    {
        logger.log(Level.SEVERE, error.getMessage(), error);
        container.getComponent(Housekeeper.class).shutDown(1);
    }

    /**
     * Attempts to relaunch the uninstaller with elevated permissions.
     *
     * @param platform the current platform
     * @return <tt>true</tt> if the relaunch was successful, otherwise <tt>false</tt>
     */
    private static boolean relaunchWithElevatedRights(Platform platform, String[] args)
    {
        boolean result = false;
        PrivilegedRunner runner = new PrivilegedRunner(platform);
        if (runner.isPlatformSupported())
        {
            try
            {
                if (runner.relaunchWithElevatedRights(args) == 0)
                {
                    result = true;
                }
            }
            catch (Exception exception)
            {
                JOptionPane.showMessageDialog(null,
                        "The uninstaller could not launch itself with administrator permissions.\n" +
                                "Please re-run the installer with administrative privileges.");
                System.exit(0);
            }
        }
        else
        {
            JOptionPane.showMessageDialog(null, "The current platform you are running is not supported.\n" +
                    "The uninstallation will still continue but you may encounter problems.");
        }
        return result;
    }

    /**
     * Determines if permission elevation is required to uninstall the application.
     * <p/>
     * Permission elevation is required if:
     * <ul>
     * <li>the <em>exec-admin</em> resource exists; and</li>
     * <li>the current user doesn't have permission to write to the install path</li>
     * </ul>
     *
     * @param platform the current platform
     * @return <tt>true</tt> if elevation is needed
     * @throws IOException if the installation path cannot be determined
     */
    private static boolean isElevationRequired(Platform platform) throws IOException
    {
        boolean result = false;
        if (Uninstaller.class.getResource(EXEC_ADMIN) != null)
        {
            String path = InstallLog.getInstallPath(new DefaultResources());
            PrivilegedRunner runner = new PrivilegedRunner(platform);
            result = runner.isElevationNeeded(path);
        }
        return result;
    }

}
