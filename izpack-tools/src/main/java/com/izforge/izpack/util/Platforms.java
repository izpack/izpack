/*
 * IzPack - Copyright 2001-2011 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2011 Tim Anderson
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Factory for {@link Platform} instances.
 *
 * @author Tim Anderson
 */
public class Platforms
{

    /**
     * AIX platform.
     */
    public static Platform AIX = new Platform(Name.AIX);

    /**
     * Debian Linux platform.
     */
    public static Platform DEBIAN_LINUX = new Platform(Name.DEBIAN_LINUX);

    /**
     * Fedora Linux platform.
     */
    public static Platform FEDORA_LINUX = new Platform(Name.FEDORA_LINUX);

    /**
     * FreeBSD platform.
     */
    public static Platform FREEBSD = new Platform(Name.FREEBSD);

    /**
     * HP/UX platform.
     */
    public static Platform HP_UX = new Platform(Name.HP_UX);

    /**
     * Generic Linux platform.
     */
    public static Platform LINUX = new Platform(Name.LINUX);

    /**
     * Mac platform.
     */
    public static Platform MAC = new Platform(Name.MAC);

    /**
     * Mac OSX platform.
     */
    public static Platform MAC_OSX = new Platform(Name.MAC_OSX);

    /**
     * Mandrake Linux platform.
     */
    public static Platform MANDRAKE_LINUX = new Platform(Name.MANDRAKE_LINUX);

    /**
     * Mandriva Linux platform.
     */
    public static Platform MANDRIVA_LINUX = new Platform(Name.MANDRIVA_LINUX);

    /**
     * OS/2 platform.
     */
    public static Platform OS_2 = new Platform(Name.OS_2);

    /**
     * Red Hat Linux platform.
     */
    public static Platform RED_HAT_LINUX = new Platform(Name.RED_HAT_LINUX);

    /**
     * SunOS platform.
     */
    public static Platform SUNOS = new Platform(Name.SUNOS);

    /**
     * SunOS/x86 platform.
     */
    public static Platform SUNOS_X86 = new Platform(Name.SUNOS, "SUNOS_X86", Arch.X86);

    /**
     * SunOS/SPARC platform.
     */
    public static Platform SUNOS_SPARC = new Platform(Name.SUNOS, "SUNOS_SPARC", Arch.SPARC);

    /**
     * SuSE Linux platform.
     */
    public static Platform SUSE_LINUX = new Platform(Name.SUSE_LINUX);

    /**
     * Generic UNIX platform.
     */
    public static Platform UNIX = new Platform(Name.UNIX);

    /**
     * Ubuntu Linux platform.
     */
    public static Platform UBUNTU_LINUX = new Platform(Name.UBUNTU_LINUX);

    /**
     * Windows platform.
     */
    public static Platform WINDOWS = new Platform(Name.WINDOWS);

    /**
     * Windows XP platform.
     */
    public static Platform WINDOWS_XP = new Platform(Name.WINDOWS, "WINDOWS_XP",
                                                     OsVersionConstants.WINDOWS_XP_VERSION);

    /**
     * Windows 2003 platform.
     */
    public static Platform WINDOWS_2003 = new Platform(Name.WINDOWS, "WINDOWS_2003",
                                                       OsVersionConstants.WINDOWS_2003_VERSION);

    /**
     * Windows Vista platform.
     */
    public static Platform WINDOWS_VISTA = new Platform(Name.WINDOWS, "WINDOWS_VISTA",
                                                        OsVersionConstants.WINDOWS_VISTA_VERSION);

    /**
     * Windows 7 platform.
     */
    public static Platform WINDOWS_7 = new Platform(Name.WINDOWS, "WINDOWS_7", OsVersionConstants.WINDOWS_7_VERSION);

    /**
     * Windows 7 platform.
     */
    public static Platform WINDOWS_8 = new Platform(Name.WINDOWS, "WINDOWS_8", OsVersionConstants.WINDOWS_8_VERSION);

    /**
     * Known platforms.
     */
    public static Platform[] PLATFORMS = {AIX, DEBIAN_LINUX, FEDORA_LINUX, FREEBSD, HP_UX, LINUX, MAC, MAC_OSX,
            MANDRAKE_LINUX, MANDRIVA_LINUX, OS_2, RED_HAT_LINUX, SUNOS, SUNOS_X86,
            SUNOS_SPARC, SUSE_LINUX, UBUNTU_LINUX, UNIX, WINDOWS, WINDOWS_XP,
            WINDOWS_2003, WINDOWS_VISTA, WINDOWS_7, WINDOWS_8};

    /**
     * Cached linux name.
     */
    private Name linuxName;


    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(Platforms.class.getName());


    /**
     * Returns the current platform.
     * <p/>
     * This may query the underlying OS to determine the platform name.
     *
     * @return the current platform
     */
    public Platform getCurrentPlatform()
    {
        return getCurrentPlatform(System.getProperty(OsVersionConstants.OSNAME),
                                  System.getProperty(OsVersionConstants.OSARCH),
                                  System.getProperty(OsVersionConstants.OSVERSION),
                                  System.getProperty("java.version"));
    }

    /**
     * Returns the platform for the specified operating system name and architecture.
     * <p/>
     * This may query the underlying OS to determine the platform name.
     *
     * @param name the operating system name
     * @param arch the operating system architecture, or symbolic architecture
     * @return the corresponding platform
     */
    public Platform getCurrentPlatform(String name, String arch)
    {
        return getCurrentPlatform(name, arch, null);
    }

    /**
     * Returns the current platform given the operating system name, architecture and version.
     * <p/>
     * This may query the underlying OS to determine the platform name.
     *
     * @param name    the operating system name
     * @param arch    the operating system architecture, or symbolic architecture
     * @param version the operating system version. May be {@code null}
     * @return the corresponding platform
     */
    public Platform getCurrentPlatform(String name, String arch, String version)
    {
        return getCurrentPlatform(name, arch, version, null);
    }

    /**
     * Returns the current platform given the operating system name, architecture and version.
     * <p/>
     * This may query the underlying OS to determine the platform name.
     *
     * @param name        the operating system name
     * @param arch        the operating system architecture, or symbolic architecture
     * @param version     the operating system version. May be {@code null}
     * @param javaVersion the java version. May be {@code null}
     * @return the corresponding platform
     */
    public Platform getCurrentPlatform(String name, String arch, String version, String javaVersion)
    {
        Platform result;
        Name pname = getCurrentOSName(name);
        Arch parch = getArch(arch);
        Platform match = findMatch(name, pname, parch, version);
        result = getPlatform(match, parch, version, javaVersion);
        return result;
    }

    /**
     * Returns the platform for the specified operating system name and architecture.
     *
     * @param name the operating system name or symbolic name
     * @param arch the operating system architecture, or symbolic architecture
     * @return the corresponding platform
     */
    public Platform getPlatform(String name, String arch)
    {
        return getPlatform(name, arch, null);
    }

    /**
     * Returns the platform given the operating system name, architecture and version.
     *
     * @param name    the operating system name or symbolic name
     * @param arch    the operating system architecture, or symbolic architecture
     * @param version the operating system version. May be {@code null}
     * @return the corresponding platform
     */
    public Platform getPlatform(String name, String arch, String version)
    {
        return getPlatform(name, arch, version, null);
    }

    /**
     * Returns the platform given the operating system name, architecture and version.
     *
     * @param name        the operating system name or symbolic name
     * @param arch        the operating system architecture, or symbolic architecture
     * @param version     the operating system version. May be {@code null}
     * @param javaVersion the java version
     * @return the corresponding platform
     */
    public Platform getPlatform(String name, String arch, String version, String javaVersion)
    {
        Platform result;
        Name pname = getName(name);
        Arch parch = getArch(arch);
        Platform match = findMatch(name, pname, parch, version);
        result = getPlatform(match, parch, version, javaVersion);
        return result;
    }

    /**
     * Returns the platform family name given the operating system or symbolic name.
     *
     * @param arch the operating system architecture or symbolic architecture. May be {@code null}
     * @return the corresponding platform architecture
     */
    public Arch getArch(String arch)
    {
        Arch result = null;
        if (arch != null)
        {
            try
            {
                result = Arch.valueOf(arch.toUpperCase());
            }
            catch (IllegalArgumentException ignore)
            {
                // do nothing
            }
        }
        if (result == null)
        {
            if (StringTool.startsWithIgnoreCase(arch, OsVersionConstants.X86)
                    || StringTool.startsWithIgnoreCase(arch, OsVersionConstants.I386))
            {
                result = Arch.X86;
            }
            else if (StringTool.startsWithIgnoreCase(arch, OsVersionConstants.X64)
                    || StringTool.startsWithIgnoreCase(arch, OsVersionConstants.AMD64))
            {
                result = Arch.X64;
            }
            else if (StringTool.startsWithIgnoreCase(arch, OsVersionConstants.PPC))
            {
                result = Arch.PPC;
            }
            else if (StringTool.startsWithIgnoreCase(arch, OsVersionConstants.SPARC))
            {
                result = Arch.SPARC;
            }
            else
            {
                result = Arch.UNKNOWN;
            }
        }
        return result;
    }

    /**
     * Returns the platform family name for the current operating system.
     * This may query the underlying OS to determine the platform name.
     *
     * @return the corresponding platform family name
     */
    public Name getCurrentOSName()
    {
        return getCurrentOSName(System.getProperty(OsVersionConstants.OSNAME));
    }

    /**
     * Returns the platform family name for the specified operating system name.
     *
     * @param name the operating system name, or symbolic name
     * @return the corresponding platform family name
     */
    public Name getName(String name)
    {
        Name result;
        if (StringTool.startsWithIgnoreCase(name, OsVersionConstants.FREEBSD))
        {
            result = Name.FREEBSD;
        }
        else if (StringTool.startsWithIgnoreCase(name, OsVersionConstants.LINUX))
        {
            result = Name.LINUX;
        }
        else if (StringTool.startsWithIgnoreCase(name, OsVersionConstants.HP_UX))
        {
            result = Name.HP_UX;
        }
        else if (StringTool.startsWithIgnoreCase(name, OsVersionConstants.AIX))
        {
            result = Name.AIX;
        }
        else if (StringTool.startsWithIgnoreCase(name, OsVersionConstants.SUNOS)
                || StringTool.startsWithIgnoreCase(name, OsVersionConstants.SOLARIS))
        {
            result = Name.SUNOS;
        }
        else if (StringTool.startsWith(name, OsVersionConstants.OS_2))
        {
            result = Name.OS_2;
        }
        else if (StringTool.startsWithIgnoreCase(name, OsVersionConstants.MACOSX))
        {
            result = Name.MAC_OSX;
        }
        else if (StringTool.startsWith(name, OsVersionConstants.MAC))
        {
            result = Name.MAC;
        }
        else if (StringTool.startsWith(name, OsVersionConstants.WINDOWS))
        {
            result = Name.WINDOWS;
        }
        else
        {
            try
            {
                result = Name.valueOf(name.toUpperCase());
            }
            catch (IllegalArgumentException exception)
            {
                result = Name.UNKNOWN;
            }
        }
        return result;
    }

    /**
     * Returns the platform family name for the current operating system.
     * This may query the underlying OS to determine the platform name.
     *
     * @return the corresponding platform family name
     */
    protected Name getCurrentOSName(String name)
    {
        Name result = getName(name);
        if (result == Name.LINUX)
        {
            result = getLinuxName();
        }
        return result;
    }

    /**
     * Constructs a new <tt>Platform</tt> given a match, architecture, version and java version
     *
     * @param match       the matching platform. If {@code null} the new platform will have name set to
     *                    {@link Name#UNKNOWN}.
     * @param arch        the platform architecture
     * @param version     the platform version. May be {@code null}
     * @param javaVersion the java version. May be {@code null}
     * @return a new platform
     */
    protected Platform getPlatform(Platform match, Arch arch, String version, String javaVersion)
    {
        Platform result;
        if (match != null)
        {
            if (arch == Arch.UNKNOWN)
            {
                arch = match.getArch();
            }
            if (version == null)
            {
                version = match.getVersion();
            }
            if (javaVersion == null)
            {
                javaVersion = match.getJavaVersion();
            }
            result = new Platform(match.getName(), match.getSymbolicName(), version, arch, javaVersion);
        }
        else
        {
            // name doesn't correspond to a known platform
            result = new Platform(Name.UNKNOWN, null, version, arch, javaVersion);
        }
        return result;
    }

    /**
     * Attempts to find a platform that matches the platform name, architecture and version.
     *
     * @param name    the platform name or symbolic name
     * @param pname   the resolved platform name
     * @param arch    the resolved architecture
     * @param version the version. May be {@code null}
     * @return the closest match to the arguments, or {@code null} if none is found
     */
    protected Platform findMatch(String name, Name pname, Arch arch, String version)
    {
        Platform best = null;
        int bestMatches = 0;
        for (Platform platform : PLATFORMS)
        {
            if ((pname == Name.UNKNOWN && equals(name, platform.getSymbolicName(), true))
                    || (pname != Name.UNKNOWN && pname == platform.getName()))
            {
                int currentMatches = 0;
                boolean archMatch = arch == platform.getArch();
                boolean optArchMatch = platform.getArch() == Arch.UNKNOWN;
                boolean versionMatch = version != null && equals(version, platform.getVersion());
                boolean optVersionMatch = platform.getVersion() == null;
                boolean symbolicMatch = equals(name, platform.getSymbolicName(), true);
                if (archMatch)
                {
                    currentMatches += 2;
                }
                else if (optArchMatch)
                {
                    currentMatches++;
                }
                if (symbolicMatch)
                {
                    currentMatches += 2;
                }
                if (versionMatch)
                {
                    currentMatches += 2;
                }
                else if (optVersionMatch)
                {
                    currentMatches++;
                }
                if (currentMatches > bestMatches)
                {
                    best = platform;
                    if (currentMatches == 6)
                    {
                        // best possible
                        break;
                    }
                    else
                    {
                        bestMatches = currentMatches;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Returns the Linux platform family name.
     *
     * @return the Linux platform family name
     */
    protected synchronized Name getLinuxName()
    {
        Name result = linuxName;
        if (result == null)
        {
            result = Name.LINUX;
            String path = getReleasePath();
            if (path != null)
            {
                List<String> text = getText(path);
                if (text != null)
                {
                    if (search(text, OsVersionConstants.REDHAT) || search(text, OsVersionConstants.RED_HAT))
                    {
                        result = Name.RED_HAT_LINUX;
                    }
                    else if (search(text, OsVersionConstants.FEDORA))
                    {
                        result = Name.FEDORA_LINUX;
                    }
                    else if (search(text, OsVersionConstants.MANDRAKE))
                    {
                        result = Name.MANDRAKE_LINUX;
                    }
                    else if (search(text, OsVersionConstants.MANDRIVA))
                    {
                        result = Name.MANDRIVA_LINUX;
                    }
                    else if (search(text, OsVersionConstants.SUSE, true))
                    {
                        result = Name.SUSE_LINUX; // case-insensitive since 'SUSE' 10)
                    }
                }
            }
            if (result == Name.LINUX)
            {
                List<String> text = getText(OsVersionConstants.PROC_VERSION);
                if (text != null)
                {
                    if (search(text, OsVersionConstants.DEBIAN))
                    {
                        result = Name.DEBIAN_LINUX;
                    }
                    else if (search(text, OsVersionConstants.UBUNTU))
                    {
                        result = Name.UBUNTU_LINUX;
                    }
                }
                if (result == Name.LINUX && exists("/etc/debian_version"))
                {
                    result = Name.DEBIAN_LINUX;
                }
            }
            linuxName = result;
        }
        return result;
    }

    /**
     * Returns the text from the specified file.
     *
     * @param path the file path
     * @return the corresponding text, or {@code null} if the file could not be read
     */
    protected List<String> getText(String path)
    {
        List<String> text = null;
        try
        {
            text = FileUtil.getFileContent(path);
        }
        catch (IOException ignore)
        {
            if (log.isLoggable(Level.FINE))
            {
                log.log(Level.FINE, "Failed to read " + path, ignore);
            }
        }
        return text;
    }

    /**
     * Returns the release info file path, for Linux distributions.
     *
     * @return name of the file the release info is stored in for Linux distributions
     */
    protected String getReleasePath()
    {
        String result = null;

        File[] etcList = new File("/etc").listFiles();

        if (etcList != null)
        {
            for (File etcEntry : etcList)
            {
                if (etcEntry.isFile())
                {
                    if (etcEntry.getName().endsWith("-release"))
                    {
                        result = etcEntry.toString();
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determines if the specified path exists.
     *
     * @param path the path
     * @return <tt>true</tt> if the path exists, otherwise <tt>false</tt>
     */
    protected boolean exists(String path)
    {
        return new File(path).exists();
    }

    /**
     * Searches text for the specified string.
     *
     * @param text the text to search
     * @param str  the search string
     * @return <tt>true</tt> if the text contains the search string, otherwise <tt>false</tt>
     */
    private boolean search(List<String> text, String str)
    {
        return search(text, str, false);
    }

    /**
     * Searches text for the specified string.
     *
     * @param text            the text to search
     * @param str             the search string
     * @param caseInsensitive if <tt>true</tt>, perform a case insensitive search
     * @return <tt>true</tt> if the text contains the search string, otherwise <tt>false</tt>
     */
    private boolean search(List<String> text, String str, boolean caseInsensitive)
    {
        boolean result = false;
        if (caseInsensitive)
        {
            str = str.toLowerCase();
        }
        for (String line : text)
        {
            if (caseInsensitive)
            {
                line = line.toLowerCase();
            }

            if (line.contains(str))
            {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Compares two strings for equality.
     *
     * @param a the first string to compare. May be {@code null}
     * @param b the second string to compare. May be {@code null}
     * @return <tt>true</tt> if the strings match
     */
    private boolean equals(String a, String b)
    {
        return equals(a, b, false);
    }

    /**
     * Compares two strings for equality.
     *
     * @param a          the first string to compare. May be {@code null}
     * @param b          the second string to compare. May be {@code null}
     * @param ignoreCase if <tt>true</tt>, ignore case
     * @return <tt>true</tt> if the strings match
     */
    private boolean equals(String a, String b, boolean ignoreCase)
    {
        boolean result = false;
        if (a == null && b == null)
        {
            result = true;
        }
        else if (a != null)
        {
            if (ignoreCase)
            {
                result = a.equalsIgnoreCase(b);
            }
            else
            {
                result = a.equals(b);
            }
        }
        return result;
    }

}
