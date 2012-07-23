package com.izforge.izpack.panels.process;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;

import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.resource.Resources;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Type;

import com.izforge.izpack.installer.console.PanelConsole;
import com.izforge.izpack.installer.console.AbstractPanelConsole;
import com.izforge.izpack.util.Console;

import com.izforge.izpack.panels.process.ProcessPanelWorker;
import com.izforge.izpack.panels.process.AbstractUIProcessHandler;


public class ProcessPanelConsole extends AbstractPanelConsole implements PanelConsole, AbstractUIProcessHandler
{
    private VariableSubstitutor vs;
    private RulesEngine rules;
    private Resources resources;
    private Prompt prompt;

    private int noOfJobs = 0;

    private int currentJob = 0;

    public ProcessPanelConsole(VariableSubstitutor vs, RulesEngine rules, Resources resources, Prompt prompt)
    {
        this.vs = vs;
        this.rules = rules;
        this.resources = resources;
        this.prompt = prompt;
    }
    public void emitNotification(String message)
    {
        // TODO Auto-generated method stub
    }

    public boolean emitWarning(String title, String message)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void emitError(String title, String message)
    {
        // TODO Auto-generated method stub
    }

    public void emitErrorAndBlockNext(String title, String message)
    {
        // TODO Auto-generated method stub

    }

    public int askQuestion(String title, String question, int choices)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        // TODO Auto-generated method stub
        return 0;
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

    public boolean runConsole(InstallData installData)
    {
        return true;
    }

    public boolean runConsole(InstallData installData, Console console)
    {

        try
        {
            ProcessPanelWorker worker = new ProcessPanelWorker(installData, vs, rules, resources);
            worker.setHandler(this);

            worker.run();

            if (!worker.getResult())
            {
                return false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("The work done by the ProcessPanel failed", e);
        }
        return true;
    }
}
