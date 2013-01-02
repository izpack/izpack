/**
 * 
 */
package com.izforge.izpack.panels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

import com.coi.tools.os.win.MSWinConstants;
import com.izforge.izpack.Info;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;

/**
 * @author apozzo
 *
 */
public class CheckedHelloPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole, MSWinConstants
{

    /**
     * Flag to break installation or not.
     */
    protected boolean abortInstallation;
    /**
     * The installer internal data (actually a melting-pot class with all-public fields.
     */
    protected AutomatedInstallData idata;
    

    
    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelConsole#runGeneratePropertiesFile(com.izforge.izpack.installer.AutomatedInstallData, java.io.PrintWriter)
     */
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelConsole#runConsoleFromPropertiesFile(com.izforge.izpack.installer.AutomatedInstallData, java.util.Properties)
     */
    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelConsole#runConsole(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public boolean runConsole(AutomatedInstallData installData)
    {
        this.idata = installData;
        
        if (isRegistered())
        {
            try
            {
                if (multipleInstall())
                {
                    setUniqueUninstallKey();
                    abortInstallation = false;
                }
                else
                {
                    return false;
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        if (rh != null)
        {
            idata.setVariable("UNINSTALL_NAME", rh.getUninstallName());
        }

        // from HelloPanelConsoleHelper
        String str;
        str = idata.langpack.getString("HelloPanel.welcome1") + idata.info.getAppName() + " "
                + idata.info.getAppVersion() + idata.langpack.getString("HelloPanel.welcome2");
        out("");
        out(str);
        ArrayList<Info.Author> authors = idata.info.getAuthors();
        int size = authors.size();
        if (size > 0)
        {
            str = idata.langpack.getString("HelloPanel.authors");

            for (int i = 0; i < size; i++)
            {
                Info.Author a = authors.get(i);
                String email = (a.getEmail() != null && a.getEmail().length() > 0) ? (" <"
                        + a.getEmail() + ">") : "";
                System.out.println(" - " + a.getName() + email);
            }

        }

        if (idata.info.getAppURL() != null)
        {
            str = idata.langpack.getString("HelloPanel.url") + idata.info.getAppURL();
            System.out.println(str);
        }
        out ("");
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
            return runConsole(idata);
        }
    }
    
    /**
     * This method should only be called if this product was allready installed. It resolves the
     * install path of the first already installed product and asks the user whether to install
     * twice or not.
     *
     * @return whether a multiple Install should be performed or not.
     * @throws Exception
     */
    protected boolean multipleInstall() throws Exception
    {
        // Let us play a little bit with the regstry...
        // Just for fun we would resolve the path of the already
        // installed application.
        // First we need a handler. There is no overhead at a
        // secound call of getInstance, therefore we do not buffer
        // the handler in this class.
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        int oldVal = rh.getRoot(); // Only for security...
        // We know, that the product is already installed, else we
        // would not in this method. Now we search for the path...
        String uninstallName = rh.getUninstallName();
        String oldInstallPath = "<not found>";
        while (true) // My goto alternative :-)
        {

            if (uninstallName == null)
            {
                break; // Should never be...
            }
            // First we "create" the reg key.
            String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
            rh.setRoot(HKEY_LOCAL_MACHINE);
            if (!rh.valueExist(keyName, "UninstallString"))
            // We assume that the application was installed with
            // IzPack. Therefore there should be the value "UninstallString"
            // which contains the uninstaller call. If not we can do nothing.
            {
                break;
            }
            // Now we would get the value. A value can have different types.
            // Therefore we get an container which can handle all possible types.
            // There are different ways to handle. Use normally only one of the
            // ways; at this point more are used to demonstrate the different ways.

            // 1. If we are secure about the type, we can extract the value immediately.
            String valString = rh.getValue(keyName, "UninstallString").getStringData();

            // 2. If we are not so much interessted at the type, we can get the value
            // as Object. A DWORD is then a Long Object not a long primitive type.
            Object valObj = rh.getValue(keyName, "UninstallString").getDataAsObject();
            if (valObj instanceof String) // Only to inhibit warnings about local variable never read.
            {
                valString = (String) valObj;
            }

            // 3. If we are not secure about the type we should differ between possible
            // types.
            RegDataContainer val = rh.getValue(keyName, "UninstallString");
            int typeOfVal = val.getType();
            switch (typeOfVal)
            {
                case REG_EXPAND_SZ:
                case REG_SZ:
                    valString = val.getStringData();
                    break;
                case REG_BINARY:
                case REG_DWORD:
                case REG_LINK:
                case REG_MULTI_SZ:
                    throw new Exception("Bad data type of chosen registry value " + keyName);
                default:
                    throw new Exception("Unknown data type of chosen registry value " + keyName);
            }
            // That's all with registry this time... Following preparation of
            // the received value.
            // It is [java path] -jar [uninstaller path]
            int start = valString.lastIndexOf("-jar") + 5;
            if (start < 5 || start >= valString.length())
            // we do not know what todo with it.
            {
                break;
            }
            String uPath = valString.substring(start).trim();
            if (uPath.startsWith("\""))
            {
                uPath = uPath.substring(1).trim();
            }
            int end = uPath.indexOf("uninstaller");
            if (end < 0)
            // we do not know what todo with it.
            {
                break;
            }
            oldInstallPath = uPath.substring(0, end - 1);
            // Much work for such a peanuts...
            break; // That's the problem with the goto alternative. Forget this
            // break produces an endless loop.
        }

        rh.setRoot(oldVal); // Only for security...

        // The text will be to long for one line. Therefore we should use
        // the multi line label. Unfortunately it has no icon. Nothing is
        // perfect...
        out ("");
        String noLuck = idata.langpack.getString("CheckedHelloPanel.productAlreadyExist0")
                + oldInstallPath
                + idata.langpack.getString("CheckedHelloPanel.productAlreadyExist1");
        return askQuestionYesNo( noLuck);
    }
    
    private boolean askQuestionYesNo(String pstrMessage)
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                out (pstrMessage);
                out ("");
                System.out.println("press 1 to accept, 2 to reject, 3 to redisplay");
                String strIn = br.readLine();
                if (strIn.equals("1"))
                {
                    return true;
                }
                else if (strIn.equals("2"))
                {
                    return false;
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    
    private static void out(String message)
    {
        System.out.println(message);
    }
    
    private void setUniqueUninstallKey() throws Exception
    {
        // Let us play a little bit with the regstry again...
        // Now we search for an unique uninstall key.
        // First we need a handler. There is no overhead at a
        // secound call of getInstance, therefore we do not buffer
        // the handler in this class.
        RegistryHandler rh = RegistryDefaultHandler.getInstance();
        int oldVal = rh.getRoot(); // Only for security...
        // We know, that the product is already installed, else we
        // would not in this method. First we get the
        // "default" uninstall key.
        if (oldVal > 100) // Only to inhibit warnings about local variable never read.
        {
            return;
        }
        String uninstallName = rh.getUninstallName();
        int uninstallModifier = 1;
        while (true)
        {
            if (uninstallName == null)
            {
                break; // Should never be...
            }
            // Now we define a new uninstall name.
            String newUninstallName = uninstallName + "(" + Integer.toString(uninstallModifier)
                    + ")";
            // Then we "create" the reg key with it.
            String keyName = RegistryHandler.UNINSTALL_ROOT + newUninstallName;
            rh.setRoot(HKEY_LOCAL_MACHINE);
            if (!rh.keyExist(keyName))
            { // That's the name for which we searched.
                // Change the uninstall name in the reg helper.
                rh.setUninstallName(newUninstallName);
                // Now let us inform the user.
                out(idata.langpack
                        .getString("CheckedHelloPanel.infoOverUninstallKey")
                        + newUninstallName);
                // Now a little hack if the registry spec file contains
                // the pack "UninstallStuff".
                break;
            }
            uninstallModifier++;
        }
    }
    
    /**
     * Returns wether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows).
     *
     * @return wether the handled application is already registered or not
     */
    
    protected boolean isRegistered()
    {
        boolean retval = false;
        try
        {
            // Get the default registry handler.
            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh != null)
            {
                rh.verify(idata);
                retval = rh.isProductRegistered();

            }
            // else we are on a os which has no registry or the
            // needed dll was not bound to this installation. In
            // both cases we forget the "already exist" check.

        }
        catch (Exception e)
        { // Will only be happen if registry handler is good, but an
            // exception at performing was thrown. This is an error...
            e.printStackTrace();
        }
        return (retval);
    }
    

}
