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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * A Generator, Wrapper and Executor for Unix ShellScripts
 *
 * @author marc.eppelmann&#064;reddot.de
 */
public class ShellScript
{

    // ~ Static fields/initializers *********************************************************
    // ~ Static fields/initializers *********************************************************
    /**
     * Author = "marc.eppelmann_at_gmx.de"
     */
    private final static String AUTHOR = "Author: marc.eppelmann_at_gmx.de";

    /**
     * Generator = "Generator: " + ShellScript.class.getName()
     */
    private final static String GENERATOR = "Generator: " + ShellScript.class.getName();

    /**
     * internal SourceCode Management ( currently 'svn') ID :: 'SCM_ID = "$Id$"'
     */
    private final static String SCM_ID = "$Id$";

    /**
     * internal Revision = "$Revision$"
     */
    private final static String REVISION = "$Revision$";

    /**
     * internal comment prefix; makes a line as comment:-) :: 'CommentPre = "#
     * "'
     */
    private final static String COMMENT_PRE = "# ";

    /**
     * H = CommentPre
     */
    private final static String H = COMMENT_PRE;

    /**
     * the linefeed: lf = "\n"
     */
    private final static String LF = "\n";

    /**
     * lh = lf + H = "\n#"
     */
    private final static String LH = LF + H;

    /**
     * the explanation header for this generated script
     */
    private final static String EXPLANATION = LH + "This is an automatically generated Script."
            + LH + "Usually this can be removed if the Generator " + LH
            + "was unable to remove the script after execution." + LF;

    /**
     * "Generated at: " + new Date().toString()
     */
    private static String currentDateMsg = "Generated at: " + new Date().toString();

    /**
     * the header of this ShellScript
     */
    private final static String HEADER = LF + EXPLANATION + LF + H + GENERATOR + LF + H + SCM_ID
            + LF + H + AUTHOR + LF + H + REVISION + LF + H + currentDateMsg + LF + LF;

    // ~ Instance fields ********************************************************************
    // ~ Instance fields ********************************************************************
    /**
     * Internal ContentBuffer of this ShellScript
     */
    private StringBuffer content = new StringBuffer();

    /**
     * internal field: where to write via write( itsLocation ) this shellscript.
     */
    private String itsLocation;

    /**
     * internal field: holds the shell
     */
    private String itsShell;

    public final static String SH = "sh";

    public final static String BOURNE_SHELL = SH;

    public final static String BASH = "bash";

    public final static String BOURNE_AGAIN_SHELL = BASH;

    // ~ Constructors ***********************************************************************
    // ~ Constructors ***********************************************************************
    /**
     * Creates and initializes the ShellScript for running on the given shell.
     *
     * @param aShell "sh", "bash", "ksh", "csh" and so an...
     */
    public ShellScript(String aShell)
    {
        // Null was given the Header has to be self created
        if (null != aShell)
        {
            setShell(aShell);
            content.append("#!/usr/bin/env ").append(getShell());
            content.append(HEADER);
        }
    }

    /**
     * Creates and initializes the ShellScript for running on the bourne shell:
     * "sh".
     */
    public ShellScript()
    {
        this(SH);
    }

    /**
     * Sets the current used shell
     *
     * @param aShell The Shell which should set and used.
     */
    public void setShell(String aShell)
    {
        itsShell = aShell;
    }

    /**
     * Gets the current used shell. Can be null.
     *
     * @return the Shell
     */
    public String getShell()
    {
        return itsShell;
    }

    // ~ Methods ****************************************************************************
    // ~ Methods ****************************************************************************
    /**
     * Appends an Object or String to this ShellScript.
     *
     * @param anObject the Object to append
     */
    public void append(Object anObject)
    {
        content.append(anObject);
    }

    /**
     * Appends a Char to this ShellScript.
     *
     * @param aChar a char to append
     */
    public void append(char aChar)
    {
        content.append(aChar);
    }

    /**
     * Appends the Array as one Commandline Space Separated This is usefull with
     * our FileExecutor
     *
     * @param anArray
     */
    public void append(String[] anArray)
    {
        for (int i = 0; i < anArray.length; i++)
        {
            String string = anArray[i];

            append(string);
            if (anArray.length > i)
            {
                append(' ');
            }
        }
    }

    /**
     * Appends the Array as ONE! Commandline Space Separated And additionionally
     * append an linefeed "\n" This is usefull with our FileExecutor
     *
     * @param anArray
     */
    public void appendln(String[] anArray)
    {
        for (int i = 0; i < anArray.length; i++)
        {
            String string = anArray[i];

            append(string);
            if (anArray.length > i)
            {
                append(' ');
            }
        }
        appendln();
    }

    /**
     * Appends an Object or String to this ShellScript with unix linefeed
     * ("\n").
     *
     * @param anObject the Object to append
     */
    public void appendln(Object anObject)
    {
        append(anObject);
        append(LF);
    }

    /**
     * Appends a Char Object or String to this ShellScript with unix linefeed
     * ("\n").
     *
     * @param aChar a char to append
     */
    public void appendln(char aChar)
    {
        append(aChar);
        append(LF);
    }

    /**
     * Appends an unix linefeed to this script ("\n").
     */
    public void appendln()
    {
        append(LF);
    }

    /**
     * gets the Content of this Script.
     *
     * @return the Content
     */
    public StringBuffer getContent()
    {
        return content;
    }

    /**
     * Gets the Content of this Script as String
     *
     * @return the script as String
     */
    public String getContentAsString()
    {
        return content.toString();
    }

    /**
     * Dumps the ShellScript Content, and Location. Use getContentAsString() to
     * get this ShellScripts Content
     *
     * @return The ShellScript as Object dump.
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append(getClass().getName());
        result.append('\n');
        result.append(itsLocation);
        result.append('\n');
        result.append(content);

        return result.toString();
    }

    /**
     * write this to the given Destination FileName
     *
     * @param aDestination a destination filename
     */
    public void write(String aDestination)
    {
        itsLocation = aDestination;

        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(aDestination));
            writer.write(content.toString());
            writer.write(LH + aDestination + LF);
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Executes this ShellScript with the given Params.<br>
     * NOTE: the params cannot be contain whitespaces.<br>
     * This (su -c &lt;br&gt;"cp from to"&lt;/br&gt;) would not work:<br>
     * because the underlaying java.runtime.exec("command") does not handle
     * balanced or unbalanced (") correctly.<br>
     * else just whitespace separate tokens.<br>
     * This means for the sample. runtime.exec() would ever execute such as: su
     * "-c" "\"cp" "fromFile" "toFile\""<br>
     * But this his hidden in Sun's native code ;-(<br>
     * This was the reason to write this class to have a Workaround :-)
     *
     * @param itsParams
     * @return the output from stdout of the execution.
     */
    public String exec(String itsParams)
    {
        FileExecutor.getExecOutput(new String[]
        {
            UnixHelper.getCustomCommand("chmod"), "+x",
            itsLocation
        });

        if (itsParams != null)
        {
            return FileExecutor.getExecOutput(new String[]
            {
                itsLocation, itsParams
            });
        }
        else
        {
            return FileExecutor.getExecOutput(new String[]
            {
                itsLocation
            });
        }
    }

    /**
     * Execute this ShellScript.
     *
     * @return the output from stdout of the execution.
     */
    public String exec()
    {
        return exec(null);
    }

//    /**
//     * Prepared for later use and for refactoring
//     **/
//    public String storeAndExecInTemp()
//    {
//        String pseudoUnique = this.getClass().getName() + Long.toString(System.currentTimeMillis());
//
//        String scriptFilename = null;
//
//        try
//        {
//            scriptFilename = File.createTempFile(pseudoUnique, ".sh").toString();
//        }
//        catch (IOException e)
//        {
//            scriptFilename = System.getProperty("java.io.tmpdir", "/tmp") + "/" + pseudoUnique
//                    + ".sh";
//            e.printStackTrace();
//        }
//
//        write( scriptFilename );
//        return exec();
//
//    }
    /**
     * Execs ths given lines in the creted shell stored on location.
     *
     * @param aShell A Shell which will be eexecute the script.
     * @param lines The content of the script.
     * @param aLocation The location where to store.
     * @param itsParams Th eoptional params of the script.
     * @return the exec result
     */
    public static String execute(String aShell, StringBuffer lines, String aLocation,
            String itsParams)
    {
        ShellScript script = new ShellScript((aShell == null) ? "sh" : aShell);
        script.append(lines);
        script.write(aLocation);

        return (itsParams == null) ? script.exec() : script.exec(itsParams);
    }

    /**
     * Executes ths given lines in the created default shell (sh) stored on
     * location.
     *
     * @param lines the lines of the script to exec.s
     * @param aLocation where to store
     * @return the stdout of the script.
     */
    public static String execute(StringBuffer lines, String aLocation)
    {
        return ShellScript.execute(null, lines, aLocation, null);
    }

    /**
     * Executes and removes the script.<br>
     * The Lines be also written in python or perl,<br>
     * In this case, the Shell must be python or perl or so.
     *
     * @param aShell The Shell which should exec the script. Can be also be
     * python or perl, if the shellcontent is given in this language.
     * @param lines of the script.
     * @param aLocation where to store.
     * @param itsParams which should be pass to the script.
     * @return the stdout.
     */
    public static String execAndDelete(String aShell, StringBuffer lines, String aLocation,
            String itsParams)
    {
        String result = execute(aShell, lines, aLocation, itsParams);
        File location = new File(aLocation);

        try
        {
            location.delete();
        }
        catch (Exception e)
        {
            location.deleteOnExit();
        }

        return result;
    }

    /**
     * Executes and removes the script.
     *
     * @param lines of the script.
     * @param aLocation where to store.
     * @return the sdtout.
     */
    public static String execAndDelete(StringBuffer lines, String aLocation)
    {
        return execAndDelete(null, lines, aLocation, null);
    }

    /**
     * Test Main Method Run test with: java -cp .jar
     * com.izforge.izpack.util.unix.ShellScript
     *
     * @param args Arguments from Commandline
     */
    public static void main(String[] args)
    {
        /*
         * ShellScript s = new ShellScript( ); s.append( "ls $HOME" ); s.write( System.getProperty(
         * "user.home", "." ) + File.separator + "test.sh" );
         */

 /*
         * System.out.println(ShellScript.execute(new StringBuffer("ls $HOME"), System.getProperty(
         * "user.home", ".") + File.separator + Long.toString(System.currentTimeMillis()) +
         * "test.sh"));
         */
    }

    /**
     * Deletes only if Location is not null.
     */
    public void delete()
    {
        if (itsLocation != null)
        {
            File location = new File(itsLocation);

            try
            {
                location.delete();
            }
            catch (Exception e)
            {
                location.deleteOnExit();
            }
        }
    }
}
