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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * Tests the {@link Platform} class.
 *
 * @author Tim Anderson
 */
public class PlatformTest extends AbstractPlatformTest
{

    /**
     * Tests the {@link Platform} constructors.
     */
    @Test
    public void testConstructors()
    {
        checkPlatform(new Platform(Name.UNIX), Name.UNIX, null, null, Arch.UNKNOWN);

        checkPlatform(new Platform(Name.WINDOWS, OsVersionConstants.WINDOWS_7_VERSION),
                      Name.WINDOWS, null, OsVersionConstants.WINDOWS_7_VERSION, Arch.UNKNOWN);

        checkPlatform(new Platform(Name.WINDOWS, "windows_vista", OsVersionConstants.WINDOWS_VISTA_VERSION),
                      Name.WINDOWS, "windows_vista", OsVersionConstants.WINDOWS_VISTA_VERSION, Arch.UNKNOWN);

        checkPlatform(new Platform(Name.WINDOWS, Arch.X86), Name.WINDOWS, null, null, Arch.X86);

        checkPlatform(new Platform(Name.SUNOS, "sunos_sparc", Arch.SPARC), Name.SUNOS, "sunos_sparc", null, Arch.SPARC);

        checkPlatform(new Platform(Name.MAC_OSX, "mac_osx", OsVersionConstants.MACOSX, Arch.X64), Name.MAC_OSX,
                      "mac_osx", OsVersionConstants.MACOSX, Arch.X64);

        Platform win7 = new Platform(Name.WINDOWS, "windows_7", OsVersionConstants.WINDOWS_7_VERSION);
        checkPlatform(new Platform(win7, Arch.X64), Name.WINDOWS, "windows_7", OsVersionConstants.WINDOWS_7_VERSION,
                      Arch.X64);

        Platform win8 = new Platform(Name.WINDOWS, "windows_8", OsVersionConstants.WINDOWS_8_VERSION);
        checkPlatform(new Platform(win8, Arch.X64), Name.WINDOWS, "windows_8", OsVersionConstants.WINDOWS_8_VERSION,
                      Arch.X64);
    }

    /**
     * Tests the {@link Platform#isA(Name)} method.
     */
    @Test
    public void testIsAName()
    {
        Platform p1 = new Platform(Name.UNIX);
        assertTrue(p1.isA(Name.UNIX));
        assertFalse(p1.isA(Name.LINUX));

        Platform p2 = new Platform(Name.LINUX);
        assertTrue(p2.isA(Name.LINUX));
        assertTrue(p2.isA(Name.UNIX));
        assertFalse(p2.isA(Name.DEBIAN_LINUX));

        Name[] linuxes = {Name.DEBIAN_LINUX, Name.FEDORA_LINUX, Name.MANDRAKE_LINUX, Name.MANDRIVA_LINUX,
                Name.RED_HAT_LINUX, Name.SUSE_LINUX, Name.UBUNTU_LINUX};
        for (Name name : linuxes)
        {
            Platform linux = new Platform(name);
            assertTrue(linux.isA(name));
            assertTrue(linux.isA(Name.LINUX));
            assertTrue(linux.isA(Name.UNIX));
            assertFalse(linux.isA(Name.WINDOWS));
        }

        Name[] unixes = {Name.AIX, Name.LINUX, Name.FREEBSD, Name.HP_UX, Name.MAC_OSX, Name.SUNOS};
        for (Name name : unixes)
        {
            Platform unix = new Platform(name);
            assertTrue(unix.isA(name));
            assertTrue(unix.isA(Name.UNIX));
        }

        Platform p3 = new Platform(Name.MAC_OSX);
        assertTrue(p3.isA(Name.MAC_OSX));
        assertTrue(p3.isA(Name.UNIX));
        assertFalse(p3.isA(Name.LINUX));
        assertTrue(p3.isA(Name.MAC));

        Platform p4 = new Platform(Name.MAC);
        assertTrue(p4.isA(Name.MAC));
        assertFalse(p4.isA(Name.MAC_OSX));
    }

    /**
     * Tests the {@link Platform#isA(Platform)} method.
     */
    @Test
    public void testIsAPlatform()
    {
        Platform debian = new Platform(Name.DEBIAN_LINUX);
        Platform linux = new Platform(Name.LINUX);
        Platform unix = new Platform(Name.UNIX);
        Platform windows = new Platform(Name.WINDOWS);
        Platform windows7 = new Platform(Name.WINDOWS, OsVersionConstants.WINDOWS_7_VERSION);
        Platform windows8 = new Platform(Name.WINDOWS, OsVersionConstants.WINDOWS_8_VERSION);
        Platform windows64 = new Platform(Name.WINDOWS, Arch.X64);
        Platform vista32 = new Platform(Name.WINDOWS, OsVersionConstants.WINDOWS_VISTA_VERSION, Arch.X86);
        Platform vista64 = new Platform(Name.WINDOWS, OsVersionConstants.WINDOWS_VISTA_VERSION, Arch.X64);

        assertTrue(debian.isA(debian));
        assertTrue(debian.isA(linux));
        assertTrue(debian.isA(unix));
        assertFalse(debian.isA(windows));
        assertFalse(linux.isA(debian));
        assertFalse(unix.isA(debian));

        assertTrue(windows7.isA(windows7));
        assertTrue(windows7.isA(windows));
        assertFalse(windows.isA(windows7));

        assertTrue(windows8.isA(windows8));
        assertTrue(windows8.isA(windows));
        assertFalse(windows.isA(windows8));

        assertTrue(windows64.isA(windows64));
        assertTrue(windows64.isA(windows));
        assertFalse(windows.isA(windows64));

        assertTrue(vista32.isA(vista32));
        assertTrue(vista32.isA(windows));
        assertFalse(vista64.isA(vista32));
        assertTrue(vista64.isA(vista64));
        assertTrue(vista64.isA(windows));
        assertFalse(vista32.isA(vista64));
    }

    /**
     * Tests the {@link Platform#isA(Arch) method.
     */
    @Test
    public void testIsArch()
    {
        Platform platform = new Platform(Name.WINDOWS, Arch.X64);
        assertTrue(platform.isA(Arch.X64));
        assertFalse(platform.isA(Arch.X86));
    }

    /**
     * Tests the {@link Platform#equals} method.
     */
    @Test
    public void testEquals()
    {
        Platform platform1 = new Platform(Name.WINDOWS);
        Platform platform2 = new Platform(Name.WINDOWS);
        Platform platform3 = new Platform(Name.WINDOWS, OsVersionConstants.WINDOWS_7_VERSION);
        Platform platform4 = new Platform(Name.WINDOWS, Arch.X86);
        Platform platform5 = new Platform(Name.WINDOWS, null, OsVersionConstants.WINDOWS_2003_VERSION, Arch.X86);
        Platform platform6 = new Platform(Name.WINDOWS, "win2003", OsVersionConstants.WINDOWS_2003_VERSION, Arch.X86);

        assertTrue(platform1.equals(platform1));
        assertTrue(platform1.equals(platform2));
        assertFalse(platform1.equals(platform3));
        assertFalse(platform1.equals(platform4));
        assertFalse(platform1.equals(platform5));
        assertFalse(platform4.equals(platform5));
        assertTrue(platform5.equals(platform6));  // symbolic name not used in equality
    }

    /**
     * Verifies that symbolic names cannot contain commas or spaces.
     */
    @Test
    public void testSymbolicName()
    {
        String validName = "Windows_7";
        Platform platform1 = new Platform(Name.WINDOWS, validName, OsVersionConstants.WINDOWS_7_VERSION);
        assertEquals(validName, platform1.getSymbolicName());

        String invalidSpaces = "Windows 7";
        try
        {
            new Platform(Name.WINDOWS, invalidSpaces, OsVersionConstants.WINDOWS_7_VERSION);
            fail("Expected IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        String invalidCommas = "Windows,7";
        try
        {
            new Platform(Name.WINDOWS, invalidCommas, OsVersionConstants.WINDOWS_7_VERSION);
            fail("Expected IllegalArgumentException to be thrown");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }
    }

    /**
     * Tests the {@link Platform#toString()} method.
     */
    @Test
    public void testToString()
    {
        Platform platform1 = new Platform(Name.WINDOWS, "windows_7", OsVersionConstants.WINDOWS_7_VERSION, Arch.X64,
                                          "1.6");
        assertEquals("windows,version=6.1,arch=x64,symbolicName=windows_7,javaVersion=1.6", platform1.toString());

        Platform platform2 = new Platform(Name.SUNOS);
        assertEquals("sunos,version=null,arch=unknown,symbolicName=null,javaVersion=null", platform2.toString());
    }
}
