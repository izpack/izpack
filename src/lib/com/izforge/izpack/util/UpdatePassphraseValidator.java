/**
 * 
 */
package com.izforge.izpack.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.os.MoreAdvApi32;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.ptr.IntByReference;


/**
 * @author apozzo
 *
 */
public class UpdatePassphraseValidator implements DataValidator
{
    
    private String strMessage = "";
    public static final String strMessageId = "messageid";
    public static final String strMessageValue = "message.oldvalue"; // not to be stored
    public static final int STILL_ACTIVE = 259;

    /* (non-Javadoc)
     * @see com.izforge.izpack.installer.DataValidator#validateData(com.izforge.izpack.installer.AutomatedInstallData)
     */
    public Status validateData(AutomatedInstallData adata)
    {
        Status sreturn = Status.OK;

        boolean updateMode = (adata.getVariable("MODIFY.IZPACK.INSTALL")=="true");
        boolean createCertificate = (adata.getVariable("syracuse.certificate.install")=="true");
        
        
        if (OsVersion.IS_WINDOWS && updateMode && createCertificate)
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
            String strCertsDir = adata.getVariable("syracuse.dir.certs"); // syracuse.dir.certs
            String strHOST_NAME = adata.getVariable("HOST_NAME");
            String strPassPhraseFile = strCertsDir+"\\"+strHOST_NAME+"\\"+strHOST_NAME+".pwd";
            
            try
            {
                
                //delete old passphrase ?
                File oldPassphrase = new File (strPassPhraseFile);
                if (oldPassphrase.exists() && !oldPassphrase.delete()) throw new Exception(strPassPhraseFile);

                
                File tempFile = new File(strPassphrasePath+"\\tmpcmd.cmd");
                tempFile.deleteOnExit();
                PrintWriter printWriter = new PrintWriter(new FileOutputStream(tempFile), true);
                printWriter.println ("ping -n 5 127.0.0.1>NUL");
                
                printWriter.println ("\""+strPassphrasePath+"\\passphrase.cmd\" \""+strServerPassphrase+"\" 1>out.log 2>err.log");
                printWriter.println ("if errorlevel 1 exit /B 1");
                printWriter.close ();
                String strcommand = "/C /E:ON \""+tempFile.getCanonicalPath()+"\" \""+strServerPassphrase+"\"";
        
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
                      return Status.WARNING;
                      
                    }
                    
                    // join the process ?
                    boolean bFinished = false;
                    int loop = 0;
                    
                    while (!bFinished)
                    {    
                    
                        IntByReference lpExitCode = new IntByReference(9999);
                        result2 = Kernel32.INSTANCE.GetExitCodeProcess(processInformation.hProcess, lpExitCode) ; 
    
                        if (!result2) 
                        {
                          int error = Kernel32.INSTANCE.GetLastError();
                          //System.out.println("OS error #" + error);
                          //System.out.println(Kernel32Util.formatMessageFromLastErrorCode(error));
                          
                          strMessage = "OS error #" + error + " - " + Kernel32Util.formatMessageFromLastErrorCode(error);
                          
                          adata.setVariable(strMessageValue, strMessage);
                          return Status.WARNING;
                          
                        }
    
                        if (lpExitCode.getValue() != STILL_ACTIVE)
                        {
                            // process has finished
                            // how to interpret exit code ?
                            int nexitCode = lpExitCode.getValue(); 
                            bFinished = true;
                        }
                        
                        loop+=1;
                        
                        if (loop > 30)
                        {
                            // more than 30 s !
                            strMessage = "Error # Could not update passphrase ! ("+strPassPhraseFile+")";
                            
                            adata.setVariable(strMessageValue, strMessage);
                            return Status.WARNING;
                            
                        }
                        
                        // process is not finished
                        // wait a little
                        Thread.sleep(1000);
                    }
                    
                    // read err and out
                    String strErr = new String(Files.readAllBytes(Paths.get(strPassphrasePath+"\\err.log")));
                    File strOutFile = new File (strPassphrasePath+"\\out.log");
                    File strErrFile = new File (strPassphrasePath+"\\err.log");
                    
                    strOutFile.delete ();
                    strErrFile.delete ();
                    
                    // test for passphrase ?
                    if (!oldPassphrase.exists())
                    {
                        strMessage = "Error # Passphrase update failed ! ("+strPassPhraseFile+")\r\n"+strErr;
                        
                        adata.setVariable(strMessageValue, strMessage);
                        return Status.WARNING;
                    }
                    
                    
            }
            catch (Exception ex)
            {
                Debug.trace(ex);
                Debug.trace(ex.getMessage());
                
                strMessage = "OS error #" + ex.getMessage();
                
                adata.setVariable(strMessageValue, strMessage);
                sreturn = Status.WARNING;
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
