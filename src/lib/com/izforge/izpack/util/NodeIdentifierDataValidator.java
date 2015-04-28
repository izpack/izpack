package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


/**
 * @author apozzo
 *
 */
public class NodeIdentifierDataValidator implements DataValidator
{

    private static final String SPEC_FILE_NAME = "productsSpec.txt";
    
    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public Status validateData(AutomatedInstallData adata)
    {
        return validate (adata.getVariable("component.node.name"), adata.getVariable("APP_NAME"));
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getErrorMessageId()
     */
    public String getErrorMessageId()
    {
        // TODO Auto-generated method stub
        return "nodealreadyexisterror";
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getWarningMessageId()
     */
    public String getWarningMessageId()
    {
        // TODO Auto-generated method stub
        return "nodealreadyexistwarn";
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getDefaultAnswer()
     */
    public boolean getDefaultAnswer()
    {
        // can we validate in automated mode ?
        // say yes for now
        return true;
    }
    
    public Status validate (String pstrNodeName, String pstrAppName)
    {
        Status bReturn = Status.ERROR;
        try
        {
        
            String nodeName = pstrNodeName;
            String svcExt = ".service"; 
            
            ArrayList<String> uninstallKeyPrefixList = new ArrayList<String>();
            uninstallKeyPrefixList.add(pstrAppName);
            
            // load additionnal prefix from resource

            try
            {
                InputStream input = ResourceManager.getInstance().getInputStream(SPEC_FILE_NAME);
                
                if (input != null)
                {
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder out = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) 
                    {
                        uninstallKeyPrefixList.add(line.trim());
                    }
                    reader.close();
                }
                
            }
            catch (Exception ex)
            {
               Debug.log(ex);
            }             
            
            
            // check node unicity by service name ?
            // is there a better way ?
            String serviceName = "";
            
            if (OsVersion.IS_UNIX)
            {
                // check file /etc/init/xxxxxxx-$SERVICE_NAME.conf
                // SERVICE_NAME = node name in lower
                // first line of xxxxxxx-$SERVICE_NAME.conf contains "# pstrAppName"
                
                File etcInitDir = new File ("/etc/systemd/system");
                
                if (!(etcInitDir.exists() && etcInitDir.isDirectory()))
                {
                    etcInitDir = new File ("/etc/init");
                    svcExt = ".conf";
                }
                
                bReturn = Status.OK;
                
                //System.out.println("Service path : "+etcInitDir);

                for (File fileEntry : etcInitDir.listFiles()) 
                {
                    //System.out.println(fileEntry.getAbsolutePath());
                    if (fileEntry.getName().endsWith("-"+pstrNodeName.toLowerCase()+svcExt) || fileEntry.getName().endsWith("_-_"+pstrNodeName.toLowerCase()+svcExt))
                    {
                        BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
                        
                        String firstLine = reader.readLine();
                        reader.close();
                        
                        //System.out.println(firstLine);
                        for (String prefix : uninstallKeyPrefixList)
                        {
                            if (firstLine.startsWith("# "+prefix))
                                return Status.ERROR;
                        }
                        
                    }
                }                
                
            }
            else
            {
                // windows
                bReturn = Status.OK;
                //serviceName = pstrAppName+" - "+nodeName;
                String commandquery = "sc query state= all | findstr /R /C:\""+nodeName+"\"";
                String[] command = {"CMD", "/C", commandquery};
                
                ProcessBuilder probuilder = new ProcessBuilder( command );

                Process process = probuilder.start();
                
                //Read out dir output
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;

                while ((line = br.readLine()) != null) 
                {
                    // to find : DISPLAY_NAME: serviceName
                    for (String prefix : uninstallKeyPrefixList)
                    {
                        if (line.startsWith("DISPLAY_NAME: "+ prefix))
                        {
                            bReturn = Status.ERROR;
                        }
                    }
                }
                
                //Wait to get exit value
                try {
                    int exitValue = process.waitFor();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }                
                
            }
            
        }
        catch (Exception ex)
        {
            // got exception
            Debug.trace(ex.getMessage());
            bReturn = Status.ERROR; 
        }

        return bReturn;
        
    }

}
