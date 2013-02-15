package com.izforge.izpack.panels;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ProcessPanelWorker;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.AbstractUIProcessHandler;


public class ProcessPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole, AbstractUIProcessHandler
{
    private int noOfJobs = 0;
    private AutomatedInstallData installdata;
    private int currentJob = 0;

    
    public void emitNotification(String message)
    {
        System.out.println(message);
    }

    public boolean emitWarning(String title, String message)
    {
        System.err.println("[ WARNING: " + title + " ]");
        System.err.println(message);

        return true;
    }

    public void emitError(String title, String message)
    {
        System.err.println("[ ERROR: " + title + " ]");
        System.err.println(message);
    }
    
    public void emitErrorAndBlockNext(String title, String message)
    {
        System.err.println("[ ERROR: " + title + " ]");
        System.err.println(message);
    }

    public int askQuestion(String title, String question, int choices)
    {
        // don't know what to answer
        return askQuestion( title,  question,  choices, 2);
    }

    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        int jo_choices = 0;

        if (choices == AbstractUIHandler.CHOICES_YES_NO)
        {
            jo_choices = JOptionPane.YES_NO_OPTION;
        }
        else if (choices == AbstractUIHandler.CHOICES_YES_NO_CANCEL)
        {
            jo_choices = JOptionPane.YES_NO_CANCEL_OPTION;
        }

        //public static final int         DEFAULT_OPTION = -1;
        /** Type used for <code>showConfirmDialog</code>. */
        //public static final int         YES_NO_OPTION = 0;
        /** Type used for <code>showConfirmDialog</code>. */
        //public static final int         YES_NO_CANCEL_OPTION = 1;
        /** Type used for <code>showConfirmDialog</code>. */
        //public static final int         OK_CANCEL_OPTION = 2;
        System.out.println ("[ " + title + " ]");
        System.out.println (question);
        
        
        int user_choice = askQuestion(installdata, installdata.langpack.getString("consolehelper.askyesnocancel"), 2)-1;
        
        if (user_choice == JOptionPane.CANCEL_OPTION) { return AbstractUIHandler.ANSWER_CANCEL; }

        if (user_choice == JOptionPane.YES_OPTION) { return AbstractUIHandler.ANSWER_YES; }

        if (user_choice == JOptionPane.CLOSED_OPTION) { return AbstractUIHandler.ANSWER_NO; }

        if (user_choice == JOptionPane.NO_OPTION) { return AbstractUIHandler.ANSWER_NO; }

        return default_choice;
    }   

    public void logOutput(String message, boolean stderr)
    {
        if (stderr)
        {
            System.err.println(message);
        }
        else
        {
            System.out.println(message);
        }
    }

    public void startProcessing(int no_of_processes)
    {
        System.out.println("[ Starting processing ]");
        this.noOfJobs = no_of_processes;
    }

    public void startProcess(String name)
    {
        this.currentJob++;
        System.out.println("Starting process " + name + " (" + Integer.toString(this.currentJob)
                + "/" + Integer.toString(this.noOfJobs) + ")");
    }

    public void finishProcess()
    {
        // TODO Auto-generated method stub
        
    }

    public void finishProcessing(boolean unlockPrev, boolean unlockNext)
    {
        // TODO Auto-generated method stub
        
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        this.installdata = installData;
        // TODO finish this
        return false;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        this.installdata = installData;

        // TODO finish this
        return runConsole(installData);
    }

    public boolean runConsole(AutomatedInstallData installData)
    {
        this.installdata = installData;

        try
        {
            ProcessPanelWorker worker = new ProcessPanelWorker(installData, this);

            worker.run();
            
            System.out.println ("");
            
            if (worker.getResult())
            {
                while (true)
                {
                    int i = askEndOfConsolePanel(installData);
                    if (i == 1)
                    {
                        return true;
                    }
                    else if (i == 2)
                    {
                        return false;
                    }
                }
            }
            else return false;
            

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("The work done by the ProcessPanel failed", e);
        }
        
        //return true;
    }

    
}
