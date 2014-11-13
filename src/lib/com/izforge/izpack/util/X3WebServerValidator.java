package com.izforge.izpack.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


public class X3WebServerValidator implements Validator
{

    public boolean validate(ProcessingClient client,AutomatedInstallData adata)
    {
        boolean bReturn = false;
        try
        {
        
            //String x3webPath = adata.getVariable("syracuse.certificate.x3webserver").trim();
            String x3webPath = client.getFieldContents(0).trim();
            
            
            
            File X3WebInstallInformation = new File (x3webPath+"/"+AutomatedInstallData.INSTALLATION_INFORMATION);
            if (X3WebInstallInformation.exists() && X3WebInstallInformation.isFile())
            {
                // we need to load it to find where is stored the data directory 
                
                
                FileInputStream fin = new FileInputStream(X3WebInstallInformation);
                ObjectInputStream oin = new ObjectInputStream(fin);
                List packsinstalled = (List) oin.readObject();
                Properties variables = (Properties) oin.readObject();                
                
                String strDataPath = variables.getProperty("webserver.dir.data");
                if (strDataPath!=null)
                {
                    adata.setVariable("syracuse.certificate.x3webserverdata", strDataPath + File.separator + "KEYSTORE" + File.separator + "WEBSERVER");
                    bReturn = true;
                }
                
                
            }
            
        }
        catch (Exception ex)
        {
            // got exception
            Debug.trace(ex.getMessage());
            bReturn = false; 
        }

        return bReturn;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getErrorMessageId()
     */
    public String getErrorMessageId()
    {
        // TODO Auto-generated method stub
        return "x3webserverpath";
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getWarningMessageId()
     */
    public String getWarningMessageId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getDefaultAnswer()
     */
    public boolean getDefaultAnswer()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
