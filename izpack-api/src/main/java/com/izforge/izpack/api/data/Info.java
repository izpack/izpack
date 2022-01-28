/*
 * $Id$
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

package com.izforge.izpack.api.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains some information for an installer, as defined in the <info> section of the XML files.
 *
 * @author Julien Ponge
 */
public class Info implements Serializable
{

    private static final long serialVersionUID = 13288410782044775L;

    /**
     * Force ignoring pending file operations
     */
    public static final int REBOOT_ACTION_IGNORE = 0;
    /**
     * Notice the user interactively but don't actually reboot,
     * on pending file operations only
     */
    public static final int REBOOT_ACTION_NOTICE = 1;
    /**
     * Ask before reboot automatically,
     * on pending file operations only
     */
    public static final int REBOOT_ACTION_ASK = 2;
    /**
     * Force rebooting regardless whether there are pending operations
     */
    public static final int REBOOT_ACTION_ALWAYS = 3;

    /**
     * The application name and version
     */
    private String appName = "";
    private String appVersion = "";

    /**
     * The installation subpath
     */
    private String installationSubPath = null;

    /**
     * The application authors
     */
    private final ArrayList<Author> authors = new ArrayList<Author>();

    /**
     * The application URL
     */
    private String appURL = null;

    /**
     * The required Java version (min)
     */
    private String javaVersion = "1.4";

    /**
     * Whether to check strictly Java version at compilation time
     */
    private boolean javaVersionStrict = false;

    /**
     * Is a JDK required?
     */
    private boolean jdkRequired = false;

    /**
     * The name of the installer file (name without jar suffix)
     */
    private String installerBase = null;

    /**
     * The application Web Directory URL
     */
    private String webDirURL = null;

    /**
     * The uninstaller name
     */
    private String uninstallerName = "uninstaller.jar";
    /**
     * The uninstaller path
     */
    private String uninstallerPath = "$INSTALL_PATH/Uninstaller";
    /**
     * condition for writing the uninstaller
     */
    private String uninstallerCondition = null;

    /**
     * condition for writing the uninstaller
     */
    private boolean hideForceOption = false;

    /**
     * The path of the summary log file
     */
    private String summaryLogFilePath = "$INSTALL_PATH/Uninstaller/InstallSummary.htm";

    private String unpackerClassName = null;

    private boolean writeInstallationInformation = true;
    
    private boolean readInstallationInformation = true;

    private boolean isSingleInstance = true;

    private String compressionFormat = PackCompression.DEFAULT.toName();

    private boolean requirePrivilegedExecution = false;

    private boolean requirePrivilegedExecutionUninstaller = false;

    private String privilegedExecutionConditionID = null;

    private int rebootAction = REBOOT_ACTION_IGNORE;

    private String rebootActionConditionID = null;

    /**
     * A set of temporary directories to be created at run time
     */
    private Set<TempDir> tempdirs;

    /**
     * The date on which the installer expires, if not null
     */
    private Date expiresDate = null;
    /**
     * The format of the expiration date
     */
    public static final String EXPIRE_DATE_FORMAT = "yyyy-MM-dd";
    
    public boolean isPrivilegedExecutionRequired()
    {
        return requirePrivilegedExecution;
    }

    public void setRequirePrivilegedExecution(boolean requirePrivilegedExecution)
    {
        this.requirePrivilegedExecution = requirePrivilegedExecution;
    }

    public boolean isPrivilegedExecutionRequiredUninstaller()
    {
        return requirePrivilegedExecutionUninstaller;
    }

    public void setRequirePrivilegedExecutionUninstaller(boolean required)
    {
        this.requirePrivilegedExecutionUninstaller = required;
    }

    public String getPrivilegedExecutionConditionID()
    {
        return privilegedExecutionConditionID;
    }

    public void setPrivilegedExecutionConditionID(String privilegedExecutionConditionID)
    {
        this.privilegedExecutionConditionID = privilegedExecutionConditionID;
    }

    public int getRebootAction()
    {
        return rebootAction;
    }

    public void setRebootAction(int rebootAction)
    {
        this.rebootAction = rebootAction;
    }

    public String getRebootActionConditionID()
    {
        return rebootActionConditionID;
    }

    public void setRebootActionConditionID(String rebootActionConditionID)
    {
        this.rebootActionConditionID = rebootActionConditionID;
    }

    /**
     * The constructor, deliberately void.
     */
    public Info()
    {
    }

    /**
     * Sets the application name.
     *
     * @param appName The new application name.
     */
    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    /**
     * Gets the application name.
     *
     * @return The application name.
     */
    public String getAppName()
    {
        return appName;
    }

    /**
     * Sets the version.
     *
     * @param appVersion The application version.
     */
    public void setAppVersion(String appVersion)
    {
        this.appVersion = appVersion;
    }

    /**
     * Gets the version.
     *
     * @return The application version.
     */
    public String getAppVersion()
    {
        return appVersion;
    }

    /**
     * Adds an author to the authors list.
     *
     * @param author The author to add.
     */
    public void addAuthor(Author author)
    {
        authors.add(author);
    }

    /**
     * Gets the authors list.
     *
     * @return The authors list.
     */
    public ArrayList<Author> getAuthors()
    {
        return authors;
    }

    /**
     * Sets the application URL.
     *
     * @param appURL The application URL.
     */
    public void setAppURL(String appURL)
    {
        this.appURL = appURL;
    }

    /**
     * Gets the application URL.
     *
     * @return The application URL.
     */
    public String getAppURL()
    {
        return appURL;
    }

    /**
     * Sets the minimum Java version required.
     *
     * @param javaVersion The Java version.
     */
    public void setJavaVersion(String javaVersion)
    {
        this.javaVersion = javaVersion;
    }

    /**
     * Gets the Java version required.
     *
     * @return The Java version.
     */
    public String getJavaVersion()
    {
        return javaVersion;
    }

    /**
     * Sets the minimum Java version strict parameter.
     *
     * @param javaVersionStrict The Java version strict parameter.
     */
    public void setJavaVersionStrict(boolean javaVersionStrict)
    {
        this.javaVersionStrict = javaVersionStrict;
    }

    /**
     * Gets the Java version strict parameter.
     *
     * @return The Java version strict parameter.
     */
    public boolean getJavaVersionStrict()
    {
        return javaVersionStrict;
    }

    /**
     * Sets the installer name.
     *
     * @param installerBase The new installer name.
     */
    public void setInstallerBase(String installerBase)
    {
        this.installerBase = installerBase;
    }

    /**
     * Gets the installer name.
     *
     * @return The name of the installer file, without the jar suffix.
     */
    public String getInstallerBase()
    {
        return installerBase;
    }

    /**
     * Sets the webDir URL.
     *
     * @param url The application URL.
     */
    public void setWebDirURL(String url)
    {
        this.webDirURL = url;
    }

    /**
     * Gets the webDir URL if it has been specified
     *
     * @return The webDir URL from which the installer is retrieved, or <tt>null</tt> if non has
     *         been set.
     */
    public String getWebDirURL()
    {
        return webDirURL;
    }

    /**
     * Sets the name of the uninstaller.
     *
     * @param name the name of the uninstaller.
     */
    public void setUninstallerName(String name)
    {
        this.uninstallerName = name;
    }

    /**
     * Returns the name of the uninstaller.
     *
     * @return the name of the uninstaller.
     */
    public String getUninstallerName()
    {
        return this.uninstallerName;
    }

    /**
     * Sets the path to the uninstaller
     *
     * @param path the path to the uninstaller
     */
    public void setUninstallerPath(String path)
    {
        this.uninstallerPath = path;
    }

    /**
     * Returns the path to the uninstaller
     *
     * @return the path to the uninstaller
     */
    public String getUninstallerPath()
    {
        return this.uninstallerPath;
    }

    public boolean isJdkRequired()
    {
        return jdkRequired;
    }

    public void setJdkRequired(boolean jdkRequired)
    {
        this.jdkRequired = jdkRequired;
    }

    public PackCompression getCompressionFormat()
    {
        return PackCompression.byName(compressionFormat);
    }

    public void setCompressionFormat(PackCompression compression)
    {
        this.compressionFormat = compression.toName();
    }

    /**
     * This class represents an author.
     *
     * @author Julien Ponge
     */
    public static class Author implements Serializable
    {

        private static final long serialVersionUID = -3090178155004960243L;

        /**
         * The author name
         */
        private final String name;

        /**
         * The author email
         */
        private final String email;

        /**
         * Gets the author name.
         *
         * @return The author name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Gets the author email.
         *
         * @return The author email.
         */
        public String getEmail()
        {
            return email;
        }

        /**
         * The constructor.
         *
         * @param name  The author name.
         * @param email The author email.
         */
        public Author(String name, String email)
        {
            this.name = name;
            this.email = email;
        }

        /**
         * Gets a String representation of the author.
         *
         * @return The String representation of the author, in the form : name <email> .
         */
        public String toString()
        {
            return name + " <" + email + ">";
        }

    }

    /**
     * Represents a temporary directory with a randomly generated file name starting with
     * the specified prefix and ending with the specified suffix. The full path to this
     * directory will be defined in a variable with the specified name.
     *
     * @author Thrupoint
     */
    public static class TempDir implements Serializable
    {
        private static final long serialVersionUID = 7578346199971960904L;

        private final String prefix;
        private final String suffix;
        private final String variableName;

        /**
         * Create a new TempDir with the specified name prefix and suffix
         *
         * @param variableName the variable to hold the resulting path
         * @param prefix the path name prefix
         * @param suffix the path name suffix
         */
        public TempDir(String variableName, String prefix, String suffix)
        {
            this.variableName = variableName;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
         * @return the prefix to be used to create the temporary directory
         */
        public String getPrefix()
        {
            return prefix;
        }

        /**
         * @return the suffix to be used to create the temporary directory
         */
        public String getSuffix()
        {
            return suffix;
        }

        /**
         * @return the name of the variable which will contain the temporary
         *         directory path
         */
        public String getVariableName()
        {
            return variableName;
        }
    }

    /**
     * Gets the installation subpath.
     *
     * @return the installation subpath
     */
    public String getInstallationSubPath()
    {
        return installationSubPath;
    }

    /**
     * Sets the installation subpath.
     *
     * @param installationSubPath subpath to be set
     */
    public void setInstallationSubPath(String installationSubPath)
    {
        this.installationSubPath = installationSubPath;
    }

    /**
     * Returns the summary log file path.
     *
     * @return the summary log file path
     */
    public String getSummaryLogFilePath()
    {
        return summaryLogFilePath;
    }

    /**
     * Sets the summary log file path.
     *
     * @param summaryLogFilePath the summary log file path to set
     */
    public void setSummaryLogFilePath(String summaryLogFilePath)
    {
        this.summaryLogFilePath = summaryLogFilePath;
    }

    public String getUnpackerClassName()
    {
        return unpackerClassName;
    }

    public void setUnpackerClassName(String unpackerClassName)
    {
        this.unpackerClassName = unpackerClassName;
    }


    public boolean isWriteInstallationInformation()
    {
        return writeInstallationInformation;
    }

    public void setWriteInstallationInformation(boolean writeInstallationInformation)
    {
        this.writeInstallationInformation = writeInstallationInformation;
    }
    
    public boolean isReadInstallationInformation()
    {
        return readInstallationInformation;
    }

    public void setReadInstallationInformation(boolean readInstallationInformation)
    {
        this.readInstallationInformation = readInstallationInformation;
    }


    public boolean isSingleInstance()
    {
        return this.isSingleInstance;
    }

    public void setSingleInstance(boolean flag)
    {
        this.isSingleInstance = flag;

    }

    public boolean getHideForceOption()
    {
        return hideForceOption;
    }

    public void setHideForceOption(boolean hideForceOption)
    {
        this.hideForceOption = hideForceOption;
    }

    public String getUninstallerCondition()
    {
        return uninstallerCondition;
    }

    public void setUninstallerCondition(String uninstallerCondition)
    {
        this.uninstallerCondition = uninstallerCondition;
    }

    public void addTempDir(TempDir tempDir)
    {
        if (null == tempdirs)
        {
            tempdirs = new HashSet<TempDir>();
        }
        tempdirs.add(tempDir);

    }

    public Set<TempDir> getTempDirs()
    {
        return tempdirs;
    }
    
    public Date getExpiresDate()
    {
        return expiresDate;
    }
    
    public void setExpiresDate(Date value)
    {
        expiresDate = value;
    }

    public void setExpiresDate(String value) throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(EXPIRE_DATE_FORMAT);
        expiresDate = dateFormat.parse(value);
    }
}
