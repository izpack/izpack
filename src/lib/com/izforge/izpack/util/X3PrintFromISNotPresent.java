package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.sun.jna.platform.win32.Advapi32Util;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

public class X3PrintFromISNotPresent implements DataValidator
{

    public Status validateData(AutomatedInstallData adata)
    {
        // we must check for "HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\Sage\Safe X3 SrvEdt V1"
        if (Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, "SOFTWARE\\Wow6432Node\\Sage\\Safe X3 SrvEdt V1") 
                || Advapi32Util.registryKeyExists(HKEY_LOCAL_MACHINE, "SOFTWARE\\Sage\\Safe X3 SrvEdt V1"))
        {
            // key seems to exist
            // we cannot continue !
            return Status.ERROR;
        }
        
        return Status.OK;
    }

    public String getErrorMessageId()
    {
        // Print server already installed with IS
        return "errprintispresent";
    }

    public String getWarningMessageId()
    {
        return null;
    }

    public boolean getDefaultAnswer()
    {
        // we cannot accept to continue by default !
        return false;
    }

}
