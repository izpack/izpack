/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Tino Schwarze
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
package com.izforge.izpack.panels.compile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * This class does alle the work for compiling sources.
 * <p/>
 * It responsible for
 * <ul>
 * <li>parsing the compilation spec XML file
 * <li>collecting and creating all jobs
 * <li>doing the actual compilation
 * </ul>
 *
 * @author Tino Schwarze
 */
public class CompileWorker implements Runnable
{

    private static final Logger LOGGER = Logger.getLogger(CompileWorker.class.getName());

    /**
     * Compilation jobs
     */
    private ArrayList<CompilationJob> jobs;

    /**
     * Name of resource for specifying compilation parameters.
     */
    private static final String SPEC_RESOURCE_NAME = "CompilePanel.Spec.xml";

    private static final String ECLIPSE_COMPILER_NAME = "Integrated Eclipse JDT Compiler";

    private static final String ECLIPSE_COMPILER_CLASS = "org.eclipse.jdt.internal.compiler.batch.Main";

    private VariableSubstitutor vs;

    private IXMLElement spec;

    private InstallData idata;

    private CompileHandler handler;

    private IXMLElement compilerSpec;

    private ArrayList<String> compilerList;

    private String compilerToUse;

    private IXMLElement compilerArgumentsSpec;

    private ArrayList<String> compilerArgumentsList;

    private String compilerArgumentsToUse;

    private CompileResult result = null;

    private final Resources resources;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The constructor.
     *
     * @param installData the installation data
     * @param handler the handler to notify of progress
     * @param variableSubstitutor the variable substituter
     * @param resources the resources
     * @param matcher The platform-model matcher
     * @throws IOException for any I/O error
     */
    public CompileWorker(InstallData installData, CompileHandler handler, VariableSubstitutor variableSubstitutor,
            Resources resources, PlatformModelMatcher matcher) throws IOException
    {
        this.idata = installData;
        this.handler = handler;
        this.vs = variableSubstitutor;
        this.resources = resources;
        this.matcher = matcher;
        if (!readSpec())
        {
            throw new IOException("Error reading compilation specification");
        }
    }

    /**
     * Return list of compilers to choose from.
     *
     * @return ArrayList of String
     */
    public ArrayList<String> getAvailableCompilers()
    {
        readChoices(this.compilerSpec, this.compilerList);
        return this.compilerList;
    }

    /**
     * Set the compiler to use.
     * <p/>
     * The compiler is checked before compilation starts.
     *
     * @param compiler compiler to use (not checked)
     */
    public void setCompiler(String compiler)
    {
        this.compilerToUse = compiler;
    }

    /**
     * Get the compiler used.
     *
     * @return the compiler.
     */
    public String getCompiler()
    {
        return this.compilerToUse;
    }

    /**
     * Return list of compiler arguments to choose from.
     *
     * @return ArrayList of String
     */
    public ArrayList<String> getAvailableArguments()
    {
        readChoices(this.compilerArgumentsSpec, this.compilerArgumentsList);
        return this.compilerArgumentsList;
    }

    /**
     * Set the compiler arguments to use.
     *
     * @param arguments The argument to use.
     */
    public void setCompilerArguments(String arguments)
    {
        this.compilerArgumentsToUse = arguments;
    }

    /**
     * Get the compiler arguments used.
     *
     * @return The arguments used for compiling.
     */
    public String getCompilerArguments()
    {
        return this.compilerArgumentsToUse;
    }

    /**
     * Get the result of the compilation.
     *
     * @return The result.
     */
    public CompileResult getResult()
    {
        return this.result;
    }

    /**
     * Start the compilation in a separate thread.
     */
    public void startThread()
    {
        Thread compilationThread = new Thread(this, "compilation thread");
        // will call this.run()
        compilationThread.start();
    }

    /**
     * This is called when the compilation thread is activated.
     * <p/>
     * Can also be called directly if asynchronous processing is not desired.
     */
    @Override
    public void run()
    {
        try
        {
            if (!collectJobs())
            {
                List<String> args = new ArrayList<String>();
                args.add("nothing to do");

                this.result = new CompileResult(idata.getMessages().get("CompilePanel.worker.nofiles"), args, "", "");
            }
            else
            {
                this.result = compileJobs();
            }
        }
        catch (Exception e)
        {
            this.result = new CompileResult(e);
        }

        this.handler.stopAction();
    }

    private boolean readSpec()
    {
        InputStream input;
        try
        {
            input = resources.getInputStream(SPEC_RESOURCE_NAME);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        IXMLParser parser = new XMLParser();

        try
        {
            this.spec = parser.parse(input);
        }
        catch (Exception e)
        {
            System.out.println("Error parsing XML specification for compilation.");
            e.printStackTrace();
            return false;
        }

        if (!this.spec.hasChildren())
        {
            return false;
        }

        this.compilerArgumentsList = new ArrayList<String>();
        this.compilerList = new ArrayList<String>();

        // read <global> information
        IXMLElement global = this.spec.getFirstChildNamed("global");

        // use some default values if no <global> section found
        if (global != null)
        {

            // get list of compilers
            this.compilerSpec = global.getFirstChildNamed("compiler");

            if (this.compilerSpec != null)
            {
                readChoices(this.compilerSpec, this.compilerList);
            }

            this.compilerArgumentsSpec = global.getFirstChildNamed("arguments");

            if (this.compilerArgumentsSpec != null)
            {
                // basicly perform sanity check
                readChoices(this.compilerArgumentsSpec, this.compilerArgumentsList);
            }

        }

        // supply default values if no useful ones where found
        if (this.compilerList.size() == 0)
        {
            this.compilerList.add("javac");
            this.compilerList.add("jikes");
        }

        if (this.compilerArgumentsList.size() == 0)
        {
            this.compilerArgumentsList.add("-O -g:none");
            this.compilerArgumentsList.add("-O");
            this.compilerArgumentsList.add("-g");
            this.compilerArgumentsList.add("");
        }

        return true;
    }

    // helper function
    private void readChoices(IXMLElement element, ArrayList<String> choiceList)
    {
        List<IXMLElement> choices = element.getChildrenNamed("choice");

        if (choices == null)
        {
            return;
        }

        choiceList.clear();

        for (IXMLElement choice : choices)
        {
            String value = choice.getAttribute("value");

            if (value != null)
            {
                List<OsModel> osconstraints = OsConstraintHelper.getOsList(choice);

                if (matcher.matchesCurrentPlatform(osconstraints))
                {
                    if (value.equalsIgnoreCase(ECLIPSE_COMPILER_NAME))
                    {
                        // check for availability of eclipse compiler
                        try
                        {
                            Class.forName(ECLIPSE_COMPILER_CLASS);
                            choiceList.add(value);
                        }
                        catch (ExceptionInInitializerError eiie)
                        {
                            // ignore, just don't add it as a choice
                        }
                        catch (ClassNotFoundException cnfe)
                        {
                            // ignore, just don't add it as a choice
                        }
                    }
                    else
                    {
                        try
                        {
                            choiceList.add(this.vs.substitute(value, SubstitutionType.TYPE_PLAIN));
                        }
                        catch (Exception e)
                        {
                            // ignore, just don't add it as a choice
                        }
                    }
                }
            }

        }

    }

    /**
     * Parse the compilation specification file and create jobs.
     */
    private boolean collectJobs() throws Exception
    {
        IXMLElement data = this.spec.getFirstChildNamed("jobs");

        if (data == null)
        {
            return false;
        }

        // list of classpath entries
        List<String> classpath = new ArrayList<String>();

        this.jobs = new ArrayList<CompilationJob>();

        // we throw away the toplevel compilation job
        // (all jobs are collected in this.jobs)
        collectJobsRecursive(data, classpath);

        return true;
    }

    /**
     * perform the actual compilation
     */
    private CompileResult compileJobs()
    {
        ArrayList<String> args = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(this.compilerArgumentsToUse);

        while (tokenizer.hasMoreTokens())
        {
            args.add(tokenizer.nextToken());
        }

        Iterator<CompilationJob> jobIt = this.jobs.iterator();

        this.handler.startAction("Compilation", this.jobs.size());

        // check whether compiler is valid (but only if there are jobs)
        if (jobIt.hasNext())
        {
            CompilationJob firstJob = this.jobs.get(0);

            CompileResult checkResult = firstJob.checkCompiler(this.compilerToUse, args);
            if (!checkResult.isContinue())
            {
                return checkResult;
            }

        }

        int jobNo0 = 0;

        while (jobIt.hasNext())
        {
            CompilationJob job = jobIt.next();

            this.handler.nextStep(job.getName(), job.getSize(), jobNo0++);

            CompileResult jobResult = job.perform(this.compilerToUse, args);

            if (!jobResult.isContinue())
            {
                return jobResult;
            }
        }

        LOGGER.fine("Compilation finished");
        return new CompileResult();
    }

    private CompilationJob collectJobsRecursive(IXMLElement node, List<String> classpath)
            throws Exception
    {
        List<IXMLElement> toplevelTags = node.getChildren();
        List<String> ourclasspath = new ArrayList<String>(classpath);
        ArrayList<File> files = new ArrayList<File>();

        for (IXMLElement child : toplevelTags)
        {
            if ("classpath".equals(child.getName()))
            {
                changeClassPath(ourclasspath, child);
            }
            else if ("job".equals(child.getName()))
            {
                CompilationJob subjob = collectJobsRecursive(child, ourclasspath);
                if (subjob != null)
                {
                    this.jobs.add(subjob);
                }
            }
            else if ("directory".equals(child.getName()))
            {
                String name = child.getAttribute("name");

                if (name != null)
                {
                    // substitute variables
                    String finalname = this.vs.substitute(name);

                    files.addAll(scanDirectory(new File(finalname)));
                }

            }
            else if ("file".equals(child.getName()))
            {
                String name = child.getAttribute("name");

                if (name != null)
                {
                    // substitute variables
                    String finalname = this.vs.substitute(name);

                    files.add(new File(finalname));
                }

            }
            else if ("packdependency".equals(child.getName()))
            {
                String name = child.getAttribute("name");

                if (name == null)
                {
                    System.out
                            .println("invalid compilation spec: <packdependency> without name attribute");
                    return null;
                }

                // check whether the wanted pack was selected for installation
                boolean found = false;

                for (Pack pack : this.idata.getSelectedPacks())
                {
                    if (pack.getName().equals(name))
                    {
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    LOGGER.fine("Skipping job because pack " + name + " was not selected.");
                    return null;
                }

            }

        }

        if (files.size() > 0)
        {
            return new CompilationJob(this.handler, this.idata, node.getAttribute("name"), files, ourclasspath);
        }

        return null;
    }

    /**
     * helper: process a <code>&lt;classpath&gt;</code> tag.
     */
    private void changeClassPath(List<String> classpath, IXMLElement child) throws Exception
    {
        String add = child.getAttribute("add");
        if (add != null)
        {
            add = this.vs.substitute(add, SubstitutionType.TYPE_PLAIN);
            if (!new File(add).exists())
            {
                if (!this.handler.emitWarning("Invalid classpath", "The path " + add
                        + " could not be found.\nCompilation may fail."))
                {
                    throw new Exception("Classpath " + add + " does not exist.");
                }
            }
            else
            {
                classpath.add(this.vs.substitute(add, SubstitutionType.TYPE_PLAIN));
            }

        }

        String sub = child.getAttribute("sub");
        if (sub != null)
        {
            int cpidx = -1;
            sub = this.vs.substitute(sub, SubstitutionType.TYPE_PLAIN);

            do
            {
                cpidx = classpath.indexOf(sub);
                classpath.remove(cpidx);
            }
            while (cpidx >= 0);

        }

    }

    /**
     * helper: recursively scan given directory.
     *
     * @return list of files found (might be empty)
     */
    private ArrayList<File> scanDirectory(File path)
    {
        LOGGER.fine("scanning directory " + path.getAbsolutePath());

        ArrayList<File> scanResult = new ArrayList<File>();

        if (!path.isDirectory())
        {
            return scanResult;
        }

        File[] entries = path.listFiles();

        for (File file : entries)
        {
            if (file == null)
            {
                continue;
            }

            if (file.isDirectory())
            {
                scanResult.addAll(scanDirectory(file));
            }
            else if ((file.isFile()) && (file.getName().toLowerCase().endsWith(".java")))
            {
                scanResult.add(file);
            }

        }

        return scanResult;
    }

    /**
     * a compilation job
     */
    private static class CompilationJob
    {

        private CompileHandler listener;

        private String name;

        private ArrayList<File> files;

        private List<String> classpath;

        private Messages messages;

        private InstallData idata;

        // XXX: figure that out (on runtime?)
        private static final int MAX_CMDLINE_SIZE = 4096;

        /**
         * Construct new compilation job.
         *
         * @param listener The listener to report progress to.
         * @param idata The installation data.
         * @param name The name of the job.
         * @param files The files to compile.
         * @param classpath The class path to use.
         */
        public CompilationJob(CompileHandler listener, InstallData idata, String name,
                ArrayList<File> files, List<String> classpath)
        {
            this.listener = listener;
            this.idata = idata;
            this.messages = idata.getMessages();
            this.name = name;
            this.files = files;
            this.classpath = classpath;
        }

        /**
         * Get the name of the job.
         *
         * @return The name or an empty string if there is no name.
         */
        public String getName()
        {
            if (this.name != null)
            {
                return this.name;
            }

            return "";
        }

        /**
         * Get the number of files in this job.
         *
         * @return The number of files to compile.
         */
        public int getSize()
        {
            return this.files.size();
        }

        /**
         * Perform this job - start compilation.
         *
         * @param compiler The compiler to use.
         * @param arguments The compiler arguments to use.
         * @return The result.
         */
        public CompileResult perform(String compiler, ArrayList<String> arguments)
        {
            LOGGER.fine("starting job " + this.name);
            // we have some maximum command line length - need to count
            int cmdlineLen = 0;

            // used to collect the arguments for executing the compiler
            LinkedList<String> args = new LinkedList<String>(arguments);

            {
                for (String arg : args)
                {
                    cmdlineLen += (arg).length() + 1;
                }
            }

            boolean isEclipseCompiler = compiler.equalsIgnoreCase(ECLIPSE_COMPILER_NAME);

            // add compiler in front of arguments
            args.add(0, compiler);
            cmdlineLen += compiler.length() + 1;

            // construct classpath argument for compiler
            // - collect all classpaths
            StringBuffer classpathSb = new StringBuffer();
            for (String cp : this.classpath)
            {
                if (classpathSb.length() > 0)
                {
                    classpathSb.append(File.pathSeparatorChar);
                }
                classpathSb.append(new File(cp).getAbsolutePath());
            }

            String classpathStr = classpathSb.toString();

            // - add classpath argument to command line
            if (classpathStr.length() > 0)
            {
                args.add("-classpath");
                cmdlineLen += 11;
                args.add(classpathStr);
                cmdlineLen += classpathStr.length() + 1;
            }

            // remember how many arguments we have which don't change for the
            // job
            int commonArgsNo = args.size();
            // remember how long the common command line is
            int commonArgsLen = cmdlineLen;

            // used for execution
            FileExecutor executor = new FileExecutor();
            String output[] = new String[2];

            // used for displaying the progress bar
            String jobfiles = "";
            int fileno = 0;
            int lastFileno = 0;

            // now iterate over all files of this job
            for (File file : this.files)
            {
                String fpath = file.getAbsolutePath();

                LOGGER.fine("processing " + fpath);

                // we add the file _first_ to the arguments to have a better
                // chance to get something done if the command line is almost
                // MAX_CMDLINE_SIZE or even above
                fileno++;
                jobfiles += file.getName() + " ";
                args.add(fpath);
                cmdlineLen += fpath.length();

                // start compilation if maximum command line length reached
                if (!isEclipseCompiler && cmdlineLen >= MAX_CMDLINE_SIZE)
                {
                    LOGGER.fine("compiling " + jobfiles);

                    // display useful progress bar (avoid showing 100% while
                    // still compiling a lot)
                    this.listener.progress(lastFileno, jobfiles);
                    lastFileno = fileno;

                    int retval = runCompiler(executor, output, args);

                    // update progress bar: compilation of fileno files done
                    this.listener.progress(fileno, jobfiles);

                    if (retval != 0)
                    {
                        CompileResult result = new CompileResult(messages.get("CompilePanel.error"), args,
                                output[0], output[1]);
                        this.listener.handleCompileError(result);
                        if (!result.isContinue())
                        {
                            return result;
                        }
                    }
                    else
                    {
                        // verify that all files have been compiled successfully
                        // I found that sometimes, no error code is returned
                        // although compilation failed.
                        Iterator<String> argIt = args.listIterator(commonArgsNo);
                        while (argIt.hasNext())
                        {
                            File javaFile = new File(argIt.next());

                            String basename = javaFile.getName();
                            int dotpos = basename.lastIndexOf('.');
                            basename = basename.substring(0, dotpos) + ".class";
                            File classFile = new File(javaFile.getParentFile(), basename);

                            if (!classFile.exists())
                            {
                                CompileResult result = new CompileResult(messages.get("CompilePanel.error.noclassfile")
                                        + javaFile.getAbsolutePath(), args,
                                        output[0],
                                        output[1]);
                                this.listener.handleCompileError(result);
                                if (!result.isContinue())
                                {
                                    return result;
                                }
                                // don't continue any further
                                break;
                            }

                        }

                    }

                    // clean command line: remove files we just compiled
                    for (int i = args.size() - 1; i >= commonArgsNo; i--)
                    {
                        args.removeLast();
                    }

                    cmdlineLen = commonArgsLen;
                    jobfiles = "";
                }

            }

            if (cmdlineLen > commonArgsLen)
            {
                this.listener.progress(lastFileno, jobfiles);

                int retval = runCompiler(executor, output, args);

                if (!isEclipseCompiler)
                {
                    this.listener.progress(fileno, jobfiles);
                }

                if (retval != 0)
                {
                    CompileResult result = new CompileResult(messages.get("CompilePanel.error"), args, output[0],
                            output[1]);
                    this.listener.handleCompileError(result);
                    if (!result.isContinue())
                    {
                        return result;
                    }
                }
                else
                {
                    // verify that all files have been compiled successfully
                    // I found that sometimes, no error code is returned
                    // although compilation failed.
                    Iterator<String> argIt = args.listIterator(commonArgsNo);
                    while (argIt.hasNext())
                    {
                        File javaFile = new File(argIt.next());

                        String basename = javaFile.getName();
                        int dotpos = basename.lastIndexOf('.');
                        basename = basename.substring(0, dotpos) + ".class";
                        File classFile = new File(javaFile.getParentFile(), basename);

                        if (!classFile.exists())
                        {
                            CompileResult result = new CompileResult(messages.get("CompilePanel.error.noclassfile")
                                    + javaFile.getAbsolutePath(), args,
                                    output[0],
                                    output[1]);
                            this.listener.handleCompileError(result);
                            if (!result.isContinue())
                            {
                                return result;
                            }
                            // don't continue any further
                            break;
                        }

                    }

                }

            }

            LOGGER.fine("Job " + this.name + " done (" + fileno + " files compiled)");

            return new CompileResult();
        }

        /**
         * Internal helper method.
         *
         * @param executor The executor, only used when using external compiler.
         * @param output The output from the compiler ([0] = stdout, [1] =
         * stderr)
         * @return The result of the compilation.
         */
        private int runCompiler(FileExecutor executor, String[] output, List<String> cmdline)
        {
            if (cmdline.get(0).equals(ECLIPSE_COMPILER_NAME))
            {
                return runEclipseCompiler(output, cmdline);
            }

            return executor.executeCommand(cmdline.toArray(new String[cmdline.size()]), output);
        }

        private int runEclipseCompiler(String[] output, List<String> cmdline)
        {
            try
            {
                List<String> finalCmdline = new LinkedList<String>(cmdline);

                // remove compiler name from argument list
                finalCmdline.remove(0);

                Class<?> eclipseCompiler = Class.forName(ECLIPSE_COMPILER_CLASS);

                Method compileMethod = eclipseCompiler.getMethod("main", new Class[]
                {
                    String[].class
                });

                finalCmdline.add(0, "-noExit");
                finalCmdline.add(0, "-progress");
                finalCmdline.add(0, "-verbose");

                File logfile0 = new File(this.idata.getInstallPath(), "compile-" + getName() + ".log");

                final boolean isTrace = LogManager.getLogManager().getLogger("").isLoggable(Level.FINE);

                if (isTrace)
                {
                    finalCmdline.add(0, logfile0.getPath());
                    finalCmdline.add(0, "-log");
                }

                // get log files / determine results...
                try
                {
                    // capture stdout and stderr
                    PrintStream orgStdout = System.out;
                    PrintStream orgStderr = System.err;
                    int errorCount = 0;

                    try
                    {
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        EclipseStdOutHandler ownStdout = new EclipseStdOutHandler(outStream, this.listener);
                        System.setOut(ownStdout);
                        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
                        EclipseStdErrHandler ownStderr = new EclipseStdErrHandler(errStream, this.listener);
                        System.setErr(ownStderr);

                        compileMethod.invoke(null,
                                new Object[]
                                {
                                    finalCmdline.toArray(new String[finalCmdline.size()])
                                });

                        // TODO: launch thread which updates the progress
                        output[0] = outStream.toString();
                        output[1] = errStream.toString();
                        errorCount = ownStderr.getErrorCount();
                        // for debugging: write output to log files
                        if (errorCount > 0 || isTrace)
                        {
                            File out = new File(logfile0.getPath() + ".stdout");
                            FileOutputStream fout = new FileOutputStream(out);
                            fout.write(outStream.toByteArray());
                            fout.close();
                            out = new File(logfile0.getPath() + ".stderr");
                            fout = new FileOutputStream(out);
                            fout.write(errStream.toByteArray());
                            fout.close();
                        }

                    }
                    finally
                    {
                        System.setOut(orgStdout);
                        System.setErr(orgStderr);
                    }

                    if (errorCount == 0)
                    {
                        return 0;
                    }

                    // TODO: construct human readable error message from log
                    this.listener.emitNotification("Compiler reported " + errorCount + " errors");

                    return 1;
                }
                catch (FileNotFoundException fnfe)
                {
                    this.listener.emitError("error compiling", fnfe.getMessage());
                    return -1;
                }
                catch (IOException ioe)
                {
                    this.listener.emitError("error compiling", ioe.getMessage());
                    return -1;
                }

            }
            catch (ClassNotFoundException cnfe)
            {
                output[0] = "error getting eclipse compiler";
                output[1] = cnfe.getMessage();
                return -1;
            }
            catch (NoSuchMethodException nsme)
            {
                output[0] = "error getting eclipse compiler method";
                output[1] = nsme.getMessage();
                return -1;
            }
            catch (IllegalAccessException iae)
            {
                output[0] = "error calling eclipse compiler";
                output[1] = iae.getMessage();
                return -1;
            }
            catch (InvocationTargetException ite)
            {
                output[0] = "error calling eclipse compiler";
                output[1] = ite.getMessage();
                return -1;
            }

        }

        /**
         * Check whether the given compiler works.
         * <p/>
         * This performs two steps:
         * <ol>
         * <li>check whether we can successfully call "compiler -help"</li>
         * <li>check whether we can successfully call "compiler -help arguments"
         * (not all compilers return an error here)</li>
         * </ol>
         * <p/>
         * On failure, the method CompileHandler#errorCompile is called with a
         * descriptive error message.
         *
         * @param compiler the compiler to use
         * @param arguments additional arguments to pass to the compiler
         * @return false on error
         */
        public CompileResult checkCompiler(String compiler, ArrayList<String> arguments)
        {
            // don't do further checks for eclipse compiler - it would exit
            if (compiler.equalsIgnoreCase(ECLIPSE_COMPILER_NAME))
            {
                return new CompileResult();
            }

            int retval = 0;
            FileExecutor executor = new FileExecutor();
            String[] output = new String[2];

            LOGGER.fine("checking whether \"" + compiler + " -help\" works");

            {
                List<String> args = new ArrayList<String>();
                args.add(compiler);
                args.add("-help");

                retval = runCompiler(executor, output, args);

                if (retval != 0)
                {
                    CompileResult result = new CompileResult(messages.get("CompilePanel.error.compilernotfound"),
                            args, output[0],
                            output[1]);
                    this.listener.handleCompileError(result);
                    if (!result.isContinue())
                    {
                        return result;
                    }
                }
            }

            LOGGER.fine("Checking whether \"" + compiler + " -help +arguments\" works");

            // used to collect the arguments for executing the compiler
            LinkedList<String> args = new LinkedList<String>(arguments);

            // add -help argument to prevent the compiler from doing anything
            args.add(0, "-help");

            // add compiler in front of arguments
            args.add(0, compiler);

            // construct classpath argument for compiler
            // - collect all classpaths
            StringBuffer classpathSb = new StringBuffer();
            for (String cp : this.classpath)
            {
                if (classpathSb.length() > 0)
                {
                    classpathSb.append(File.pathSeparatorChar);
                }
                classpathSb.append(new File(cp).getAbsolutePath());
            }

            String classpathStr = classpathSb.toString();

            // - add classpath argument to command line
            if (classpathStr.length() > 0)
            {
                args.add("-classpath");
                args.add(classpathStr);
            }

            retval = runCompiler(executor, output, args);

            if (retval != 0)
            {
                CompileResult result = new CompileResult(messages.get("CompilePanel.error.invalidarguments"),
                        args, output[0], output[1]);
                this.listener.handleCompileError(result);
                if (!result.isContinue())
                {
                    return result;
                }
            }

            return new CompileResult();
        }

    }

    /**
     * This PrintStream is used to track the Eclipse compiler output.
     * <p/>
     * It will pass on all println requests and report progress to the listener.
     */
    private static class EclipseStdOutHandler extends PrintStream
    {

        private CompileHandler listener;
        private StdOutParser parser;

        /**
         * Default constructor.
         *
         * @param anOutputStream The stream to wrap.
         * @param aHandler the handler to use.
         */
        public EclipseStdOutHandler(final OutputStream anOutputStream, final CompileHandler aHandler)
        {
            // initialize with dummy stream (PrintStream needs it)
            super(anOutputStream);
            this.listener = aHandler;
            this.parser = new StdOutParser();
        }

        /**
         * Eclipse compiler hopefully only uses println(String).
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void println(String x)
        {
            if (x.startsWith("[completed "))
            {
                int pos = x.lastIndexOf("#");
                int endpos = x.lastIndexOf("/");
                String filenoStr = x.substring(pos + 1, endpos - pos - 1);
                try
                {
                    int fileno = Integer.parseInt(filenoStr);
                    this.listener.progress(fileno, x);
                }
                catch (NumberFormatException e)
                {
                    LOGGER.log(Level.WARNING,
                            "Could not parse eclipse compiler output: '" + x + "': " + e.getMessage(),
                            e);
                }
            }

            super.println(x);
        }

        /**
         * Unfortunately, the Eclipse compiler wraps System.out into a
         * BufferedWriter.
         * <p/>
         * So we get whole buffers here and cannot do anything about it.
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] buf, int off, int len)
        {
            super.write(buf, off, len);
            // we cannot convert back to string because the buffer might start
            // _inside_ a multibyte character
            // so we build a simple parser.
            int fileno = this.parser.parse(buf, off, len);
            if (fileno > -1)
            {
                this.listener.setSubStepNo(this.parser.getJobSize());
                this.listener.progress(fileno, this.parser.getLastFilename());
            }
        }

    }

    /**
     * This PrintStream is used to track the Eclipse compiler error output.
     * <p/>
     * It will pass on all println requests and report progress to the listener.
     */
    private static class EclipseStdErrHandler extends PrintStream
    {

        // private CompileHandler listener;   // Unused
        private int errorCount = 0;
        private StdErrParser parser;

        /**
         * Default constructor.
         *
         * @param anOutputStream The stream to wrap.
         * @param aHandler the handler to use.
         */
        public EclipseStdErrHandler(final OutputStream anOutputStream, final CompileHandler aHandler)
        {
            // initialize with dummy stream (PrintStream needs it)
            super(anOutputStream);
            // this.listener = aHandler; // TODO : reactivate this when we want to do something with it
            this.parser = new StdErrParser();
        }

        /**
         * Eclipse compiler hopefully only uses println(String).
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void println(String x)
        {
            if (x.indexOf(". ERROR in ") > 0)
            {
                this.errorCount++;
            }

            super.println(x);
        }

        /**
         * Unfortunately, the Eclipse compiler wraps System.out into a
         * BufferedWriter.
         * <p/>
         * So we get whole buffers here and cannot do anything about it.
         * <p/>
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] buf, int off, int len)
        {
            super.write(buf, off, len);
            // we cannot convert back to string because the buffer might start
            // _inside_ a multibyte character
            // so we build a simple parser.
            int errno = this.parser.parse(buf, off, len);
            if (errno > 0)
            {
                // TODO: emit error message instantly, but it may be incomplete yet
                // and we'd need to throw an exception to abort compilation
                this.errorCount += errno;
            }
        }

        /**
         * Get the error state.
         *
         * @return true if there was an error detected.
         */
        public int getErrorCount()
        {
            return this.errorCount;
        }
    }

    /**
     * Common class for parsing Eclipse compiler output.
     */
    private static abstract class StreamParser
    {

        int idx;
        byte[] buffer;
        int offset;
        int length;
        byte[] lastIdentifier;
        int lastDigit;

        abstract int parse(byte[] buf, int off, int len);

        void init(byte[] buf, int off, int len)
        {
            this.buffer = buf;
            this.offset = off;
            this.length = len;
            this.idx = 0;
            this.lastIdentifier = null;
            this.lastDigit = -1;
        }

        int getNext()
        {
            if (this.offset + this.idx == this.length)
            {
                return Integer.MIN_VALUE;
            }

            return this.buffer[this.offset + this.idx++];
        }

        boolean findString(final String aString)
        {
            byte[] searchBytes = aString.getBytes();
            int searchIdx = 0;

            do
            {
                int c = getNext();
                if (c == Integer.MIN_VALUE)
                {
                    return false;
                }

                if (c == searchBytes[searchIdx])
                {
                    searchIdx++;
                }
                else
                {
                    searchIdx = 0;
                    if (c == searchBytes[searchIdx])
                    {
                        searchIdx++;
                    }
                }
            }
            while (searchIdx < searchBytes.length);

            return true;
        }

        boolean readIdentifier()
        {
            int c;
            int startIdx = this.idx;

            do
            {
                c = getNext();
                // abort on incomplete string
                if (c == Integer.MIN_VALUE)
                {
                    return false;
                }
            }
            while (!Character.isWhitespace((char) c));

            this.idx--;
            this.lastIdentifier = new byte[this.idx - startIdx];
            System.arraycopy(this.buffer, startIdx, this.lastIdentifier, 0, this.idx - startIdx);

            return true;
        }

        boolean readNumber()
        {
            int c;
            int startIdx = this.idx;

            do
            {
                c = getNext();
                // abort on incomplete string
                if (c == Integer.MIN_VALUE)
                {
                    return false;
                }
            }
            while (Character.isDigit((char) c));

            this.idx--;
            String digitStr = new String(this.buffer, startIdx, this.idx - startIdx);
            try
            {
                this.lastDigit = Integer.parseInt(digitStr);
            }
            catch (NumberFormatException ex)
            {
                // should not happen - ignore
            }

            return true;
        }

        boolean skipSpaces()
        {
            int c;

            do
            {
                c = getNext();
                if (c == Integer.MIN_VALUE)
                {
                    return false;
                }
            }
            while (Character.isWhitespace((char) c));

            this.idx--;

            return true;
        }

    }

    private static class StdOutParser extends StreamParser
    {

        int fileno;
        int jobSize;
        String lastFilename;

        @Override
        int parse(byte[] buf, int off, int len)
        {
            super.init(buf, off, len);
            this.fileno = -1;
            this.jobSize = -1;
            this.lastFilename = null;

            // a line looks like this:
            // [completed  /path/to/file.java - #1/2025]
            do
            {
                if (findString("[completed ")
                        && skipSpaces()
                        && readIdentifier())
                {
                    // remember file name
                    String filename = new String(this.lastIdentifier);

                    if (!skipSpaces())
                    {
                        continue;
                    }

                    int c = getNext();
                    if (c == Integer.MIN_VALUE)
                    {
                        return this.fileno;
                    }
                    if (c != '-')
                    {
                        continue;
                    }

                    if (!skipSpaces())
                    {
                        continue;
                    }

                    c = getNext();
                    if (c == Integer.MIN_VALUE)
                    {
                        return this.fileno;
                    }
                    if (c != '#')
                    {
                        continue;
                    }

                    if (!readNumber())
                    {
                        return this.fileno;
                    }

                    int fileno = this.lastDigit;

                    c = getNext();
                    if (c == Integer.MIN_VALUE)
                    {
                        return this.fileno;
                    }
                    if (c != '/')
                    {
                        continue;
                    }

                    if (!readNumber())
                    {
                        return this.fileno;
                    }

                    c = getNext();
                    if (c == Integer.MIN_VALUE)
                    {
                        return this.fileno;
                    }
                    if (c != ']')
                    {
                        continue;
                    }

                    this.lastFilename = filename;
                    this.fileno = fileno;
                    this.jobSize = this.lastDigit;
                    // continue parsing (figure out last occurence)
                }
                else
                {
                    return this.fileno;
                }

            }
            while (true);
        }

        String getLastFilename()
        {
            return this.lastFilename;
        }

        int getJobSize()
        {
            return this.jobSize;
        }
    }

    private static class StdErrParser extends StreamParser
    {

        int errorCount;

        @Override
        int parse(byte[] buf, int off, int len)
        {
            super.init(buf, off, len);
            this.errorCount = 0;

            // a line looks like this:
            // [completed  /path/to/file.java - #1/2025]
            do
            {
                if (findString(". ERROR in "))
                {
                    this.errorCount++;
                }
                else
                {
                    return this.errorCount;
                }
            }
            while (true);
        }
    }
}
