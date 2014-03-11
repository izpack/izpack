/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Jan Blok
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.izforge.izpack.Info;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;

/**
 * Hello Panel console helper
 * 
 * @author Mounir el hajj
 */
public class HelloPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata)
    {
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
            return false;
        }
        
        
        
        int i = askEndOfConsolePanel(idata);
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
            
            File adxadmFile = new File ("/sage/adxadm");
            if (adxadmFile.exists())
            {
                // il semble que ce soit une mise a jour
                // on positionne update mode
                // puis on charge .installinformation
                
                try
                {
                    String adxadmPath = readFile (OsVersion.IS_UNIX?"/sage/adxadm":"c:\\sage\\adxadm", Charset.defaultCharset());
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
