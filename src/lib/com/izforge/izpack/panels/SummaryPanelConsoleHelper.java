package com.izforge.izpack.panels;

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.util.SummaryProcessor;
import com.izforge.izpack.installer.PanelConsoleHelper;



public class SummaryPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean runConsole(AutomatedInstallData installData)
    {
        // TODO Auto-generated method stub

        System.out.println ("");
        System.out.println (installData.langpack.getString("SummaryPanel.info"));
        SummaryProcessor.processSummaryText(installData);
        System.out.println ("");

        int i = askEndOfConsolePanel(installData);
        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(installData);
        }
    }

}
