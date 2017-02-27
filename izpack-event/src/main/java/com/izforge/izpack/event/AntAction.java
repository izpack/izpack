/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.util.JavaEnvUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class contains data and 'perform' logic for ant action listeners.
 *
 * @author Thomas Guenter
 * @author Klaus Bartz
 */
public class AntAction extends ActionBase
{

    private static final Logger LOGGER = Logger.getLogger(AntAction.class.getName());

    private static final long serialVersionUID = 3258131345250005557L;

    public static final String CONDITIONID_ATTR = "condition";
    public static final String ANTCALL = "antcall";

    private boolean quiet = false;

    private boolean verbose = false;

    private AntLogLevel logLevel = AntLogLevel.INFO;

    private Prompt.Type severity = Prompt.Type.ERROR;

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
    public void performInstallAction() throws IzPackException
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
    public void performUninstallAction() throws IzPackException
    {
        performAction(true);
    }

    /**
     * Performs all defined actions.
     *
     * @param uninstall An install/uninstall switch. If this is <tt>true</tt>
     * only the uninstall actions, otherwise only the install actions are being
     * performed.
     * @throws IzPackException for any error
     * @see #performInstallAction() for calling all install actions.
     * @see #performUninstallAction() for calling all uninstall actions.
     */
    public void performAction(boolean uninstall) throws IzPackException
    {
        if (verbose)
        {
            System.out.print("Calling ANT with buildfile: " + buildFile);
            System.out.print(buildDir != null ? " in directory " + buildDir : " in default base directory");
            System.out.println();
        }
        SecurityManager oldsm = null;
        if (!JavaEnvUtils.isJavaVersion("1.0") && !JavaEnvUtils.isJavaVersion("1.1"))
        {
            oldsm = System.getSecurityManager();
        }
        PrintStream err = System.err;
        PrintStream out = System.out;
        Project antProj = new Project();
        try
        {
            antProj.setInputHandler(new AntActionInputHandler());
            antProj.setName("antcallproject");
            if (verbose)
            {
                logLevel = AntLogLevel.VERBOSE;
            }
            else if (quiet)
            {
                logLevel = AntLogLevel.WARNING;
            }
            final int antLogLevel = logLevel.getLevel();
            antProj.addBuildListener(new AntSystemLogBuildListener(antLogLevel));
            if (logFile != null)
            {
                antProj.addBuildListener(new AntActionLogBuildListener(logFile, logFileAppend, antLogLevel));
            }
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
                Ant antcall;
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
            antProj.fireBuildFinished(null);
        }
        catch (BuildException exception)
        {
            antProj.fireBuildFinished(exception);
            throw new IzPackException("Ant build failed", exception, getSeverity());
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
     * @param buildDir build working directory path to be used
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
     * Sets the property file path list to the given list. Old settings will be
     * lost.
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
     * Sets the internal properties to the given properties. Old settings will
     * be lost.
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
     * @param name key of the property
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
     *
     * @return logLevel
     * @see org.apache.tools.ant.Project
     */
    public AntLogLevel getLogLevel()
    {
        return logLevel;
    }

    /**
     * Set Ant log priority level the action should use when logging.
     *
     * @param logLevel
     * @see org.apache.tools.ant.Project
     */
    public void setLogLevel(AntLogLevel logLevel)
    {
        this.logLevel = logLevel;
    }

    /**
     * Get severity of this action in case of errors
     *
     * @return
     */
    public Prompt.Type getSeverity()
    {
        return severity;
    }

    /**
     * Set severity of this action in case of errors
     *
     * @param severity
     */
    public void setSeverity(Prompt.Type severity)
    {
        this.severity = severity;
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
     * Sets the targets which should be performed at installation time. Old
     * settings are lost.
     *
     * @param targets list of targets
     */
    public void setTargets(ArrayList<String> targets)
    {
        this.targets = targets;
    }

    /**
     * Adds the given target to the target list which should be performed at
     * installation time.
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
     * Sets the targets which should be performed at uninstallation time. Old
     * settings are lost.
     *
     * @param targets list of targets
     */
    public void setUninstallTargets(ArrayList<String> targets)
    {
        this.uninstallTargets = targets;
    }

    /**
     * Adds the given target to the target list which should be performed at
     * uninstallation time.
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
            IOUtils.closeQuietly(fis);
        }
        addProperties(proj, props);
    }

    /**
     * Wraps a Ant {@link BuildException} to an {@link InstallerException}. This
     * is mainly done for the purpose of cutting of the location from the build
     * failure message. Locations should appear just in logs, not to the user.
     *
     * @param e the {@link IzPackException} with the nested
     * {@link BuildException}
     * @throws InstallerException
     */
    public void throwBuildException(IzPackException e) throws InstallerException
    {
        String message;
        Throwable cause = e.getCause(), nested = e;
        IzPackException ize = e;
        while (cause != null)
        {
            nested = cause;
            cause = cause.getCause();
        }
        if (nested instanceof BuildException)
        {
            // Workaround for BuildException.toString():
            // Filter off the location, just leave the clean failure message
            Location location = ((BuildException) nested).getLocation();
            message = nested.toString().substring(location.toString().length());
            ize = new IzPackException(message, e, severity);
        }
        throw new InstallerException(ize);
    }

}
