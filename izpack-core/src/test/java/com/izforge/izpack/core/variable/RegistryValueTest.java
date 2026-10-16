package com.izforge.izpack.core.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import org.junit.jupiter.api.Test;

public class RegistryValueTest {

    private final boolean skipTests = new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded();

    private final boolean isAdminUser = new PrivilegedRunner(Platforms.WINDOWS).isAdminUser();

    @Test
    public void testResolve() throws Exception {
        // run tests only if not elevation is needed
        assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");

        // CompilerConfig and ConfigurationInstallerListener both check for the existance of regKey - this must be provided
        String regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control";
        String regValue = "CurrentUser";
        assertThat(new RegistryValue(regKey, regValue).resolve()).isEqualTo("USERNAME");

//		Assertions.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(regKey + "\\CrashControl", "DumpFile").resolve());
        assertThat(new RegistryValue(regKey + "\\CrashControl", "MinidumpDir").resolve()).isEqualTo("%SystemRoot%\\Minidump");

// This won't work
//			Assertions.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(null, regKey, "CrashControl\\DumpFile").resolve());

        regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\DOES_NOT_EXIST";
        regValue = "ImagePath";
        assertThat(new RegistryValue(regKey, regValue).resolve()).isEqualTo(null);
    }
}
