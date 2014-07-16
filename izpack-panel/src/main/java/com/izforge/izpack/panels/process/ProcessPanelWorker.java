package com.izforge.izpack.panels.process;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.IXMLParser;
import com.izforge.izpack.api.adaptator.impl.XMLParser;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.PlatformModelMatcher;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class does alle the work for the process panel.
 * <p/>
 * It responsible for
 * <ul>
 * <li>parsing the process spec XML file
 * <li>performing the actions described therein
 * </ul>
 *
 * @author Tino Schwarze
 */
public class ProcessPanelWorker implements Runnable
{

    /**
     * Name of resource for specifying processing parameters.
     */
    private static final String SPEC_RESOURCE_NAME = "ProcessPanel.Spec.xml";

    private AbstractUIProcessHandler handler;

    /**
     * List of jobs to run in the process panel (try).
     */
    private ArrayList<ProcessPanelWorker.ProcessingJob> jobs = new ArrayList<ProcessPanelWorker.ProcessingJob>();

    /**
     * List of process panel jobs that run only in the event of a process panel failure (catch).
     */
    private ArrayList<ProcessPanelWorker.ProcessingJob> catchJobs = new ArrayList<ProcessPanelWorker.ProcessingJob>();

    /**
     * List of process panel jobs that run regardless of previous job failing (finally).
     */
    private ArrayList<ProcessPanelWorker.ProcessingJob> finalJobs = new ArrayList<ProcessPanelWorker.ProcessingJob>();

    private boolean result = true;

    private PrintWriter logfile = null;

    private String logfiledir = null;

    private final InstallData idata;

    private final Map<Boolean, List<ButtonConfig>> buttonConfigs = new HashMap<Boolean, List<ButtonConfig>>();
    private final RulesEngine rules;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ProcessPanelWorker.class.getName());


    /**
     * Constructs a <tt>ProcessPanelWorker</tt>.
     *
     * @param installData the installation data
     * @param rules       the rules engine
     * @param resources   the resources
     * @param matcher     the platform-model matcher
     */
    public ProcessPanelWorker(InstallData installData, RulesEngine rules, Resources resources,
                              PlatformModelMatcher matcher)
    {
        this.idata = installData;
        this.rules = rules;
        this.resources = resources;
        this.matcher = matcher;
    }

    public void setHandler(AbstractUIProcessHandler handler)
    {
        this.handler = handler;
    }

    private boolean readSpec() throws IOException
    {
        InputStream input;
        try
        {
            input = resources.getInputStream(SPEC_RESOURCE_NAME);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Failed to read " + SPEC_RESOURCE_NAME, e);
            return false;
        }

        IXMLParser parser = new XMLParser();
        IXMLElement spec;
        try
        {
            spec = parser.parse(input);
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Failed to parse " + SPEC_RESOURCE_NAME, e);
            return false;
        }

        if (!spec.hasChildren())
        {
            return false;
        }

        // Handle logfile
        IXMLElement logFileDirElement = spec.getFirstChildNamed("logfiledir");
        if (logFileDirElement != null)
        {
            logfiledir = logFileDirElement.getContent();
        }

        for (IXMLElement job_el : spec.getChildrenNamed("job"))
        {
            // normally use condition attribute, but also read conditionid to not break older versions.
            String conditionid = job_el.hasAttribute("condition") ? job_el.getAttribute(
                    "condition") : job_el.hasAttribute("conditionid") ? job_el.getAttribute("conditionid") : null;
            if ((conditionid != null) && (conditionid.length() > 0))
            {
                logger.fine("Checking condition for job: " + conditionid);
                Condition cond = rules.getCondition(conditionid);
                if ((cond != null) && !cond.isTrue())
                {
                    logger.fine("condition " + conditionid + " is not fulfilled.");
                    // skip, if there is a condition and this condition isn't true
                    continue;
                }
            }
            logger.fine("Condition " + conditionid + " is fulfilled or does not exist");
            // ExecuteForPack Patch
            // Check if processing required for pack
            List<IXMLElement> forPacks = job_el.getChildrenNamed("executeForPack");
            if (!jobRequiredFor(forPacks))
            {
                continue;
            }

            // first check OS constraints - skip jobs not suited for this OS
            List<OsModel> constraints = OsConstraintHelper.getOsList(job_el);

            if (matcher.matchesCurrentPlatform(constraints))
            {
                List<ProcessPanelWorker.Processable> ef_list = new ArrayList<ProcessPanelWorker.Processable>();

                String job_name = job_el.getAttribute("name", "");

                for (IXMLElement executeFileElement : job_el.getChildrenNamed("executefile"))
                {
                    String ef_name = executeFileElement.getAttribute("name");

                    if ((ef_name == null) || (ef_name.length() == 0))
                    {
                        System.err.println("missing \"name\" attribute for <executefile>");
                        return false;
                    }
                    String ef_working_dir = executeFileElement.getAttribute("workingDir");

                    List<String> args = new ArrayList<String>();

                    for (IXMLElement arg_el : executeFileElement.getChildrenNamed("arg"))
                    {
                        String arg_val = arg_el.getContent();

                        args.add(arg_val);
                    }

                    List<String> envvars = new ArrayList<String>();

                    for (IXMLElement env_el : executeFileElement.getChildrenNamed("env"))
                    {
                        String env_val = env_el.getContent();

                        envvars.add(env_val);
                    }

                    ef_list.add(new ProcessPanelWorker.ExecutableFile(ef_name, args, envvars, ef_working_dir));
                }

                for (IXMLElement executeClassElement : job_el.getChildrenNamed("executeclass"))
                {
                    String ef_name = executeClassElement.getAttribute("name");
                    if ((ef_name == null) || (ef_name.length() == 0))
                    {
                        System.err.println("missing \"name\" attribute for <executeclass>");
                        return false;
                    }

                    List<String> args = new ArrayList<String>();
                    for (IXMLElement arg_el : executeClassElement.getChildrenNamed("arg"))
                    {
                        String arg_val = arg_el.getContent();
                        args.add(arg_val);
                    }

                    ef_list.add(new ProcessPanelWorker.ExecutableClass(ef_name, args));
                }

                Boolean isCatch = job_el.hasAttribute("catch") && Boolean.parseBoolean(job_el.getAttribute("catch"));
                Boolean isFinal = job_el.hasAttribute("final") && Boolean.parseBoolean(job_el.getAttribute("final"));

                if (ef_list.isEmpty())
                {
                    logger.fine("Nothing to do for job '" + job_name + "'");
                }
                else
                {
                    if (isCatch) {
                        this.catchJobs.add(new ProcessingJob(job_name, ef_list));
                    } else if (isFinal) {
                        this.finalJobs.add(new ProcessingJob(job_name, ef_list));
                    } else {
                        this.jobs.add(new ProcessingJob(job_name, ef_list));
                    }
                }
            }
        }

        buttonConfigs.put(Boolean.FALSE, new ArrayList<ButtonConfig>());
        buttonConfigs.put(Boolean.TRUE, new ArrayList<ButtonConfig>());

        for (IXMLElement onFailElement : spec.getChildrenNamed("onFail"))
        {
            String conditionid = onFailElement.hasAttribute("condition") ? onFailElement.getAttribute(
                    "condition") : onFailElement.hasAttribute("conditionid") ? onFailElement.getAttribute(
                    "conditionid") : null;
            boolean unlockPrev = onFailElement.hasAttribute("previous") ? Boolean.parseBoolean(
                    onFailElement.getAttribute("previous")) : false;
            boolean unlockNext = onFailElement.hasAttribute("next") ? Boolean.parseBoolean(
                    onFailElement.getAttribute("next")) : false;
            buttonConfigs.get(Boolean.FALSE).add(new ButtonConfig(conditionid, unlockPrev, unlockNext));
        }
        for (IXMLElement onSuccessElement : spec.getChildrenNamed("onSuccess"))
        {
            String conditionid = onSuccessElement.hasAttribute("condition") ? onSuccessElement.getAttribute(
                    "condition") : onSuccessElement.hasAttribute("conditionid") ? onSuccessElement.getAttribute(
                    "conditionid") : null;
            boolean unlockPrev = onSuccessElement.hasAttribute("previous") ? Boolean.parseBoolean(
                    onSuccessElement.getAttribute("previous")) : false;
            buttonConfigs.get(Boolean.TRUE).add(new ButtonConfig(conditionid, unlockPrev, true));
        }

        return true;
    }

    /**
     * This is called when the processing thread is activated.
     * <p/>
     * Can also be called directly if asynchronous processing is not desired.
     */
    @Override
    public void run()
    {
        // ExecuteForPack patch
        // Read spec only here... not before, cause packs are otherwise
        // all selected or de-selected
        try
        {
            jobs.clear();
            if (!readSpec())
            {
                System.err.println("Error parsing XML specification for processing.");
                return;
            }
        }
        catch (IOException ioe)
        {
            System.err.println(ioe.toString());
            return;
        }

        // Create logfile if needed. Do it at this point because
        // variable substitution needs selected install path.
        if (logfiledir != null)
        {
            logfiledir = IoHelper.translatePath(logfiledir, idata.getVariables());

            String appVersion = idata.getVariable("APP_VER");

            if (appVersion != null)
            {
                appVersion = "V" + appVersion;
            }
            else
            {
                appVersion = "undef";
            }

            String identifier = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());

            identifier = appVersion.replace(' ', '_') + "_" + identifier;

            try
            {
                File tempLogFile = File.createTempFile("Install_" + identifier + "_", ".log",
                                                       new File(logfiledir));
                logfile = new PrintWriter(new FileOutputStream(tempLogFile), true);
            }
            catch (IOException e)
            {
                logger.log(Level.WARNING, e.getMessage(), e);
                // TODO throw or throw not, that's the question...
            }
        }

        this.handler.startProcessing(this.jobs.size());

        /**
         * Process panel jobs.
         */
        for (ProcessPanelWorker.ProcessingJob processingJob : this.jobs)
        {
            this.result = runJob(processingJob);

            if (!this.result)
            {
                /**
                 * Jobs run in event of failure.
                 */
                for (ProcessPanelWorker.ProcessingJob catchJob : this.catchJobs)
                {
                    runJob(catchJob);
                }
                break;
            }
        }

        /**
         * Jobs run every time despite of failure or success.
         */
        for (ProcessPanelWorker.ProcessingJob finalJob : this.finalJobs)
        {
            runJob(finalJob);
        }

        boolean unlockNext = true;
        boolean unlockPrev = false;

        // get the ButtonConfigs matching the this.result
        for (ButtonConfig buttonConfig : buttonConfigs.get(this.result))
        {
            String conditionid = buttonConfig.getConditionid();
            if ((conditionid != null) && (conditionid.length() > 0))
            {
                logger.fine("Condition for job: " + conditionid);
                Condition cond = rules.getCondition(conditionid);
                if ((cond != null) && !cond.isTrue())
                {
                    logger.fine("Condition " + conditionid + " is not fulfilled");
                    // skip, if there is a condition and this condition isn't true
                    continue;
                }
            }

            unlockNext = buttonConfig.isUnlockNext();
            unlockPrev = buttonConfig.isUnlockPrev();
            break;
        }

        this.handler.finishProcessing(unlockPrev, unlockNext);
        if (logfile != null)
        {
            logfile.close();
        }
    }

    /**
     * Runs the specified process panel job.
     * @param job a ProcessPanelWorker job.
     * @return the job's return value.
     */
    private boolean runJob(ProcessPanelWorker.ProcessingJob job)
    {
        Boolean val;

        this.handler.startProcess(job.name);

        val = job.run(this.handler, idata.getVariables());

        this.handler.finishProcess();

        return val;
    }

    /**
     * Start the compilation in a separate thread.
     */
    public void startThread()
    {
        Thread processingThread = new Thread(this, "processing thread");
        // will call this.run()
        processingThread.start();
    }

    /**
     * Return the result of the process execution.
     *
     * @return true if all processes succeeded, false otherwise.
     */
    public boolean getResult()
    {
        return this.result;
    }

    interface Processable
    {

        /**
         * @param handler   The UI handler for user interaction and to send output to.
         * @param variables the variables
         * @return true on success, false if processing should stop
         */
        public boolean run(AbstractUIProcessHandler handler, Variables variables);
    }

    private static class ProcessingJob implements ProcessPanelWorker.Processable
    {

        public String name;

        private List<ProcessPanelWorker.Processable> processables;

        public ProcessingJob(String name, List<ProcessPanelWorker.Processable> processables)
        {
            this.name = name;
            this.processables = processables;
        }

        @Override
        public boolean run(AbstractUIProcessHandler handler, Variables variables)
        {
            for (ProcessPanelWorker.Processable processable : this.processables)
            {
                if (!processable.run(handler, variables))
                {
                    return false;
                }
            }

            return true;
        }

    }

    private class ExecutableFile implements ProcessPanelWorker.Processable
    {

        private String filename;
        private String workingDir;

        private List<String> arguments;

        private List<String> envvariables;

        protected AbstractUIProcessHandler handler;

        public ExecutableFile(String fn, List<String> args, List<String> envvars, String workingDir)
        {
            this.filename = fn;
            this.arguments = args;
            this.envvariables = envvars;
            this.workingDir = workingDir;
        }

        @Override
        public boolean run(AbstractUIProcessHandler handler, Variables variables)
        {
            this.handler = handler;

            List<String> params = new ArrayList<String>(this.arguments.size() + 1);

            try
            {
                params.add(variables.replace(this.filename));
            }
            catch (Exception e)
            {
                params.add(this.filename);
            }

            for (String argument : this.arguments)
            {
                try
                {
                    params.add(variables.replace(argument));
                }
                catch (Exception e)
                {
                    params.add(argument);
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder(params);
            if (workingDir != null && !workingDir.equals(""))
            {
                workingDir = IoHelper.translatePath(workingDir, variables);
                processBuilder.directory(new File(workingDir));
            }
            Map<String, String> environment = processBuilder.environment();
            for (String envvar : envvariables)
            {
                String ev = variables.replace(envvar);
                int i = ev.indexOf("=");
                if (i > 0)
                {
                    environment.put(ev.substring(0, i), ev.substring(i + 1));
                }
            }

            try
            {

                Process process = processBuilder.start();

                ProcessPanelWorker.ExecutableFile.OutputMonitor stdoutMon = new ProcessPanelWorker.ExecutableFile.OutputMonitor(
                        this.handler, process.getInputStream(), false);
                ProcessPanelWorker.ExecutableFile.OutputMonitor stderrMon = new ProcessPanelWorker.ExecutableFile.OutputMonitor(
                        this.handler, process.getErrorStream(), true);
                Thread stdoutThread = new Thread(stdoutMon);
                Thread stderrThread = new Thread(stderrMon);
                stdoutThread.setDaemon(true);
                stderrThread.setDaemon(true);
                stdoutThread.start();
                stderrThread.start();

                try
                {
                    int exitStatus = process.waitFor();

                    stopMonitor(stdoutMon, stdoutThread);
                    stopMonitor(stderrMon, stderrThread);

                    if (exitStatus != 0)
                    {
                        QuestionErrorDisplayer myErrorAlter = new QuestionErrorDisplayer(handler);
                        SwingUtilities.invokeAndWait(myErrorAlter);
                        return myErrorAlter.shouldContinue();
                    }
                }
                catch (InvocationTargetException ex)
                {
                    process.destroy();
                    this.handler.emitError("process interrupted", ex.toString());
                    return false;
                }
                catch (InterruptedException ie)
                {
                    process.destroy();
                    this.handler.emitError("process interrupted", ie.toString());
                    return false;
                }
            }
            catch (IOException ioe)
            {
                this.handler.emitError("I/O error", ioe.toString());
                return false;
            }

            return true;
        }

        private void stopMonitor(ProcessPanelWorker.ExecutableFile.OutputMonitor monitor, Thread thread)
        {
            // taken from com.izforge.izpack.util.FileExecutor
            monitor.doStop();
            long softTimeout = 500;
            try
            {
                thread.join(softTimeout);
            }
            catch (InterruptedException e)
            {
            }

            if (!thread.isAlive())
            {
                return;
            }

            thread.interrupt();
            long hardTimeout = 500;
            try
            {
                thread.join(hardTimeout);
            }
            catch (InterruptedException e)
            {
            }
        }

        public class OutputMonitor implements Runnable
        {

            private boolean stderr = false;

            private AbstractUIProcessHandler handler;

            private BufferedReader reader;

            private Boolean stop = false;

            public OutputMonitor(AbstractUIProcessHandler handler, InputStream is, boolean stderr)
            {
                this.stderr = stderr;
                this.reader = new BufferedReader(new InputStreamReader(is));
                this.handler = handler;
            }

            @Override
            public void run()
            {
                try
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        this.handler.logOutput(line, stderr);

                        // log output also to file given in ProcessPanelSpec

                        if (logfile != null)
                        {
                            logfile.println(line);
                        }

                        synchronized (this.stop)
                        {
                            if (stop)
                            {
                                return;
                            }
                        }
                    }
                }
                catch (IOException ioe)
                {
                    this.handler.logOutput(ioe.toString(), true);

                    // log errors also to file given in ProcessPanelSpec

                    if (logfile != null)
                    {
                        logfile.println(ioe.toString());
                    }

                }

            }

            public void doStop()
            {
                synchronized (this.stop)
                {
                    this.stop = true;
                }
            }

        }

    }

    /**
     * Tries to create a class that has an empty contstructor and a method
     * run(AbstractUIProcessHandler, String[]) If found, it calls the method and processes all
     * returned exceptions
     */
    private static class ExecutableClass implements ProcessPanelWorker.Processable
    {

        final private String myClassName;

        final private List<String> myArguments;

        protected AbstractUIProcessHandler myHandler;

        public ExecutableClass(String className, List<String> args)
        {
            myClassName = className;
            myArguments = args;
        }

        @Override
        public boolean run(AbstractUIProcessHandler aHandler, Variables variables)
        {
            boolean result = false;
            myHandler = aHandler;

            String params[] = new String[myArguments.size()];

            int i = 0;
            for (String myArgument : myArguments)
            {
                params[i] = variables.replace(myArgument);
                i++;
            }

            try
            {
                ClassLoader loader = this.getClass().getClassLoader();
                Class<?> procClass = loader.loadClass(myClassName);

                Object instance = procClass.newInstance();
                Method method = procClass.getMethod("run", new Class[]{AbstractUIProcessHandler.class,
                        String[].class});

                if (method.getReturnType().getName().equals("boolean"))
                {
                    result = (Boolean) method.invoke(instance, new Object[]{myHandler, params});
                }
                else
                {
                    method.invoke(instance, new Object[]{myHandler, params});
                    result = true;
                }
            }
            catch (SecurityException e)
            {
                myHandler.emitError("Post Processing Error",
                                    "Security exception thrown when processing class: " + myClassName);
            }
            catch (ClassNotFoundException e)
            {
                myHandler.emitError("Post Processing Error", "Cannot find processing class: "
                        + myClassName);
            }
            catch (NoSuchMethodException e)
            {
                myHandler.emitError("Post Processing Error",
                                    "Processing class does not have 'run' method: " + myClassName);
            }
            catch (IllegalAccessException e)
            {
                myHandler.emitError("Post Processing Error", "Error accessing processing class: "
                        + myClassName);
            }
            catch (InvocationTargetException e)
            {
                myHandler.emitError("Post Processing Error", "Invocation Problem calling : "
                        + myClassName + ", " + e.getCause().getMessage());
            }
            catch (Exception e)
            {
                myHandler.emitError("Post Processing Error",
                                    "Exception when running processing class: " + myClassName + ", "
                                            + e.getMessage());
            }
            catch (Error e)
            {
                myHandler.emitError("Post Processing Error",
                                    "Error when running processing class: " + myClassName + ", "
                                            + e.getMessage());
            }
            catch (Throwable e)
            {
                myHandler.emitError("Post Processing Error",
                                    "Error when running processing class: " + myClassName + ", "
                                            + e.getMessage());
            }
            return result;
        }
    }

    /*------------------------ ExecuteForPack PATCH -------------------------*/
    /*
     * Verifies if the job is required for any of the packs listed. The job is required for a pack
     * in the list if that pack is actually selected for installation. <br><br> <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returned. The same
     * is true if the <code>packs</code> list is empty.
     *
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes
     * a pack for which the schortcut should be created if the pack is actually installed.
     *
     * @return <code>true</code> if the shortcut is required for at least on pack in the list,
     * otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * @design
     *
     * The information about the installed packs comes from GUIInstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     *
     * /*--------------------------------------------------------------------------
     */

    private boolean jobRequiredFor(List<IXMLElement> packs)
    {
        String selected;
        String required;

        if (packs.size() == 0)
        {
            return (true);
        }

        // System.out.println ("Number of selected packs is "
        // +installData.selectedPacks.size () );

        for (int i = 0; i < idata.getSelectedPacks().size(); i++)
        {
            selected = idata.getSelectedPacks().get(i).getName();

            // System.out.println ("Selected pack is " + selected);

            for (IXMLElement pack : packs)
            {
                required = pack.getAttribute("name", "");
                // System.out.println ("Attribute name is " + required);
                if (selected.equals(required))
                {
                    // System.out.println ("Return true");
                    return (true);
                }
            }
        }
        return (false);
    }

    private static class QuestionErrorDisplayer implements Runnable
    {
        private AbstractUIProcessHandler uiHandler;
        private boolean toBeContinued = true;

        QuestionErrorDisplayer(AbstractUIProcessHandler uiHandler)
        {
            this.uiHandler = uiHandler;
        }

        @Override
        public void run()
        {
            if (uiHandler.askQuestion("Process execution failed",
                                      "Continue anyway?", AbstractUIHandler.CHOICES_YES_NO,
                                      AbstractUIHandler.ANSWER_YES) == AbstractUIHandler.ANSWER_NO)
            {
                mustContinue(false);
            }
        }

        public synchronized boolean shouldContinue()
        {
            return toBeContinued;
        }

        public synchronized void mustContinue(boolean toBeContinued)
        {
            this.toBeContinued = toBeContinued;
        }
    }
}
