/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2006 Klaus Bartz
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

package com.izforge.izpack.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * This class tries to remove a given list of files which are locked by this process. For this the
 * paths of the files are stored in a temporary file and a new process will be created. The class
 * files which are needed by the new process will be unpacked from the jar file under users temp dir
 * in a "sandbox". The new process receive the path of the temporary file and some other
 * information. After a wait intervall it reads the path file and removes all files which there are
 * listed. Next the created "sandbox" and the path file will be removed. This class uses the
 * characteristik of the system loader that jar files will be keeped open, simple class files will
 * be closed after loading a class. Therefore jar files are locked and cannot be deleted, class
 * files are not locked and deletable.<br>
 * The idea for this stuff is copied from Chadwick McHenry's SelfModifier in the uninstaller stuff
 * of IzPack.
 *
 * @author Klaus Bartz
 */
public class LibraryRemover
{

    /**
     * All class files which are needed for the second process. All have to be in this installers
     * jar file. No slash in front should be used; no dot else slashes should be used;
     * extension (.class) will be required.
     */
    private static final String[] SANDBOX_CONTENT = {"com/izforge/izpack/util/LibraryRemover.class"};

    /**
     * System property name of base for log and sandbox of secondary processes.
     */
    private static final String BASE_KEY = "lib.rem.base";

    /**
     * System property name of phase (1, 2, or 3) indicator.
     */
    private static final String PHASE_KEY = "self.mod.phase";

    /**
     * Prefix of sandbox, path and log file.
     */
    private static final String PREFIX = "InstallRemover";

    /**
     * Phase of this process.
     */
    private int phase = 0;

    /**
     * Log for phase 2, because we can't capture the stdio from them.
     */
    private File logFile = null;

    /**
     * Directory which we extract too, invoke from, and finally delete.
     */
    private File sandbox = null;

    /**
     * The file which contains the paths of the files to delete.
     */
    private File specFile = null;

    /**
     * For logging time.
     */
    private SimpleDateFormat isoPoint = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Also for logging time.
     */
    private Date date = new Date();

    /**
     * Constructor for both phases. Depending on the phase different initializing will be performed.
     *
     * @param phase for which an object should be created.
     * @throws IOException
     */
    private LibraryRemover(int phase) throws IOException
    {
        this.phase = phase;
        if (phase == 1)
        {
            ProcessHelper.tryExecJava();
        }
        else
        {
            logFile = new File(System.getProperty(BASE_KEY) + ".log");
            specFile = new File(System.getProperty(BASE_KEY) + ".spec");
            sandbox = new File(System.getProperty(BASE_KEY) + ".d");
        }
    }

    /**
     * Entry point for phase 1. This class tries to remove all files given in the Vector.
     *
     * @param temporaryFileNames
     * @throws IOException
     */
    public static void invoke(List<String> temporaryFileNames) throws IOException
    {
        LibraryRemover self = new LibraryRemover(1);
        self.invoke1(temporaryFileNames);
    }

    /**
     * Internal invoke method for phase 1.
     *
     * @param temporaryFileNames list of paths of the files which should be removed
     * @throws IOException
     */
    private void invoke1(List<String> temporaryFileNames) throws IOException
    {
        // Initialize sandbox and log file to be unique, but similarly named
        while (true)
        {
            logFile = File.createTempFile(PREFIX, ".log");
            String canonicalPath = logFile.getCanonicalPath();
            specFile = new File(canonicalPath.substring(0, canonicalPath.length() - 4) + ".spec");
            sandbox = new File(canonicalPath.substring(0, canonicalPath.length() - 4) + ".d");

            // check if the similarly named directory is free
            if (!sandbox.exists())
            {
                break;
            }

            logFile.delete();
        }
        if (!sandbox.mkdir())
        {
            throw new RuntimeException("Failed to create temp dir: " + sandbox);
        }

        sandbox = sandbox.getCanonicalFile();
        logFile = logFile.getCanonicalFile();
        OutputStream out = null;
        InputStream in = null;
        byte[] buf = new byte[5120];
        int extracted = 0;
        // Write out the class files from the current installer jar into the sandbox.
        // This allows later to delete the classes because class files are deleteable
        // also the using process is running, jar files are not deletable in that
        // situation.,
        for (String aSANDBOX_CONTENT : SANDBOX_CONTENT)
        {
            in = getClass().getResourceAsStream("/" + aSANDBOX_CONTENT);

            File outFile = new File(sandbox, aSANDBOX_CONTENT);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists())
            {
                parent.mkdirs();
            }

            out = new BufferedOutputStream(new FileOutputStream(outFile));

            int n;
            while ((n = in.read(buf, 0, buf.length)) > 0)
            {
                out.write(buf, 0, n);
            }

            out.close();
            extracted++;

        }
        // We write a file which contains the paths to remove.
        out = new BufferedOutputStream(new FileOutputStream(specFile));
        BufferedWriter specWriter = new BufferedWriter(new OutputStreamWriter(out));
        Iterator<String> iter = temporaryFileNames.iterator();
        while (iter.hasNext())
        {
            specWriter.write(iter.next());
            if (iter.hasNext())
            {
                specWriter.newLine();
            }
        }
        specWriter.flush();
        out.close();

        spawn(2);

        // finally, if all went well, the invoking process must exit
        log("library cleanup done");
        //
        // IZPACK-276:
        // Do never call System.exit during a cleanup otherwise the correct exit value is lost!
        // System.exit(0);
    }

    /**
     * Returns an ArrayList of the files to delete.
     *
     * @return The files list.
     * @throws Exception Description of the Exception
     */
    private ArrayList<File> getFilesList() throws Exception
    {
        // Initialisations
        TreeSet<File> files = new TreeSet<File>(Collections.reverseOrder());
        InputStream in = new FileInputStream(specFile);
        InputStreamReader inReader = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(inReader);

        // We read it
        String read = reader.readLine();
        while (read != null)
        {
            files.add(new File(read));
            read = reader.readLine();
        }
        in.close();
        // We return it
        return new ArrayList<File>(files);
    }

    /**
     * Invoke methode for phase 2.
     */
    private void invoke2()
    {

        try
        {
            // Give main program time to exit.
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception x)
            {
            }

            ArrayList<File> files = getFilesList();
            // We destroy the files

            log("deleteing temporary dlls/shls");
            for (File file : files)
            {
                file.delete();
                if (file.exists())
                {
                    log("    deleting of " + file.getCanonicalPath() + " failed!!!");
                }
                else
                {
                    log("    " + file.getCanonicalPath());
                }

            }

            // clean up and go
            log("deleteing sandbox");
            deleteTree(sandbox);
            specFile.delete();
        }
        catch (Exception e)
        {
            log(e);
        }
    }

    /**
     * Copied from com.izforge.izpack.uninstaller.SelfModifier. Little addaption for this class.
     *
     * @param nextPhase phase of the spawn
     * @return created process object
     * @throws IOException
     */
    private Process spawn(int nextPhase) throws IOException
    {
        String base = logFile.getAbsolutePath();
        base = base.substring(0, base.length() - 4);

        // invoke from tmpdir, passing target method arguments as args, and
        // SelfModifier parameters as sustem properties
        String[] javaCmd = new String[]{ProcessHelper.getJavaCommand(), "-classpath", sandbox.getAbsolutePath(),
                "-D" + BASE_KEY + "=" + base, "-D" + PHASE_KEY + "=" + nextPhase,
                getClass().getName()};

        StringBuilder sb = new StringBuilder("Spawning phase ");
        sb.append(nextPhase).append(": ");
        for (String aJavaCmd : javaCmd)
        {
            sb.append("\n\t").append(aJavaCmd);
        }
        log(sb.toString());

        // Just invoke it and let it go, the exception will be caught above
        return ProcessHelper.exec(javaCmd);
    }

    /**
     * Recursively delete a file structure. Copied from com.izforge.izpack.uninstaller.SelfModifier.
     * Little addaption to this class.
     *
     * @return command for spawning
     */
    public static boolean deleteTree(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File file1 : files)
                {
                    deleteTree(file1);
                }
            }
        }
        return file.delete();
    }

    public static void main(String[] args)
    {
        // Phase 2 removes given path list, sandbox and spec file.
        // Phase 3 as used in SelfModifier will be not needed here because
        // this class do not use a GUI which can call exit like the
        // one in SelfModifier.

        try
        {
            // all it's attributes are retrieved from system properties
            LibraryRemover librianRemover = new LibraryRemover(2);
            librianRemover.invoke2();
        }
        catch (IOException ioe)
        {
            System.err.println("Error invoking a secondary phase");
            System.err.println("Note that this program is only intended as a secondary process");
            ioe.printStackTrace();
        }
    }

    /**
     * ********************************************************************************************
     * --------------------------------------------------------------------- Logging
     * --------------------------------------------------------------------- Copied from
     * com.izforge.izpack.uninstaller.SelfModifier.
     */

    PrintStream log = null;

    private PrintStream checkLog()
    {
        try
        {
            if (log == null)
            {
                log = new PrintStream(new FileOutputStream(logFile.toString(), true));
            }
        }
        catch (IOException x)
        {
            System.err.println("Phase " + phase + " log err: " + x.getMessage());
            x.printStackTrace();
        }
        date.setTime(System.currentTimeMillis());
        return log;
    }

    private void log(Throwable t)
    {
        if (checkLog() != null)
        {
            log.println(isoPoint.format(date) + " Phase " + phase + ": " + t.getMessage());
            t.printStackTrace(log);
        }
    }

    private void log(String msg)
    {
        if (checkLog() != null)
        {
            log.println(isoPoint.format(date) + " Phase " + phase + ": " + msg);
        }
    }

}
