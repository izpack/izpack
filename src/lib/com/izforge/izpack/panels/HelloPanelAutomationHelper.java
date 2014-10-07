/**
 * 
 */
package com.izforge.izpack.panels;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.izforge.izpack.Info;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
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
        else if (idata.info.isAdxAdmin())
        {
            // unix case
            // search for /sage/adxadm
            if (OsVersion.IS_UNIX)
            {
            
                File adxadmFileUnix = new File ("/sage/adxadm");
                if (adxadmFileUnix.exists())
                {
                    // il semble que ce soit une mise a jour
                    // on positionne update mode
                    // puis on charge .installinformation
                    
                    try
                    {
                        String adxadmPath = readFile ("/sage/adxadm", Charset.defaultCharset());
                        adxadmPath = adxadmPath.replace("\r\n", "").replace("\n", "").trim();
                        String installInformation = adxadmPath+"/"+AutomatedInstallData.INSTALLATION_INFORMATION;
                        File installInformationFile = new File (installInformation);
                        if (!installInformationFile.exists())
                        {
                            System.out.println(idata.langpack.getString( "adxadminNoAdxDir"));
                            return false;
                        }
                        else
                        {
                            System.out.println(idata.langpack.getString("compFoundDoUpdate"));
                            System.out.println();

                            // relecture installInformation
                            idata.setInstallPath(adxadmPath);
                            // positionnement update
                            Debug.trace("modification installation");
                            idata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
                            
                        }
                    }
                    catch (IOException e)
                    {
                        System.out.println(idata.langpack.getString( "adxadminNoAdxDir"));
                        return false;
                    }
                    
                }
                else
                {
                    // adxadmin V6 ?
                    // in /adonix/adxadm
                    adxadmFileUnix = new File ("/adonix/adxadm");
                    if (adxadmFileUnix.exists())
                    {
                        try
                        {
                            String adxadmPath = readFile ("/sage/adxadm", Charset.defaultCharset());
                            adxadmPath = adxadmPath.replace("\r\n", "").replace("\n", "").trim();
                            
                            System.out.println(idata.langpack.getString( "adxadminV6found"));
                            System.out.println(adxadmPath);
                            return false;
                            
                        }
                        catch (IOException e)
                        {
                            System.out.println(idata.langpack.getString( "adxadminNoAdxDir"));
                            return false;
                        }
                       
                    }
                    
                    
                }
            }
            else
            {
                try
                {
                    String strAdxAdminPath = "";
                    // win32 pltaform
                    // vérifier la présence d'un adxadmin
                    RegistryHandler rh = RegistryDefaultHandler.getInstance();
                    if (rh != null)
                    {
        
                        rh.verify(idata);
        
                        // test adxadmin déjà installé avec registry
                        if (rh.adxadminProductRegistered())
                        {
                            // adxadmin ok
                            // read path and test for .installationinformation
                            
                            String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
                            int oldVal = rh.getRoot();
                            rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                            if (!rh.valueExist(keyName, "ADXDIR")) 
                            {
                                keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
                                if (!rh.valueExist(keyName, "ADXDIR")) 
                                {
                                    System.out.println(idata.langpack.getString( "adxadminNoAdxDir"));
                                    // free RegistryHandler
                                    rh.setRoot(oldVal);
                                    return false;
                                }
                                else
                                {
                                    // adxadmin 32bits
                                    // récup path
                                    strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();                            
                                }
                            }
                            else
                            {
                                // récup path
                                strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();                            
                            }
                            
                            // free RegistryHandler
                            rh.setRoot(oldVal);

                            // test .installationinformation
                            File fInstallINf = new File (strAdxAdminPath+ File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                            
                            if (!fInstallINf.exists())
                            {
                                //adxadmin V6
                                System.out.println(idata.langpack.getString( "adxadminV6found"));
                                System.out.println(strAdxAdminPath);
                                return false;
                                
                            }
                            else
                            {
                                System.out.println(idata.langpack.getString("compFoundDoUpdate"));
                                System.out.println();

                                // relecture installInformation
                                idata.setInstallPath(strAdxAdminPath);
                                // positionnement update
                                Debug.trace("modification installation");
                                idata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
                                
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
            
        }
        
        return true;

    }
    
    static String readFile(String path, Charset encoding) 
            throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

}
