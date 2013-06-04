/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Olexij Tkatchenko
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

import static com.izforge.izpack.util.Platform.Name.UNIX;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.ExecutableFile;

/**
 * Executes a bunch of files. This class is intended to do a system dependent installation
 * postprocessing. Executable file can be any file installed with current package. After execution
 * the file can be optionally removed. Before execution on Unix systems execution flag will be set
 * on processed file.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class FileExecutor
{

    private static final Logger logger = Logger.getLogger(FileExecutor.class.getName());

    private static final String JAR_FILE_SUFFIX = ".jar";

    private boolean stopThread(Thread t, MonitorInputStream monitorInputStream)
    {
        monitorInputStream.stop();
        long softTimeout = 1000;
        try
        {
            t.join(softTimeout);
        }
        catch (InterruptedException e)
        {
            // ignore
        }

        if (!t.isAlive())
        {
            return true;
        }

        t.interrupt();
        long hardTimeout = 1000;
        try
        {
            t.join(hardTimeout);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        return !t.isAlive();
    }

    /**
     * Constructs a new executor. The executable files specified must have pretranslated paths
     * (variables expanded and file separator characters converted if necessary).
     *
     * @param files the executable files to process
     */
    public FileExecutor(Collection<ExecutableFile> files)
    {
        this.files = files;
    }

    /**
     * Constructs a new executor.
     */
    public FileExecutor()
    {
        this.files = null;
    }

    /**
     * Gets the output of the given (console based) commandline
     *
     * @param aCommandLine to execute
     * @return the result of the command
     */
    public static String getExecOutput(String[] aCommandLine)
    {
        return getExecOutput(aCommandLine, null, false);

    }

    /**
     * Gets the output of the given (console based) commandline
     *
     * @param aCommandLine to execute
     * @param dir          the working directory for the execution
     * @return the result of the command
     */
    public static String getExecOutput(String[] aCommandLine, String dir)
    {
        return getExecOutput(aCommandLine, dir, false);

    }

    /**
     * Gets the output of the given (console based) commandline
     *
     * @param aCommandLine     to execute
     * @param forceToGetStdOut if true returns stdout
     * @return the result of the command
     */
    public static String getExecOutput(String[] aCommandLine, boolean forceToGetStdOut)
    {
        return getExecOutput(aCommandLine, null, forceToGetStdOut);

    }

    /**
     * Executes the given Command and gets the result of StdOut, or if exec returns !=0:  StdErr.
     *
     * @param aCommandLine     aCommandLine to execute
     * @param dir              the working directory for the execution
     * @param forceToGetStdOut if true returns stdout
     * @return the result of the command stdout or stderr if exec returns !=0
     */
    public static String getExecOutput(String[] aCommandLine, String dir, boolean forceToGetStdOut)
    {
        FileExecutor fileExecutor = new FileExecutor();

        String[] execOut = new String[2];

        int execResult = fileExecutor.executeCommand(aCommandLine, execOut, dir);

        if (execResult == 0)

        {
            return execOut[0];
        }
        else if (forceToGetStdOut)
        {
            return execOut[0];
        }
        else
        {
            return execOut[1];
        }
    }

    /**
     * Executed a system command and waits for completion.
     *
     * @param params system command as string array
     * @param output contains output of the command index 0 = standard output index 1 = standard
     *               error
     * @return exit status of process
     */
    public int executeCommand(String[] params, String[] output)
    {
        return executeCommand(params, output, null);
    }

    /**
     * Executed a system command and waits for completion.
     *
     * @param params system command as string array
     * @param output contains output of the command index 0 = standard output index 1 = standard
     *               error
     * @param dir    the working directory for the execution
     * @return exit status of process
     */
    public int executeCommand(String[] params, String[] output, String dir)
    {
        StringBuilder retval = new StringBuilder();
        retval.append("executeCommand\n");
        if (params != null)
        {
            for (String param : params)
            {
                retval.append("\tparams: ").append(param);
                retval.append("\n");
            }
        }
        if (dir != null)
        {
            retval.append("working dir: ").append(dir).append("\n");
        }
        Process process = null;
        MonitorInputStream outMonitor = null;
        MonitorInputStream errMonitor = null;
        Thread outMonitorThread = null;
        Thread errMonitorThread = null;
        int exitStatus = -1;

        logger.fine(retval.toString());

        try
        {
            // Resolve ".." and "." in paths which otherwise couldn't be found
            if (params[0].matches("^.*[\\\\/]+[\\.]+[\\\\/]+.*$"))
            {
                params[0] = new File(params[0]).getCanonicalPath();
            }
            if (dir != null)
            {
                if (dir.matches("^.*[\\\\/]+[\\.]+[\\\\/]+.*$"))
                {
                    dir = new File(dir).getCanonicalPath();
                }
                process = Runtime.getRuntime().exec(params, null, new File(dir));
            }
            else
            {
                process = Runtime.getRuntime().exec(params);
            }

            StringWriter outWriter = new StringWriter();
            StringWriter errWriter = new StringWriter();

            InputStreamReader outStreamReader = new InputStreamReader(process.getInputStream());
            InputStreamReader errStreamReader = new InputStreamReader(process.getErrorStream());
            outMonitor = new MonitorInputStream(outStreamReader, outWriter);
            errMonitor = new MonitorInputStream(errStreamReader, errWriter);
            outMonitorThread = new Thread(outMonitor);
            errMonitorThread = new Thread(errMonitor);
            outMonitorThread.setDaemon(true);
            errMonitorThread.setDaemon(true);
            outMonitorThread.start();
            errMonitorThread.start();

            // wait for command to complete
            exitStatus = process.waitFor();
            outMonitorThread.join();
            errMonitorThread.join();

            // save command output
            output[0] = outWriter.toString();
            logger.fine("stdout:");
            logger.fine(output[0]);
            output[1] = errWriter.toString();
            logger.fine("stderr:");
            logger.fine(output[1]);
            logger.fine("exit status: " + Integer.toString(exitStatus));
        }
        catch (InterruptedException e)
        {
            logger.log(Level.FINE, e.toString(), e);
            stopThread(outMonitorThread, outMonitor);
            stopThread(errMonitorThread, errMonitor);
            output[0] = "";
            output[1] = e.getMessage() + "\n";
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING, e.toString(), e);
            output[0] = "";
            output[1] = e.getMessage() + "\n";
        }
        finally
        {
            // cleans up always resources like file handles etc.
            // else many calls (like chmods for every file) can produce
            // too much open handles.
            if (process != null)
            {
                process.destroy();
            }
        }
        return exitStatus;
    }

    /**
     * Executes files specified at construction time.
     *
     * @param currentStage the stage of the installation
     * @param matcher      the platform-model matcher
     * @param handler      The AbstractUIHandler to notify on errors.
     * @return 0 on success, else the exit status of the last failed command
     */
    public int executeFiles(int currentStage, PlatformModelMatcher matcher, AbstractUIHandler handler)
    {
        int exitStatus = 0;
        String[] output = new String[2];
        // String permissions = (System.getProperty("user.name").equals("root"))
        // ? "a+x" : "u+x";
        String permissions = "a+x";
        boolean isUnix = matcher.getCurrentPlatform().isA(UNIX);

        // loop through all executables
        Iterator<ExecutableFile> efileIterator = this.files.iterator();
        while (exitStatus == 0 && efileIterator.hasNext())
        {
            ExecutableFile efile = efileIterator.next();
            boolean deleteAfterwards = !efile.keepFile;
            File file = new File(efile.path);
            logger.fine("Handling executable file " + efile);

            // skip file if not for current OS (it might not have been installed
            // at all)
            if (!matcher.matchesCurrentPlatform(efile.osList))
            {
                continue;
            }

            if (ExecutableFile.BIN == efile.type && currentStage != ExecutableFile.UNINSTALL && isUnix)
            {
                // fix executable permission for unix systems
                logger.fine("Making file executable (setting executable flag)");
                String[] params = {"/bin/chmod", permissions, file.toString()};
                exitStatus = executeCommand(params, output);
                if (exitStatus != 0)
                {
                    handler.emitWarning("file execution error", "Error executing \n" + params[0]
                            + " " + params[1] + " " + params[2]);
                    continue;
                }
            }

            // execute command in POSTINSTALL stage
            if ((exitStatus == 0)
                    && ((currentStage == ExecutableFile.POSTINSTALL && efile.executionStage == ExecutableFile.POSTINSTALL) || (currentStage == ExecutableFile.UNINSTALL && efile.executionStage == ExecutableFile.UNINSTALL)))
            {
                List<String> paramList = new ArrayList<String>();
                if (ExecutableFile.BIN == efile.type)
                {
                    paramList.add(file.toString());
                }
                else if (ExecutableFile.JAR == efile.type && null == efile.mainClass)
                {
                    paramList.add(System.getProperty("java.home") + "/bin/java");
                    paramList.add("-jar");
                    paramList.add(file.toString());
                }
                else if (ExecutableFile.JAR == efile.type && null != efile.mainClass)
                {
                    paramList.add(System.getProperty("java.home") + "/bin/java");
                    paramList.add("-cp");
                    try
                    {
                        paramList.add(buildClassPath(file.toString()));
                    }
                    catch (Exception e)
                    {
                        exitStatus = -1;
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                    paramList.add(efile.mainClass);
                }

                if (null != efile.argList && !efile.argList.isEmpty())
                {
                    paramList.addAll(efile.argList);
                }

                String[] params = new String[paramList.size()];
                for (int i = 0; i < paramList.size(); i++)
                {
                    params[i] = paramList.get(i);
                }

                exitStatus = executeCommand(params, output);

                // bring a dialog depending on return code and failure handling
                if (exitStatus != 0)
                {
                    deleteAfterwards = false;
                    String message = output[0] + "\n" + output[1];
                    if (message.length() == 1)
                    {
                        message = "Failed to execute " + file.toString() + ".";
                    }

                    if (efile.onFailure == ExecutableFile.ABORT)
                    {
                        handler.emitError("File execution returned " + exitStatus, message);
                    }
                    else if (efile.onFailure == ExecutableFile.WARN)
                    {
                        handler.emitWarning("File execution returned " + exitStatus, message);
                        exitStatus = 0;
                    }
                    else if (efile.onFailure == ExecutableFile.IGNORE)
                    {
                        // do nothing
                        exitStatus = 0;
                    }
                    else
                    {
                        if (handler
                                .askQuestion("Execution Failed", message + "\nContinue Installation?",
                                             AbstractUIHandler.CHOICES_YES_NO) == AbstractUIHandler.ANSWER_YES)
                        {
                            exitStatus = 0;
                        }
                    }

                }

            }

            // POSTINSTALL executables will be deleted
            if (efile.executionStage == ExecutableFile.POSTINSTALL && deleteAfterwards)
            {
                if (file.canWrite())
                {
                    file.delete();
                }
            }

        }
        return exitStatus;
    }

    /**
     * Transform classpath as specified in targetFile attribute into
     * OS specific classpath. This method also resolves directories
     * containing jar files. ';' and ':' are valid delimiters allowed
     * in targetFile attribute.
     *
     * @param targetFile
     * @return valid Java classpath
     * @throws Exception
     */
    private String buildClassPath(String targetFile) throws Exception
    {
        StringBuilder classPath = new StringBuilder();
        List<String> jars = new ArrayList<String>();
        String rawClassPath =
                targetFile
                        .replaceAll(":\\\\", "#DRIVE#")
                        .replaceAll(";", "#")
                        .replaceAll(":", "#")
                        .replace("#DRIVE#", ":\\");
        String[] rawJars = rawClassPath.split("#");
        for (String rawJar : rawJars)
        {
            File file = new File(rawJar);
            jars.add(rawJar);

            if (file.isDirectory())
            {
                String[] subDirJars = FileUtil.getFileNames(rawJar,
                                                            new FilenameFilter()
                                                            {
                                                                @Override
                                                                public boolean accept(File dir, String name)
                                                                {
                                                                    return name.toLowerCase().endsWith(JAR_FILE_SUFFIX);
                                                                }

                                                            });
                if (subDirJars != null)
                {
                    for (String subDirJar : subDirJars)
                    {
                        jars.add(rawJar + File.separator + subDirJar);
                    }
                }
            }
        }

        Iterator<String> iter = jars.iterator();
        if (iter.hasNext())
        {
            classPath.append(iter.next());
        }
        while (iter.hasNext())
        {
            classPath.append(File.pathSeparatorChar).append(iter.next());
        }

        return classPath.toString();
    }

    /**
     * The files to execute.
     */
    private Collection<ExecutableFile> files;
}
