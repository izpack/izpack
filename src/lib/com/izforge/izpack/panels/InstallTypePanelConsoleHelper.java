package com.izforge.izpack.panels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;


public class InstallTypePanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

    public static String ADX_NODE_TYPE = "component.node.type";  
    public static String ADX_NODE_FAMILY = "component.node.family";  

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        printWriter.println(InstallData.MODIFY_INSTALLATION + "=");
        printWriter.println("installpath=");
               
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        String strType = p.getProperty(InstallData.MODIFY_INSTALLATION).trim ();
        if (strType == null || "".equals(strType))
        {
            // assume a normal install 
            installData.setVariable(InstallData.MODIFY_INSTALLATION, "false");
        }
        else
        {
            if (Boolean.parseBoolean(strType))
            {
                // is a modify type install 
                installData.setVariable(InstallData.MODIFY_INSTALLATION, "true");
                
                String strInstallpath = p.getProperty("installpath").trim ();
                installData.setInstallPath(strInstallpath);
                
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
            
            return chooseComponent (installData);
        }
        else
        {
            // want to exit
            return false;
        }
        
        return true;
    }

    public boolean chooseComponent(AutomatedInstallData installData)
    {

        String strQuestion = installData.langpack.getString("InstallationTypePanel.askUpdatePath");
        
        List installedProducts = loadListInstalledProducts (installData);
        
        System.out.println();
        
        for (int i=0; i<installedProducts.size(); i++)
        {
            String[] product = (String[]) installedProducts.get(i);
            
            System.out.println( i + " - " + product[0]);
            
            //System.out.println(i + "  [" + (input.iSelectedChoice == i ? "x" : " ") + "] "
            //        + (choice.strText != null ? choice.strText : ""));
        }
        
        System.out.println();
        
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true)
            {
                System.out.println(strQuestion);
                String strIn = br.readLine().trim();
                int j = -1;
                try
                {
                    j = Integer.valueOf(strIn).intValue();
                }
                catch (Exception ex)
                {}
                if (j>-1 && j<installedProducts.size())
                {
                    String[] product = (String[]) installedProducts.get(j);

                    installData.setInstallPath((String) product[2]);
                    return true;
                }
                else
                {
                    System.out.println(installData.langpack.getString("UserInputPanel.search.wrongselection.caption"));
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }        
        
        return false;
    }

    private List loadListInstalledProducts(AutomatedInstallData installData)
    {
        if (installData.info.needAdxAdmin())
        {
            // Component is registered in adxadmin service
            // we can read pathes from adxinstalls.xml
            
           return loadListFromAdxadmin (installData);
        }
        else
        {
            if (OsVersion.IS_WINDOWS)
            {
                // we can read from registry
                return loadListFromRegistry (installData);
                
            }
            else
            {
                // maybe we can find a service ?? 
                
                
                emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
                
            }
        }
        return null;
    }

    private List loadListFromRegistry(AutomatedInstallData installData)
    {
        try
        {
            // need to process prefix
            
            String uninstallName = installData.getVariable("UNINSTALL_NAME");
            String uninstallKeySuffix = installData.getVariable("UninstallKeySuffix");
            String uninstallKeyPrefix = new String (uninstallName);
            
            if (uninstallKeySuffix!=null && !"".equals(uninstallKeySuffix))
            {
                uninstallKeyPrefix = uninstallKeyPrefix.substring(0, uninstallKeyPrefix.length() -  uninstallKeySuffix.length());
            }
            
            // load registry
            RegistryHandler rh = RegistryDefaultHandler.getInstance();
            if (rh == null)
            {
                // nothing to do
                emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
            }
                
            rh.verify(installData);

            String UninstallKeyName = RegistryHandler.UNINSTALL_ROOT; //"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
            int oldVal = rh.getRoot();
            rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
            
            List<String> lstSubKeys = Arrays.asList( rh.getSubkeys(UninstallKeyName));
            
            ArrayList tblComps = new ArrayList ();
            
            for (String uninstallKey : lstSubKeys) 
            {
                if (uninstallKey.startsWith(uninstallKeyPrefix))
                {
                    // read path from uninstall string :((
                    String productPath =  rh.getValue(UninstallKeyName+"\\"+uninstallKey, "UninstallString").getStringData();
                    productPath = productPath.substring(productPath.lastIndexOf("\"", productPath.length()-2)+1, productPath.length()-29);
                    
                    String productVersion = rh.getValue(UninstallKeyName+"\\"+uninstallKey, "DisplayVersion").getStringData();
                    
                    String name = uninstallKey;
                    if (name.indexOf(" - ")>0)
                    {
                        name=name.substring(name.indexOf(" - ")+3);
                    }
                    
                    // test for .installinformation existence
                    
                    File installInformation = new File(productPath + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                    
                    if (installInformation.exists())
                    {
                        String[] elem = new String[]{name+ " " + productVersion +" ("+productPath+")",name,productPath, productVersion};
                        tblComps.add (elem);
                    }
                }
            }
            
            // free RegistryHandler
            rh.setRoot(oldVal);            
            return tblComps;
            
            
        }
        catch (Exception ex)
        {
            Debug.trace(ex); 
            emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));

        }
        return null;
        
    }

    private List loadListFromAdxadmin(AutomatedInstallData installData)
    {
        try
        {
            
            String strAdxAdminPath = "";
            
            if (OsVersion.IS_UNIX)
            {
                java.io.File adxadmFile = new java.io.File ("/sage/adxadm");
                if (!adxadmFile.exists())
                {
                    adxadmFile = new java.io.File ("/adonix/adxadm");
                }

                if (!adxadmFile.exists())
                {
                    // nothing to do
                    emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));

                }
                
                FileReader readerAdxAdmFile = new FileReader(adxadmFile);
                BufferedReader buffread = new BufferedReader(readerAdxAdmFile);
                strAdxAdminPath = buffread.readLine();

                
            }
            else
            {
                RegistryHandler rh = RegistryDefaultHandler.getInstance();
                if (rh == null)
                {
                    // nothing to do
                    emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
                }
                    
                rh.verify(installData);

                // test adxadmin déjà installé avec registry
                if (!rh.adxadminProductRegistered())
                {
                    // nothing to do
                    emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
                }

                String keyName = "SOFTWARE\\Adonix\\X3RUNTIME\\ADXADMIN";
                int oldVal = rh.getRoot();
                rh.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                if (!rh.valueExist(keyName, "ADXDIR")) keyName = "SOFTWARE\\Wow6432Node\\Adonix\\X3RUNTIME\\ADXADMIN";
                if (!rh.valueExist(keyName, "ADXDIR")) 
                {
                    // nothing to do
                    emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
                }
                
                // récup path
                strAdxAdminPath = rh.getValue(keyName, "ADXDIR").getStringData();

                // free RegistryHandler
                rh.setRoot(oldVal);
                
            }
    
            
            // check strAdxAdminPath
            
            if (strAdxAdminPath == null || "".equals(strAdxAdminPath)) 
            {
                // nothing to do
                emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
            }
            
            java.io.File dirAdxDir = new java.io.File (strAdxAdminPath);
            
            if (!dirAdxDir.exists() || !dirAdxDir.isDirectory()) 
            {
                // nothing to do
                emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
            }
            
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(dirAdxDir.getAbsolutePath());
            strBuilder.append(dirAdxDir.separator);
            strBuilder.append("inst");
            strBuilder.append(dirAdxDir.separator);
            strBuilder.append("adxinstalls.xml");
            
            java.io.File fileAdxinstalls = new java.io.File (strBuilder.toString());
            
            if (!fileAdxinstalls.exists())
            {
                // nothing to do
                emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
            }
        
            
            // we need to know type and family
            String strComponentType = installData.getVariable(ADX_NODE_TYPE);
            String strComponentFamily = installData.getVariable(ADX_NODE_FAMILY);
            
            // do nothing if we don't know family
            if (strComponentFamily == null) 
            {
                emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
            }
            
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fileAdxinstalls);

            XPath xPath =  XPathFactory.newInstance().newXPath();
            String expression = "//module[@family='"+strComponentFamily+"'";
            
            if (strComponentType!=null ) expression+=" and @type='"+strComponentType+"'";
            
            expression+="]";
            
            
            NodeList nodeLst = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
            
            ArrayList tblComps = new ArrayList ();
             
            for (int i = 0; i < nodeLst.getLength(); i++) 
            {
             
                Element moduleNode = (Element) nodeLst.item(i);
                String path = xPath.evaluate("./component."+strComponentFamily.toLowerCase()+".path", moduleNode);
                String strversion = xPath.evaluate("./component."+strComponentFamily.toLowerCase()+".version", moduleNode);
                String name = moduleNode.getAttribute("name");
                
                File installInformation = new File(path + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                
                if (installInformation.exists())
                {
                    String[] elem = new String[]{name+" "+ strversion +" ("+path+")",name,path, strversion};
                    tblComps.add (elem);
                }
               
                else if (path.endsWith(File.separator +"tool"))
                {
                    path = path.substring(0, path.length()-5);
                    installInformation = new File(path + File.separator + AutomatedInstallData.INSTALLATION_INFORMATION);
                    
                    if (installInformation.exists())
                    {
                        String[] elem = new String[]{name+" "+ strversion +" ("+path+")",name,path, strversion};
                        tblComps.add (elem);
                    }
                }
                
            }
            
            return tblComps;
        
        }
        catch (Exception ex)
        {
            Debug.trace(ex); 
            emitErrorAndBlockNext(installData.langpack.getString("installer.error"), installData.langpack.getString("InstallationTypePanel.errNoCompFound"));
        }
        
        return null;
    }

    

}
