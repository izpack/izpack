/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
 * Copyright 2010 Florian Buehlmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels.shortcut;

import static com.izforge.izpack.util.Platform.Name.UNIX;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.event.AbstractInstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.Shortcut;
import com.izforge.izpack.util.unix.UnixHelper;
import com.izforge.izpack.util.xml.XMLHelper;


/**
 * This class implements a the logic for the creation of shortcuts. The logic is used in the
 * ShortcutPanel, ShortcutPanelAutomationHelper.
 * <p/>
 *
 * @version $Revision: 1.2 $
 */
public class ShortcutPanelLogic implements CleanupClient
{
    private static transient final Logger logger = Logger.getLogger(ShortcutPanelLogic.class.getName());

    public final static String SPEC_ATTRIBUTE_CONDITION = "condition";

    private final static String SPEC_ATTRIBUTE_KDE_USERNAME = "KdeUsername";

    private final static String SPEC_ATTRIBUTE_KDE_SUBST_UID = "KdeSubstUID";

    private final static String SPEC_ATTRIBUTE_URL = "url";

    private final static String SPEC_ATTRIBUTE_TYPE = "type";

    private final static String SPEC_ATTRIBUTE_TERMINAL_OPTIONS = "terminalOptions";

    private final static String SPEC_ATTRIBUTE_TERMINAL = "terminal";

    private final static String SPEC_ATTRIBUTE_MIMETYPE = "mimetype";

    private final static String SPEC_ATTRIBUTE_ENCODING = "encoding";

    private static final String SPEC_CATEGORIES = "categories";

    private static final String SPEC_TRYEXEC = "tryexec";

    private static final String SEPARATOR_LINE = "--------------------------------------------------------------------------------";

    private static final String SPEC_FILE_NAME = "shortcutSpec.xml";

    // ------------------------------------------------------
    // spec file section keys
    // -----------------------------------------------------
    private static final String SPEC_KEY_SKIP_IFNOT_SUPPORTED = "skipIfNotSupported";

    private static final String SPEC_KEY_NOT_SUPPORTED = "notSupported";

    private static final String SPEC_KEY_DEF_CUR_USER = "defaultCurrentUser";

    private static final String SPEC_KEY_LATE_INSTALL = "lateShortcutInstall";

    private static final String SPEC_KEY_PROGRAM_GROUP = "programGroup";

    private static final String SPEC_KEY_SHORTCUT = "shortcut";

    private static final String SPEC_KEY_PACKS = "createForPack";

    // ------------------------------------------------------
    // spec file key attributes
    // ------------------------------------------------------
    private static final String SPEC_ATTRIBUTE_DEFAULT_GROUP = "defaultName";

    private static final String SPEC_ATTRIBUTE_INSTALLGROUP = "installGroup";

    private static final String SPEC_ATTRIBUTE_LOCATION = "location";

    private static final String SPEC_ATTRIBUTE_NAME = "name";

    private static final String SPEC_ATTRIBUTE_SUBGROUP = "subgroup";

    private static final String SPEC_ATTRIBUTE_DESCRIPTION = "description";

    private static final String SPEC_ATTRIBUTE_TARGET = "target";

    private static final String SPEC_ATTRIBUTE_COMMAND = "commandLine";

    private static final String SPEC_ATTRIBUTE_ICON = "iconFile";

    private static final String SPEC_ATTRIBUTE_ICON_INDEX = "iconIndex";

    private static final String SPEC_ATTRIBUTE_WORKING_DIR = "workingDirectory";

    private static final String SPEC_ATTRIBUTE_INITIAL_STATE = "initialState";

    private static final String SPEC_ATTRIBUTE_DESKTOP = "desktop";

    private static final String SPEC_ATTRIBUTE_APPLICATIONS = "applications";

    private static final String SPEC_ATTRIBUTE_START_MENU = "startMenu";

    private static final String SPEC_ATTRIBUTE_STARTUP = "startup";

    private static final String SPEC_ATTRIBUTE_PROGRAM_GROUP = "programGroup";

    private static final String SPEC_ATTRIBUTE_RUN_AS_ADMINISTRATOR = "runAsAdministrator";

    // ------------------------------------------------------
    // spec file attribute values
    // ------------------------------------------------------

    private static final String SPEC_VALUE_APPLICATIONS = "applications";

    private static final String SPEC_VALUE_START_MENU = "startMenu";

    private static final String SPEC_VALUE_NO_SHOW = "noShow";

    private static final String SPEC_VALUE_NORMAL = "normal";

    private static final String SPEC_VALUE_MAXIMIZED = "maximized";

    private static final String SPEC_VALUE_MINIMIZED = "minimized";

    // ------------------------------------------------------
    // automatic script keys attributes values
    // ------------------------------------------------------
    private static final String AUTO_KEY_PROGRAM_GROUP = "programGroup";

    private static final String AUTO_KEY_SHORTCUT_TYPE = "shortcutType";

    private static final String AUTO_KEY_CREATE_DESKTOP_SHORTCUTS = "createDesktopShortcuts";

    private static final String AUTO_KEY_CREATE_SHORTCUTS = "createShortcuts";

    private static final String AUTO_KEY_SHORTCUT_TYPE_VALUE_ALL = "all";

    private static final String AUTO_KEY_SHORTCUT_TYPE_VALUE_USER = "user";

    // permission flags

    private static final String CREATE_FOR_ALL = "createForAll";

    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------

    /**
     * The default name to use for the program group. This comes from the XML specification.
     */
    private String suggestedProgramGroup;

    /**
     * The name chosen by the user for the program group,
     */
    private String groupName;

    /**
     * The icon for the group in XDG/unix menu
     */
    private String programGroupIconFile;

    /**
     * Comment for XDG/unix group
     */
    private String programGroupComment;

    /**
     * The parsed result from reading the XML specification from the file
     */
    private IXMLElement spec;

    /**
     * Set to true by analyzeShortcutSpec() if there are any desktop shortcuts to create.
     */
    private boolean hasDesktopShortcuts = false;

    /**
     * Tells wether to skip if the platform is not supported.
     */
    private boolean skipIfNotSupported = false;

    /**
     * the one shortcut instance for reuse in many locations
     */
    private Shortcut shortcut;

    /**
     * A list of ShortcutData> objects. Each object is the complete specification for one shortcut
     * that must be created.
     */
    private List<ShortcutData> shortcuts;

    /**
     * Holds a list of all the shortcut files that have been created. Note: this variable contains
     * valid data only after createShortcuts() has been called. This list is created so that the
     * files can be added to the uninstaller.
     */
    private List<String> files;

    /**
     * Holds a list of all executables to set the executable flag alter shortcut createn.
     */
    private List<ExecutableFile> execFiles;

    /**
     * If true it indicates that there are shortcuts to create. The value is set by
     * analyzeShortcutSpec()
     */
    private boolean createShortcuts = false;

    /**
     * If true it indicates that the spec file is existing and could be read.
     */
    private boolean haveShortcutSpec = false;

    /**
     * This is set to true if the shortcut spec instructs to simulate running on an operating system
     * that is not supported.
     */
    private boolean simulateNotSupported = false;

    private int userType;

    private final InstallData installData;

    private final Resources resources;

    private final UninstallData uninstallData;

    private final PlatformModelMatcher matcher;

    private boolean createDesktopShortcuts;

    private boolean defaultCurrentUserFlag = false;

    private boolean createShortcutsImmediately = true;

    /**
     * Constructs a <tt>ShortcutPanelLogic</tt>.
     *
     * @param installData   the installation data
     * @param resources     the resources
     * @param uninstallData the uninstallation data
     * @param housekeeper   the house keeper
     * @param factory       the factory for platform-specific implementations
     * @param matcher       the platform-model matcher
     * @throws Exception for any error
     */
    public ShortcutPanelLogic(InstallData installData, Resources resources, UninstallData uninstallData,
                              Housekeeper housekeeper, TargetFactory factory, InstallerListeners listeners,
                              PlatformModelMatcher matcher)
            throws Exception
    {
        this.installData = installData;
        this.resources = resources;
        this.uninstallData = uninstallData;
        this.matcher = matcher;
        shortcut = factory.makeObject(Shortcut.class);
        shortcut.initialize(Shortcut.APPLICATIONS, "-");

        readShortcutSpec();
        analyzeShortcutSpec();
        if (!isCreateShortcutsImmediately())
        {
            listeners.add(new LateShortcutInstallListener());
        }

        housekeeper.registerForCleanup(this);
    }

    /**
     * @return <code>true</code> it the shortcuts will be created after clicking next,
     *         otherwise <code>false</code>
     */
    public final boolean isCreateShortcutsImmediately()
    {
        return createShortcutsImmediately;
    }

    /**
     * Tell the ShortcutPanel to not create the shortcuts immediately after clicking next.
     *
     * @param createShortcutsImmediately
     */
    public final void setCreateShortcutsImmediately(boolean createShortcutsImmediately)
    {
        this.createShortcutsImmediately = createShortcutsImmediately;
    }

    /**
     * Creates the shortcuts at a specified time. Before this function can be called, a
     * ShortcutPanel must be used to initialise the logic properly.
     *
     * @throws Exception
     */
    public void createAndRegisterShortcuts() throws Exception
    {
        String groupName = this.groupName;
        boolean createShortcuts = this.createShortcuts;
        boolean createDesktopShortcuts = this.createDesktopShortcuts;
        readShortcutSpec();  // need to re-read the specs now as variable replacement needs to be done
        analyzeShortcutSpec();
        this.groupName = groupName;
        this.createShortcuts = createShortcuts;
        this.createDesktopShortcuts = createDesktopShortcuts;
        createShortcuts();
        addToUninstaller();
    }

    /**
     * @param user type of the user {@link Shortcut#ALL_USERS} or {@link Shortcut#CURRENT_USER}
     * @return a list of progrma group names.
     */
    public List<String> getProgramGroups(int user)
    {
        return shortcut.getProgramGroups(user);
    }

    /**
     * Returns the ProgramsFolder for the current User
     *
     * @param user type of the user {@link Shortcut#ALL_USERS} or {@link Shortcut#CURRENT_USER}
     * @return The basedir
     */
    public File getProgramsFolder(int user)
    {
        String path = shortcut.getProgramsFolder(user);

        return (new File(path));

        // }
        // else
        // {
        // TODO
        // 0ptional: Test if KDE is installed.
        // boolean isKdeInstalled = UnixHelper.kdeIsInstalled();
        // 1. Test if User can write into
        // File kdeRootShareApplinkDir = getKDERootShareApplinkDir();
        // if so: return getKDERootShareApplinkDir()
        // else
        // return getKDEUsersShareApplinkDir() +
        // }
        // return(result);
    }

    /**
     * @return the suggested program group
     */
    public String getSuggestedProgramGroup()
    {
        return installData.getVariables().replace(suggestedProgramGroup);
    }

    /**
     * @param suggestedProgramGroup name of the suggested program group
     */
    public void setSuggestedProgramGroup(String suggestedProgramGroup)
    {
        this.suggestedProgramGroup = suggestedProgramGroup;
    }

    /**
     * @return alist of all shortcut targets
     */
    public List<String> getTargets()
    {
        List<String> retVal = new ArrayList<String>();
        for (ShortcutData data : shortcuts)
        {
            retVal.add(data.target);
        }
        return retVal;
    }

    public int getUserType()
    {
        return userType;
    }

    /**
     * @return a list of xml child elements to write a autoinstall.xml file for later execution
     */
    public List<IXMLElement> getAutoinstallXMLData()
    {
        List<IXMLElement> xmlData = new ArrayList<IXMLElement>();
        IXMLElement dataElement;
        dataElement = new XMLElementImpl(AUTO_KEY_CREATE_SHORTCUTS);
        dataElement.setContent(Boolean.toString(isCreateShortcuts()));
        xmlData.add(dataElement);

        if (isCreateShortcuts())
        {
            // ----------------------------------------------------
            // add all other items
            // ----------------------------------------------------
            dataElement = new XMLElementImpl(AUTO_KEY_PROGRAM_GROUP);
            dataElement.setContent(getGroupName());
            xmlData.add(dataElement);
            dataElement = new XMLElementImpl(AUTO_KEY_CREATE_DESKTOP_SHORTCUTS);
            dataElement.setContent(getGroupName());
            xmlData.add(dataElement);
            dataElement = new XMLElementImpl(AUTO_KEY_SHORTCUT_TYPE);
            String userTypeString;
            if (getUserType() == Shortcut.CURRENT_USER)
            {
                userTypeString = AUTO_KEY_SHORTCUT_TYPE_VALUE_USER;
            }
            else
            {
                userTypeString = AUTO_KEY_SHORTCUT_TYPE_VALUE_ALL;
            }
            dataElement.setContent(userTypeString);
            xmlData.add(dataElement);
        }
        return xmlData;
    }

    /**
     * Reads the xml content for automated installations.
     *
     * @param panelRoot specifies the xml elemnt for this panel
     */
    public void setAutoinstallXMLData(IXMLElement panelRoot)
    {
        IXMLElement dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_SHORTCUTS);
        setCreateShortcuts(Boolean.valueOf(dataElement.getContent()).booleanValue());

        if (isCreateShortcuts())
        {
            // ----------------------------------------------------
            // add all other items
            // ----------------------------------------------------
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_PROGRAM_GROUP);
            setGroupName(dataElement.getContent());
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_DESKTOP_SHORTCUTS);
            setCreateDesktopShortcuts(Boolean.valueOf(dataElement.getContent()).booleanValue());
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_SHORTCUT_TYPE);
            if (AUTO_KEY_SHORTCUT_TYPE_VALUE_USER.equals(dataElement.getContent()))
            {
                setUserType(Shortcut.CURRENT_USER);
            }
            else
            {
                setUserType(Shortcut.ALL_USERS);
            }
        }
    }

    /**
     * @return <code>true</code> if current user is the default for the panel otherwise
     *         <code>false</code>
     */
    public final boolean isDefaultCurrentUserFlag()
    {
        return defaultCurrentUserFlag;
    }

    /**
     * @return <code>true</code> if we have desktop shortcuts in the spec otherwise
     *         <code>false</code>
     */
    public boolean hasDesktopShortcuts()
    {
        return hasDesktopShortcuts;
    }

    /**
     * @return <code>true</code> if we create desktop shortcuts otherwise <code>false</code>
     */
    public boolean isCreateDesktopShortcuts()
    {
        return createDesktopShortcuts;
    }

    /**
     * @param createDesktopShortcuts
     */
    public void setCreateDesktopShortcuts(boolean createDesktopShortcuts)
    {
        this.createDesktopShortcuts = createDesktopShortcuts;
    }

    /**
     * @return <code>true</code> if we create shortcuts at all otherwise <code>false</code>
     */
    public boolean isCreateShortcuts()
    {
        return createShortcuts;
    }

    /**
     * @param createShortcuts
     */
    public final void setCreateShortcuts(boolean createShortcuts)
    {
        this.createShortcuts = createShortcuts;
    }

    /**
     * @return <code>true</code> if we do a shortcut creation simulation otherwise
     *         <code>false</code>
     */
    public boolean isSimulateNotSupported()
    {
        return simulateNotSupported;
    }

    /**
     * @return <code>true</code> if we skip shortcut panel and shortcut creation if this is not
     *         supported on the current OS otherwise <code>false</code>
     */
    public boolean isSkipIfNotSupported()
    {
        return skipIfNotSupported;
    }

    /**
     * @return <code>true</code> if shortcut creation is supported otherwise <code>false</code>
     */
    public boolean isSupported()
    {
        return !simulateNotSupported && shortcut.supported();
    }

    /**
     * @return <code>true</code> if we support multiple users otherwise <code>false</code>
     */
    public boolean isSupportingMultipleUsers()
    {
        return shortcut.multipleUsers();
    }

    /**
     * Called by {@link Housekeeper} to cleanup after installation.
     */
    @Override
    public void cleanUp()
    {
        if (!installData.isInstallSuccess())
        {
            // Shortcuts may have been deleted, but let's try to delete them once again
            for (String file : files)
            {
                File fl = new File(file);
                if (fl.exists())
                {
                    fl.delete();
                }
            }
        }
    }

    /**
     * This method saves all shortcut information to a text file.
     *
     * @param file to save the information to
     */
    public void saveToFile(File file)
    {

        // ----------------------------------------------------
        // save to the file
        // ----------------------------------------------------
        FileWriter output = null;
        StringBuilder buffer = new StringBuilder();
        Messages messages = installData.getMessages();
        String header = messages.get("ShortcutPanel.textFile.header");

        String newline = System.getProperty("line.separator", "\n");

        try
        {
            output = new FileWriter(file);
        }
        catch (Throwable exception)
        {
            // !!! show an error dialog
            return;
        }

        // ----------------------------------------------------
        // break the header down into multiple lines based
        // on '\n' line breaks.
        // ----------------------------------------------------
        int nextIndex = 0;
        int currentIndex = 0;

        do
        {
            nextIndex = header.indexOf("\\n", currentIndex);

            if (nextIndex > -1)
            {
                buffer.append(header.substring(currentIndex, nextIndex));
                buffer.append(newline);
                currentIndex = nextIndex + 2;
            }
            else
            {
                buffer.append(header.substring(currentIndex, header.length()));
                buffer.append(newline);
            }
        }
        while (nextIndex > -1);

        buffer.append(SEPARATOR_LINE);
        buffer.append(newline);
        buffer.append(newline);

        for (ShortcutData data : shortcuts)
        {
            buffer.append(messages.get("ShortcutPanel.textFile.name"));
            buffer.append(data.name);
            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.location"));

            switch (data.type)
            {
                case Shortcut.DESKTOP:
                {
                    buffer.append(messages.get("ShortcutPanel.location.desktop"));
                    break;
                }

                case Shortcut.APPLICATIONS:
                {
                    buffer.append(messages.get("ShortcutPanel.location.applications"));
                    break;
                }

                case Shortcut.START_MENU:
                {
                    buffer.append(messages.get("ShortcutPanel.location.startMenu"));
                    break;
                }

                case Shortcut.START_UP:
                {
                    buffer.append(messages.get("ShortcutPanel.location.startup"));
                    break;
                }
            }

            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.description"));
            buffer.append(data.description);
            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.target"));
            buffer.append(data.target);
            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.command"));
            buffer.append(data.commandLine);
            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.iconName"));
            buffer.append(data.iconFile);
            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.iconIndex"));
            buffer.append(data.iconIndex);
            buffer.append(newline);

            buffer.append(messages.get("ShortcutPanel.textFile.work"));
            buffer.append(data.workingDirectory);
            buffer.append(newline);

            buffer.append(newline);
            buffer.append(SEPARATOR_LINE);
            buffer.append(newline);
            buffer.append(newline);
        }

        try
        {
            output.write(buffer.toString());
        }
        catch (Throwable exception)
        {
        }
        finally
        {
            try
            {
                output.flush();
                output.close();
                files.add(file.getPath());
            }
            catch (Throwable exception)
            {
                // not really anything I can do here, maybe should show a dialog that
                // tells the user that installDataGUI might not have been saved completely!?
            }
        }
    }

    /**
     * @param groupName Name of the group where the shortcuts are placed in
     */
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    /**
     * @return the name of the group where the shortcuts are placed in
     */
    public String getGroupName()
    {
        return groupName;
    }

    /**
     * @param userType {@link Shortcut#CURRENT_USER} {@link Shortcut#ALL_USERS}
     */
    public void setUserType(int userType)
    {
        this.userType = userType;
        shortcut.setUserType(this.userType);
    }

    /**
     * Initialises the user type.
     *
     * @return {@code true} if the current user has permissions to write to the All Users programs folder.
     */
    public boolean initUserType()
    {
        File dir = getProgramsFolder(Shortcut.ALL_USERS);

        logger.fine("All Users Program Folder: '" + dir + "'");
        boolean writable = isWritable(dir);
        logger.fine((writable ? "Can" : "Cannot") + " write into '" + dir + "'");

        boolean allUsers = !isDefaultCurrentUserFlag() && writable;

        int type = (allUsers) ? Shortcut.ALL_USERS : Shortcut.CURRENT_USER;
        setUserType(type);
        return writable;
    }

    /**
     * Determines if the desktop shortcut checkbox is enabled.
     *
     * @return {@code true} if the desktop shortcut checkbox is enabled
     */
    public boolean isDesktopShortcutCheckboxSelected()
    {
        return Boolean.valueOf(installData.getVariable("DesktopShortcutCheckboxEnabled"));
    }

    /**
     * Helper to format a message to create shortcuts for the current platform.
     *
     * @return a formatted message
     */
    public String getCreateShortcutsPrompt()
    {
        Messages messages = installData.getMessages();
        String menuKind = messages.get("ShortcutPanel.regular.StartMenu:Start-Menu");

        if (installData.getPlatform().isA(UNIX) && UnixHelper.kdeIsInstalled())
        {
            menuKind = messages.get("ShortcutPanel.regular.StartMenu:K-Menu");
        }

        return StringTool.replace(messages.get("ShortcutPanel.regular.create"), "StartMenu", menuKind);
    }

    /**
     * Helper to return a prompt to create desktop shortcuts.
     *
     * @return the desktop shortcut prompt
     */
    public String getCreateDesktopShortcutsPrompt()
    {
        return installData.getMessages().get("ShortcutPanel.regular.desktop");
    }

    public String getCreateForUserPrompt()
    {
        return installData.getMessages().get("ShortcutPanel.regular.userIntro");
    }

    public String getCreateForAllUsersPrompt()
    {
        return installData.getMessages().get("ShortcutPanel.regular.allUsers");
    }

    public String getCreateForCurrentUserPrompt()
    {
        return installData.getMessages().get("ShortcutPanel.regular.allUsers");
    }

    /**
     * Determines if a directory can be written to.
     *
     * @param dir the directory
     * @return {@code true} if the directory can be written to
     */
    private boolean isWritable(File dir)
    {
        boolean result = false;
        File test;
        try
        {
            test = File.createTempFile("shortcut", "", dir);
            FileUtils.delete(test);
            result = true;
        }
        catch (IOException exception)
        {
            logger.log(Level.WARNING, "Cannot write to '" + dir + "'", exception);
        }
        return result;
    }

    private void addToUninstaller()
    {
        for (String file : files)
        {
            uninstallData.addFile(file, true);
        }
    }

    /**
     * This method analyzes the specifications for creating shortcuts and builds a list of all the
     * Shortcuts that need to be created.
     */
    private void analyzeShortcutSpec()
    {
        if (!haveShortcutSpec)
        {
            createShortcuts = false;
            return;
        }

        IXMLElement skipper = spec.getFirstChildNamed(SPEC_KEY_SKIP_IFNOT_SUPPORTED);
        skipIfNotSupported = (skipper != null);

        // set flag if 'defaultCurrentUser' element found:
        defaultCurrentUserFlag = (spec.getFirstChildNamed(SPEC_KEY_DEF_CUR_USER) != null);

        // ----------------------------------------------------
        // find out if we should simulate a not supported
        // scenario
        // ----------------------------------------------------
        IXMLElement support = spec.getFirstChildNamed(SPEC_KEY_NOT_SUPPORTED);

        if (support != null)
        {
            simulateNotSupported = true;
        }

        // set flag if 'lateShortcutInstall' element found:
        setCreateShortcutsImmediately(spec.getFirstChildNamed(SPEC_KEY_LATE_INSTALL) == null);

        // ----------------------------------------------------
        // find out in which program group the shortcuts should
        // be placed and where this program group should be
        // located
        // ----------------------------------------------------

        IXMLElement group = null;
        List<IXMLElement> groupSpecs = spec.getChildrenNamed(SPEC_KEY_PROGRAM_GROUP);
        String selectedInstallGroup = this.installData.getVariable("INSTALL_GROUP");
        if (selectedInstallGroup != null)
        {
            // The user selected an InstallGroup before.
            // We may have some restrictions on the Installationgroup
            // search all defined ProgramGroups for the given InstallGroup
            for (IXMLElement g : groupSpecs)
            {
                String instGrp = g.getAttribute(SPEC_ATTRIBUTE_INSTALLGROUP);
                if (instGrp != null && selectedInstallGroup.equalsIgnoreCase(instGrp))
                {
                    group = g;
                    break;
                }
            }
        }
        if (group == null)
        {
            // default (old) behavior
            group = spec.getFirstChildNamed(SPEC_KEY_PROGRAM_GROUP);
        }

        String location = null;
        hasDesktopShortcuts = false;

        if (group != null)
        {
            suggestedProgramGroup = group.getAttribute(SPEC_ATTRIBUTE_DEFAULT_GROUP, "");
            programGroupIconFile = group.getAttribute("iconFile", "");
            programGroupComment = group.getAttribute("comment", "");
            location = group.getAttribute(SPEC_ATTRIBUTE_LOCATION, SPEC_VALUE_APPLICATIONS);
        }
        else
        {
            suggestedProgramGroup = "";
            location = SPEC_VALUE_APPLICATIONS;
        }

        try
        {
            if (location.equals(SPEC_VALUE_APPLICATIONS))
            {
                shortcut.setLinkType(Shortcut.APPLICATIONS);
            }
            else if (location.equals(SPEC_VALUE_START_MENU))
            {
                shortcut.setLinkType(Shortcut.START_MENU);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            // ignore
        }

        // ----------------------------------------------------
        // create a list of all shortcuts that need to be
        // created, containing all details about each shortcut
        // ----------------------------------------------------
        // String temp;
        List<IXMLElement> shortcutSpecs = spec.getChildrenNamed(SPEC_KEY_SHORTCUT);
        ShortcutData data;

        shortcuts = new ArrayList<ShortcutData>();
        files = new ArrayList<String>();
        execFiles = new ArrayList<ExecutableFile>();

        for (IXMLElement shortcutSpec : shortcutSpecs)
        {
            if (!matcher.matchesCurrentPlatform(OsConstraintHelper.getOsList(shortcutSpec)))
            {
                continue;
            }

            logger.fine("Checking Condition for " + shortcutSpec.getAttribute(SPEC_ATTRIBUTE_NAME));
            if (!checkConditions(shortcutSpec))
            {
                continue;
            }

            logger.fine("Checked Condition for " + shortcutSpec.getAttribute(SPEC_ATTRIBUTE_NAME));
            data = new ShortcutData();

            data.name = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_NAME);
            data.subgroup = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_SUBGROUP, "");
            data.description = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_DESCRIPTION, "");

            // ** Linux **//
            data.deskTopEntryLinux_Encoding = shortcutSpec
                    .getAttribute(SPEC_ATTRIBUTE_ENCODING, "");
            data.deskTopEntryLinux_MimeType = shortcutSpec
                    .getAttribute(SPEC_ATTRIBUTE_MIMETYPE, "");
            data.deskTopEntryLinux_Terminal = shortcutSpec
                    .getAttribute(SPEC_ATTRIBUTE_TERMINAL, "");
            data.deskTopEntryLinux_TerminalOptions = shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_TERMINAL_OPTIONS, "");
            data.deskTopEntryLinux_Type = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TYPE, "");

            data.deskTopEntryLinux_URL = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_URL, "");

            data.deskTopEntryLinux_X_KDE_SubstituteUID = shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_KDE_SUBST_UID, "false");

            data.deskTopEntryLinux_X_KDE_UserName = shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_KDE_USERNAME, "root");

            data.Categories = shortcutSpec.getAttribute(SPEC_CATEGORIES, "");

            data.TryExec = shortcutSpec.getAttribute(SPEC_TRYEXEC, "");

            data.createForAll = Boolean.valueOf(shortcutSpec.getAttribute(CREATE_FOR_ALL, "false"));

            // ** EndOf LINUX **//
            // temp =
            data.target = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TARGET, ""));

            // temp =
            data.commandLine = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_COMMAND, "");

            // temp =
            data.iconFile = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ICON, ""));
            data.iconIndex = Integer.parseInt(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ICON_INDEX,
                                                                        "0"));

            // temp =
            data.workingDirectory = fixSeparatorChar(shortcutSpec.getAttribute(
                    SPEC_ATTRIBUTE_WORKING_DIR, ""));

            String initialState = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_INITIAL_STATE, "");

            if (initialState.equals(SPEC_VALUE_NO_SHOW))
            {
                data.initialState = Shortcut.HIDE;
            }
            else if (initialState.equals(SPEC_VALUE_NORMAL))
            {
                data.initialState = Shortcut.NORMAL;
            }
            else if (initialState.equals(SPEC_VALUE_MAXIMIZED))
            {
                data.initialState = Shortcut.MAXIMIZED;
            }
            else if (initialState.equals(SPEC_VALUE_MINIMIZED))
            {
                data.initialState = Shortcut.MINIMIZED;
            }
            else
            {
                data.initialState = Shortcut.NORMAL;
            }
            data.runAsAdministrator = Boolean.valueOf(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_RUN_AS_ADMINISTRATOR,
                                                                                "false"));

            // LOG System.out.println("installDataGUI.initialState: " +
            // installDataGUI.initialState);

            // --------------------------------------------------
            // if the minimal installDataGUI requirements are met to create
            // the shortcut, create one entry each for each of
            // the requested types.
            // Eventually this will cause the creation of one
            // shortcut in each of the associated locations.
            // --------------------------------------------------
            // without a name we can not create a shortcut
            if (data.name == null)
            {
                continue;
            }

            // 1. Elmar: "Without a target we can not create a shortcut."
            // 2. Marc: "No, Even on Linux a Link can be an URL and has no target."
            if (data.target == null)
            {
                // TODO: write log info INFO.warn( "Shortcut: " + installDataGUI + " has no target"
                // );
                data.target = "";
            }
            // the shortcut is not actually required for any of the selected packs

            // the shortcut is not actually required for any of the selected packs // the shortcut
            // is not actually required for any of the selected packs
            List<IXMLElement> forPacks = shortcutSpec.getChildrenNamed(SPEC_KEY_PACKS);

            if (!shortcutRequiredFor(forPacks))
            {
                continue;
            }
            // --------------------------------------------------
            // This section is executed if we don't skip.
            // --------------------------------------------------
            // For each of the categories set the type and if
            // the link should be placed in the program group,
            // then clone the installDataGUI set to obtain an independent
            // instance and add this to the list of shortcuts
            // to be created. In this way, we will set up an
            // identical copy for each of the locations at which
            // a shortcut should be placed. Therefore you must
            // not use 'else if' statements!
            // --------------------------------------------------
            {
                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_DESKTOP))
                {
                    hasDesktopShortcuts = true;
                    data.addToGroup = false;
                    data.type = Shortcut.DESKTOP;
                    shortcuts.add(data.clone());
                }

                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_APPLICATIONS))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.APPLICATIONS;
                    shortcuts.add(data.clone());
                }

                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_START_MENU))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.START_MENU;
                    shortcuts.add(data.clone());
                }

                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_STARTUP))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.START_UP;
                    shortcuts.add(data.clone());
                }

                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_PROGRAM_GROUP))
                {
                    data.addToGroup = true;
                    data.type = Shortcut.APPLICATIONS;
                    shortcuts.add(data.clone());
                }
            }
        }

        // ----------------------------------------------------
        // signal if there are any shortcuts to create
        // ----------------------------------------------------
        if (shortcuts.size() > 0)
        {
            createShortcuts = true;
        }
    }

    /**
     * This returns true if a Shortcut should or can be created. Returns false to suppress Creation
     *
     * @param shortcutSpec
     * @return true if condtion is resolved positive
     */
    private boolean checkConditions(IXMLElement shortcutSpec)
    {
        boolean result = true;
        String conditionid = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_CONDITION);
        if (conditionid != null)
        {
            result = installData.getRules().isConditionTrue(conditionid);
        }
        // Vector conditions = shortcutSpec.getChildrenNamed( Condition.CONDITION );
        //
        // for( int i = 0; i < conditions.size(); i++ ) { Condition condition = new Condition(
        // conditions.elementAt( i ) );
        //
        // //System.out.println( "Readed: " + condition.toString( true ) ); boolean result =
        // condition.eval();
        //
        // if( result == false ) { System.out.println( "Unresolved Condition: " + condition );
        //
        // return result; } }

        return result; // If there is no Condition defined, just create the shortcut.
    }

    /**
     * Creates all shortcuts based on the information in shortcuts.
     */
    private void createShortcuts()
    {
        if (!createShortcuts)
        {
            logger.warning("No shortcuts to be created");
            return;
        }

        String gn;

        List<String> startMenuShortcuts = new ArrayList<String>();
        for (ShortcutData data : shortcuts)
        {
            try
            {
                gn = groupName + data.subgroup;
                shortcut.setUserType(userType);
                shortcut.setLinkName(data.name);
                shortcut.setLinkType(data.type);
                shortcut.setArguments(data.commandLine);
                shortcut.setDescription(data.description);
                shortcut.setIconLocation(data.iconFile, data.iconIndex);

                shortcut.setShowCommand(data.initialState);
                shortcut.setTargetPath(data.target);
                shortcut.setWorkingDirectory(data.workingDirectory);
                shortcut.setEncoding(data.deskTopEntryLinux_Encoding);
                shortcut.setMimetype(data.deskTopEntryLinux_MimeType);
                shortcut.setRunAsAdministrator(data.runAsAdministrator);

                shortcut.setTerminal(data.deskTopEntryLinux_Terminal);
                shortcut.setTerminalOptions(data.deskTopEntryLinux_TerminalOptions);
                shortcut.setType(data.deskTopEntryLinux_Type);
                shortcut.setKdeSubstUID(data.deskTopEntryLinux_X_KDE_SubstituteUID);
                shortcut.setKdeUserName(data.deskTopEntryLinux_X_KDE_UserName);
                shortcut.setURL(data.deskTopEntryLinux_URL);
                shortcut.setTryExec(data.TryExec);
                shortcut.setCategories(data.Categories);
                shortcut.setCreateForAll(data.createForAll);

                shortcut.setUninstaller(uninstallData);

                if (data.addToGroup)
                {
                    shortcut.setProgramGroup(gn);
                }
                else
                {
                    shortcut.setProgramGroup("");
                }

                try
                {
                    // ----------------------------------------------
                    // save the shortcut only if it is either not on
                    // the desktop or if it is on the desktop and
                    // the user has signalled that it is ok to place
                    // shortcuts on the desktop.
                    // ----------------------------------------------
                    if ((data.type != Shortcut.DESKTOP)
                            || ((data.type == Shortcut.DESKTOP) && createDesktopShortcuts))
                    {
                        // save the shortcut
                        shortcut.save();

                        if (data.type == Shortcut.APPLICATIONS || data.addToGroup)
                        {
                            if (shortcut instanceof com.izforge.izpack.util.os.Unix_Shortcut)
                            {
                                com.izforge.izpack.util.os.Unix_Shortcut unixcut = (com.izforge.izpack.util.os.Unix_Shortcut) shortcut;
                                String f = unixcut.getWrittenFileName();
                                if (f != null)
                                {
                                    startMenuShortcuts.add(f);
                                }
                            }
                        }
                        // add the file and directory name to the file list
                        String fileName = shortcut.getFileName();
                        files.add(0, fileName);

                        File file = new File(fileName);
                        File base = new File(shortcut.getBasePath());
                        Vector<File> intermediates = new Vector<File>();

                        // String directoryName = shortcut.getDirectoryCreated ();
                        execFiles.add(new ExecutableFile(fileName, ExecutableFile.UNINSTALL,
                                                         ExecutableFile.IGNORE, new ArrayList<OsModel>(), false));

                        files.add(fileName);

                        while ((file = file.getParentFile()) != null)
                        {
                            if (file.equals(base))
                            {
                                break;
                            }

                            intermediates.add(file);
                        }

                        if (file != null)
                        {
                            Enumeration<File> filesEnum = intermediates.elements();

                            while (filesEnum.hasMoreElements())
                            {
                                files.add(0, filesEnum.nextElement().toString());
                            }
                        }
                    }
                }
                catch (Exception exception)
                {
                }
            }
            catch (Throwable exception)
            {
            }
        }
        if (OsVersion.IS_UNIX)
        {
            writeXDGMenuFile(startMenuShortcuts, groupName, programGroupIconFile,
                             programGroupComment);
        }
        shortcut.execPostAction();

        try
        {
            if (execFiles != null)
            {
                FileExecutor executor = new FileExecutor(execFiles);

                //
                // TODO: Hi Guys,
                // TODO The following commented-out line sometimes produces an uncatchable
                // nullpointer Exception!
                // TODO evaluate for what reason the files should exec.
                // TODO if there is a serious explanation, why to do that,
                // TODO the code must be more robust
                // evaluate executor.executeFiles( ExecutableFile.NEVER, null );
            }
        }
        catch (NullPointerException nep)
        {
            nep.printStackTrace();
        }
        catch (RuntimeException cannot)
        {
            cannot.printStackTrace();
        }
        shortcut.cleanUp();
    }

    private String createXDGDirectory(String menuName, String icon, String comment)
    {
        String menuDirectoryDescriptor = "[Desktop Entry]\n" + "Name=$Name\n"
                + "Comment=$Comment\n" + "Icon=$Icon\n" + "Type=Directory\n" + "Encoding=UTF-8";
        menuDirectoryDescriptor = StringTool.replace(menuDirectoryDescriptor, "$Name", menuName);
        menuDirectoryDescriptor = StringTool.replace(menuDirectoryDescriptor, "$Comment", comment);
        menuDirectoryDescriptor = StringTool.replace(menuDirectoryDescriptor, "$Icon", icon);
        return menuDirectoryDescriptor;
    }

    private String createXDGMenu(List<String> shortcutFiles, String menuName)
    {
        String menuConfigText = "<Menu>\n" + "<Name>Applications</Name>\n" + "<Menu>\n"
                +
                // Ubuntu can't handle spaces, replace with "-"
                "<Directory>" + menuName.replaceAll(" ", "-") + "-izpack.directory</Directory>\n"
                + "<Name>" + menuName + "</Name>\n" + "<Include>\n";

        for (String shortcutFile : shortcutFiles)
        {
            menuConfigText += "<Filename>" + shortcutFile + "</Filename>\n";
        }
        menuConfigText += "</Include>\n</Menu>\n</Menu>";
        return menuConfigText;

    }

    private String fixSeparatorChar(String path)
    {
        String newPath = path.replace('/', File.separatorChar);
        newPath = newPath.replace('\\', File.separatorChar);

        return (newPath);
    }

    /**
     * Reads the XML specification for the shortcuts to create. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */

    private void readShortcutSpec() throws Exception
    {
        // open an input stream
        InputStream input = null;

        try
        {
            input = resources.getInputStream(TargetFactory.getCurrentOSPrefix() + SPEC_FILE_NAME);
        }
        catch (ResourceNotFoundException rnfE)
        {
            input = resources.getInputStream(SPEC_FILE_NAME);
        }
        if (input == null)
        {
            haveShortcutSpec = false;

            return;
        }

        // input.
        VariableSubstitutor replacer = new VariableSubstitutorImpl(installData.getVariables());
        String substitutedSpec = replacer.substitute(input, SubstitutionType.TYPE_XML);
        /*
         * TODO: internal flag mapped if( installData.isDebug() ) { System.out.println( "SUBSTITUDED
         * SHORTCUT SPEC" ); System.out.println(
         * "==================================================================" );
         * System.out.println( "=================================================================="
         * ); System.out.println( substitutedSpec ); System.out.println(
         * "==================================================================" );
         * System.out.println( "=================================================================="
         * ); }
         */
        IXMLParser parser = new XMLParser();

        // get the installDataGUI
        spec = parser.parse(substitutedSpec);

        // close the stream
        input.close();
        haveShortcutSpec = true;
    }

    /**
     * Verifies if the shortcut is required for any of the packs listed. The shortcut is required
     * for a pack in the list if that pack is actually selected for installation. Note: If the list
     * of selected packs is empty then true is always returnd. The same is true if the packs list is
     * empty.
     *
     * @param packs a Vector of Strings. Each of the strings denotes a pack for which the schortcut
     *              should be created if the pack is actually installed.
     * @return true if the shortcut is required for at least on pack in the list, otherwise returns
     *         false.
     */
    private boolean shortcutRequiredFor(List<IXMLElement> packs)
    {
        String selected;
        String required;

        if (packs.size() == 0)
        {
            return (true);
        }

        for (int i = 0; i < this.installData.getSelectedPacks().size(); i++)
        {
            selected = this.installData.getSelectedPacks().get(i).getName();

            for (IXMLElement pack : packs)
            {
                required = pack.getAttribute(SPEC_ATTRIBUTE_NAME, "");
                if (selected.equals(required))
                {
                    return (true);
                }
            }
        }

        return (false);
    }

    private void writeString(String str, String file)
    {
        boolean failed = false;
        try
        {
            FileWriter writer = new FileWriter(file);
            writer.write(str);
            writer.close();
        }
        catch (Exception ignore)
        {
            failed = true;
            logger.warning("Failed to create Gnome menu");
        }
        if (!failed)
        {
            uninstallData.addFile(file, true);
        }
    }

    private void writeXDGMenuFile(List<String> desktopFileNames, String groupName, String icon,
                                  String comment)
    {
        if ("".equals(suggestedProgramGroup) || suggestedProgramGroup == null)
        {
            return; // No group
            // name
            // means
            // the
            // shortcuts
        }
        // will be placed by category
        if (OsVersion.IS_UNIX)
        {
            String menuFile = createXDGMenu(desktopFileNames, groupName);
            String dirFile = createXDGDirectory(groupName, icon, comment);
            String menuFolder;
            String gnome3MenuFolder;
            String directoryFolder;
            if (userType == Shortcut.ALL_USERS)
            {
                menuFolder = "/etc/xdg/menus/applications-merged/";
                gnome3MenuFolder = "/etc/xdg/menus/applications-gnome-merged/";
                directoryFolder = "/usr/share/desktop-directories/";
            }
            else
            {
                menuFolder = System.getProperty("user.home") + File.separator
                        + ".config/menus/applications-merged/";
                gnome3MenuFolder = System.getProperty("user.home") + File.separator
                        + ".config/menus/applications-gnome-merged/";
                directoryFolder = System.getProperty("user.home") + File.separator
                        + ".local/share/desktop-directories/";
            }
            File menuFolderFile = new File(menuFolder);
            File gnome3MenuFolderFile = new File(gnome3MenuFolder);
            File directoryFolderFile = new File(directoryFolder);
            String menuFilePath = menuFolder + groupName + ".menu";
            String gnome3MenuFilePath = gnome3MenuFolder + groupName + ".menu";
            // Ubuntu can't handle spaces in the directory file name
            String dirFilePath = directoryFolder + groupName.replaceAll(" ", "-")
                    + "-izpack.directory";
            menuFolderFile.mkdirs();
            gnome3MenuFolderFile.mkdirs();
            directoryFolderFile.mkdirs();
            writeString(menuFile, menuFilePath);
            writeString(menuFile, gnome3MenuFilePath);
            writeString(dirFile, dirFilePath);
        }
    }

    /**
     * Creates the Shortcuts after files have been installed. Used to support
     * {@code &lt;lateShortcutInstall/&gt;} to allow placement of ShortcutPanel before the
     * installation of the files.
     *
     * @author Marcus Schlegel, Pulinco, Daniel Abson
     */
    protected class LateShortcutInstallListener extends AbstractInstallerListener
    {

        /**
         * Triggers the creation of shortcuts.
         * {@inheritDoc}
         */
        @Override
        public void afterPacks(List<Pack> packs, ProgressListener listener)
        {
            try
            {
                createAndRegisterShortcuts();
            }
            catch (Exception exception)
            {
                throw new IzPackException("Failed to create shortcuts", exception);
            }
        }
    }
}
