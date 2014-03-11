package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


/**
 * @author apozzo
 *
 */
public class NodeIdentifierDataValidator implements DataValidator
{

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
            
            // check node unicity by service name ?
            // is there a better way ?
            String serviceName = "";
            
            if (OsVersion.IS_UNIX)
            {
                // check file /etc/init/syracuse-$SERVICE_NAME.conf
                // SERVICE_NAME = node name
                serviceName = "/etc/init/sagesyracuse-"+nodeName.toLowerCase()+".conf";
                File serviceFile = new File (serviceName);
                if (!serviceFile.exists()) bReturn = Status.OK; 
            }
            else
            {
                // windows
                bReturn = Status.OK;
                serviceName = pstrAppName+" - "+nodeName;
                String commandquery = "sc query state= all | findstr /R /C:\""+serviceName+"\"";
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
                    if (line.startsWith("DISPLAY_NAME: "+ serviceName))
                    {
                        bReturn = Status.ERROR;
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
