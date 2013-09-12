/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/ http://izpack.codehaus.org/
 * 
 * Copyright 2004 Hani Suleiman
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This is a convienient class, which helps you to detect / identify the running OS/Distribution
 * <p/>
 * Created at: Date: Nov 9, 2004 Time: 8:53:22 PM
 *
 * @author hani, Marc.Eppelmann&#064;reddot.de
 */
public final class OsVersion implements OsVersionConstants, StringConstants
{

    //~ Static fields/initializers
    // *******************************************************************************************************************************

    /**
     * OS_NAME = System.getProperty( "os.name" )
     */
    public static final String OS_NAME = System.getProperty(OSNAME);

    /**
     * OS_ARCH = System.getProperty("os.arch")
     */
    public static final String OS_ARCH = System.getProperty(OSARCH);

    /**
     * True if the processor is in the Intel x86 family.
     */
    public static final boolean IS_X86 = (StringTool.startsWithIgnoreCase(OS_ARCH, X86) && !StringTool.startsWithIgnoreCase(OS_ARCH, X86_64)) ||
            StringTool.startsWithIgnoreCase(OS_ARCH, I386);

    /**
     * True if the processor is in the Intel x86_64 family.
     */
    public static final boolean IS_X86_64 = StringTool.startsWithIgnoreCase(OS_ARCH, X86_64) ||
            StringTool.startsWithIgnoreCase(OS_ARCH, AMD64);

    /**
     * True if the processor is in the PowerPC family.
     */
    public static final boolean IS_PPC = StringTool.startsWithIgnoreCase(OS_ARCH, PPC);

    /**
     * True if the processor is in the SPARC family.
     */
    public static final boolean IS_SPARC = StringTool.startsWithIgnoreCase(OS_ARCH, SPARC);

    /**
     * True if this is FreeBSD.
     */
    public static final boolean IS_FREEBSD = StringTool.startsWithIgnoreCase(OS_NAME, FREEBSD);

    /**
     * True if this is Linux.
     */
    public static final boolean IS_LINUX = StringTool.startsWithIgnoreCase(OS_NAME, LINUX);

    /**
     * True if this is HP-UX.
     */
    public static final boolean IS_HPUX = StringTool.startsWithIgnoreCase(OS_NAME, HP_UX);

    /**
     * True if this is AIX.
     */
    public static final boolean IS_AIX = StringTool.startsWithIgnoreCase(OS_NAME, AIX);

    /**
     * True if this is SunOS.
     */
    public static final boolean IS_SUNOS = StringTool.startsWithIgnoreCase(OS_NAME, SUNOS) ||
            StringTool.startsWithIgnoreCase(OS_NAME, SOLARIS);

    /**
     * True if this is SunOS / x86
     */
    public static final boolean IS_SUNOS_X86 = IS_SUNOS && IS_X86;

    /**
     * True if this is SunOS / sparc
     */
    public static final boolean IS_SUNOS_SPARC = IS_SUNOS && IS_SPARC;

    /**
     * True if this is OS/2.
     */
    public static final boolean IS_OS2 = StringTool.startsWith(OS_NAME, OS_2);

    /**
     * True is this is Mac OS
     */
    public static final boolean IS_MAC = StringTool.startsWith(OS_NAME, MAC);

    /**
     * True if this is the Mac OS X.
     */
    public static final boolean IS_OSX = StringTool.startsWithIgnoreCase(OS_NAME, MACOSX);

    /**
     * True if this is Windows.
     */
    public static final boolean IS_WINDOWS = StringTool.startsWith(OS_NAME, WINDOWS);

    /**
     * True if this is Windows 2000
     */
    public static final boolean IS_WINDOWS_2000 =  StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_2000_NAME); 

    /**
     * True if this is Windows XP
     */
    public static final boolean IS_WINDOWS_XP =  StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_XP_NAME); // IS_WINDOWS && OS_VERSION.equals(WINDOWS_XP_VERSION);

    /**
     * True if this is Windows 2003
     */
    public static final boolean IS_WINDOWS_2003 = StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_2003_NAME); // IS_WINDOWS && OS_VERSION.equals(WINDOWS_2003_VERSION);

    /**
     * True if this is Windows VISTA
     */
    public static final boolean IS_WINDOWS_VISTA = StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_VISTA_NAME); // IS_WINDOWS && OS_VERSION.equals(WINDOWS_VISTA_VERSION);

    /**
     * True if this is Windows 7
     */
    public static final boolean IS_WINDOWS_7 = StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_7_NAME); // IS_WINDOWS && OS_VERSION.equals(WINDOWS_7_VERSION);

    /**
     * True if this is Windows 7
     */
    public static final boolean IS_WINDOWS_8 = StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_8_NAME); 

    /**
     * True if this is some variant of Unix (OSX, Linux, Solaris, FreeBSD, etc).
     */
    public static final boolean IS_UNIX = !IS_OS2 && !IS_WINDOWS;

    /**
     * True if CentOS Linux was detected
     */
    public static final boolean IS_CENTOS_LINUX = IS_LINUX
            && FileUtil.fileContains(getReleaseFileName(), CENTOS);

    /**
     * True if RedHat Linux was detected
     */
    public static final boolean IS_REDHAT_LINUX = IS_LINUX
            && !IS_CENTOS_LINUX && (IS_LINUX && !new File("/etc/oracle-release").exists()) && ((FileUtil.fileContains("/etc/redhat-release", REDHAT) || FileUtil.fileContains("/etc/redhat-release",
            RED_HAT)));

    /**
     * True if Fedora Linux was detected
     */
    public static final boolean IS_FEDORA_LINUX = IS_LINUX
            && FileUtil.fileContains("/etc/fedora-release", FEDORA);

    /**
     * True if Ubuntu Linux was detected
     */
    public static final boolean IS_UBUNTU_LINUX = IS_LINUX
            && FileUtil.fileContains("/etc/lsb-release", UBUNTU);

    /**
     * True if Oracle Linux was detected
     */
    public static final boolean IS_ORACLE_LINUX = IS_LINUX
            && ((FileUtil.fileContains("/etc/oracle-release", ORACLE) || FileUtil.fileContains("/etc/oracle-release", EL)));

    /**
     * True if Mandriva(Mandrake) Linux was detected
     */
    public static final boolean IS_MANDRAKE_LINUX = IS_LINUX
            && FileUtil.fileContains("/etc/mandrake-release", MANDRAKE);

    /**
     * True if Amazon AMI Linux was detected
     */
    public static final boolean IS_AMI_LINUX = IS_LINUX
            && FileUtil.fileContains(getReleaseFileName(), AMI);

    /**
     * True if Mandrake/Mandriva Linux was detected
     */
    public static final boolean IS_MANDRIVA_LINUX = (IS_LINUX
            && FileUtil.fileContains(getReleaseFileName(), MANDRIVA)) || IS_MANDRAKE_LINUX;

    /**
     * True if SuSE Linux was detected
     */
    public static final boolean IS_SUSE_LINUX = IS_LINUX
            && FileUtil.fileContains(getReleaseFileName(), SUSE, true);  /*  caseInsensitive , since 'SUSE' 10 */

    /**
     * True if Debian Linux or derived was detected
     */
    public static final boolean IS_DEBIAN_LINUX = (IS_LINUX
            && FileUtil.fileContains(PROC_VERSION, DEBIAN)) || (IS_LINUX && new File("/etc/debian_version").exists());
    
    /**
     * OS_VERSION = System.getProperty("os.version")
     */
    public static final String OS_VERSION = (IS_LINUX)?getLinuxversion():System.getProperty(OSVERSION);

    /**
     * True if this is Windows 2008
     */
    public static final boolean IS_WINDOWS_2008 = StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_2008_NAME) && StringTool.equalsWithIgnoreCase(OS_VERSION, WINDOWS_2008_VERSION); 

    /**
     * True if this is Windows 2008R2
     */
    public static final boolean IS_WINDOWS_2008R2 = StringTool.startsWithIgnoreCase(OS_NAME, WINDOWS_2008R2_NAME) && StringTool.equalsWithIgnoreCase(OS_VERSION, WINDOWS_2008R2_VERSION); 

    

    // TODO detect the newcomer (K)Ubuntu */
    //~ Methods
    // **************************************************************************************************************************************************

    /**
     * Gets the etc Release Filename
     *
     * @return name of the file the release info is stored in for Linux distributions
     */
    private static String getReleaseFileName()
    {
        String result = "";

        File[] etcList = new File("/etc").listFiles();

        if (etcList != null)
        {
            for (File etcEntry : etcList)
            {
                if (etcEntry.isFile())
                {
                    if (etcEntry.getName().endsWith("-release"))
                    {
                        //match :-)
                        return result = etcEntry.toString();
                    }
                }
            }
        }

        return result;
    }

    
    private static String getLinuxVersionFromFile (String strReleaseFile)
    {
        String result = null;
        
        try
        {
            ArrayList lstLines = FileUtil.getFileContent(strReleaseFile);
            
            Iterator linesIter = lstLines.iterator();

            if (linesIter.hasNext())
            {
                    String strline = (String)linesIter.next();
                    
                    String[] strPattern =  strline.split(" ");
                    
                    for (int i=0; i<strPattern.length;i++)
                    {
                        try
                        {
                            result = Float.valueOf(strPattern[i]).toString();
                            break;
                        }
                        catch (Exception ex)
                        {
                            //ex.prin
                        }
                    }
            }
        }
        catch (Exception e1)
        {
            // TODO handle Exception
            e1.printStackTrace();

        }
        
        
        return result;
    }
    
    private static String getLinuxversion()
    {
        String result = null;

        if (IS_SUSE_LINUX)
        {
                result = getLinuxVersionFromFile ("/etc/sles-release");
                if (result==null) result = getLinuxVersionFromFile ("/etc/novell-release");
                if (result==null) result = getLinuxVersionFromFile ("/etc/SuSE-release");
        }
        else if (IS_UBUNTU_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/lsb-release");
        }
        else if (IS_REDHAT_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/redhat-release");
        }
        else if (IS_ORACLE_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/oracle-release");
        }
        else if (IS_FEDORA_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/fedora-release");
        }
        else if (IS_MANDRAKE_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/mandrake-release");
        }
        else if (IS_MANDRIVA_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/mandriva-release");
            if (result==null) result = getLinuxVersionFromFile ("/etc/mandrake-release");
            if (result==null) result = getLinuxVersionFromFile ("/etc/mandrakelinux-release");
        }
        else if (IS_DEBIAN_LINUX)
        {
            result = getLinuxVersionFromFile ("/etc/debian_version");
        }
        else
        {
            result = getLinuxVersionFromFile (getReleaseFileName());
        }

        return result;
    }

    
    
    /**
     * Gets the Details of a Linux Distribution
     *
     * @return description string of the Linux distribution
     */
    public static String getLinuxDistribution()
    {
        String result = null;

        if (IS_SUSE_LINUX)
        {
            try
            {
                result = SUSE + SP + LINUX + NL + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_UBUNTU_LINUX)
        {
            try
            {
                result = (String)FileUtil.getFileContent(getReleaseFileName()).get(3);
                result = result.split("\"")[1];
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_REDHAT_LINUX)
        {
            try
            {
                result = REDHAT + SP + LINUX + NL + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_ORACLE_LINUX)
        {
            try
            {
                result = ORACLE + SP + LINUX + NL + StringTool.stringArrayListToString(FileUtil.getFileContent("/etc/oracle-release"));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_FEDORA_LINUX)
        {
            try
            {
                result = FEDORA + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_MANDRAKE_LINUX)
        {
            try
            {
                result = MANDRAKE + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_MANDRIVA_LINUX)
        {
            try
            {
                result = MANDRIVA + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else if (IS_DEBIAN_LINUX)
        {
            try
            {
                result = DEBIAN + SP + LINUX + NL
                        + StringTool.stringArrayListToString(FileUtil.getFileContent("/etc/debian_version"));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }
        else
        {
            try
            {
                result = "Unknown Linux Distribution\n"
                        + StringTool.stringArrayListToString(FileUtil.getFileContent(getReleaseFileName()));
            }
            catch (IOException e)
            {
                // TODO ignore
            }
        }

        return result;
    }

    /**
     * returns a String which contains details of known OSs
     *
     * @return the details
     */
    public static String getOsDetails()
    {
        StringBuffer result = new StringBuffer();
        result.append("OS_NAME=").append(OS_NAME).append(NL);

        if (IS_UNIX)
        {
            if (IS_LINUX)
            {
                result.append(getLinuxDistribution()).append(NL);
            }
            else
            {
                try
                {
                    result.append(FileUtil.getFileContent(getReleaseFileName())).append(NL);
                }
                catch (IOException e)
                {
                    Debug.log("Unable to get release file contents in 'getOsDetails'.");
                }
            }
        }

        if (IS_WINDOWS)
        {
            result.append(System.getProperty(OSNAME)).append(SP).append(System.getProperty("sun.os.patch.level", "")).append(NL);
        }
        return result.toString();
    }

    /**
     * Testmain
     *
     * @param args Commandline Args
     */
    public static void main(String[] args)
    {
        System.out.println(getOsDetails());
    }
}
