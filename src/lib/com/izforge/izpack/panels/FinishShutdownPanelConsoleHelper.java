package com.izforge.izpack.panels;


import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.VariableSubstitutor;


public class FinishShutdownPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole 
{
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter) {
        return true;
    }
    
    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p){
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata) {
        if (idata.installSuccess) {
            
            System.out.println();
            System.out.println(idata.langpack.getString("FinishPanel.success"));
            System.out.println(idata.langpack.getString("FinishPanel.installed.on") + " " + idata.getInstallPath());

            if (idata.uninstallOutJar != null)
            {
                VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());             
                // We prepare a message for the uninstaller feature
                String path = idata.info.getUninstallerPath();
                path = vs.substitute(path, null);

                // Convert the file separator characters
                path = path.replace('/', File.separatorChar);
               
                System.out.println(idata.langpack
                        .getString("FinishPanel.uninst.info") + " " + path);
            }       
            
        } else {
            System.out.println();
            System.out.println(idata.langpack.getString("FinishPanel.fail"));
        }
        System.out.println();
        return true;
    }
}
