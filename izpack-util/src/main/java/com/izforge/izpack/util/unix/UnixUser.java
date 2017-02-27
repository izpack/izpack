/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Marc Eppelmann
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
package com.izforge.izpack.util.unix;

import com.izforge.izpack.util.FileExecutor;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This represents a Unix User. If initialized via fromEtcPasswdLine(), the
 * users Name, home, uid, gid, and shell can be asked.
 *
 * @author marc.eppelmann&#064;reddot.de
 */
public class UnixUser
{
    //~ Instance fields ********************************************************************

    /**
     * internal itsName
     */
    private String itsName;

    /**
     * internal itsPasswdDigest
     */
    private String itsPasswdDigest;

    /**
     * internal itsId
     */
    private String itsId;

    /**
     * internal itsGid
     */
    private String itsGid;

    /**
     * internal itsDescription
     */
    private String itsDescription;

    /**
     * internal itsHome
     */
    private String itsHome;

    /**
     * internal itsShell
     */
    private String itsShell;

    /**
     * internal name
     */
    private static String xDGDesktopFolderNameScriptFilename;

    private static File xDGDesktopFolderNameScript;
    //~ Methods ****************************************************************************

    /**
     * Gets the Users Login Name
     *
     * @return the users login Name
     */
    public String getName()
    {
        return itsName;
    }

    /**
     * Gets the users passwd Digest or X if hidden in /etc/shadow
     *
     * @return the passwdDigest or x
     */
    public String getPasswdDigest()
    {
        return itsPasswdDigest;
    }

    /**
     * Gets the Users UID
     *
     * @return The Uid
     */
    public String getId()
    {
        return itsId;
    }

    /**
     * Gtes the Users Group ID
     *
     * @return the gid
     */
    public String getGid()
    {
        return itsGid;
    }

    /**
     * Gets the Description aka Full Name
     *
     * @return the users descriptio or full name
     */
    public String getDescription()
    {
        return itsDescription;
    }

    /**
     * Gets the Users Home Directory
     *
     * @return the users home dir
     */
    public String getHome()
    {
        return itsHome.trim();
    }

    /**
     * Gets the users default Login-Shell
     *
     * @return The login shell or /bin/false for system users
     */
    public String getShell()
    {
        return itsShell;
    }

    /**
     * Parses a Line from /etc/passwd and stores each :token: in their field of
     * the user. Sample Line from /etc/passwd "eppelmann.local:x:900:100:Marc
     * Eppelmann:/mnt/local/home/eppelmann.local:/bin/bash"
     *
     * @param anEtcPasswdLine A Passwd Line of the User.
     * @return The filled User
     */
    public UnixUser fromEtcPasswdLine(String anEtcPasswdLine)
    {
        if (anEtcPasswdLine == null)
        {
            return null;
        }

        StringTokenizer usersToken = new StringTokenizer(anEtcPasswdLine, ":");

        UnixUser user = new UnixUser();

        if (usersToken.hasMoreTokens())
        {
            user.itsName = usersToken.nextToken();
        }

        if (usersToken.hasMoreTokens())
        {
            user.itsPasswdDigest = usersToken.nextToken();
        }

        if (usersToken.hasMoreTokens())
        {
            user.itsId = usersToken.nextToken();
        }

        if (usersToken.hasMoreTokens())
        {
            user.itsGid = usersToken.nextToken();
        }

        if (usersToken.hasMoreTokens())
        {
            user.itsDescription = usersToken.nextToken();
        }

        if (usersToken.hasMoreTokens())
        {
            user.itsHome = usersToken.nextToken();
        }

        if (usersToken.hasMoreTokens())
        {
            user.itsShell = usersToken.nextToken();
        }

        return user;
    }

    /**
     * Creates a small script, which calls $HOME/.config/user-dirs.dirs then
     * echoes the $XDG_DESKTOP_DIR in the /tmp folder and returns its pseudo
     * unique absolute filename. The call of this script should return the
     * absolute Desktop foldername.
     *
     * @return the absolute Filename of the script.
     */
    public String getCreatedXDGDesktopFolderNameScriptFilename()
    {
        ShellScript sh = new ShellScript();

        sh.appendln(". " + getHome() + File.separator + ".config" + File.separator + "user-dirs.dirs");
        sh.appendln();
        sh.appendln("echo $XDG_DESKTOP_DIR");

        String pseudoUnique = this.getClass().getName() + Long.toString(System.currentTimeMillis());

        String scriptFilename = null;

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

        sh.write(scriptFilename);

        return scriptFilename;
    }

    /**
     * Gets the Name of the XDG-Desktop Folder if defined in the
     * $HOME/.config/user-dirs.dirs File as absolute File/Pathname
     *
     * @return The absolute File/Pathname of the Desktop foldername.
     */
    public String getXdgDesktopfolder()
    {
        File configFile = new File(getHome() + File.separator + ".config" + File.separator + "user-dirs.dirs");
        if (configFile.exists())
        {
            if (xDGDesktopFolderNameScript == null)
            /**
             * TODO: can be optimized with a shared script *
             */
            {
                xDGDesktopFolderNameScriptFilename = getCreatedXDGDesktopFolderNameScriptFilename();
            }

            FileExecutor.getExecOutput(new String[]
            {
                UnixHelper.getCustomCommand("chmod"), "+x", xDGDesktopFolderNameScriptFilename
            }, true);
            String xdgDesktopfolder = FileExecutor.getExecOutput(new String[]
            {
                xDGDesktopFolderNameScriptFilename
            }, true).trim();
            new File(xDGDesktopFolderNameScriptFilename).delete();

            return xdgDesktopfolder;

        }
        else
        {
            return getHome() + File.separator + "Desktop";
        }
    }

    /**
     * Dumps the USer fields
     *
     * @return The User representation as String
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("User: ");
        result.append(itsName);

        result.append(" X: ");
        result.append(itsPasswdDigest);

        result.append(" Id: ");
        result.append(itsId);

        result.append(" Gid: ");
        result.append(itsGid);

        result.append(" Desc.: ");
        result.append(itsDescription);

        result.append(" Home: ");
        result.append(itsHome);

        result.append(" Shell: ");
        result.append(itsShell);

        return result.toString();
    }

    /**
     * Static Test Main
     *
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println(new UnixUser().fromEtcPasswdLine(""));
        System.out.println(new UnixUser().fromEtcPasswdLine("eppelmann.local:x:500:100:Marc L Eppelmann:/mnt/local/home/eppelmann.local:/bin/bash"));
    }

}
