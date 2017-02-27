package com.izforge.izpack.api.config;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.api.config.Registry.Key;

public class RegTest
{

    private final boolean skipTests = new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded();

    private final boolean isAdminUser = new PrivilegedRunner(Platforms.WINDOWS).isAdminUser();

    @Test
    public void testConstructorWithRegistryKey()
    {
        Assume.assumeTrue("This test must be run as administrator, or with Windows UAC turned off", !skipTests && isAdminUser);

        try
        {
            Reg reg = new Reg("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet");
            Key key = reg.get("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control");
            Assert.assertEquals("USERNAME", key.get("CurrentUser"));
        }
        catch (IOException e)
        {
            Assert.assertNull("Failed to read registry: " + e.getMessage(), e);
        }
    }
}
