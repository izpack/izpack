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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.unix.ShellScript;
import com.izforge.izpack.util.unix.UnixHelper;
import com.izforge.izpack.util.unix.UnixUser;
import com.izforge.izpack.util.unix.UnixUsers;

/**
 * This is the Implementation of the RFC-Based Desktop-Link. Used in KDE and GNOME.
 *
 * @author marc.eppelmann&#064;reddot.de
 * @author Bill Root
 */
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
     * internal String createdDirectory
     */
    private String createdDirectory;

    /**
     * internal String itsFileName
     */
    private String itsFileName;

    /**
     * my Install ShellScript *
     */
    public ShellScript myInstallScript;

    /**
     * Internal Constant: FS = File.separator // *
     */
    public final String FS = File.separator;

    /**
     * Internal Constant: myHome = System.getProperty("user.home") *
     */
    public final String myHome = System.getProperty("user.home");

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
     * @return
     */
    public String build()
    {
        StringBuffer result = new StringBuffer();

        String userLanguage = System.getProperty("user.language", "en");

        result.append("[Desktop Entry]" + N);

        // TODO implement Attribute: X-KDE-StartupNotify=true

        result.append("Categories=").append(categories).append(N);

        result.append("Comment=").append(description).append(N);
        result.append("Comment[").append(userLanguage).append("]=").append(description).append(N);
        result.append("Encoding=").append(encoding).append(N);

        // this causes too many problems
        // result.append("TryExec=" + $E_QUOT + $Exec + $E_QUOT + S + $Arguments + N);

        if (!targetPath.isEmpty() || !arguments.isEmpty())
        {
            result.append("Exec=");
            if (targetPath.contains(S))
                result.append("'").append(targetPath).append("'");
            else
                result.append(targetPath);
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


    /**
     * Overridden Method
     *
     * @param aType
     * @param aName
     * @throws java.lang.Exception
     * @see com.izforge.izpack.util.os.Shortcut#initialize(int, java.lang.String)
     */
    @Override
    public void initialize(int aType, String aName) throws Exception
    {
        this.linkName = aName;
    }


    /**
     * This indicates that Unix will be supported.
     *
     * @return
     * @see com.izforge.izpack.util.os.Shortcut#supported()
     */
    @Override
    public boolean supported()
    {
        return true;
    }


    /**
     * Dummy
     *
     * @return
     * @see com.izforge.izpack.util.os.Shortcut#getDirectoryCreated()
     */
    @Override
    public String getDirectoryCreated()
    {
        return this.createdDirectory; // while not stored...
    }


    /**
     * Dummy
     *
     * @return
     * @see com.izforge.izpack.util.os.Shortcut#getFileName()
     */
    @Override
    public String getFileName()
    {
        return (this.itsFileName);
    }


    /**
     * Overridden compatibility method. Returns all directories in $USER/.kde/share/applink.
     *
     * @return
     * @see com.izforge.izpack.util.os.Shortcut#getProgramGroups(int)
     */
    @Override
    public List<String> getProgramGroups(int userType)
    {
        List<String> groups = new ArrayList<String>();
        groups.add("(Default)"); // Should be the same value as DEFAULT_FOLDER from ShortcutConstants

        File kdeShareApplnk = getKdeShareApplnkFolder(userType);

        try
        {
            File[] listing = kdeShareApplnk.listFiles();

            for (File aListing : listing)
            {
                if (aListing.isDirectory())
                {
                    groups.add(aListing.getName());
                }
            }
        }
        catch (Exception e)
        {
            // ignore and return an empty vector.
        }

        return groups;
    }


    /**
     * Gets the Programsfolder for the given User (non-Javadoc).
     *
     * @return
     * @see com.izforge.izpack.util.os.Shortcut#getProgramsFolder(int)
     */
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
     * @param filename
     * @return
     */
    private String makeFilenameScriptable(String filename)
    {
        return filename.replace(" ", "\\ ");
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
     * @throws java.lang.Exception
     * @see com.izforge.izpack.util.os.Shortcut#save()
     */
    @Override
    public void save() throws Exception
    {

        String target = null;

        String shortCutDef = this.build();

        boolean rootUser4All = this.getUserType() == Shortcut.ALL_USERS;
        boolean create4All = this.getCreateForAll();

        // Create The Desktop Shortcuts
        if ("".equals(this.programGroup) && (this.getLinkType() == Shortcut.DESKTOP))
        {

            this.itsFileName = target;

            // read the userdefined / overridden / wished Shortcut Location
            // This can be an absolute Path name or a relative Path to the InstallPath
            File shortCutLocation = null;
            File ApplicationShortcutPath;
            String ApplicationShortcutPathName = installData.getVariable("ApplicationShortcutPath"/**
             * TODO
             * <-- Put in Docu and in Un/InstallerConstantsClass
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

            String cmd = getXdgDesktopIconCmd();
            if (cmd != null)
            {
                createExtXdgDesktopIconCmd(shortCutLocation);
                // / TODO: DELETE the ScriptFiles
                myInstallScript.appendln(new String[]{
                    makeFilenameScriptable(myXdgDesktopIconCmd),
                    "install",
                    "--novendor",
                    StringTool.escapeSpaces(writtenDesktopFile.toString())});
                ShellScript myUninstallScript = new ShellScript();
                myUninstallScript.appendln(new String[]{
                    makeFilenameScriptable(myXdgDesktopIconCmd),
                    "uninstall",
                    "--novendor",
                    StringTool.escapeSpaces(writtenDesktopFile.toString())});
                uninstaller.addUninstallScript(myUninstallScript.getContentAsString());
            }
            else
            {
                // otherwise copy to my desktop and add to uninstaller
                File myDesktopFile;
                do
                {
                    myDesktopFile = new File(myHome + FS + "Desktop" + writtenDesktopFile.getName()
                                                     + "-" + System.currentTimeMillis() + DESKTOP_EXT);
                }
                while (myDesktopFile.exists());

                copyTo(writtenDesktopFile, myDesktopFile);
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
                        copyTo(theIcon, commonIcon);
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

                    this.itsFileName = target;
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
                    file.mkdirs();

                    file = new java.io.File(localPixmaps);
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
                    copyTo(theIcon, commonIcon);
                    uninstaller.addFile(commonIcon.toString(), true);
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Could not copy " + theIcon + " to " + commonIcon
                            + " (" + e.getMessage() + ")", e);
                }

                // write *.desktop in the local folder

                this.itsFileName = target;
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
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    public void createExtXdgDesktopIconCmd(File shortCutLocation) throws IOException,
            ResourceNotFoundException
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
     * @param writtenDesktopFile
     * @throws IOException
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

        copyTo(writtenDesktopFile, tempFile);

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

    /**
     * Copies the inFile file to outFile using cbuff as buffer.
     *
     * @param inFile  The File to read from.
     * @param outFile The targetFile to write to.
     * @throws IOException If an IO Error occurs
     */
    public static void copyTo(File inFile, File outFile) throws IOException
    {
        char[] cbuff = new char[32768];
        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        int readBytes;

        while ((readBytes = reader.read(cbuff, 0, cbuff.length)) != -1)
        {
            writer.write(cbuff, 0, readBytes);
        }

        reader.close();
        writer.close();
    }


    private String writtenFileName;

    public String getWrittenFileName()
    {
        return writtenFileName;
    }

    protected void setWrittenFileName(String s)
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
     * TargetPath. ALSO all WhiteSpaces in the ShortCutName will be repalced with "-"
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
        if (!(targetPath.endsWith("/") || targetPath.endsWith("\\")))
        {
            targetPath += File.separatorChar;
        }

        File shortcutFile;

        do
        {
            shortcutFile = new File(targetPath
                                            + (replaceSpacesWithMinus == true ? StringTool
                    .replaceSpacesWithMinus(shortcutName) : shortcutName) + "-"
                                            + System.currentTimeMillis() + DESKTOP_EXT);
        }
        while (shortcutFile.exists());

        FileWriter fileWriter = null;

        try
        {
            fileWriter = new FileWriter(shortcutFile);
        }
        catch (IOException e1)
        {
            System.out.println(e1.getMessage());
        }

        try
        {
            if (fileWriter != null)
                fileWriter.write(shortcutDef);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (fileWriter != null)
                fileWriter.close();
        }
        catch (IOException e2)
        {
            e2.printStackTrace();
        }
        return shortcutFile;

    }


    /**
     * Set the command line Arguments
     *
     * @param args
     * @see com.izforge.izpack.util.os.Shortcut#setArguments(java.lang.String)
     */
    @Override
    public void setArguments(String args)
    {
        this.arguments = args;
    }


    /**
     * Sets the Categories Field
     *
     * @param theCategories the categories
     */
    @Override
    public void setCategories(String theCategories)
    {
        this.categories = theCategories;
    }


    /**
     * Sets the Description
     *
     * @see com.izforge.izpack.util.os.Shortcut#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * Sets The Encoding
     *
     * @param aEncoding
     * @see com.izforge.izpack.util.os.Shortcut#setEncoding(java.lang.String)
     */
    @Override
    public void setEncoding(String aEncoding)
    {
        this.encoding = aEncoding;
    }


    /**
     * Sets The KDE Specific subst UID property
     *
     * @param trueFalseOrNothing
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
    @Override
    public void setKdeSubstUID(String trueFalseOrNothing)
    {
        this.kdeSubstituteUID = trueFalseOrNothing;
    }


    /**
     * Sets The KDE Specific subst UID property
     *
     * @param aUserName
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
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


    /**
     * Sets the Name of this Shortcut
     *
     * @param aName
     * @see com.izforge.izpack.util.os.Shortcut#setLinkName(java.lang.String)
     */
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


    /**
     * Sets the MimeType
     *
     * @param aMimeType
     * @see com.izforge.izpack.util.os.Shortcut#setMimetype(java.lang.String)
     */
    @Override
    public void setMimetype(String aMimeType)
    {
        this.mimeType = aMimeType;
    }


    /**
     * Sets the ProgramGroup
     *
     * @param aGroupName
     * @see com.izforge.izpack.util.os.Shortcut#setProgramGroup(java.lang.String)
     */
    @Override
    public void setProgramGroup(String aGroupName)
    {
        this.programGroup = aGroupName;
    }


    /**
     * Sets the ShowMode
     *
     * @see com.izforge.izpack.util.os.Shortcut#setShowCommand(int)
     */
    @Override
    public void setShowCommand(int show)
    {
        // ignored for Linux
    }

    /**
     * Sets The TargetPath
     *
     * @param aPath
     * @see com.izforge.izpack.util.os.Shortcut#setTargetPath(java.lang.String)
     */
    @Override
    public void setTargetPath(String aPath)
    {
        this.targetPath = aPath;
    }


    /**
     * Sets the user type.
     *
     * @param aUserType
     * @see com.izforge.izpack.util.os.Shortcut#setUserType(int)
     */
    @Override
    public void setUserType(int aUserType)
    {
        this.userType = aUserType;
    }
    /**
     * Gets the user type of the Shortcut.
     *
     * @return
     * @see com.izforge.izpack.util.os.Shortcut#getUserType()
     */
    @Override
    public int getUserType()
    {
        return userType;
    }



    /**
     * Sets the working-directory
     *
     * @param aDirectory
     * @see com.izforge.izpack.util.os.Shortcut#setWorkingDirectory(java.lang.String)
     */
    @Override
    public void setWorkingDirectory(String aDirectory)
    {
        this.workingDirectory = aDirectory;
    }


    /**
     * Sets the terminal
     *
     * @param trueFalseOrNothing
     * @see com.izforge.izpack.util.os.Shortcut#setTerminal(java.lang.String)
     */
    @Override
    public void setTerminal(String trueFalseOrNothing)
    {
        this.terminal = trueFalseOrNothing;
    }

    /**
     * Sets the terminal options
     *
     * @param someTerminalOptions
     * @see com.izforge.izpack.util.os.Shortcut#setTerminalOptions(java.lang.String)
     */
    @Override
    public void setTerminalOptions(String someTerminalOptions)
    {
        this.terminalOptions = someTerminalOptions;
    }


    /**
     * Sets the TryExecField.
     *
     * @param aTryExec the try exec command
     */
    @Override
    public void setTryExec(String aTryExec)
    {
        // currently ignored
    }


    /**
     * Sets the Shortcut type (one of Application, Link or Device)
     *
     * @param aType
     * @see com.izforge.izpack.util.os.Shortcut#setType(java.lang.String)
     */
    @Override
    public void setType(String aType)
    {
        this.type = aType;
    }


    /**
     * Sets the Url for type Link. Can be also a absolute file/path
     *
     * @param anUrl
     * @see com.izforge.izpack.util.os.Shortcut#setURL(java.lang.String)
     */
    @Override
    public void setURL(String anUrl)
    {
        this.url = anUrl;
    }


    /**
     * Dumps the Name to console.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.linkName + N + build();
    }

}
