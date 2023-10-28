package com.izforge.izpack.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.api.config.Registry.Key;

public class RegTest {

    private final boolean skipTests = new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded();

    private final boolean isAdminUser = new PrivilegedRunner(Platforms.WINDOWS).isAdminUser();
    
	@Test
	public void testConstructorWithRegistryKey() 
	{
		assumeTrue(!skipTests && isAdminUser, "This test must be run as administrator, or with Windows UAC turned off");
		
		try {
			Reg reg = new Reg("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet");
			Key key = reg.get("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control");
			assertEquals("USERNAME", key.get("CurrentUser"));
		} catch (IOException e) {
			assertNull(e, "Failed to read registry: " + e.getMessage());
		}
	}
}
