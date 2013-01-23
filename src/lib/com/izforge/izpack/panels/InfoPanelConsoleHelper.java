/**
 * 
 */
package com.izforge.izpack.panels;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.VariableSubstitutor;


/**
 * @author apozzo
 *
 */
public class InfoPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelConsole#runGeneratePropertiesFile(com.izforge.izpack.installer.AutomatedInstallData, java.io.PrintWriter)
     */
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        // nothing to do here
        return true;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelConsole#runConsoleFromPropertiesFile(com.izforge.izpack.installer.AutomatedInstallData, java.util.Properties)
     */
    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        // nothing to do here
        return true;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelConsole#runConsole(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public boolean runConsole(AutomatedInstallData installData)
    {
        String readme = null;
        String resNamePrefix = "InfoPanel.info";
        try
        {
            // We read it
             readme = ResourceManager.getInstance().getTextResource(resNamePrefix);
        }
        catch (Exception err)
        {
            readme = "Error : could not load the information text for defined resource " + resNamePrefix;
            System.out.println(readme);
            return false;
        }
        
        // controls # of lines to display at a time, to allow simulated scrolling down
        int lines=25;
        int l = 0;
        
        StringTokenizer st = new StringTokenizer(readme, "\n");
        while (st.hasMoreTokens())
        {
             String token = st.nextToken();
             System.out.println(token);
             l++;
             if (l >= lines) {
                 String strIn = doContinue(); 
                 if ( strIn.equalsIgnoreCase("x") ) {
                     return false;
                 } else if ( strIn.equalsIgnoreCase("s") ) {
                     break;
                 }
                 l=0;
             }             
        }
   
        int i = askEndOfConsolePanel();

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

    private String doContinue()
    {
        try
        {
            System.out.println("\r");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println("press Enter to continue, S to skip or X to exit");
                String strIn = br.readLine();
                return strIn;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "X";
    }
    
    
}
