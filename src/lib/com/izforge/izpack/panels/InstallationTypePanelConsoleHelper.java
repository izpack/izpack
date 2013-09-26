package com.izforge.izpack.panels;

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ScriptParser;


public class InstallationTypePanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        printWriter.println(InstallData.MODIFY_INSTALLATION + "=");
               
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        String strType = p.getProperty(InstallData.MODIFY_INSTALLATION);
        if (strType == null || "".equals(strType.trim()))
        {
            // assume a normal install 
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else
        {
            if (Boolean.parseBoolean(strType.trim()))
            {
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
            }
            else
            {
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
            }
        }
        
        
        return true;
    }

    public boolean runConsole(AutomatedInstallData installData)
    {
        String str;
        str = installData.langpack.getString("InstallationTypePanel.info"); 
        
        System.out.println("");
        System.out.println(str);
        

        int i = 0;

        while (i<1 || i>3)
        {
            i= askQuestion(installData, installData.langpack.getString("InstallationTypePanel.asktype"), 1);
        }
        
        if (i==1)
        {
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else if (i==2)
        {
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
        }
        else
        {
            // want to exit
            return false;
        }
        
        return true;
    }
    

}
