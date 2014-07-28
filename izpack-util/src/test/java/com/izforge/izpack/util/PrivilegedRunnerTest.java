package com.izforge.izpack.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;


/**
 * Tests the {@link PrivilegedRunner}.
 *
 * @author Tim Anderson
 */
public class PrivilegedRunnerTest
{

    /**
     * Tests {@link PrivilegedRunner#isPlatformSupported()}.
     */
    @Test
    public void testIsPlatformSupported()
    {
        assertTrue(new PrivilegedRunner(Platforms.UNIX).isPlatformSupported());
        assertTrue(new PrivilegedRunner(Platforms.LINUX).isPlatformSupported());

        assertTrue(new PrivilegedRunner(Platforms.WINDOWS).isPlatformSupported());

        assertFalse(new PrivilegedRunner(Platforms.MAC).isPlatformSupported());
        assertTrue(new PrivilegedRunner(Platforms.MAC_OSX).isPlatformSupported());
    }

    /**
     * Tests the {@link PrivilegedRunner#getElevator} command on Unix.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetElevatorOnUnix() throws Exception
    {
        File file = new File(System.getProperty("java.io.tmpdir"), "Installer");
        if (file.exists())
        {
            assertTrue(file.delete());
        }

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.UNIX);
        List<String> elevatorCommand = runner.getElevator("java", "installer.jar", new String[0]);
        assertEquals(8, elevatorCommand.size());

        assertEquals("xterm", elevatorCommand.get(0));
        assertEquals("-title", elevatorCommand.get(1));
        assertEquals("Installer", elevatorCommand.get(2));
        assertEquals("-e", elevatorCommand.get(3));
        assertEquals("sudo", elevatorCommand.get(4));
        assertEquals("java", elevatorCommand.get(5));
        assertEquals("-jar", elevatorCommand.get(6));
        assertEquals("installer.jar", elevatorCommand.get(7));

        // no elevator extracted on Unix
        assertFalse(file.exists());
    }

    /**
     * Tests the {@link PrivilegedRunner#getElevator} command on Windows.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetElevatorOnWindows() throws Exception
    {
        File script = new File(System.getProperty("java.io.tmpdir"), "Installer.js");
        String scriptPath = script.getCanonicalPath();
        if (script.exists())
        {
            assertTrue(script.delete());
        }

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.WINDOWS);
        List<String> elevatorCommand = runner.getElevator("javaw", "installer.jar", new String[0]);
        assertEquals(6, elevatorCommand.size());

        assertEquals("wscript", elevatorCommand.get(0));
        assertEquals(scriptPath, elevatorCommand.get(1));
        assertEquals("javaw", elevatorCommand.get(2));
        assertEquals("-Dizpack.mode=privileged", elevatorCommand.get(3));
        assertEquals("-jar", elevatorCommand.get(4));
        assertEquals("installer.jar", elevatorCommand.get(5));

        assertTrue(script.exists());
        assertTrue(script.length() != 0);
        assertTrue(script.delete());
    }

    /**
     * Tests the {@link PrivilegedRunner#getElevator} command on OSX.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetElevatorOnMacOSX() throws Exception
    {
        File script = new File(System.getProperty("java.io.tmpdir"), "Installer");
        String scriptPath = script.getCanonicalPath();
        if (script.exists())
        {
            assertTrue(script.delete());
        }

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.MAC_OSX);
        List<String> elevatorCommand = runner.getElevator("java", "installer.jar", new String[0]);
        assertEquals(4, elevatorCommand.size());

        assertEquals(scriptPath, elevatorCommand.get(0));
        assertEquals("java", elevatorCommand.get(1));
        assertEquals("-jar", elevatorCommand.get(2));
        assertEquals("installer.jar", elevatorCommand.get(3));

        assertTrue(script.exists());
        assertTrue(script.length() != 0);
        assertTrue(script.delete());
    }

}
