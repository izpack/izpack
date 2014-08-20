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

import java.io.File;

/**
 * Encapsulates details of the operating system platform.
 *
 * @author Tim Anderson
 */
public class Platform
{

    /** Invalid Windows Characters
     *  http://msdn.microsoft.com/en-us/library/aa365247.aspx
     *  Forward slash not included because installer will end up creating directory with backslash
     *
     *  NOTE: We choose double backslash to be invalid rather than just backslash, because we are checking against paths
     *        A normal backslash would just represent another folder
     */
    public static final String [] invalidWindowsDirectoryChars = {"<", ">", ":", "\"", "/", "\\\\", "|", "?", "*", "\\ "};

    /**
     * Platform family name.
     */
    public enum Name
    {
        UNIX,
        LINUX(Name.UNIX),
        AIX(Name.UNIX),
        DEBIAN_LINUX(Name.LINUX),
        FEDORA_LINUX(Name.LINUX),
        FREEBSD(Name.UNIX),
        HP_UX(Name.UNIX),
        MAC,
        MAC_OSX(Name.MAC, Name.UNIX),
        MANDRAKE_LINUX(Name.LINUX),
        MANDRIVA_LINUX(Name.LINUX), // formerly 'Mandrake'
        OS_2,
        RED_HAT_LINUX(Name.LINUX),
        SUNOS(Name.UNIX),
        SUSE_LINUX(Name.LINUX),
        UBUNTU_LINUX(Name.LINUX),
        WINDOWS,
        UNKNOWN;

        /**
         * The parent platform families. May be empty
         */
        private final Name[] parents;

        /**
         * Constructs a <tt>Name</tt> with no parent family.
         */
        Name()
        {
            parents = new Name[0];
        }

        /**
         * Constructs a <tt>Name</tt> with a parent family.
         *
         * @param parents the immediate parent family names. May be empty
         */
        Name(Name... parents)
        {
            this.parents = parents;
        }

        /**
         * Returns the parent family names.
         *
         * @return the parent family names
         */
        public Name[] getParents()
        {
            return parents;
        }

        /**
         * Determines if this is an instance of the specified name.
         *
         * @param name the name to check
         * @return {@code true} if this is an instance of {@code name}
         */
        public boolean isA(Name name)
        {
            return isA(this, name);
        }

        /**
         * Determines if the current name is an instance of the specified platform family name.
         *
         * @param current the current name
         * @param name    the platform family name
         * @return {@code true} if current is an instance of {@code name}
         */
        private boolean isA(Name current, Name name)
        {
            if (name == current)
            {
                return true;
            }
            else
            {
                for (Name parent : current.getParents())
                {
                    if (isA(parent, name))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    /**
     * The operating system architecture.
     */
    public enum Arch
    {
        X86, X64, PPC, SPARC, UNKNOWN
    }

    /**
     * The platform family name.
     */
    private final Name name;

    /**
     * The symbolic name. May be {@code null}
     */
    private final String symbolicName;

    /**
     * The platform architecture.
     */
    private final Arch arch;

    /**
     * The OS version.
     */
    private final String version;

    /**
     * The java version. May be {@code null}
     */
    private final String javaVersion;


    /**
     * Constructs a <tt>Platform</tt> from the specified name.
     *
     * @param name the platform name
     */
    public Platform(Name name)
    {
        this(name, (String) null);
    }

    /**
     * Constructs a <tt>Platform</tt> from the specified name and version.
     *
     * @param name    the platform name
     * @param version the platform version. May be {@code null}
     */
    public Platform(Name name, String version)
    {
        this(name, null, version);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be {@code null}
     * @param version      the platform version. May be {@code null}
     * @throws IllegalArgumentException if {@code symbolicName} contains spaces or commas
     */
    public Platform(Name name, String symbolicName, String version)
    {
        this(name, symbolicName, version, null);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name the platform name
     * @param arch the platform architecture. May be {@code null}
     */
    public Platform(Name name, Arch arch)
    {
        this(name, null, arch);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be {@code null}
     * @param arch         the platform architecture. May be {@code null}
     * @throws IllegalArgumentException if {@code symbolicName} contains spaces or commas
     */
    public Platform(Name name, String symbolicName, Arch arch)
    {
        this(name, symbolicName, null, arch);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be {@code null}
     * @param version      the platform version. May be {@code null}
     * @param arch         the platform architecture. May be {@code null}
     * @throws IllegalArgumentException if {@code symbolicName} contains spaces or commas
     */
    public Platform(Name name, String symbolicName, String version, Arch arch)
    {
        this(name, symbolicName, version, arch, null);
    }

    /**
     * Constructs a <tt>Platform</tt>.
     *
     * @param name         the platform name
     * @param symbolicName the symbolic name. May be {@code null}
     * @param version      the platform version. May be {@code null}
     * @param arch         the platform architecture. May be {@code null}
     * @param javaVersion  the java version. May be {@code null}
     * @throws IllegalArgumentException if {@code symbolicName} contains spaces or commas
     */
    public Platform(Name name, String symbolicName, String version, Arch arch, String javaVersion)
    {
        if (symbolicName != null && (symbolicName.indexOf(' ') > 0 || symbolicName.indexOf(',') > 0))
        {
            throw new IllegalArgumentException("Argument 'symbolicName' should not contain spaces or commas");
        }
        this.name = name;
        this.symbolicName = symbolicName;
        this.version = version;
        this.arch = (arch != null) ? arch : Arch.UNKNOWN;
        this.javaVersion = javaVersion;
    }

    /**
     * Constructs a <tt>Platform</tt> from another, with the specified architecture.
     *
     * @param platform the template
     * @param arch     the architecture
     */
    public Platform(Platform platform, Arch arch)
    {
        this(platform.name, platform.symbolicName, platform.version, arch);
    }

    /**
     * Returns the platform family name.
     *
     * @return the platform family name
     */
    public Name getName()
    {
        return name;
    }

    /**
     * Returns the symbolic name for the platform.
     * <p/>
     * This is not the OS name. It is an arbitrary name that may be used to help identify a platform.
     * E.g. windows_7 for name=WINDOWS,version=6.1.
     *
     * @return the symbolic name for the platform. May be {@code null}
     */
    public String getSymbolicName()
    {
        return symbolicName;
    }

    /**
     * Returns the operating system version.
     *
     * @return the operating system version. May be {@code null}
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the java version.
     *
     * @return the java version. May be {@code null}
     */
    public String getJavaVersion()
    {
        return javaVersion;
    }

    /**
     * Returns the operating system architecture.
     *
     * @return the operating system architecture
     */
    public Arch getArch()
    {
        return arch;
    }

    /**
     * Determines if this platform is an instance of another.
     *
     * @param platform the platform to compare against
     * @return {@code true} if the platform is an instance of <tt>platform</tt>
     */
    public boolean isA(Platform platform)
    {
        boolean result = false;
        if (isA(platform.name) && hasSymbolicName(platform.symbolicName) && hasArch(platform.arch)
                && hasVersion(platform.version) && hasJavaVersion(platform.javaVersion))
        {
            result = true;
        }
        return result;
    }

    /**
     * Determines if this platform is an instance of the platform family name.
     *
     * @param name the platform family name
     * @return {@code true} if the platform is an instance of <tt>name</tt>
     */
    public boolean isA(Name name)
    {
        return isA(this.name, name);
    }

    /**
     * Determines if this platform is the specified architecture.
     *
     * @param arch the architecture
     * @return {@code true} if this platform is the specified architecture, otherwise {@code false}
     */
    public boolean isA(Arch arch)
    {
        return this.arch == arch;
    }

    /**
     * Determine invalid directory character for the given OS.
     * Expects the the driver name has already been trimmed of.
     * @return
     */
    public boolean isValidDirectorySyntax(String directoryPath)
    {
        String[] invalidDirectoryCharacters = new String[0];
        if (name == Name.WINDOWS)
        {
            invalidDirectoryCharacters = invalidWindowsDirectoryChars;
        }
        for (String invalidChar : invalidDirectoryCharacters)
        {
            if (directoryPath.contains(invalidChar))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a fully qualified directory path contains valid syntax.
     * @param directory
     * @return
     */
    public boolean isValidDirectoryPath(File directory)
    {
        return isValidDirectoryPath(directory.getAbsolutePath());
    }
    public boolean isValidDirectoryPath(String directoryPath)
    {
        String filteredPath = directoryPath;
        if (name == Name.WINDOWS)
        {
            if (!directoryPath.matches("^[A-Z]:.*"))
            {
                return false;
            }
            filteredPath = directoryPath.substring(2, directoryPath.length());
        }
        return isValidDirectorySyntax(filteredPath);
    }
    /**
     * Determines if this platform equals another.
     *
     * @param other the other instance
     * @return {@code true} if the name, arch and version are identical, otherwise {@code false}
     */
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other instanceof Platform)
        {
            Platform p = (Platform) other;
            if (name == p.name && arch == p.arch
                    && ((version == null && p.version == null) || (version != null && version.equals(p.version)))
                    && ((javaVersion == null && p.javaVersion == null)
                    || (javaVersion != null && javaVersion.equals(p.javaVersion))))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hash for the platform.
     *
     * @return a hash for the platform
     */
    public int hashCode()
    {
        int hash = name.hashCode() ^ arch.hashCode();
        if (version != null)
        {
            hash ^= version.hashCode();
        }
        if (javaVersion != null)
        {
            hash ^= javaVersion.hashCode();
        }
        return hash;
    }

    /**
     * Returns a string representation of the platform.
     *
     * @return a string representation of the platform
     */
    public String toString()
    {
        return name.toString().toLowerCase() + ",version=" + version + ",arch=" + arch.toString().toLowerCase()
                + ",symbolicName=" + symbolicName + ",javaVersion=" + javaVersion;
    }

    /**
     * Determines if the current name is an instance of the specified platform family name.
     *
     * @param current the current name
     * @param name    the plaform family name
     * @return {@code true} if current is an instance of <tt>name</tt>
     */
    private boolean isA(Name current, Name name)
    {
        if (name == current)
        {
            return true;
        }
        else
        {
            for (Name parent : current.getParents())
            {
                if (isA(parent, name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the symbolic name matches another.
     *
     * @param other the other name. May be {@code null}
     * @return {@code true} if {@code other} is {@code null} or they are equal
     */
    private boolean hasSymbolicName(String other)
    {
        return other == null || (symbolicName != null && symbolicName.equals(other));
    }

    /**
     * Determines if the architecture matches another.
     *
     * @param other the other architecture
     * @return {@code true} if they are equal, or the other architecture is unknown
     */
    private boolean hasArch(Arch other)
    {
        return arch == other || other == Arch.UNKNOWN;
    }

    /**
     * Determines if the version name matches another.
     *
     * @param other the other version. May be {@code null}
     * @return {@code true} if {@code other} is {@code null} or they are equal
     */
    private boolean hasVersion(String other)
    {
        return (other == null) || (version != null && version.equals(other));
    }

    /**
     * Determines if the java version matches another.
     *
     * @param other the other version. May be {@code null}
     * @return {@code true} if {@code other} is {@code null} or they are equal
     */
    private boolean hasJavaVersion(String other)
    {
        return (other == null) || (javaVersion != null && javaVersion.equals(other));
    }
}
