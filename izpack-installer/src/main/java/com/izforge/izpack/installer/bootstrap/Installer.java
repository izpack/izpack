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

package com.izforge.izpack.installer.bootstrap;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.core.data.DefaultOverrides;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.ConsoleInstallerAction;
import com.izforge.izpack.installer.container.impl.AutomatedInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.logging.FileFormatter;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.LogUtils;
import com.izforge.izpack.util.StringTool;
import org.apache.commons.io.FilenameUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The program entry point. Selects between GUI and text install modes.
 *
 * @author Jonathan Halliday
 * @author René Krell
 */
public class Installer
{
    private static Logger logger = Logger.getLogger(Installer.class.getName());

    @SuppressWarnings("WeakerAccess")
    public static final int INSTALLER_GUI = 0, INSTALLER_AUTO = 1, INSTALLER_CONSOLE = 2;

    /**
     * Used to keep track of the current installation mode.
     */
    private static int installerMode = 0;

    /*
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        try
        {
            Installer installer = new Installer();
            installer.start(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void initializeLogging(String logFileName) throws IOException
    {
        if (logFileName != null)
        {
            final Properties props = new Properties();
            final String cname = FileHandler.class.getName();
            props.setProperty("handlers", cname);
            props.setProperty(cname + ".pattern", FilenameUtils.normalize(logFileName));
            props.setProperty(cname + ".formatter", FileFormatter.class.getName());
            props.setProperty(ConsoleHandler.class.getName() + ".level", "OFF");
            props.setProperty(".level", "OFF");
            LogUtils.loadConfiguration(props);
        }
        else
        {
            LogUtils.loadConfiguration();
        }
        logger = Logger.getLogger(Installer.class.getName());
    }

    private static String fetchArgument(Iterator<String> iterator, String prev) throws IllegalArgumentException
    {
        if (prev != null)
        {
            throw new IllegalArgumentException("Option used twice or in an ambiguous combination");
        }
        String next = null;
        if (iterator.hasNext())
        {
            next = iterator.next().trim();
        }
        return next;
    }

    private static void checkPath(String arg) throws IllegalArgumentException
    {
        if (arg == null)
        {
            throw new IllegalArgumentException("Option must be followed by a path");
        }
    }

    private void start(String[] args)
    {
        // OS X tweaks
        if (System.getProperty("mrj.version") != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }

        try
        {
            Iterator<String> args_it = Arrays.asList(args).iterator();

            int type = INSTALLER_GUI;
            ConsoleInstallerAction consoleAction = ConsoleInstallerAction.CONSOLE_INSTALL;
            String path = null;
            String langcode = null;
            String media = null;
            String defaultsFile = null;
            String logFileName = null;

            while (args_it.hasNext())
            {
                String arg = args_it.next().trim();
                try
                {
                    if ("-logfile".equalsIgnoreCase(arg))
                    {
                        logFileName = fetchArgument(args_it, logFileName);
                        checkPath(logFileName);
                    } else if ("-debug".equalsIgnoreCase(arg))
                    {
                        Debug.setDEBUG(true);
                    } else if ("-trace".equalsIgnoreCase(arg))
                    {
                        Debug.setTRACE(true);
                    } else if ("-stacktrace".equalsIgnoreCase(arg))
                    {
                        Debug.setSTACKTRACE(true);
                    } else if ("-console".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                    } else if ("-auto".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_AUTO;
                    } else if ("-defaults-file".equalsIgnoreCase(arg))
                    {
                        defaultsFile = fetchArgument(args_it, defaultsFile);
                        checkPath(defaultsFile);
                    } else if ("-options-template".equalsIgnoreCase(arg))
                    {
                        path = fetchArgument(args_it, path);
                        checkPath(path);
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_GEN_TEMPLATE;
                    } else if ("-options".equalsIgnoreCase(arg))
                    {
                        path = fetchArgument(args_it, path);
                        checkPath(path);
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_FROM_TEMPLATE;
                    } else if ("-options-system".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_FROM_SYSTEMPROPERTIES;
                    } else if ("-options-auto".equalsIgnoreCase(arg))
                    {
                        path = fetchArgument(args_it, path);
                        checkPath(path);
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_FROM_SYSTEMPROPERTIESMERGE;
                    } else if ("-language".equalsIgnoreCase(arg))
                    {
                        langcode = fetchArgument(args_it, langcode);
                        if (langcode == null || langcode.startsWith("-"))
                        {
                            throw new IllegalArgumentException("Option must be followed by a language code");
                        }
                    } else if ("-media".equalsIgnoreCase(arg))
                    {
                        media = fetchArgument(args_it, media);
                        checkPath(media);
                    } else if ("-v".equals(arg) || "--version".equals(arg))
                    {
                        URL url = getClass().getClassLoader().getResource("META-INF/MANIFEST.MF");
                        try (InputStream is = url.openStream())
                        {
                            Manifest manifest = new Manifest(is);
                            Attributes attr = manifest.getMainAttributes();
                            System.out.println(attr.getValue("Created-By"));
                            System.exit(0);
                        }
                        catch (IOException e)
                        {
                            logger.log(Level.SEVERE, "IzPack version not found in manifest", e);
                            System.err.println("IzPack version not found in manifest");
                            System.exit(1);
                        }
                    } else
                    {
                        type = INSTALLER_AUTO;
                        path = arg;
                    }
                }
                catch (IllegalArgumentException e)
                {
                    logger.severe("Wrong usage of command line argument \"" + arg + "\": " + e.getMessage());
                    System.exit(1);
                }
            }

            initializeLogging(logFileName);

            if (args.length != 0)
            {
                logger.info("Command line arguments: " + StringTool.stringArrayToSpaceSeparatedString(args));
            }

            Overrides defaults = getDefaults(defaultsFile);
            launchInstall(type, consoleAction, path, langcode, media, defaults, args);

        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    public Overrides getDefaults(String path) throws IOException
    {
        File overridePropFile = null;

        if (path != null)
        {
            overridePropFile = new File(path);
        }
        else
        {
            try
            {
                File jarFile = new File(
                        DefaultVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                String jarDir = jarFile.getParentFile().getPath();
                overridePropFile = new File(jarDir, FilenameUtils.getBaseName(jarFile.getPath()) + ".defaults");
                if (!overridePropFile.exists())
                {
                    overridePropFile = null;
                }
            }
            catch (URISyntaxException e) { /* Should not happen */ }
        }

        if (overridePropFile != null)
        {
            return new DefaultOverrides(overridePropFile);
        }

        return null;
    }

    private void launchInstall(int type, ConsoleInstallerAction consoleAction, String path, String langCode,
                               String mediaDir, Overrides defaults, String[] args) throws Exception
    {
        // if headless, just use the console mode
        if (type == INSTALLER_GUI && GraphicsEnvironment.isHeadless())
        {
            type = INSTALLER_CONSOLE;
        }

        installerMode = type;

        switch (type)
        {
            case INSTALLER_GUI:
                InstallerGui.run(langCode, mediaDir, defaults);
                break;

            case INSTALLER_AUTO:
                launchAutomatedInstaller(path, mediaDir, defaults, args);
                break;

            case INSTALLER_CONSOLE:
                InstallerConsole.run(consoleAction, path, langCode, mediaDir, defaults, args);
                break;
        }
    }

    /**
     * Launches an {@link AutomatedInstaller}.
     *
     * @param path     the input file path
     * @param mediaDir the multi-volume media directory. May be <tt>null</tt>
     * @param defaults the overrides, pre-initialized with a file name but not loaded
     * @param args more command line arguments
     * @throws Exception for any error
     */
    private void launchAutomatedInstaller(String path, String mediaDir, Overrides defaults, String[] args) throws Exception
    {
        InstallerContainer container = new AutomatedInstallerContainer();

        if (defaults != null)
        {
            defaults.setInstallData(container.getComponent(AutomatedInstallData.class));
            defaults.load();
            logger.info("Loaded " + defaults.size() + " override(s) from " + defaults.getFile());

            container.getComponent(DefaultVariables.class).setOverrides(defaults);
        }

        AutomatedInstaller automatedInstaller = container.getComponent(AutomatedInstaller.class);
        automatedInstaller.init(path, mediaDir, args);
        automatedInstaller.doInstall();
    }

    public static int getInstallerMode() {
        return installerMode;
    }

}
