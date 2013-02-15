package com.izforge.izpack.panels.process;

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Type;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.installer.console.AbstractPanelConsole;
import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;


public class ProcessPanelConsole extends AbstractPanelConsole implements PanelConsole, AbstractUIProcessHandler
{
    private RulesEngine rules;
    private Resources resources;

    private Prompt prompt;

    private PromptUIHandler handler;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    private int noOfJobs = 0;

    private int currentJob = 0;

    public ProcessPanelConsole(RulesEngine rules, Resources resources, Prompt prompt, PlatformModelMatcher matcher)
    {
        this.rules = rules;
        this.resources = resources;
        this.prompt = prompt;
        handler = new PromptUIHandler(prompt);
        this.matcher = matcher;
    }

    public void emitNotification(String message)
    {
        handler.emitNotification(message);
    }

    public boolean emitWarning(String title, String message)
    {
        return handler.emitWarning(title, message);
    }

    public void emitError(String title, String message)
    {
        handler.emitError(title, message);
    }

    public void emitErrorAndBlockNext(String title, String message)
    {
        emitError(title, message);
    }

    public int askQuestion(String title, String question, int choices)
    {
        return handler.askQuestion(title, question, choices);
    }

    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        return handler.askQuestion(title, question, choices, default_choice);
    }

    public void logOutput(String message, boolean stderr)
    {
        if (stderr)
        {
            prompt.message(Type.ERROR, message);
        }
        else
        {
            prompt.message(Type.INFORMATION, message);
        }
    }

    public void startProcessing(int no_of_processes)
    {
        logOutput("[ Starting processing ]", false);
        this.noOfJobs = no_of_processes;
    }

    public void startProcess(String name)
    {
        this.currentJob++;
        logOutput("Starting process " + name + " (" + Integer.toString(this.currentJob)
                          + "/" + Integer.toString(this.noOfJobs) + ")", false);
    }

    public void finishProcess()
    {
        // TODO Auto-generated method stub
    }

    public void finishProcessing(boolean unlockPrev, boolean unlockNext)
    {
        // TODO Auto-generated method stub

    }

    public boolean runGeneratePropertiesFile(InstallData installData,
                                             PrintWriter printWriter)
    {
        // TODO finish this
        return false;
    }

    public boolean runConsoleFromProperties(InstallData installData, Properties p)
    {
        // TODO finish this
        return runConsole(installData);
    }

    public boolean runConsole(InstallData installData, Console console)
    {
        ProcessPanelWorker worker = new ProcessPanelWorker(installData, rules, resources, matcher);
        worker.setHandler(this);
        worker.run();
        return worker.getResult();
    }
}
