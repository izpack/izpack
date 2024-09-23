package com.izforge.izpack.core.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;

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
		assertEquals("USERNAME", new RegistryValue(regKey, regValue).resolve());
		
//		Assert.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(regKey + "\\CrashControl", "DumpFile").resolve());
		assertEquals("%SystemRoot%\\Minidump", new RegistryValue(regKey + "\\CrashControl", "MinidumpDir").resolve());
		
// This won't work			
//			Assert.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(null, regKey, "CrashControl\\DumpFile").resolve());
		
		regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\DOES_NOT_EXIST";
		regValue = "ImagePath";
    assertNull(new RegistryValue(regKey, regValue).resolve());
	}
}
