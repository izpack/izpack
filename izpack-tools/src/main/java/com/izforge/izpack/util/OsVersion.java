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

import static com.izforge.izpack.util.Platform.Arch;
import static com.izforge.izpack.util.Platform.Name;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a convienient class, which helps you to detect / identify the running OS/Distribution
 * <p/>
 * Created at: Date: Nov 9, 2004 Time: 8:53:22 PM
 *
 * @author hani, Marc.Eppelmann&#064;reddot.de
 */
public final class OsVersion implements OsVersionConstants, StringConstants
{
    private static final Logger LOGGER = Logger.getLogger(OsVersion.class.getName());

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
     * OS_VERSION = System.getProperty("os.aversion")
     */
    public static final String OS_VERSION = System.getProperty(OSVERSION);

    /**
     * The current platform.
     */
    public static final Platform PLATFORM = new Platforms().getCurrentPlatform(OS_NAME, OS_ARCH, OS_VERSION);

    /**
     * True if the processor is in the Intel x86 family. Also true if you're running
     * a x86 JVM on an amd64 CPU. 
     */
    public static final boolean IS_X86 = PLATFORM.isA(Arch.X86);

    /**
     * True if the processor is in the AMD64 family AND you're running an x64 JVM.
     */
    public static final boolean IS_X64 = PLATFORM.isA(Arch.X64);

    /**
     * True if the processor is in the PowerPC family.
     */
    public static final boolean IS_PPC = PLATFORM.isA(Arch.PPC);

    /**
     * True if the processor is in the SPARC family.
     */
    public static final boolean IS_SPARC = PLATFORM.isA(Arch.SPARC);

    /**
     * True if this is FreeBSD.
     */
    public static final boolean IS_FREEBSD = PLATFORM.isA(Name.FREEBSD);

    /**
     * True if this is Linux.
     */
    public static final boolean IS_LINUX = PLATFORM.isA(Name.LINUX);

    /**
     * True if this is HP-UX.
     */
    public static final boolean IS_HPUX = PLATFORM.isA(Name.HP_UX);

    /**
     * True if this is AIX.
     */
    public static final boolean IS_AIX = PLATFORM.isA(Name.AIX);

    /**
     * True if this is SunOS.
     */
    public static final boolean IS_SUNOS = PLATFORM.isA(Name.SUNOS);

    /**
     * True if this is SunOS / x86
     */
    public static final boolean IS_SUNOS_X86 = PLATFORM.isA(Platforms.SUNOS_X86);

    /**
     * True if this is SunOS / sparc
     */
    public static final boolean IS_SUNOS_SPARC = PLATFORM.isA(Platforms.SUNOS_SPARC);

    /**
     * True if this is OS/2.
     */
    public static final boolean IS_OS2 = PLATFORM.isA(Name.OS_2);

    /**
     * True is this is Mac OS
     */
    public static final boolean IS_MAC = PLATFORM.isA(Name.MAC);

    /**
     * True if this is the Mac OS X.
     */
    public static final boolean IS_OSX = PLATFORM.isA(Name.MAC_OSX);

    /**
     * True if this is Windows.
     */
    public static final boolean IS_WINDOWS = PLATFORM.isA(Platforms.WINDOWS);

    /**
     * True if this is Windows XP
     */
    public static final boolean IS_WINDOWS_XP = PLATFORM.isA(Platforms.WINDOWS_XP);

    /**
     * True if this is Windows 2003
     */
    public static final boolean IS_WINDOWS_2003 = PLATFORM.isA(Platforms.WINDOWS_2003);

    /**
     * True if this is Windows VISTA
     */
    public static final boolean IS_WINDOWS_VISTA = PLATFORM.isA(Platforms.WINDOWS_VISTA);

    /**
     * True if this is Windows 7
     */
    public static final boolean IS_WINDOWS_7 = PLATFORM.isA(Platforms.WINDOWS_7);

    /**
     * True if this is some variant of Unix (OSX, Linux, Solaris, FreeBSD, etc).
     */
    public static final boolean IS_UNIX = PLATFORM.isA(Name.UNIX);

    /**
     * True if RedHat Linux was detected
     */
    public static final boolean IS_REDHAT_LINUX = PLATFORM.isA(Name.RED_HAT_LINUX);

    /**
     * True if Fedora Linux was detected
     */
    public static final boolean IS_FEDORA_LINUX = PLATFORM.isA(Name.FEDORA_LINUX);

    /**
     * True if Mandriva(Mandrake) Linux was detected
     */
    public static final boolean IS_MANDRAKE_LINUX = PLATFORM.isA(Name.MANDRAKE_LINUX);

    /**
     * True if Mandrake/Mandriva Linux was detected
     */
    public static final boolean IS_MANDRIVA_LINUX = PLATFORM.isA(Name.MANDRIVA_LINUX) || IS_MANDRAKE_LINUX;

    /**
     * True if SuSE Linux was detected
     */
    public static final boolean IS_SUSE_LINUX = PLATFORM.isA(Name.SUSE_LINUX);

    /**
     * True if Debian Linux or derived was detected
     */
    public static final boolean IS_DEBIAN_LINUX = PLATFORM.isA(Name.DEBIAN_LINUX);

    /** 
     * True if Ubuntu Linux or derived was detected
     */ 
    public static final boolean IS_UBUNTU_LINUX = PLATFORM.isA(Name.UBUNTU_LINUX);

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
                        return etcEntry.toString();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the Details of a Linux Distribution
     *
     * @return description string of the Linux distribution
     */
    private static String getLinuxDistribution()
    {
        String result = null;

        if (IS_SUSE_LINUX)
        {
            try
            {
                result = SUSE + SP + LINUX + NL + StringTool.listToString(FileUtil.getFileContent(getReleaseFileName()));
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
                result = REDHAT + SP + LINUX + NL + StringTool.listToString(FileUtil.getFileContent(getReleaseFileName()));
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
                        + StringTool.listToString(FileUtil.getFileContent(getReleaseFileName()));
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
                        + StringTool.listToString(FileUtil.getFileContent(getReleaseFileName()));
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
                        + StringTool.listToString(FileUtil.getFileContent(getReleaseFileName()));
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
                        + StringTool.listToString(FileUtil.getFileContent("/etc/debian_version"));
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
                        + StringTool.listToString(FileUtil.getFileContent(getReleaseFileName()));
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
                    LOGGER.log(Level.INFO,"Unable to get release file contents in 'getOsDetails'.");
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
