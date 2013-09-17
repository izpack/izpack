package com.izforge.izpack.util.config.base;

import java.io.IOException;

import static org.junit.Assert.*;
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
				assertEquals("USERNAME", key.get("CurrentUser"));
			} catch (IOException e) {
				assertNull( "Failed to read registry: " + e.getMessage(), e );
				e.printStackTrace();
			}
		}
	}
	
   @Test
   public void testConstructorWithMissingRegistryKey() {
        if( OsVersion.IS_WINDOWS ) {
            try {
                Reg reg = new Reg("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\DOESNOTEXIST");
                assertEquals(0, reg.size());
            } catch (IOException e) {
                assertNull( "Failed to read registry: " + e.getMessage(), e );
                e.printStackTrace();
            }
        }
    }
   
   @Test
   public void testConstructorCreateRegistryKey() {
        if( OsVersion.IS_WINDOWS ) {
            try {
                String keyName = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\NEWKEY";
                Reg reg = new Reg(keyName, true);
                assertTrue( "New registry key was not added", reg.containsKey(keyName) );
            } catch (IOException e) {
                assertNull( "Failed to read registry: " + e.getMessage(), e );
                e.printStackTrace();
            }
        }
    }
}
