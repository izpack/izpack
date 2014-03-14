package com.izforge.izpack.util;

import java.util.List;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.os.MoreAdvApi32;
import com.mongodb.MongoClient;
import com.sun.jna.LastErrorException;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;

public class WinServiceUserValidator implements DataValidator
{
    private final static String errLogon = "errwinsrvaccount";
    private final static String errLogonService = "warnwinsrvaccount";
    private final static String warnLogonServiceChanged = "warnLogonServiceChanged";
    private String errorMsg = errLogon;

    public Status validateData(AutomatedInstallData adata)
    {
        if (!OsVersion.IS_WINDOWS) return Status.OK;
        
        Status bReturn = Status.ERROR;
        try
        {
        
            String userName = adata.getVariable("syracuse.winservice.username");
            String passWord = adata.getVariable("syracuse.winservice.password");
            String strDomain = ".";
            String bUseDomain = "true";
            
            // check domain
            if (userName.contains("\\"))
            {
                strDomain = userName.substring(0, userName.indexOf("\\"));
                userName = userName.substring(userName.indexOf("\\")+1);
            }
            else if (userName.contains("@"))
            {
                strDomain = null;
            }
            else
            {
                // local database
                bUseDomain = "false";
            }
            
            
            HANDLEByReference phToken = new  HANDLEByReference();

            if (! Advapi32.INSTANCE.LogonUser(userName, strDomain, passWord, WinBase.LOGON32_LOGON_NETWORK, WinBase.LOGON32_PROVIDER_DEFAULT, phToken))
            {
                throw new LastErrorException(Kernel32.INSTANCE.GetLastError());
            }

/*            // just for testing
 * 
 * WARN : DOES NOT WORK !
            WString nullW = null;
            PROCESS_INFORMATION processInformation = new PROCESS_INFORMATION();
            STARTUPINFO startupInfo = new STARTUPINFO();
            
            boolean result1 = MoreAdvApi32.INSTANCE.CreateProcessWithTokenW
                    (phToken.getValue(), 
                    MoreAdvApi32.LOGON_NETCREDENTIALS_ONLY,                 // dwLogonFlags
                    nullW,                                           // lpApplicationName
                    new WString("c:\\temp\\test.cmd bob1.log"),   // command line
                    MoreAdvApi32.CREATE_NEW_CONSOLE,                 // dwCreationFlags
                    null,                                            // lpEnvironment
                    new WString("C:\\temp"),                   // directory
                    startupInfo,
                    processInformation);
            

            if (!result1) {
                int error = Kernel32.INSTANCE.GetLastError();
                System.out.println("OS error #" + error);
                System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));
              }
            
*/            
            Kernel32.INSTANCE.CloseHandle(phToken.getValue());

/*
 * OK !!
 *             
            // just for testing
            nullW = null;
            processInformation = new PROCESS_INFORMATION();
            startupInfo = new STARTUPINFO();
            boolean result2 = MoreAdvApi32.INSTANCE.CreateProcessWithLogonW
               (new WString(userName),                         // user
                (strDomain==null)?nullW:new WString(strDomain),                                           // domain , null if local
                new WString(passWord),                         // password
                MoreAdvApi32.LOGON_NETCREDENTIALS_ONLY,                 // dwLogonFlags
                nullW,                                           // lpApplicationName
                new WString("c:\\temp\\test.cmd bob2.log"),   // command line
                MoreAdvApi32.CREATE_NEW_CONSOLE,                 // dwCreationFlags
                null,                                            // lpEnvironment
                new WString("C:\\temp"),                   // directory
                startupInfo,
                processInformation);

                if (!result2) {
                  int error = Kernel32.INSTANCE.GetLastError();
                  System.out.println("OS error #" + error);
                  System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));
                }
            
*/            
            
            
            if (! Advapi32.INSTANCE.LogonUser(userName, strDomain, passWord, WinBase.LOGON32_LOGON_SERVICE, WinBase.LOGON32_PROVIDER_DEFAULT, phToken))
            {
                bReturn = Status.ERROR;
                errorMsg = errLogonService;
                return bReturn;
            }
            else
            {
                bReturn = Status.OK;
            }

            Kernel32.INSTANCE.CloseHandle(phToken.getValue());
            //userName = strDomain + "\\" + userName;
            //adata.setVariable("syracuse.winservice.username", userName);
            adata.setVariable("syracuse.winservice.usedomain", bUseDomain);
            if (strDomain != null && ".".equals(strDomain))
            {
                // local database
                adata.setVariable("syracuse.winservice.username", userName);
            }
            
//            // test for old value in update mode ?
//            Boolean modifyinstallation = Boolean.valueOf(adata.getVariable(InstallData.MODIFY_INSTALLATION));
//            if (modifyinstallation)
//            {
//                String olduserName = adata.getVariable("syracuse.winservice.username.oldvalue");
//                
//                if (!userName.equalsIgnoreCase(olduserName))
//                {
//                    bReturn = Status.WARNING;
//                    errorMsg = warnLogonServiceChanged;
//                }
//                
//            }
            
            
            

        }
        catch (Exception ex)
        {
            Debug.trace(ex.getMessage());
            bReturn = Status.ERROR; 
        }

        return bReturn;
    }

    public String getErrorMessageId()
    {
        return errorMsg;
    }

    public String getWarningMessageId()
    {
        return errorMsg;
    }

    public boolean getDefaultAnswer()
    {
        // by default assume provided account is valid
        return true;
    }

}
