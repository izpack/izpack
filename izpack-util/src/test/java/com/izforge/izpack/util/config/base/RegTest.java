package com.izforge.izpack.util.config.base;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.config.base.Registry.Key;

public class RegTest {

	@Test
	public void testConstructorWithRegistryKey() {
		if( OsVersion.IS_WINDOWS ) {
			try {
				Reg reg = new Reg("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet");
				Key key = reg.get("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control");
				Assert.assertEquals("USERNAME", key.get("CurrentUser"));
			} catch (IOException e) {
				Assert.assertNull( "Failed to read registry: " + e.getMessage(), e );
				e.printStackTrace();
			}
		}
	}
}
