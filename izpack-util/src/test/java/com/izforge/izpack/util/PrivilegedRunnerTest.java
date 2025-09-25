package com.izforge.izpack.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


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

        List<String> expectedElevatorCommand = new ArrayList<String>();
        expectedElevatorCommand.add("xterm");
        expectedElevatorCommand.add("-title");
        expectedElevatorCommand.add("Installer");
        expectedElevatorCommand.add("-e");
        expectedElevatorCommand.add("sudo");
        expectedElevatorCommand.add("java");
        expectedElevatorCommand.addAll(new JVMHelper().getJVMArguments());
        expectedElevatorCommand.add("-jar");
        expectedElevatorCommand.add("installer.jar");

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.UNIX);
        List<String> elevatorCommand = runner.getElevator("java", "installer.jar", new String[0]);
        assertEquals(expectedElevatorCommand, elevatorCommand);

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

        List<String> expectedElevatorCommand = new ArrayList<String>();
        expectedElevatorCommand.add("wscript");
        expectedElevatorCommand.add(scriptPath);
        expectedElevatorCommand.add("javaw");
        expectedElevatorCommand.addAll(new JVMHelper().getJVMArguments());
        expectedElevatorCommand.add("-Dizpack.mode=privileged");
        expectedElevatorCommand.add("-jar");
        expectedElevatorCommand.add("installer.jar");

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.WINDOWS);
        List<String> elevatorCommand = runner.getElevator("javaw", "installer.jar", new String[0]);
        assertEquals(expectedElevatorCommand, elevatorCommand);

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

        List<String> expectedElevatorCommand = new ArrayList<String>();
        expectedElevatorCommand.add(scriptPath);
        expectedElevatorCommand.add("java");
        expectedElevatorCommand.addAll(new JVMHelper().getJVMArguments());
        expectedElevatorCommand.add("-jar");
        expectedElevatorCommand.add("installer.jar");

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.MAC_OSX);
        List<String> elevatorCommand = runner.getElevator("java", "installer.jar", new String[0]);
        assertEquals(expectedElevatorCommand, elevatorCommand);

        assertTrue(script.exists());
        assertTrue(script.length() != 0);
        assertTrue(script.delete());
    }

}
