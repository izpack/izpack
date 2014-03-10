/**
 * 
 */
package com.izforge.izpack.util.os;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION;
import com.sun.jna.platform.win32.WinBase.STARTUPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;

/**
 * @author apozzo
 *   // http://msdn.microsoft.com/en-us/library/windows/desktop/ms682431%28v=vs.85%29.aspx
 */

public interface MoreAdvApi32 extends Advapi32 {
  MoreAdvApi32 INSTANCE =
        (MoreAdvApi32) Native.loadLibrary("AdvApi32", MoreAdvApi32.class);

  /*
   * 
   * BOOL WINAPI CreateProcessWithLogonW(
   *   _In_         LPCWSTR lpUsername,
   *   _In_opt_     LPCWSTR lpDomain,
   *   _In_         LPCWSTR lpPassword,
   *   _In_         DWORD dwLogonFlags,
   *   _In_opt_     LPCWSTR lpApplicationName,
   *   _Inout_opt_  LPWSTR lpCommandLine,
   *   _In_         DWORD dwCreationFlags,
   *   _In_opt_     LPVOID lpEnvironment,
   *   _In_opt_     LPCWSTR lpCurrentDirectory,
   *   _In_         LPSTARTUPINFOW lpStartupInfo,
   *   _Out_        LPPROCESS_INFORMATION lpProcessInfo
   * );
  */

  boolean CreateProcessWithLogonW
            (WString lpUsername,
             WString lpDomain,
             WString lpPassword,
             int dwLogonFlags,
             WString lpApplicationName,
             WString lpCommandLine,
             int dwCreationFlags,
             Pointer lpEnvironment,
             WString lpCurrentDirectory,
             STARTUPINFO  lpStartupInfo,
             PROCESS_INFORMATION lpProcessInfo);

  public static final int LOGON_WITH_PROFILE          = 0x00000001;
  public static final int LOGON_NETCREDENTIALS_ONLY   = 0x00000002;


  int CREATE_NO_WINDOW            = 0x08000000;
  int CREATE_UNICODE_ENVIRONMENT  = 0x00000400;
  int CREATE_NEW_CONSOLE          = 0x00000010;
  int DETACHED_PROCESS            = 0x00000008;
  
  
/*  BOOL WINAPI CreateProcessWithTokenW(
 *         _In_         HANDLE hToken,
 *         _In_         DWORD dwLogonFlags,
 *         _In_opt_     LPCWSTR lpApplicationName,
 *         _Inout_opt_  LPWSTR lpCommandLine,
 *         _In_         DWORD dwCreationFlags,
 *         _In_opt_     LPVOID lpEnvironment,
 *         _In_opt_     LPCWSTR lpCurrentDirectory,
 *         _In_         LPSTARTUPINFOW lpStartupInfo,
 *         _Out_        LPPROCESS_INFORMATION lpProcessInfo
 *       );
 */ 
  
  boolean CreateProcessWithTokenW
              (HANDLE hToken,
               int dwLogonFlags,
               WString lpApplicationName,
               WString lpCommandLine,
               int dwCreationFlags,
               Pointer lpEnvironment,
               WString lpCurrentDirectory,
               STARTUPINFO  lpStartupInfo,
               PROCESS_INFORMATION lpProcessInfo);
  
  
/*
 * 
 * Logon Type
(LOGON32_LOGON_xxx)
    Integer Value
(From WinBase.h)    Associated Rights
(From NTSecAPI.h)   Description

BATCH   4   SeBatchLogonRight / SeDenyBatchLogonRight   
Perform a batch logon.  This is intended for servers where logon performance is vital.  LogonUser will not cache credentials for a user logged in with this type.

INTERACTIVE     2   SeInteractiveLogonRight / SeDenyInteractiveLogonRight     
Log a user into the computer if they will be interactively using the machine (for instance if your code is going to provide them with a shell).  LogonUser will cache credentials for a user logged in with this type.

NETWORK     3   SeBatchNetworkRight / SeDenyNetworkLogonRight     
Similar to batch logon in that this logon type is intended for servers where logon performance is important.  Credentials will not be cached, and the token returned is not a primary token but an impersonation token instead.  It can be converted to a primary token with DuplicateHandle.

NETWORK_CLEARTEXT   8   SeBatchNetworkRight / SeDenyNetworkLogonRight     
Similar to a network logon, except that the credentials are stored with the authentication package that validates the user.  This logon type is only available with the WINNT50 logon provider, and on Windows 2000 or higher.

NEW_CREDENTIALS     9   SeBatchNetworkRight / SeDenyNetworkLogonRight     
Create a new set of credentials for network connections.  (For instance runas /netonly).  This logon type is only available with the WINNT50 logon provider and on Windows 2000 or higher.

SERVICE     5   SeServiceLogonRight / SeDenyServiceLogonRight     Logon as a service.

UNLOCK  7       Reserved for use by GINA implementations.
 * 
 * 
 */
 
  
  
}