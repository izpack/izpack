/*
 /*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.util.PlatformModelMatcher;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Functions to support automated usage of the CompilePanel
 *
 * @author Jonathan Halliday
 * @author Tino Schwarze
 */
public class CompilePanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation, CompileHandler
{

    private CompileWorker worker = null;

    private int jobMax = 0;

    private String jobName = null;

    private int lastLineLen = 0;

    // when using the eclipse compiler, we're capturing System.out and System.err...
    private PrintStream stdout;
    private PrintStream stderr;
    private VariableSubstitutor variableSubstitutor;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * Constructs a <tt>CompilePanelAutomationHelper</tt>.
     *
     * @param variableSubstitutor the variable substituter
     * @param resources the resources
     * @param matcher the platform-model matcher
     */
    public CompilePanelAutomationHelper(VariableSubstitutor variableSubstitutor, Resources resources,
            PlatformModelMatcher matcher)
    {
        this.variableSubstitutor = variableSubstitutor;
        this.resources = resources;
        this.matcher = matcher;
    }

    /**
     * Save installDataGUI for running automated.
     *
     * @param installData installation parameters
     * @param panelRoot unused.
     */
    public void makeXMLData(InstallData installData, IXMLElement panelRoot)
    {
        // not used here - during automatic installation, no automatic
        // installation information is generated
    }

    public void runAutomated(InstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement compilerXml = panelRoot.getFirstChildNamed("compiler");

        String compiler = null;

        if (compilerXml != null)
        {
            compiler = compilerXml.getContent();
        }

        if (compiler == null)
        {
            throw new InstallerException("invalid automation installDataGUI: could not find compiler");
        }

        IXMLElement argsXml = panelRoot.getFirstChildNamed("arguments");

        String args = null;

        if (argsXml != null)
        {
            args = argsXml.getContent();
        }

        if (argsXml == null)
        {
            throw new InstallerException("invalid automation installDataGUI: could not find compiler arguments");
        }

        try
        {

            this.worker = new CompileWorker(idata, this, variableSubstitutor, resources, matcher);
            this.worker.setCompiler(compiler);
            this.worker.setCompilerArguments(args);

            this.stdout = System.out;
            this.stderr = System.err;

            this.worker.run();

            if (this.worker.getResult().isSuccess())
            {
                throw new InstallerException("Compilation failed (xml line " + panelRoot.getLineNr() + ")");
            }
        }
        catch (IOException e)
        {
            throw new InstallerException(e);
        }
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
    }

    /**
     * Reports progress on System.out
     */
    public void startAction(String name, int noOfJobs)
    {
        this.stdout.println("[ Starting compilation ]");
        this.jobName = "";
    }

    /**
     * Reports the error to System.err
     *
     * @param error the error
     * @see CompileHandler#handleCompileError(CompileResult)
     */
    public void handleCompileError(CompileResult error)
    {
        this.stdout.println();
        this.stdout.println("[ Compilation failed ]");
        this.stderr.println("Command line: " + error.getCmdline());
        this.stderr.println();
        this.stderr.println("stdout of compiler:");
        this.stderr.println(error.getStdout());
        this.stderr.println("stderr of compiler:");
        this.stderr.println(error.getStderr());
        // abort instantly and make installation fail
        error.setAction(CompileResult.ACTION_ABORT);
    }

    /**
     * Sets state variable for thread sync.
     */
    public void stopAction()
    {
        if ((this.jobName != null) && (this.lastLineLen > 0))
        {
            String line = this.jobName + ": done.";
            this.stdout.print("\r" + line);
            for (int i = line.length(); i < this.lastLineLen; i++)
            {
                this.stdout.print(' ');
            }
            this.stdout.println();
        }

        if (this.worker.getResult().isSuccess())
        {
            this.stdout.println("[ Compilation successful ]");
        }
    }

    /**
     * Tell about progress.
     *
     * @param val
     * @param msg
     */
    public void progress(int val, String msg)
    {
        double percentage = ((double) val) * 100.0d / (double) this.jobMax;

        String percent = (new Integer((int) percentage)).toString() + '%';
        String line = this.jobName + ": " + percent;

        int lineLen = line.length();

        this.stdout.print("\r" + line);
        for (int i = lineLen; i < this.lastLineLen; i++)
        {
            this.stdout.print(' ');
        }

        this.lastLineLen = lineLen;
    }

    /**
     * Reports progress to System.out
     *
     * @param jobName The next job's name.
     * @param max unused
     * @param jobNo The next job's number.
     */
    public void nextStep(String jobName, int max, int jobNo)
    {
        if ((this.jobName != null) && (this.lastLineLen > 0))
        {
            String line = this.jobName + ": done.";
            this.stdout.print("\r" + line);
            for (int i = line.length(); i < this.lastLineLen; i++)
            {
                this.stdout.print(' ');
            }
            this.stdout.println();
        }

        this.jobMax = max;
        this.jobName = jobName;
        this.lastLineLen = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setSubStepNo(int noOfSubsteps)
    {
        this.jobMax = noOfSubsteps;
    }

    /**
     * Invoked to notify progress.
     * <p/>
     * This increments the current step.
     *
     * @param message a message describing the step
     */
    @Override
    public void progress(String message)
    {
        // no-op
    }

    /**
     * Invoked when an action restarts.
     *
     * @param name the name of the action
     * @param overallMessage a message describing the overall progress
     * @param tip a tip describing the current progress
     * @param steps the number of steps the action consists of
     */
    @Override
    public void restartAction(String name, String overallMessage, String tip, int steps)
    {
        // no-op
    }

    @Override
    public void createInstallationRecord(InstallData installData, IXMLElement rootElement)
    {
    }
}
