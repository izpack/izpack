/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2006 Marc Eppelmann
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
package com.izforge.izpack.util.unix;

import com.izforge.izpack.util.FileExecutor;

import java.io.*;
import java.util.ArrayList;

/**
 * Helper Methods for unix-systems and derived.
 *
 * @author marc.eppelmann&#064;reddot.de
 * @version $Revision$
 */
public class UnixHelper
{

    // ~ Static fields/initializers *********************************************************
    /**
     * whichCommand = "/usr/bin/which" or /bin/which
     */
    public static String whichCommand = FileExecutor.getExecOutput(
        new String[]
        {
            "/usr/bin/env", "which", "which"
        }, false).trim();

    public final static String VERSION = "$Revision$";

    // ~ Methods ****************************************************************************
    /**
     * Get the lines from /etc/passwd as Array
     *
     * @return the /etc/passwd as String ArrayList
     */
    public static ArrayList<String> getEtcPasswdArray()
    {
        ArrayList<String> result = new ArrayList<String>();

        String line = "";
        BufferedReader reader = null;

        try
        {
            reader = new BufferedReader(new FileReader(UnixConstants.etcPasswd));

            while ((line = reader.readLine()) != null)
            {
                result.add(line);
            }
        }
        catch (Exception e)
        {
            // ignore - there are maybe no users
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (Exception ignore)
            {
                // ignore
            }
        }

        return result;
    }

    /**
     * Get the YelloyPages (NIS) Users lines from <code>ypcat passwd</code> as
     * Array. Ypcat passwd's output has the same format as the the local
     * /etc/passwd. Because there can be thousands of yp-users and this query is
     * net-based, this is a candidate for a token-based optimization.
     *
     * @return the /etc/passwd as String ArrayList
     */
    public static ArrayList<String> getYpPasswdArray()
    {
        ArrayList<String> result = new ArrayList<String>();

        String line = "";
        BufferedReader reader = null;
        String ypcommand = getYpCatCommand();

        if (ypcommand != null && ypcommand.length() > 0)
        {

            try
            {
                reader = new BufferedReader(new StringReader(FileExecutor
                        .getExecOutput(new String[]
                        {
                            ypcommand, "passwd"
                        })));
                while ((line = reader.readLine()) != null)
                {
                    result.add(line);
                }
            }
            catch (Exception e)
            {
                // ignore - there are maybe no users
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }

                }
                catch (Exception exception)
                {
                    // ignore
                }
            }
        }
        return result;
    }

    /**
     * Test if KDE is installed. This is done by
     * <code> $>/usr/bin/env kwin --version</code> This assumes that kwin, the
     * window Manager, as part of the kde-base package is already installed. If
     * this returns with 0 kwin resp. kde means to be installed,
     *
     * @return true if kde is installed otherwise false.
     */
    public static boolean kdeIsInstalled()
    {
        FileExecutor fileExecutor = new FileExecutor();

        String[] execOut = new String[2];

        int execResult = fileExecutor.executeCommand(new String[]
        {
            "/usr/bin/env", "kwin", "--version"
        },
                execOut);

        return execResult == 0;
    }

    /**
     * Gets the absolute path of the which command. This is necessary, because
     * the command is located at /bin on linux but in /usr/bin on Sun Solaris.
     *
     * @return /bin/which on linux /usr/bin/which on solaris
     */
    public static String getWhichCommand()
    {
        return whichCommand;
    }

    /**
     * Gets the absolute path of the cp (Copy) command. This is necessary,
     * because the command is located at /bin on linux but in /usr/bin on Sun
     * Solaris.
     *
     * @return /bin/cp on linux /usr/bin/cp on solaris
     */
    public static String getCpCommand()
    {
        return FileExecutor.getExecOutput(new String[]
        {
            getWhichCommand(), "cp"
        }).trim();
    }

    /**
     * Gets the absolute path to the su (SuperUser) command. This is necessary,
     * because the command is located at /bin on linux but in /usr/bin on Sun
     * Solaris.
     *
     * @return /bin/su on linux /usr/bin/su on solaris
     */
    public static String getSuCommand()
    {
        return FileExecutor.getExecOutput(new String[]
        {
            getWhichCommand(), "su"
        }).trim();
    }

    /**
     * Gets the absolute Pathe to the rm (Remove) Command. This is necessary,
     * because the command is located at /bin on linux but in /usr/bin on Sun
     * Solaris.
     *
     * @return /bin/rm on linux /usr/bin/rm on solaris
     */
    public static String getRmCommand()
    {
        return FileExecutor.getExecOutput(new String[]
        {
            whichCommand, "rm"
        }).trim();
    }

    /**
     * Gets the absolute Pathe to the ypcat (YellowPage/NIS Cat) Command. This
     * is necessary, because the command is located at /bin on linux but in
     * /usr/bin on Sun Solaris.
     *
     * @return /bin/ypcat on linux /usr/bin/ypcat on solaris
     */
    public static String getYpCatCommand()
    {
        return FileExecutor.getExecOutput(new String[]
        {
            whichCommand, "ypcat"
        }).trim();
    }

    /**
     * Gets the absolute Pathe to the given custom command. This is necessary,
     * because the command may be located at /bin on linux but in /usr/bin on
     * Sun Solaris. Which can locate it in your $PATH for you.
     *
     * @param aCommand a Custom Command
     * @return /bin/aCommand on linux /usr/bin/aCommand on solaris
     */
    public static String getCustomCommand(String aCommand)
    {
        return FileExecutor.getExecOutput(new String[]
        {
            whichCommand, aCommand
        }).trim();
    }

    /**
     * Standalone Test Main Method call with : &gt; java -cp ../_build
     * com.izforge.izpack.util.unix.UnixHelper
     *
     * @param args commandline args
     */
    public static void main(String[] args)
    {
        System.out.println("Hallo from " + UnixHelper.class.getName() + VERSION);

        // System.out.println( StringTool.listToString(UnixUsers.getUsersAsArrayList())
        // );
        // System.out.println("Kde is" + (kdeIsInstalled() ? " " : " not ") + "installed");
        System.out.println("WhichCommand: '" + getWhichCommand() + "'");
        System.out.println("SuCommand: " + getSuCommand());
        System.out.println("RmCommand: " + getRmCommand());
        System.out.println("CopyCommand: " + getCpCommand());
        System.out.println("YpCommand: " + getYpCatCommand());

        System.out.println("CustomCommand: " + getCustomCommand("cat"));

        File tempFile = null;

        try
        {
            tempFile = File.createTempFile(UnixHelper.class.getName(), Long.toString(System
                    .currentTimeMillis())
                    + ".tmp");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Tempfile: " + tempFile.toString());
        System.out.println("TempfileName: " + tempFile.getName());

        // This does not work :-(
        /*
         * FileExecutor.getExecOutput(new String[] { getCustomCommand("echo"), "Hallo", ">",
         * tempFile.toString()});
         */
        String username = System.getProperty("user.name");

        // System.out.println("Your Name: " + username);
        // so try:
        if ("root".equals(username))
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                writer.write("Hallo");
                writer.flush();
                writer.close();
                if (tempFile.exists())
                {
                    System.out.println("Wrote: " + tempFile + ">>Hallo");
                }
                else
                {
                    System.out.println("Could not Wrote: " + tempFile + "Hallo");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            String destfilename = "/home/marc.eppelmann" + File.separator + "Desktop"
                    + File.separator + tempFile.getName();

            System.out.println("Copy: " + tempFile.toString() + " to " + destfilename);

            ShellScript script = new ShellScript("bash");

            script.append(getSuCommand() + " " + "marc.eppelmann" + " " + "-c" + " " + "\""
                    + getCpCommand() + " " + tempFile.toString() + " " + destfilename + "\"");

            String shLocation = "/tmp/x.21.21";
            try
            {
                shLocation = File.createTempFile(UnixHelper.class.getName(),
                        Long.toString(System.currentTimeMillis()) + ".sh").toString();
            }
            catch (Exception x)
            {
                x.printStackTrace();
            }
            script.write(shLocation);
            script.exec();

            // File deleteMe = new File( shLocation ); deleteMe.delete();
        }
        else
        {
            System.out.println("Your name: '" + username
                    + "' is not 'root'. But this is a test for the user root.");
        }
    }
}
