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
import com.izforge.izpack.util.*;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.Shortcut;
import com.izforge.izpack.util.unix.UnixHelper;
import com.izforge.izpack.util.xml.XMLHelper;
import static com.izforge.izpack.panels.shortcut.ShortcutConstants.*;

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
     * Tells wether to skip if the platform is not supported.
     */
    private boolean skipIfNotSupported = false;

    /**
     * the one shortcut instance for reuse in many locations
     */
    private Shortcut shortcut;

    /**
     * A list of all <ShortcutData> objects, excluding those that should be placed on the desktop.
     * Each object is the complete specification for one shortcut that must be created.
     */
    private List<ShortcutData> shortcuts;

    /**
     * A list of <ShortcutData> objects that should be placed on the desktop.
     * Each object is the complete specification for one shortcut that must be created.
     */
    private List<ShortcutData> desktopShortcuts;

    /**
     * A list of <ShortcutData> objects that should be placed in the startup location.
     * Each object is the complete specification for one shortcut that must be created.
     */
    private List<ShortcutData> startupShortcuts;

    /**
     * Holds a list of all the shortcut files that have been created. Note: this variable contains
     * valid data only after createMenuShortcuts() has been called. This list is created so that the
     * files can be added to the uninstaller.
     */
    private List<String> files;

    /**
     * Holds a list of all executables to set the executable flag alter shortcut createn.
     */
    private List<ExecutableFile> execFiles;

    /**
     * If true it indicates that there are shortcuts to create. The value is set by
     * createShortcutData()
     */
    private boolean createMenuShortcuts = false;

    private boolean createShortcuts = false;

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

    private boolean createStartupShortcuts;

    private boolean defaultCurrentUserFlag = false;

    private boolean createShortcutsImmediately = true;

    private boolean allowProgramGroup;

    Vector<String> defaultGroup;

    private Platform platform;

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
                              PlatformModelMatcher matcher) throws Exception
    {
        this.matcher = matcher;
        this.resources = resources;
        this.installData = installData;
        this.uninstallData = uninstallData;
        this.shortcut = factory.makeObject(Shortcut.class);
        this.shortcut.initialize(Shortcut.APPLICATIONS, "-");

        if (!isCreateShortcutsImmediately())
        {
            listeners.add(new LateShortcutInstallListener());
        }
        this.defaultGroup = new Vector<String>();
        defaultGroup.add(DEFAULT_FOLDER);

        housekeeper.registerForCleanup(this);
    }

    /**
     * Refresh the shortcut data.
     * Should be done every time the shortcut panel is visited as variables can change,
     * and we need to make the appropriate substitutions.
     *
     * @throws Exception
     */
    public void refreshShortcutData() throws  Exception
    {
        IXMLElement spec = readShortcutSpec();
        loadClassData(spec);
        createShortcutData(spec);
    }
    /**
     * Creates the shortcuts
     *
     * @throws Exception
     */
    public void createAndRegisterShortcuts()
    {
        if(createMenuShortcuts)
        {
            createShortcuts(shortcuts);
        }
        if (createDesktopShortcuts)
        {
            createShortcuts(desktopShortcuts);
        }
        if (createStartupShortcuts)
        {
            createShortcuts(startupShortcuts);
        }
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

    public Vector<String> getDefaultGroup()
    {
        return defaultGroup;
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
     * @param panelRoot 
     * @return a list of xml child elements to write a autoinstall.xml file for later execution
     */
    public List<IXMLElement> getAutoinstallXMLData(IXMLElement panelRoot)
    {
        List<IXMLElement> xmlData = new ArrayList<IXMLElement>();

        /** For backwards compatibility reasons */
        IXMLElement dataElement;
        dataElement = new XMLElementImpl(AUTO_KEY_CREATE_MENU_SHORTCUTS, panelRoot);

        //Menu Information
        dataElement.setContent(Boolean.toString(createMenuShortcuts));
        xmlData.add(dataElement);

        //Program Group Information
        dataElement = new XMLElementImpl(AUTO_KEY_PROGRAM_GROUP, panelRoot);
        dataElement.setContent(getGroupName());
        xmlData.add(dataElement);

        //Desktop Information
        dataElement = new XMLElementImpl(AUTO_KEY_CREATE_DESKTOP_SHORTCUTS, panelRoot);
        dataElement.setContent(Boolean.toString(createDesktopShortcuts));
        xmlData.add(dataElement);

        //Startup Information
        dataElement = new XMLElementImpl(AUTO_KEY_CREATE_STARTUP_SHORTCUTS, panelRoot);
        dataElement.setContent(Boolean.toString(createStartupShortcuts));
        xmlData.add(dataElement);

        //User Information
        dataElement = new XMLElementImpl(AUTO_KEY_SHORTCUT_TYPE, panelRoot);
        String userTypeString = AUTO_KEY_SHORTCUT_TYPE_VALUE_USER;
        if (getUserType() == Shortcut.ALL_USERS)
        {
            userTypeString = AUTO_KEY_SHORTCUT_TYPE_VALUE_ALL;
        }
        dataElement.setContent(userTypeString);
        xmlData.add(dataElement);

        return xmlData;
    }

    /**
     * Reads the xml content for automated installations.
     *
     * @param panelRoot specifies the xml elemnt for this panel
     */
    public void setAutoinstallXMLData(IXMLElement panelRoot)
    {
        IXMLElement dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_SHORTCUTS_LEGACY);

        //Support of legacy auto installation files
        if (dataElement != null)
        {
            setCreateMenuShortcuts(Boolean.valueOf(dataElement.getContent()).booleanValue());

            if (isCreateMenuShortcuts())
            {
                setCreateStartupShortcuts(true);

                dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_PROGRAM_GROUP);
                setGroupName(dataElement.getContent());
                dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_DESKTOP_SHORTCUTS);
                setCreateDesktopShortcuts(Boolean.valueOf(dataElement.getContent()));
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
        //Current autoamtic installation file parser
        else
        {
            //Set all the shortcut types
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_MENU_SHORTCUTS);
            setCreateMenuShortcuts(Boolean.valueOf(dataElement.getContent()));
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_DESKTOP_SHORTCUTS);
            setCreateDesktopShortcuts(Boolean.valueOf(dataElement.getContent()));
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_CREATE_STARTUP_SHORTCUTS);
            setCreateStartupShortcuts(Boolean.valueOf(dataElement.getContent()));

            //Set program group
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_PROGRAM_GROUP);
            setGroupName(dataElement.getContent());

            //Set user information
            dataElement = panelRoot.getFirstChildNamed(AUTO_KEY_SHORTCUT_TYPE);
            setUserType(Shortcut.ALL_USERS);
            if (AUTO_KEY_SHORTCUT_TYPE_VALUE_USER.equals(dataElement.getContent()))
            {
                setUserType(Shortcut.CURRENT_USER);
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
        return desktopShortcuts.size() > 0;
    }

    public boolean hasStartupShortcuts()
    {
        return startupShortcuts.size() > 0;
    }

    /**
     * @param createDesktopShortcuts
     */
    public void setCreateDesktopShortcuts(boolean createDesktopShortcuts)
    {
        this.createDesktopShortcuts = createDesktopShortcuts;
    }

    public void setCreateStartupShortcuts(boolean createStartupShortcuts)
    {
        this.createStartupShortcuts = createStartupShortcuts;
    }

    /**
     * @return <code>true</code> if we create shortcuts at all otherwise <code>false</code>
     */
    public boolean isCreateMenuShortcuts()
    {
        return createMenuShortcuts;
    }

    /**
     * @param createMenuShortcuts
     */
    public final void setCreateMenuShortcuts(boolean createMenuShortcuts)
    {
        this.createMenuShortcuts = createMenuShortcuts;
    }

    /**
     * @return <code>true</code> if we skip shortcut panel and shortcut creation if this is not
     *         supported on the current OS otherwise <code>false</code>
     */
    public boolean skipIfNotSupported()
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
     * TODO: Show an error dialog if fail to write
     * @param file to save the information to
     */
    public void saveToFile(File file)
    {
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
            return;
        }

        /**
         *  Break the header down into multiple lines based on '\n' line breaks.
         */
        int nextIndex;
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

    public boolean isStartupShortcutCheckboxSelected()
    {
        return Boolean.valueOf(installData.getVariable("StartupShortcutCheckboxEnabled"));
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

    public String getCreateStartupShortcutsPrompt()
    {
        return installData.getMessages().get("ShortcutPanel.regular.startup");
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
        return installData.getMessages().get("ShortcutPanel.regular.currentUser");
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

    private void loadClassData(IXMLElement spec)
    {
        if (spec == null)
        {
            createShortcuts = false;
            return;
        }

        /**
         * 1.
         * 2. Set flag if 'defaultCurrentUser' element found
         * 3. Find out if we should simulate a not supported scenario
         * 4. Set flag if 'lateShortcutInstall' element found
         */
        simulateNotSupported = (spec.getFirstChildNamed(SPEC_KEY_NOT_SUPPORTED) != null);
        defaultCurrentUserFlag = (spec.getFirstChildNamed(SPEC_KEY_DEF_CUR_USER) != null);
        skipIfNotSupported = (spec.getFirstChildNamed(SPEC_KEY_SKIP_IFNOT_SUPPORTED) != null);
        setCreateShortcutsImmediately(spec.getFirstChildNamed(SPEC_KEY_LATE_INSTALL) == null);


        /**
         * Find out in which program group the shortcuts should
         * be placed and where this program group should be located
         */
        IXMLElement group = null;
        List<IXMLElement> groupSpecs = spec.getChildrenNamed(SPEC_KEY_PROGRAM_GROUP);
        String selectedInstallGroup = this.installData.getVariable("INSTALL_GROUP");
        if (selectedInstallGroup != null)
        {
            // The user selected an InstallGroup before.
            // We may have some restrictions on the installation group
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
            group = spec.getFirstChildNamed(SPEC_KEY_PROGRAM_GROUP);
        }

        String location;
        if (group != null)
        {
            programGroupComment = group.getAttribute("comment", "");
            programGroupIconFile = group.getAttribute("iconFile", "");
            suggestedProgramGroup = group.getAttribute(SPEC_ATTRIBUTE_DEFAULT_GROUP, "");
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
    }
    /**
     * This method analyzes the specifications for creating shortcuts and builds a list of all the
     * Shortcuts that need to be created.
     */
    private void createShortcutData(IXMLElement spec)
    {
        if (spec == null)
        {
            return;
        }

        /**
         * Create a list of all shortcuts that need to be
         * created, containing all details about each shortcut
         */
        ShortcutData data;
        List<IXMLElement> shortcutSpecs = spec.getChildrenNamed(SPEC_KEY_SHORTCUT);


        files = new ArrayList<String>();
        execFiles = new ArrayList<ExecutableFile>();

        shortcuts = new ArrayList<ShortcutData>();
        desktopShortcuts = new ArrayList<ShortcutData>();
        startupShortcuts = new ArrayList<ShortcutData>();
        allowProgramGroup = false;
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
            data.deskTopEntryLinux_URL = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_URL, "");
            data.deskTopEntryLinux_Type = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TYPE, "");
            data.deskTopEntryLinux_Encoding = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ENCODING, "");
            data.deskTopEntryLinux_MimeType = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_MIMETYPE, "");

            data.deskTopEntryLinux_Terminal = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TERMINAL, "");
            data.deskTopEntryLinux_TerminalOptions = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TERMINAL_OPTIONS, "");

            data.deskTopEntryLinux_X_KDE_UserName = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_KDE_USERNAME, "root");
            data.deskTopEntryLinux_X_KDE_SubstituteUID = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_KDE_SUBST_UID, "false");


            data.TryExec = shortcutSpec.getAttribute(SPEC_TRYEXEC, "");
            data.Categories = shortcutSpec.getAttribute(SPEC_CATEGORIES, "");
            data.createForAll = Boolean.valueOf(shortcutSpec.getAttribute(CREATE_FOR_ALL, "false"));
            // ** End of Linux **//

            data.commandLine = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_COMMAND, "");
            data.target = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_TARGET, ""));

            data.iconFile = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ICON, ""));
            data.iconIndex = Integer.parseInt(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_ICON_INDEX, "0"));

            data.workingDirectory = fixSeparatorChar(shortcutSpec.getAttribute(SPEC_ATTRIBUTE_WORKING_DIR, ""));

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
            data.runAsAdministrator = Boolean.valueOf(
                    shortcutSpec.getAttribute(SPEC_ATTRIBUTE_RUN_AS_ADMINISTRATOR, "false"));

            /**
             * If the minimal installDataGUI requirements are met to create the shortcut,
             * create one entry each for each of the requested types.
             * Eventually this will cause the creation of one shortcut in each of the associated locations.
             */

            // without a name we can not create a shortcut
            if (data.name == null)
            {
                logger.warning("Shorcut specification is missing the name attribute");
                continue;
            }

            // 1. Elmar: "Without a target we can not create a shortcut."
            // 2. Marc: "No, Even on Linux a Link can be an URL and has no target."
            if (data.target == null)
            {
                logger.warning("Shortcut " + data.name + "has not target");
                data.target = "";
            }

            // the shortcut is not actually required for any of the selected packs
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
                /**
                 * Attribute: desktop
                 *
                 * 	If the value is true, then a copy of the shortcut is placed on the desktop.
                 * 	On Unix the shortcuts will only be placed on the KDE desktop of the user currently running the installer.
                 * 	For Gnome the user can simply copy the .desktop files from *\/Desktop to /gnome-desktop.
                 */
                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_DESKTOP))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.DESKTOP;
                    desktopShortcuts.add(data.clone());
                }

                /**
                 * Attribute: applications
                 *
                 * If the value is true, then a copy of the shortcut is placed in the applications menu
                 * (if the target operating system supports this). This is the same location as the applications
                 * choice for the programGroup element. Setting applications="true" is equivalent to setting
                 * programGroup="true" on Unix.
                 */
                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_APPLICATIONS))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.APPLICATIONS;
                    shortcuts.add(data.clone());
                }

                /**
                 * Attribute: startMenu
                 *
                 * If the value is true, then a copy of the shortcut is placed directly in the top most menu
                 * that is available for placing application shortcuts. This is not supported on Unix.
                 */
                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_START_MENU))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.START_MENU;
                    shortcuts.add(data.clone());
                }

                /**
                 * Attribute: startup
                 *
                 * If the value is true, then a copy of the shortcut is placed in a location where all
                 * applications get automatically started at OS launch time, if this is available on the target OS.
                 * This is not supported on Unix.
                 */
                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_STARTUP))
                {
                    data.addToGroup = false;
                    data.type = Shortcut.START_UP;
                    startupShortcuts.add(data.clone());
                }

                /**
                 *  Attribute: programGroup
                 *
                 * 	If the value is true, then a copy of this shortcut will be placed in the group menu.
                 * 	On Unix (KDE) this will always be placed on the top level.
                 */
                if (XMLHelper.attributeIsTrue(shortcutSpec, SPEC_ATTRIBUTE_PROGRAM_GROUP))
                {
                    allowProgramGroup = true;
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
     * This returns true if a Shortcut should or can be created.
     * Returns false to suppress Creation
     *
     * @param shortcutSpec
     * @return true if condition is resolved positive
     */
    private boolean checkConditions(IXMLElement shortcutSpec)
    {
        boolean result = true;
        String condition = shortcutSpec.getAttribute(SPEC_ATTRIBUTE_CONDITION);
        if (condition != null)
        {
            result = installData.getRules().isConditionTrue(condition);
        }
        return result;
    }

    /**
     * Creates all shortcuts based on the information in shortcuts.
     */
    private void createShortcuts(List<ShortcutData> shortcuts)
    {
        if(!createShortcuts)
        {
            return;
        }
        String groupName;

        List<String> startMenuShortcuts = new ArrayList<String>();
        for (ShortcutData data : shortcuts)
        {
            try
            {
                groupName = this.groupName + data.subgroup;
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
                    shortcut.setProgramGroup(groupName);
                }
                else
                {
                    shortcut.setProgramGroup("");
                }

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
            catch (Exception exception)
            {
            }
        }
        if (OsVersion.IS_UNIX)
        {
            writeXDGMenuFile(startMenuShortcuts, this.groupName, programGroupIconFile, programGroupComment);
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
     * Attempt to read load specifications from OS specific shortcut specification file.
     * If fail to read from OS specific shortcut specification file, attempt to load general shortcut specification file.
     *
     * @throws Exception for any problems in reading the specification
     * TODO: If internal flag mapped installData.isDebug() print out information on substitutedSpec
     */
    private IXMLElement readShortcutSpec() throws Exception
    {
        IXMLElement spec = null;

        InputStream shortcutSpec = null;
        try
        {
            shortcutSpec = resources.getInputStream(TargetFactory.getCurrentOSPrefix() + SPEC_FILE_NAME);
        }
        catch (ResourceNotFoundException resourceNotFound)
        {
            try
            {
                shortcutSpec = resources.getInputStream(SPEC_FILE_NAME);
            }
            catch (ResourceNotFoundException shortcutsNotFound)
            {
                //Fail on next try block
            }
        }

        try
        {
            VariableSubstitutor replacer = new VariableSubstitutorImpl(installData.getVariables());
            String substitutedSpec = replacer.substitute(shortcutSpec, SubstitutionType.TYPE_XML);
            IXMLParser parser = new XMLParser();
            spec = parser.parse(substitutedSpec);
        }
        catch (Exception e)
        {
            return null;
        }

        shortcutSpec.close();
        return spec;
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

    /**
     * Validate that groupName is a valid directory path
     *
     * @param groupName
     * @return
     */
    public String verifyProgramGroup(String groupName)
    {
        if(!platform.isValidDirectorySyntax(groupName))
        {
                return installData.getMessages().get("ShortcutPanel.group.error");
        }
       return "";
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
     * Shortcut Panel should know what platform it is dealing with.
     * @param platform
     */
    public void setPlatform(Platform platform)
    {
        this.platform = platform;
    }

    /**
     * If specifications were valid than we can create shortcuts.
     * @return
     */
    public boolean canCreateShortcuts()
    {
        return this.createShortcuts;
    }

    public boolean allowProgramGroup()
    {
        return this.allowProgramGroup;
    }
}
