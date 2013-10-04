/**
 * 
 */
package com.izforge.izpack.panels;

import java.util.ArrayList;

import com.izforge.izpack.Info;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.PanelAutomationHelper;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;
import com.coi.tools.os.win.MSWinConstants;
import com.coi.tools.os.win.RegDataContainer;


/**
 * @author apozzo
 *
 */
public class HelloPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation,
        AbstractUIProgressHandler, MSWinConstants
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
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#startAction(java.lang.String, int)
     */
    public void startAction(String name, int no_of_steps)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#stopAction()
     */
    public void stopAction()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#nextStep(java.lang.String, int, int)
     */
    public void nextStep(String step_name, int step_no, int no_of_substeps)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#setSubStepNo(int)
     */
    public void setSubStepNo(int no_of_substeps)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.util.AbstractUIProgressHandler#progress(int, java.lang.String)
     */
    public void progress(int substep_no, String message)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelAutomation#makeXMLData(com.izforge.izpack.installer.AutomatedInstallData, com.izforge.izpack.adaptator.IXMLElement)
     */
    public void makeXMLData(AutomatedInstallData installData, IXMLElement panelRoot)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.PanelAutomation#runAutomated(com.izforge.izpack.installer.AutomatedInstallData, com.izforge.izpack.adaptator.IXMLElement)
     */
    public void runAutomated(AutomatedInstallData installData, IXMLElement panelRoot)
            throws InstallerException
    {
        // méthode pour l'installation automatique
        
        this.idata = installData;

        String str;
        str = idata.langpack.getString("HelloPanel.welcome1") + idata.info.getAppName() + " "
                + idata.info.getAppVersion() + idata.langpack.getString("HelloPanel.welcome2");
        
        System.out.println("");
        System.out.println(str);
        
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
        
        System.out.println("");
        
        // recherche d'un adxadmin
        if (!adxadminPresent ( idata))
        {
            emitErrorAndBlockNext ("",idata.langpack
                    .getString("installer.quit.message"));
        }
    }
    
    protected boolean adxadminPresent (AutomatedInstallData idata)
    {
        if (idata.info.needAdxAdmin())
        {
            try
            {
                // vérifier la présence d'un adxadmin
                RegistryHandler rh = RegistryDefaultHandler.getInstance();
                if (rh != null)
                {
    
                    rh.verify(idata);
    
                    // test adxadmin déjà installé avec registry
                    if (!rh.adxadminProductRegistered())
                    {
                        // pas d'adxadmin
                        System.out.println(idata.langpack.getString( "adxadminNotRegistered"));
                        return false;
                    }
                }
                else
                {
    
                    // else we are on a os which has no registry or the
                    // needed dll was not bound to this installation. In
                    // both cases we forget the "already exist" check.
                    
                    // test adxadmin sous unix avec /adonix/adxadm ?
                        if (OsVersion.IS_UNIX)
                        {
                            java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                            if (!adxadmFile.exists())
                            {
                                adxadmFile = new java.io.File ("/adonix/adxadm");
                                if (!adxadmFile.exists())
                                {
                                    // pas d'adxadmin
                                    System.out.println(idata.langpack.getString( "adxadminNotRegistered"));
                                    return false;
                                }
                            }
                        }
                }
            }
            catch (Exception e)
            { // Will only be happen if registry handler is good, but an
                // exception at performing was thrown. This is an error...
                Debug.log(e);
                System.out.println(idata.langpack.getString( "installer.error"));
                return false;
            }
        }
        
        return true;

    }
    

}
