package com.izforge.izpack.util;

import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(new PrivilegedRunner(Platforms.UNIX).isPlatformSupported()).isTrue();
        assertThat(new PrivilegedRunner(Platforms.LINUX).isPlatformSupported()).isTrue();

        assertThat(new PrivilegedRunner(Platforms.WINDOWS).isPlatformSupported()).isTrue();

        assertThat(new PrivilegedRunner(Platforms.MAC).isPlatformSupported()).isFalse();
        assertThat(new PrivilegedRunner(Platforms.MAC_OSX).isPlatformSupported()).isTrue();
    }

    /**
     * Tests the {@link PrivilegedRunner#getElevator} command on Unix.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetElevatorOnUnix() throws Exception
    {
        File file = new File(getProperty("java.io.tmpdir"), "Installer");
        if (file.exists())
        {
            assertThat(file.delete()).isTrue();
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
        assertThat(elevatorCommand).isEqualTo(expectedElevatorCommand);

        // no elevator extracted on Unix
        assertThat(file).doesNotExist();
    }

    /**
     * Tests the {@link PrivilegedRunner#getElevator} command on Windows.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetElevatorOnWindows() throws Exception
    {
        File script = new File(getProperty("java.io.tmpdir"), "Installer.js");
        String scriptPath = script.getCanonicalPath();
        if (script.exists())
        {
            assertThat(script.delete()).isTrue();
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
        assertThat(elevatorCommand).isEqualTo(expectedElevatorCommand);

        assertThat(script).exists();
        assertThat(script.length() != 0).isTrue();
        assertThat(script.delete()).isTrue();
    }

    /**
     * Tests the {@link PrivilegedRunner#getElevator} command on OSX.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGetElevatorOnMacOSX() throws Exception
    {
        File script = new File(getProperty("java.io.tmpdir"), "Installer");
        String scriptPath = script.getCanonicalPath();
        if (script.exists())
        {
            assertThat(script.delete()).isTrue();
        }

        List<String> expectedElevatorCommand = new ArrayList<String>();
        expectedElevatorCommand.add(scriptPath);
        expectedElevatorCommand.add("java");
        expectedElevatorCommand.addAll(new JVMHelper().getJVMArguments());
        expectedElevatorCommand.add("-jar");
        expectedElevatorCommand.add("installer.jar");

        PrivilegedRunner runner = new PrivilegedRunner(Platforms.MAC_OSX);
        List<String> elevatorCommand = runner.getElevator("java", "installer.jar", new String[0]);
        assertThat(elevatorCommand).isEqualTo(expectedElevatorCommand);

        assertThat(script).exists();
        assertThat(script.length() != 0).isTrue();
        assertThat(script.delete()).isTrue();
    }

}
