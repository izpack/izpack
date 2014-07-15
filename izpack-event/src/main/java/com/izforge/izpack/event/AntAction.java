/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
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

package com.izforge.izpack.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.util.JavaEnvUtils;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.file.FileUtils;

/**
 * This class contains data and 'perform' logic for ant action listeners.
 *
 * @author Thomas Guenter
 * @author Klaus Bartz
 */
public class AntAction extends ActionBase
{

    // --------AntAction specific String constants for ------------
    // --- parsing the XML specification ------------

    private static final long serialVersionUID = 3258131345250005557L;

    public static final String ANTACTIONS = "antactions";

    public static final String ANTACTION = "antaction";

    public static final String ANTCALL = "antcall";

    private boolean quiet = false;

    private boolean verbose = false;

    private AntLogLevel logLevel = AntLogLevel.INFO;

    private Properties properties = null;

    private List<String> targets = null;

    private List<String> uninstallTargets = null;

    private File logFile = null;
    private boolean logFileAppend = false;

    private File buildDir = null;

    private File buildFile = null;

    private String conditionId = null;

    private List<String> propertyFiles = null;

    /**
     * Default constructor
     */
    public AntAction()
    {
        super();
        properties = new Properties();
        targets = new ArrayList<String>();
        uninstallTargets = new ArrayList<String>();
        propertyFiles = new ArrayList<String>();
    }

    /**
     * Performs all defined install actions.
     * <p/>
     * Calls {#performAction performAction(false)}.
     *
     * @throws Exception
     */
    public void performInstallAction() throws Exception
    {
        performAction(false);
    }

    /**
     * Performs all defined uninstall actions.
     * <p/>
     * Calls {#performAction performAction(true)}.
     *
     * @throws IzPackException for any error
     */
    public void performUninstallAction()
    {
        performAction(true);
    }

    /**
     * Performs all defined actions.
     *
     * @param uninstall An install/uninstall switch. If this is <tt>true</tt> only the uninstall
     *                  actions, otherwise only the install actions are being performed.
     * @throws IzPackException for any error
     * @see #performInstallAction() for calling all install actions.
     * @see #performUninstallAction() for calling all uninstall actions.
     */
    public void performAction(boolean uninstall)
    {
        if (verbose)
        {
            System.out.print("Calling ANT with buildfile: " + buildFile);
            System.out.print(buildDir!=null ? " in directory "+buildDir : " in default base directory");
            System.out.println();
        }
        SecurityManager oldsm = null;
        if (!JavaEnvUtils.isJavaVersion("1.0") && !JavaEnvUtils.isJavaVersion("1.1"))
        {
            oldsm = System.getSecurityManager();
        }
        PrintStream err = System.err;
        PrintStream out = System.out;
        try
        {
            Project antProj = new Project();
            antProj.setName("antcallproject");
            antProj.addBuildListener(createLogger());
            antProj.setInputHandler(new AntActionInputHandler());
            antProj.setSystemProperties();
            addProperties(antProj, getProperties());
            addPropertiesFromPropertyFiles(antProj);
            // TODO: propertyfiles, logFile
            antProj.fireBuildStarted();
            antProj.init();
            List<Ant> antcalls = new ArrayList<Ant>();
            List<String> choosenTargets = (uninstall) ? uninstallTargets : targets;
            if (choosenTargets.size() > 0)
            {
                Ant antcall = null;
                for (String choosenTarget : choosenTargets)
                {
                    antcall = (Ant) antProj.createTask("ant");
                    if (buildDir != null)
                    {
                        antcall.setDir(buildDir);
                    }
                    antcall.setAntfile(buildFile.getAbsolutePath());
                    antcall.setTarget(choosenTarget);
                    antcalls.add(antcall);
                }
            }
            Target target = new Target();
            target.setName("calltarget");

            for (Ant antcall : antcalls)
            {
                target.addTask(antcall);
            }
            antProj.addTarget(target);
            System.setOut(new PrintStream(new DemuxOutputStream(antProj, false)));
            System.setErr(new PrintStream(new DemuxOutputStream(antProj, true)));
            antProj.executeTarget("calltarget");
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
        finally
        {
            if (oldsm != null)
            {
                System.setSecurityManager(oldsm);
            }
            System.setOut(out);
            System.setErr(err);
        }
    }


    public String getConditionId()
    {
        return conditionId;
    }


    public void setConditionId(String conditionId)
    {
        this.conditionId = conditionId;
    }

    /**
     * Returns the build file.
     *
     * @return the build file
     */
    public File getBuildFile()
    {
        return buildFile;
    }

    /**
     * Sets the build file to be used to the given string.
     *
     * @param buildFile build file path to be used
     */
    public void setBuildFile(File buildFile)
    {
        this.buildFile = buildFile;
    }

    /**
     * Returns the build working directory.
     *
     * @return the working directory
     */
    public File getBuildDir()
    {
        return buildDir;
    }

    /**
     * Sets the build working directory to be used to the given string.
     *
     * @param buildFile build working directory path to be used
     */
    public void setBuildDir(File buildDir)
    {
        this.buildDir = buildDir;
    }

    /**
     * Returns the current logfile path as string.
     *
     * @return current logfile path
     */
    public File getLogFile()
    {
        return logFile;
    }

    /**
     * Sets the logfile path to the given string.
     *
     * @param logFile to be set
     * @append if true, then append new log entries to existing files
     */
    public void setLogFile(File logFile, boolean append)
    {
        this.logFile = logFile;
        this.logFileAppend = append;
    }

    /**
     * Returns the property file paths as list of strings.
     *
     * @return the property file paths
     */
    public List<String> getPropertyFiles()
    {
        return propertyFiles;
    }

    /**
     * Adds one property file path to the internal list of property file paths.
     *
     * @param propertyFile to be added
     */
    public void addPropertyFile(String propertyFile)
    {
        this.propertyFiles.add(propertyFile);
    }

    /**
     * Sets the property file path list to the given list. Old settings will be lost.
     *
     * @param propertyFiles list of property file paths to be set
     */
    public void setPropertyFiles(List<String> propertyFiles)
    {
        this.propertyFiles = propertyFiles;
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public Properties getProperties()
    {
        return properties;
    }

    /**
     * Sets the internal properties to the given properties. Old settings will be lost.
     *
     * @param properties properties to be set
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Sets the given value to the property identified by the given name.
     *
     * @param name  key of the property
     * @param value value to be used for the property
     */
    public void setProperty(String name, String value)
    {
        this.properties.put(name, value);
    }

    /**
     * Returns the value for the property identified by the given name.
     *
     * @param name name of the property
     * @return value of the property
     */
    public String getProperty(String name)
    {
        return this.properties.getProperty(name);
    }

    /**
     * Returns the quiet state.
     *
     * @return quiet state
     */
    public boolean isQuiet()
    {
        return quiet;
    }

    /**
     * Sets whether the associated ant task should be performed quiet or not.
     *
     * @param quiet quiet state to set
     */
    public void setQuiet(boolean quiet)
    {
        this.quiet = quiet;
    }

    /**
     * Get Ant log priority level the action uses when logging.
     * @return logLevel
     * @see org.apache.tools.ant.Project
     */
    public AntLogLevel getLogLevel()
    {
        return logLevel;
    }

    /**
     * Set Ant log priority level the action should use when logging.
     * Must be on of @TODO
     * @param logLevel
     * @see org.apache.tools.ant.Project
     */
    public void setLogLevel(AntLogLevel logLevel)
    {
        this.logLevel = logLevel;
    }

    /**
     * Returns the targets.
     *
     * @return the targets
     */
    public List<String> getTargets()
    {
        return targets;
    }

    /**
     * Sets the targets which should be performed at installation time. Old settings are lost.
     *
     * @param targets list of targets
     */
    public void setTargets(ArrayList<String> targets)
    {
        this.targets = targets;
    }

    /**
     * Adds the given target to the target list which should be performed at installation time.
     *
     * @param target target to be add
     */
    public void addTarget(String target)
    {
        this.targets.add(target);
    }

    /**
     * Returns the uninstaller targets.
     *
     * @return the uninstaller targets
     */
    public List<String> getUninstallTargets()
    {
        return uninstallTargets;
    }

    /**
     * Sets the targets which should be performed at uninstallation time. Old settings are lost.
     *
     * @param targets list of targets
     */
    public void setUninstallTargets(ArrayList<String> targets)
    {
        this.uninstallTargets = targets;
    }

    /**
     * Adds the given target to the target list which should be performed at uninstallation time.
     *
     * @param target target to be add
     */
    public void addUninstallTarget(String target)
    {
        this.uninstallTargets.add(target);
    }

    /**
     * Returns the verbose state.
     *
     * @return verbose state
     */
    public boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Sets the verbose state.
     *
     * @param verbose state to be set
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    private BuildLogger createLogger()
    {
        if (verbose)
        {
            logLevel = AntLogLevel.VERBOSE;
        }
        else if (quiet)
        {
            logLevel = AntLogLevel.WARNING;
        }
        BuildLogger logger = new DefaultLogger();
        logger.setMessageOutputLevel(logLevel.getLevel());
        if (logFile != null)
        {
            PrintStream printStream;
            try
            {
                logFile.getParentFile().mkdirs();
                printStream = new PrintStream(new FileOutputStream(logFile, logFileAppend));
                logger.setOutputPrintStream(printStream);
                logger.setErrorPrintStream(printStream);
            }
            catch (FileNotFoundException e)
            {
                logger.setOutputPrintStream(System.out);
                logger.setErrorPrintStream(System.err);
            }
        }
        else
        {
            logger.setOutputPrintStream(System.out);
            logger.setErrorPrintStream(System.err);
        }
        return logger;
    }

    private void addProperties(Project proj, Properties props)
    {
        if (proj == null)
        {
            return;
        }
        if (props.size() > 0)
        {
            for (Object o : props.keySet())
            {
                String key = (String) o;
                proj.setProperty(key, props.getProperty(key));
            }
        }
    }

    private void addPropertiesFromPropertyFiles(Project proj)
    {
        if (proj == null)
        {
            return;
        }
        Properties props = new Properties();
        FileInputStream fis = null;
        try
        {
            for (String propertyFile : propertyFiles)
            {
                File file = new File(propertyFile);
                if (file.exists())
                {
                    fis = new FileInputStream(file);
                    props.load(fis);
                    fis.close();
                }
                else
                {
                    throw new IzPackException("Required propertyfile " + file + " for antcall doesn't exist.");
                }
            }
        }
        catch (IOException exception)
        {
            throw new IzPackException(exception);
        }
        finally
        {
            FileUtils.close(fis);
        }
        addProperties(proj, props);
    }

}
