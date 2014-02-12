/**
 * 
 */
package com.izforge.izpack.util;

import java.io.File;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;


/**
 * @author apozzo
 *
 */
public class X3RuntimeValidator implements DataValidator
{

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public Status validateData(AutomatedInstallData adata)
    {
        Status bReturn = Status.ERROR;
        try
        {
        
            String x3runPath = adata.getVariable("syracuse.certificate.x3runtime").trim();
            
            File X3runKeys = new File (x3runPath+"/keys");
            if (X3runKeys.exists() && X3runKeys.isDirectory())
            {
                bReturn = Status.OK;
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

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getErrorMessageId()
     */
    public String getErrorMessageId()
    {
        // TODO Auto-generated method stub
        return "x3runtimekeysnotexist";
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
