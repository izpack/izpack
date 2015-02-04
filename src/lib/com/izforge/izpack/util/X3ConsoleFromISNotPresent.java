package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.sun.jna.platform.win32.Advapi32Util;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;


public class X3ConsoleFromISNotPresent implements DataValidator
{

    public Status validateData(AutomatedInstallData adata)
    {
        // we must check for "HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Sage\Safe X3 Console V1"
        if (Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, "SOFTWARE\\Wow6432Node\\Sage\\Safe X3 Console V1") 
                || Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, "SOFTWARE\\Sage\\Safe X3 Console V1"))
        {
            // key seems to exist
            // we cannot continue !
            return Status.ERROR;
        }
        
        return Status.OK;
    }

    public String getErrorMessageId()
    {
        // Console already installed with IS
        return "errIsConsolePresent";
    }

    public String getWarningMessageId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getDefaultAnswer()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
