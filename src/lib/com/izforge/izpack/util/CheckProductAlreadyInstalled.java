package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.util.os.RegistryHandler;
import com.sun.jna.platform.win32.Advapi32Util;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;


public class CheckProductAlreadyInstalled implements DataValidator
{

    private static final String SPEC_FILE_NAME = "productsSpec.txt";
    
    protected String errMessage = "";
    protected String warnMessage = "";


    public Status validateData(AutomatedInstallData adata)
    {
        // open an input stream
        InputStream input = null;

        try
        {
            input = ResourceManager.getInstance().getInputStream(SPEC_FILE_NAME);
            
            if (input == null)
            {
                // spec file is missing
                errMessage = "specFileMissing";
                return Status.ERROR;
            }
            else
            {
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) 
                {
                    
                    line=line.trim(); //
                    
                    if (Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, "SOFTWARE\\Wow6432Node\\Sage\\"+line) 
                            || Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, "SOFTWARE\\Sage\\"+line))
                    {                        
                        errMessage = String.format(adata.langpack.getString("errIsProductFound"), line);
                        return Status.ERROR;
                    }
                    else if (Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, RegistryHandler.UNINSTALL_ROOT+line))
                    {
                        warnMessage = String.format(adata.langpack.getString("compFoundAskUpdate"), line);
                        String oldInstallPath = Advapi32Util.registryGetStringValue(HKEY_LOCAL_MACHINE, RegistryHandler.UNINSTALL_ROOT+line, "DisplayIcon");
                        oldInstallPath = oldInstallPath.substring(0, oldInstallPath.indexOf("Uninstaller")-1);
                        adata.setInstallPath(oldInstallPath);
                        Debug.trace("modification installation");
                        Debug.trace("old path applied :"+oldInstallPath);
                        adata.setVariable(InstallData.MODIFY_INSTALLATION, "true");
                        
                        return Status.WARNING;
                    }
                }
                reader.close();
            }
            
        }
        catch (Exception ex)
        {
            errMessage = ex.getMessage();
            return Status.ERROR;
        }

        return Status.OK;
    }

    public String getErrorMessageId()
    {
        return errMessage;
    }

    public String getWarningMessageId()
    {
        return warnMessage;
    }

    public boolean getDefaultAnswer()
    {
        // unfortunately we can't say yes by default
        return false;
    }

}
