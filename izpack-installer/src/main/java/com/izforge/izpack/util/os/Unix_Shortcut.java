/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://www.izforge.com/izpack/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Marc Eppelmann
 * Copyright 2015 Bill Root
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

/*
 * This represents a Implementation of the KDE/GNOME DesktopEntry.
 * which is standard from
 * "Desktop Entry Standard"
 *  "The format of .desktop files, supported by KDE and GNOME."
 *  http://www.freedesktop.org/standards/desktop-entry-spec/
 *
 *  [Desktop Entry]
 //  Comment=$Comment
 //  Comment[de]=
 //  Encoding=$UTF-8
 //  Exec=$'/home/marc/CPS/tomcat/bin/catalina.sh' run
 //  GenericName=$
 //  GenericName[de]=$
 //  Icon=$inetd
 //  MimeType=$
 //  Name=$Start Tomcat
 //  Name[de]=$Start Tomcat
 //  Path=$/home/marc/CPS/tomcat/bin/
 //  ServiceTypes=$
 //  SwallowExec=$
 //  SwallowTitle=$
 //  Terminal=$true
 //  TerminalOptions=$
 //  Type=$Application
 //  X-KDE-SubstituteUID=$false
 //  X-KDE-Username=$
 *
 */

package com.izforge.izpack.util.os;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.unix.ShellScript;
import com.izforge.izpack.util.unix.UnixHelper;
import com.izforge.izpack.util.unix.UnixUser;
import com.izforge.izpack.util.unix.UnixUsers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * This is the Implementation of the RFC-Based Desktop-Link. Used in KDE and GNOME.
 *
 * @author marc.eppelmann&#064;reddot.de
 * @author Bill Root
 */
@OsName("unix")
@ApplicationScoped
public class Unix_Shortcut extends Shortcut
{
    // ***********************************************************************
    // ~ Static fields/initializers
    // ***********************************************************************

    private static final Logger logger = Logger.getLogger(Unix_Shortcut.class.getName());

    /**
     * version = "$Id$"
     */
    private static final String version = "$Id$";

    /**
     * rev = "$Revision$"
     */
    private static final String rev = "$Revision$";

    /**
     * DESKTOP_EXT = ".desktop"
     */
    private static final String DESKTOP_EXT = ".desktop";

    /**
     * N = "\n"
     */
    private final static String N = "\n";

    /**
     * H = "#"
     */
    private final static String H = "#";

    /**
     * S = " "
     */
    private final static String S = " ";

    /**
     * C = Comment = H+S = "# "
     */
    private final static String C = H + S;

    private static ShellScript rootScript = null;

    private static ShellScript uninstallScript = null;

    private List<UnixUser> users;


    // ***********************************************************************
    // ~ Instance fields
    // ***********************************************************************

    /**
     * The data fields defining the shortcut.
     */
    private String arguments;
    private String categories;
    private String description;
    private String encoding;
    private String iconLocation;
    private String kdeSubstituteUID;
    private String kdeUserName;
    private String linkName;
    private int    linkType;
    private String mimeType;
    private String programGroup;
    private String targetPath;
    private String terminal;
    private String terminalOptions;
    private String type;
    private String url;
    private int    userType;
    private String workingDirectory;

    /**
     * internal String itsFileName
     */
    private String itsFileName;

    /**
     * my Install ShellScript *
     */
    private ShellScript myInstallScript;

    /**
     * Internal Constant: FS = File.separator // *
     */
    private final String FS = File.separator;

    /**
     * Internal Constant: myHome = System.getProperty("user.home") *
     */
    private final String myHome = System.getProperty("user.home");

    /**
     * Cached value from {@link UnixHelper#getSuCommand()}.
     */
    private String su;

    /**
     * Cached value from <tt>UnixHelper.getCustomCommand("xdg-desktop-icon")</tt>.
     */
    private String xdgDesktopIconCmd;

    private String myXdgDesktopIconCmd = null;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The installation data.
     */
    private final InstallData installData;


    // ***********************************************************************
    // ~ Constructors
    // ***********************************************************************

    /**
     * Constructs a <tt>Unix_Shortcut</tt>.
     *
     * @param resources   the resources
     * @param installData the installation data
     */
    @Inject
    public Unix_Shortcut(Resources resources, InstallData installData)
    {
        this.resources = resources;
        this.installData = installData;

        if (rootScript == null)
        {
            rootScript = new ShellScript();
        }
        if (uninstallScript == null)
        {
            uninstallScript = new ShellScript();
        }
        if (myInstallScript == null)
        {
            myInstallScript = new ShellScript();
        }

    }


    // ***********************************************************************
    // ~ Methods
    // ***********************************************************************

    /**
     * Builds contents of desktop file.
     */
    public String build()
    {
        StringBuilder result = new StringBuilder();

        String userLanguage = System.getProperty("user.language", "en");

        result.append("[Desktop Entry]" + N);

        // TODO implement Attribute: X-KDE-StartupNotify=true

        result.append("Categories=").append(categories).append(N);

        result.append("Comment=").append(description).append(N);
        result.append("Comment[").append(userLanguage).append("]=").append(description).append(N);
        if (!encoding.isEmpty()) {
            logger.warning(String.format("using deprecated Desktop Entry key "
                    + "Encoding with value %s", encoding));
            result.append("Encoding=").append(encoding).append(N);
        }

        // this causes too many problems
        // result.append("TryExec=" + $E_QUOT + $Exec + $E_QUOT + S + $Arguments + N);

        if (!targetPath.isEmpty() || !arguments.isEmpty())
        {
            result.append("Exec=");
            result.append(targetPath);
                //escaping needs to be handed more fine-grained (putting ''
                //around everything after `Exec=` is not a solution because it
                //causes invalid .desktop files (`desktop-file-validate` fails
                //due to `error: value "'[...]'" for key "Exec" in group
                //"Desktop Entry" contains a reserved character ''' outside of
                //a quote`)
            if (!arguments.isEmpty())
                result.append(S).append(arguments);
            result.append(N);
        }

        result.append("GenericName=").append(N);
        result.append("GenericName[").append(userLanguage).append("]=").append(N);

        result.append("Icon=").append(iconLocation).append(N);
        result.append("MimeType=").append(mimeType).append(N);
        result.append("Name=").append(linkName).append(N);
        result.append("Name[").append(userLanguage).append("]=").append(linkName).append(N);

        result.append("Path=").append(workingDirectory).append(N);
        result.append("ServiceTypes=").append(N);
        result.append("SwallowExec=").append(N);
        result.append("SwallowTitle=").append(N);

        result.append("Terminal=").append(terminal).append(N);
        if (!terminal.isEmpty() && !terminal.equals("true") && !terminal.equals("false"))
            logger.warning(String.format("Shortcut '%s' has terminal '%s' but should be 'true' or 'false'", linkName, terminal));

        result.append("TerminalOptions=").append(terminalOptions).append(N);

        result.append("Type=").append(type).append(N);
        if (type.equalsIgnoreCase("Link") && url.isEmpty())
                logger.warning(String.format("Shortcut '%s' has type '%s' but URL is empty", linkName, type));

        if (!url.isEmpty())
        {
            result.append("URL=").append(url).append(N);
            if (!type.equalsIgnoreCase("Link"))
                logger.warning(String.format("Shortcut '%s' has URL but type ('%s') is not 'Link'", linkName, type));
        }

        result.append("X-KDE-SubstituteUID=").append(kdeSubstituteUID).append(N);
        result.append("X-KDE-Username=").append(kdeUserName).append(N);
        result.append(N);
        result.append(C + "created by" + S).append(getClass().getName()).append(S).append(rev).append(
                N);
        result.append(C).append(version);

        return result.toString();
    }

    @Override
    public void initialize(int aType, String aName) throws Exception
    {
        this.linkName = aName;
    }

    @Override
    public boolean supported()
    {
        return true;
    }

    @Override
    public String getFileName()
    {
        return (this.itsFileName);
    }

    @Override
    public List<String> getProgramGroups(int userType)
    {
        List<String> groups = new ArrayList<>();
        groups.add("(Default)"); // Should be the same value as DEFAULT_FOLDER from ShortcutConstants

        File kdeShareApplnk = getKdeShareApplnkFolder(userType);

        try
        {
            File[] listing = kdeShareApplnk.listFiles();

            if (listing != null)
            {
                for (File aListing : listing)
                {
                    if (aListing.isDirectory())
                    {
                        groups.add(aListing.getName());
                    }
                }
            }
        }
        catch (Exception e)
        {
            // ignore and return an empty vector.
        }

        return groups;
    }

    @Override
    public String getProgramsFolder(int current_user)
    {
        String result;

        //
        result = getKdeShareApplnkFolder(current_user).toString();

        return result;
    }


    /**
     * Gets the XDG path to place the menu shortcuts
     *
     * @param userType to get for.
     * @return handle to the directory
     */
    private File getKdeShareApplnkFolder(int userType)
    {

        if (userType == Shortcut.ALL_USERS)
        {
            return new File(File.separator + "usr" + File.separator + "share" + File.separator
                                    + "applications");
        }
        else
        {
            return new File(System.getProperty("user.home") + File.separator + ".local"
                                    + File.separator + "share" + File.separator + "applications");
        }

    }


    /**
     * Makes a filename usable in a script by escaping spaces.
     * This should <b>not</b> be used for filenames passed to Java filesystem
     * methods.
     * @param filename filename to process
     * @return escaped filename
     */
    private String makeFilenameScriptable(String filename)
    {
        return filename.replace(" ", "\\ ");
    }
    
    
    /**
     * Quotes a command in order to make it usable as a parameter for, e.g.,
     * {@code su} command.
     * @param cmd command to quote
     * @return quoted command
     */
    private String[] quoteCommand(String... cmd)
    {
        String[] quoted = Arrays.copyOf(cmd, cmd.length);
        quoted[0] = "\"" + cmd[0];
        quoted[cmd.length - 1] = "\"" + cmd[cmd.length - 1];
        return quoted;
    }

    
    /**
     * Returns value of the {@code SUDO_USER} enviroment variable. The variable
     * is set when the installer is executed using {@code sudo} command.
     * @return value of {@code SUDO_USER}
     */
    private UnixUser getSudoUser()
    {
        String printEnvCmd = UnixHelper.getCustomCommand("printenv");
        String sudoUserName = FileExecutor.getExecOutput(
                new String[]{printEnvCmd, "SUDO_USER"}, true).trim();
        
        for (UnixUser user : getUsers())
        {
            if (user.getName().equals(sudoUserName))
            {
                return user;
            }
        }
        return null;
    }
    

    /**
     * overridden method
     *
     * @return true
     * @see com.izforge.izpack.util.os.Shortcut#multipleUsers()
     */
    @Override
    public boolean multipleUsers()
    {
        // EVER true for UNIXes ;-)
        return (true);
    }


    /**
     * Creates and stores the shortcut-files.
     *
     * @throws java.lang.Exception error occured
     * @see com.izforge.izpack.util.os.Shortcut#save()
     */
    @Override
    public void save() throws Exception
    {
        String shortCutDef = this.build();

        boolean rootUser4All = this.getUserType() == Shortcut.ALL_USERS;
        boolean create4All = this.getCreateForAll();

        // Create The Desktop Shortcuts
        if ("".equals(this.programGroup) && (this.getLinkType() == Shortcut.DESKTOP))
        {

            this.itsFileName = null;

            // read the userdefined / overridden / wished Shortcut Location
            // This can be an absolute Path name or a relative Path to the InstallPath
            File shortCutLocation = null;
            File ApplicationShortcutPath;
            String ApplicationShortcutPathName = installData.getVariable("ApplicationShortcutPath"/*
              TODO
              <-- Put in Docu and in Un/InstallerConstantsClass
             */
            );
            if (null != ApplicationShortcutPathName && !ApplicationShortcutPathName.equals(""))
            {
                ApplicationShortcutPath = new File(ApplicationShortcutPathName);

                if (ApplicationShortcutPath.isAbsolute())
                {
                    // I know :-) Can be m"ORed" elegant :)
                    if (!ApplicationShortcutPath.exists() && ApplicationShortcutPath.mkdirs()
                            && ApplicationShortcutPath.canWrite())
                    {
                        shortCutLocation = ApplicationShortcutPath;
                    }
                    if (ApplicationShortcutPath.exists() && ApplicationShortcutPath.isDirectory()
                            && ApplicationShortcutPath.canWrite())
                    {
                        shortCutLocation = ApplicationShortcutPath;
                    }
                }
                else
                {
                    File relativePath = new File(installData.getInstallPath() + FS
                                                         + ApplicationShortcutPath);
                    //noinspection ResultOfMethodCallIgnored
                    relativePath.mkdirs();
                    shortCutLocation = new File(relativePath.toString());
                }
            }

            if (shortCutLocation == null)
                shortCutLocation = new File(installData.getInstallPath());

            // write the App ShortCut
            File writtenDesktopFile = writeAppShortcutWithOutSpace(shortCutLocation.toString(),
                                                                   this.linkName, shortCutDef);
            uninstaller.addFile(writtenDesktopFile.toString(), true);

            // Now install my Own with xdg-if available // Note the The reverse Uninstall-Task is on
            // TODO: "WHICH another place"

            UnixUser sudoUser = getSudoUser();
            
            String cmd = getXdgDesktopIconCmd();
            if (cmd != null)
            {
                createExtXdgDesktopIconCmd(shortCutLocation);
                
                String[] installCmd = new String[]{
                    makeFilenameScriptable(myXdgDesktopIconCmd),
                    "install",
                    "--novendor",
                    StringTool.escapeSpaces(writtenDesktopFile.toString())};
                String[] uninstallCmd = new String[]{
                    makeFilenameScriptable(myXdgDesktopIconCmd),
                    "uninstall",
                    "--novendor",
                    StringTool.escapeSpaces(writtenDesktopFile.toString())};
                
                // / TODO: DELETE the ScriptFiles
                ShellScript myUninstallScript = new ShellScript();
                if (sudoUser != null)
                {
                    // make sudo user owner of shortcuts, execute as sudo user
                    myInstallScript.append(new String[]{getSuCommand(), sudoUser.getName(), "-c"});
                    myUninstallScript.append(new String[]{getSuCommand(), sudoUser.getName(), "-c"});
                    installCmd = quoteCommand(installCmd);
                    uninstallCmd = quoteCommand(uninstallCmd);
                }
                
                myInstallScript.appendln(installCmd);
                myUninstallScript.appendln(uninstallCmd);
                uninstaller.addUninstallScript(myUninstallScript.getContentAsString());
            }
            else
            {
                // otherwise copy to my desktop and add to uninstaller
                String userHome = sudoUser != null ? sudoUser.getHome() : myHome;
                File myDesktopFile;
                do
                {
                    myDesktopFile = new File(userHome + FS + "Desktop" + FS
                                                    + FilenameUtils.getBaseName(writtenDesktopFile.getName())
                                                    + "-" + System.currentTimeMillis() + DESKTOP_EXT);
                }
                while (myDesktopFile.exists());

                FileUtils.copyFile(writtenDesktopFile, myDesktopFile, false);
                
                // make sure about permissions and ownership
                String chmodCmd = UnixHelper.getCustomCommand("chmod");
                FileExecutor.getExecOutput(new String[]{chmodCmd, "u+x",
                    StringTool.escapeSpaces(myDesktopFile.getPath())});
                
                if (sudoUser != null)
                {
                    // transfer ownership of shortcut to sudo user
                    String chownCmd = UnixHelper.getCustomCommand("chown");
                    FileExecutor.getExecOutput(new String[]{chownCmd, sudoUser.getName(),
                        StringTool.escapeSpaces(myDesktopFile.getPath())});
                }
                
                uninstaller.addFile(myDesktopFile.toString(), true);
            }

            // If I'm root and this Desktop.ShortCut should be for all other users
            if (rootUser4All && create4All)
            {
                if (cmd != null)
                {
                    installDesktopFileToAllUsersDesktop(writtenDesktopFile);
                }
                else
                // OLD ( Backward-Compatible/hardwired-"Desktop"-Foldername Styled Mechanic )
                {
                    copyDesktopFileToAllUsersDesktop(writtenDesktopFile);
                }
            }
        }

        // This is - or should be only a Link in the [K?]-Menu
        else
        {
            // the following is for backwards compatibility to older versions of KDE!
            // on newer versions of KDE the icons will appear duplicated unless you set
            // the category=""

            // removed because of compatibility issues
            /*
             * Object categoryobject = props.getProperty($Categories); if(categoryobject != null &&
             * ((String)categoryobject).length()>0) { File kdeHomeShareApplnk =
             * getKdeShareApplnkFolder(this.getUserType()); target = kdeHomeShareApplnk.toString() +
             * FS + this.itsGroupName + FS + this.itsName + DESKTOP_EXT; this.itsFileName = target;
             * File kdemenufile = writeShortCut(target, shortCutDef);
             *
             * uninstaller.addFile(kdemenufile.toString(), true); }
             */

            if (rootUser4All && create4All)
            {
                {
                    // write the icon pixmaps into /usr/share/pixmaps

                    File theIcon = new File(this.getIconLocation());
                    File commonIcon = new File("/usr/share/pixmaps/" + theIcon.getName());

                    try
                    {
                        FileUtils.copyFile(theIcon, commonIcon, false);
                        uninstaller.addFile(commonIcon.toString(), true);
                    }
                    catch (Exception e)
                    {
                        logger.log(Level.WARNING,
                                   "Could not copy " + theIcon + " to " + commonIcon + " ("
                                           + e.getMessage() + ")",
                                   e);
                    }

                    // write *.desktop

                    this.itsFileName = null;
                    File writtenFile = writeAppShortcut("/usr/share/applications/", this.linkName,
                                                        shortCutDef);
                    setWrittenFileName(writtenFile.getName());
                    uninstaller.addFile(writtenFile.toString(), true);

                }
            }
            else
            // create local XDG shortcuts
            {
                // System.out.println("Creating gnome shortcut");
                String localApps = myHome + "/.local/share/applications/";
                String localPixmaps = myHome + "/.local/share/pixmaps/";
                // System.out.println("Creating "+localApps);
                try
                {
                    java.io.File file = new java.io.File(localApps);
                    //noinspection ResultOfMethodCallIgnored
                    file.mkdirs();

                    file = new java.io.File(localPixmaps);
                    //noinspection ResultOfMethodCallIgnored
                    file.mkdirs();
                }
                catch (Exception ignore)
                {
                    // System.out.println("Failed creating "+localApps + " or " + localPixmaps);
                    logger.warning("Failed creating " + localApps + " or " + localPixmaps);
                }

                // write the icon pixmaps into ~/share/pixmaps

                File theIcon = new File(this.getIconLocation());
                File commonIcon = new File(localPixmaps + theIcon.getName());

                try
                {
                    FileUtils.copyFile(theIcon, commonIcon, false);
                    uninstaller.addFile(commonIcon.toString(), true);
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Could not copy " + theIcon + " to " + commonIcon
                            + " (" + e.getMessage() + ")", e);
                }

                // write *.desktop in the local folder

                this.itsFileName = null;
                File writtenFile = writeAppShortcut(localApps, this.linkName, shortCutDef);
                setWrittenFileName(writtenFile.getName());
                uninstaller.addFile(writtenFile.toString(), true);
            }

        }
    }


    /**
     * Creates Extended Locale Enabled XdgDesktopIcon Command script.
     * Fills the File myXdgDesktopIconScript with the content of
     * com/izforge/izpack/util/os/unix/xdgscript.sh and uses this to
     * creates User Desktop icons
     *
     * @param shortCutLocation in which folder should this stored.
     * @throws ResourceNotFoundException resource not found error
     */
    private void createExtXdgDesktopIconCmd(File shortCutLocation) throws ResourceNotFoundException
    {
        ShellScript myXdgDesktopIconScript = new ShellScript(null);

        String lines = resources.getString("/com/izforge/izpack/util/unix/xdgdesktopiconscript.sh", null);

        myXdgDesktopIconScript.append(lines);

        myXdgDesktopIconCmd = shortCutLocation + FS + "IzPackLocaleEnabledXdgDesktopIconScript.sh";
        myXdgDesktopIconScript.write(myXdgDesktopIconCmd);
        FileExecutor.getExecOutput(new String[]{UnixHelper.getCustomCommand("chmod"), "+x", myXdgDesktopIconCmd}, true);
    }


    /**
     * Calls and creates the Install/Uninstall Script which installs Desktop Icons using
     * xdgDesktopIconCmd un-/install
     *
     * @param writtenDesktopFile An applications desktop file, which should be installed.
     */
    private void installDesktopFileToAllUsersDesktop(File writtenDesktopFile)
    {
        for (UnixUser user : getUsers())
        {
            if (user.getHome().equals(myHome))
            {
                logger.info("Skipping self-copy: " + user.getHome() + " == " + myHome);
                continue;
            }
            try
            {
                // / THE Following does such as #> su username -c "xdg-desktopicon install
                // --novendor /Path/to/Filename\ with\ or\ without\ Space.desktop"
                rootScript.append(new String[]{getSuCommand(), user.getName(), "-c"});
                rootScript.appendln(new String[]{"\"" + myXdgDesktopIconCmd, "install", "--novendor",
                        StringTool.escapeSpaces(writtenDesktopFile.toString()) + "\""});

                uninstallScript.append(new String[]{getSuCommand(), user.getName(), "-c"});
                uninstallScript
                        .appendln(new String[]{"\"" + myXdgDesktopIconCmd, "uninstall", "--novendor",
                                StringTool.escapeSpaces(writtenDesktopFile.toString()) + "\""});
            }
            catch (Exception e)
            {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        logger.fine("==============================");
        logger.fine(rootScript.getContentAsString());
    }


    private String getSuCommand()
    {
        if (su == null)
        {
            su = UnixHelper.getSuCommand();
        }
        return su;
    }


    private String getXdgDesktopIconCmd()
    {
        if (xdgDesktopIconCmd == null)
        {
            xdgDesktopIconCmd = UnixHelper.getCustomCommand("xdg-desktop-icon");
        }
        return xdgDesktopIconCmd;
    }


    private List<UnixUser> getUsers()
    {
        if (users == null)
        {
            users = UnixUsers.getUsersWithValidShellsExistingHomesAndDesktops();
        }
        return users;
    }


    /**
     * @param writtenDesktopFile User desktop file
     * @throws IOException I/O error occured
     */
    private void copyDesktopFileToAllUsersDesktop(File writtenDesktopFile) throws IOException
    {
        String chmod = UnixHelper.getCustomCommand("chmod");
        String chown = UnixHelper.getCustomCommand("chown");
        String rm = UnixHelper.getRmCommand();
        String copy = UnixHelper.getCpCommand();

        File dest;

        // Create a tempFileName of this ShortCut
        File tempFile = File.createTempFile(this.getClass().getName(), Long.toString(System
                                                                                             .currentTimeMillis())
                + ".tmp");

        FileUtils.copyFile(writtenDesktopFile, tempFile, false);

        // Debug.log("Wrote Tempfile: " + tempFile.toString());

        FileExecutor.getExecOutput(new String[]{chmod, "uga+rwx", tempFile.toString()});

        // su marc.eppelmann -c "/bin/cp /home/marc.eppelmann/backup.job.out.txt
        // /home/marc.eppelmann/backup.job.out2.txt"

        for (UnixUser user : getUsers())
        {
            if (user.getHome().equals(myHome))
            {
                logger.info("Skipping self-copy: " + user.getHome() + " == " + myHome);
                continue;
            }
            try
            {
                // aHomePath = userHomesList[idx];
                dest = new File(user.getHome() + FS + "Desktop" + FS + writtenDesktopFile.getName());
                //
                // I'm root and cannot write into Users Home as root;
                // But I'm Root and I can slip in every users skin :-)
                //
                // by# su username
                //
                // This works as well
                // su $username -c "cp /tmp/desktopfile $HOME/Desktop/link.desktop"
                // chown $username $HOME/Desktop/link.desktop

                // Debug.log("Will Copy: " + tempFile.toString() + " to " + dest.toString());

                rootScript.append(getSuCommand());
                rootScript.append(S);
                rootScript.append(user.getName());
                rootScript.append(S);
                rootScript.append("-c");
                rootScript.append(S);
                rootScript.append('"');
                rootScript.append(copy);
                rootScript.append(S);
                rootScript.append(tempFile.toString());
                rootScript.append(S);
                rootScript.append(StringTool.replace(dest.toString(), " ", "\\ "));
                rootScript.appendln('"');

                rootScript.append('\n');

                // Debug.log("Will exec: " + script.toString());

                rootScript.append(chown);
                rootScript.append(S);
                rootScript.append(user.getName());
                rootScript.append(S);
                rootScript.appendln(StringTool.replace(dest.toString(), " ", "\\ "));
                rootScript.append('\n');
                rootScript.append('\n');

                // Debug.log("Will exec: " + script.toString());

                uninstallScript.append(getSuCommand());
                uninstallScript.append(S);
                uninstallScript.append(user.getName());
                uninstallScript.append(S);
                uninstallScript.append("-c");
                uninstallScript.append(S);
                uninstallScript.append('"');
                uninstallScript.append(rm);
                uninstallScript.append(S);
                uninstallScript.append(StringTool.replace(dest.toString(), " ", "\\ "));
                uninstallScript.appendln('"');
                uninstallScript.appendln();
                // Debug.log("Uninstall will exec: " + uninstallScript.toString());
            }
            catch (Exception e)
            {
                logger.log(Level.INFO,
                           "Could not copy as root: " + e.getMessage(),
                           e);

                /* ignore */
                // most distros does not allow root to access any user
                // home (ls -la /home/user drwx------)
                // But try it anyway...
            }
        }

        rootScript.append(rm);
        rootScript.append(S);
        rootScript.appendln(tempFile.toString());
        rootScript.appendln();
    }

    /**
     * Post Exec Action especially for the Unix Root User. which executes the Root ShortCut
     * Shellscript. to copy all ShellScripts to the users Desktop.
     */
    @Override
    public void execPostAction()
    {
        logger.fine("Launching post execution action");

        String pseudoUnique = this.getClass().getName() + Long.toString(System.currentTimeMillis());

        String scriptFilename;

        try
        {
            scriptFilename = File.createTempFile(pseudoUnique, ".sh").toString();
        }
        catch (IOException e)
        {
            scriptFilename = System.getProperty("java.io.tmpdir", "/tmp") + "/" + pseudoUnique
                    + ".sh";
            e.printStackTrace();
        }

        rootScript.write(scriptFilename);
        rootScript.exec();
        rootScript.delete();
        logger.fine(rootScript.toString());

        // Quick an dirty copy & paste code - will be cleanup in one of 4.1.1++
        pseudoUnique = this.getClass().getName() + Long.toString(System.currentTimeMillis());
        try
        {
            scriptFilename = File.createTempFile(pseudoUnique, ".sh").toString();
        }
        catch (IOException e)
        {
            scriptFilename = System.getProperty("java.io.tmpdir", "/tmp") + "/" + pseudoUnique
                    + ".sh";
            e.printStackTrace();
        }

        myInstallScript.write(scriptFilename);
        myInstallScript.exec();
        myInstallScript.delete();


        logger.fine(myInstallScript.toString());
        // End OF Quick AND Dirty
        logger.fine(uninstallScript.toString());

        uninstaller.addUninstallScript(uninstallScript.getContentAsString());
    }


    private String writtenFileName;

    public String getWrittenFileName()
    {
        return writtenFileName;
    }

    private void setWrittenFileName(String s)
    {
        writtenFileName = s;
    }


    /**
     * Write the given ShortDefinition in a File $ShortcutName-$timestamp.desktop in the given
     * TargetPath.
     *
     * @param targetPath   The Path in which the files should be written.
     * @param shortcutName The Name for the File
     * @param shortcutDef  The Shortcut FileContent
     * @return The written File
     */
    private File writeAppShortcut(String targetPath, String shortcutName, String shortcutDef)
    {
        return writeAppShortcutWithSimpleSpacehandling(targetPath, shortcutName, shortcutDef, false);
    }


    /**
     * Write the given ShortDefinition in a File $ShortcutName-$timestamp.desktop in the given
     * TargetPath. ALSO all WhiteSpaces in the ShortCutName will be replaced with "-"
     *
     * @param targetPath   The Path in which the files should be written.
     * @param shortcutName The Name for the File
     * @param shortcutDef  The Shortcut FileContent
     * @return The written File
     */
    private File writeAppShortcutWithOutSpace(String targetPath, String shortcutName,
                                              String shortcutDef)
    {
        return writeAppShortcutWithSimpleSpacehandling(targetPath, shortcutName, shortcutDef, true);
    }


    /**
     * Write the given ShortDefinition in a File $ShortcutName-$timestamp.desktop in the given
     * TargetPath. If the given replaceSpaces was true ALSO all WhiteSpaces in the ShortCutName will
     * be replaced with "-"
     *
     * @param targetPath   The Path in which the files should be written.
     * @param shortcutName The Name for the File
     * @param shortcutDef  The Shortcut FileContent
     * @return The written File
     */
    private File writeAppShortcutWithSimpleSpacehandling(String targetPath, String shortcutName,
                                                         String shortcutDef, boolean replaceSpacesWithMinus)
    {
        File shortcutFile = new File(
                FilenameUtils.getFullPathNoEndSeparator(targetPath)
                + '/'
                + (replaceSpacesWithMinus ? StringTool.replaceSpacesWithMinus(shortcutName) : shortcutName)
                + DESKTOP_EXT);

        try
        {
            FileUtils.writeStringToFile(shortcutFile, shortcutDef);
        }
        catch (IOException e)
        {
            logger.warning("Application shortcut could not be created (" + e.getMessage() + ")");
        }

        return shortcutFile;
    }

    @Override
    public void setArguments(String args)
    {
        this.arguments = args;
    }

    @Override
    public void setCategories(String theCategories)
    {
        this.categories = theCategories;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public void setEncoding(String aEncoding)
    {
        this.encoding = aEncoding;
    }

    @Override
    public void setKdeSubstUID(String trueFalseOrNothing)
    {
        this.kdeSubstituteUID = trueFalseOrNothing;
    }

    @Override
    public void setKdeUserName(String aUserName)
    {
        this.kdeUserName = aUserName;
    }

    @Override
    public void setIconLocation(String path, int index)
    {
        this.iconLocation = path;
    }

    @Override
    public String getIconLocation()
    {
        return this.iconLocation;
    }

    @Override
    public void setLinkName(String aName)
    {
        this.linkName = aName;
    }

    @Override
    public void setLinkType(int aType) throws IllegalArgumentException,
            UnsupportedEncodingException
    {
        this.linkType = aType;
    }

    @Override
    public int getLinkType()
    {
        return linkType;
    }

    @Override
    public void setMimetype(String aMimeType)
    {
        this.mimeType = aMimeType;
    }

    @Override
    public void setProgramGroup(String aGroupName)
    {
        this.programGroup = aGroupName;
    }

    @Override
    public void setShowCommand(int show)
    {
        // ignored for Linux
    }

    @Override
    public void setTargetPath(String aPath)
    {
        this.targetPath = aPath;
    }

    @Override
    public void setUserType(int aUserType)
    {
        this.userType = aUserType;
    }

    @Override
    public int getUserType()
    {
        return userType;
    }

    @Override
    public void setWorkingDirectory(String aDirectory)
    {
        this.workingDirectory = aDirectory;
    }

    @Override
    public void setTerminal(String trueFalseOrNothing)
    {
        this.terminal = trueFalseOrNothing;
    }

    @Override
    public void setTerminalOptions(String someTerminalOptions)
    {
        this.terminalOptions = someTerminalOptions;
    }

    @Override
    public void setTryExec(String aTryExec)
    {
        // currently ignored
    }

    @Override
    public void setType(String aType)
    {
        this.type = aType;
    }

    @Override
    public void setURL(String anUrl)
    {
        this.url = anUrl;
    }

    @Override
    public String toString()
    {
        return this.linkName + N + build();
    }

}
