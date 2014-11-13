/**
 * 
 */
package com.izforge.izpack.util;

import java.io.File;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;


/**
 * @author apozzo
 *
 */
public class X3RuntimeValidator implements Validator
{

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public boolean validate(ProcessingClient client,AutomatedInstallData adata)
    {
        boolean bReturn = false;
        try
        {
        
            //String x3runPath = adata.getVariable("syracuse.certificate.x3runtime").trim();
            String x3runPath = client.getFieldContents(0).trim();
            
            File X3runKeys = new File (x3runPath+"/keys");
            if (X3runKeys.exists() && X3runKeys.isDirectory())
            {
                bReturn = true;
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
}
