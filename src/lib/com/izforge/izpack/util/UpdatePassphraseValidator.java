/**
 * 
 */
package com.izforge.izpack.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.util.os.MoreAdvApi32;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;
import com.sun.jna.platform.win32.WinDef.WORD;


/**
 * @author apozzo
 *
 */
public class UpdatePassphraseValidator implements DataValidator
{
    
    private String strMessage = "";
    public static final String strMessageId = "messageid";
    public static final String strMessageValue = "message.oldvalue"; // not to be stored

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public Status validateData(AutomatedInstallData adata)
    {
        Status sreturn = Status.OK;
        
        if (OsVersion.IS_WINDOWS)
        {
        
            String userName = adata.getVariable("syracuse.winservice.username");
            String passWord = adata.getVariable("syracuse.winservice.password");
            String strDomain = ".";
            
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
            
            
            WString nullW = null;
            PROCESS_INFORMATION processInformation = new PROCESS_INFORMATION();
            STARTUPINFO startupInfo = new STARTUPINFO();
            startupInfo.dwFlags = 1;
            startupInfo.wShowWindow = new WORD(0);
            
            
            String strPassphrasePath = adata.getVariable("INSTALL_PATH")+"\\syracuse"; //${INSTALL_PATH}${FILE_SEPARATOR}syracuse
            String strServerPassphrase = adata.getVariable("syracuse.certificate.serverpassphrase"); //syracuse.certificate.serverpassphrase
            
            try
            {
                File tempFile = new File (strPassphrasePath+"\\tmpcmd.cmd");
                tempFile.deleteOnExit();
                PrintWriter printWriter = new PrintWriter(new FileOutputStream(tempFile), true);
                printWriter.println ("ping -n 10 127.0.0.1>NULL");
                printWriter.println ("call \""+strPassphrasePath+"\\passphrase.cmd\" "+"\""+strServerPassphrase+"\" ");
                //printWriter.println ("c:\\UnxUtils\\usr\\local\\wbin\\sleep.exe 100");
                printWriter.close ();
        
                boolean result2 = MoreAdvApi32.INSTANCE.CreateProcessWithLogonW
                   (new WString(userName),                         // user
                    (strDomain==null)?nullW:new WString(strDomain),                                           // domain , null if local
                    new WString(passWord),                         // password
                    MoreAdvApi32.LOGON_WITH_PROFILE,                 // dwLogonFlags
                    nullW,                                           // lpApplicationName
                    new WString(tempFile.getCanonicalPath()),   // command line
                    //new WString("c:\\UnxUtils\\usr\\local\\wbin\\sleep.exe 100"),   // command line
                    MoreAdvApi32.CREATE_UNICODE_ENVIRONMENT,                 // dwCreationFlags
                    null,                                            // lpEnvironment
                    new WString(strPassphrasePath),                   // directory
                    startupInfo,
                    processInformation);
        
                    if (!result2) 
                    {
                      int error = Kernel32.INSTANCE.GetLastError();
                      //System.out.println("OS error #" + error);
                      //System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));
                      
                      strMessage = "OS error #" + error + " - " + Kernel32Util.formatMessageFromLastErrorCode(error);
                      
                      adata.setVariable(strMessageValue, strMessage);
                      sreturn = Status.WARNING;
                      
                    }
            }
            catch (Exception ex)
            {
                
            }
        }
        
        
        
        return sreturn;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getErrorMessageId()
     */
    public String getErrorMessageId()
    {
        
        return strMessageId;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getWarningMessageId()
     */
    public String getWarningMessageId()
    {
        
        return strMessageId;
    }

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#getDefaultAnswer()
     */
    public boolean getDefaultAnswer()
    {
        // by default continue
        return true;
    }

}
