package com.izforge.izpack.panels;

import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;


public class LicenceParamPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    
    public static final String strLicenceId = "licenceid";
    
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata)
    {

        String license = null;
        String resNamePrefix = idata.getVariable(strLicenceId);
        try
        {
            // We read it
             license = ResourceManager.getInstance().getTextResource(resNamePrefix);
        }
        catch (Exception err)
        {
            license = "Error : could not load the licence text for defined resource " + resNamePrefix;
            System.out.println(license);
            return false;
        }
        
        // controls # of lines to display at a time, to allow simulated scrolling down
        int lines=12;
        int l = 0;
        
        StringTokenizer st = new StringTokenizer(license, "\n");
        while (st.hasMoreTokens())
        {
             String token = st.nextToken();
             System.out.println(token);
             System.out.println();
             l++;
             if (l >= lines) {
                 int strIn = doContinue(idata); 
                 if ( strIn==3 ) {
                     return false;
                 } else if ( strIn==2 ) {
                     break;
                 }
                 l=0;
             }             
        }
   
        int i = askToAccept(idata);

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
            return runConsole(idata);
        }

    }
    
    

}
