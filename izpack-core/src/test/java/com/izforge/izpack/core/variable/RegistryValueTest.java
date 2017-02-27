package com.izforge.izpack.core.variable;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;

public class RegistryValueTest
{

    private final boolean skipTests = new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded();

    private final boolean isAdminUser = new PrivilegedRunner(Platforms.WINDOWS).isAdminUser();

    @Test
    public void testResolve() throws Exception
    {
        // run tests only if not elevation is needed
        Assume.assumeTrue("This test must be run as administrator, or with Windows UAC turned off", !skipTests && isAdminUser);

        String regRoot = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control";
        // CompilerConfig and ConfigurationInstallerListener both check for the existance of regKey - this must be provided
        String regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control";
        String regValue = "CurrentUser";
        Assert.assertEquals("USERNAME", new RegistryValue(null, regKey, regValue).resolve());
        Assert.assertEquals("USERNAME", new RegistryValue(regRoot, regKey, regValue).resolve());

        Assert.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(regKey, regKey + "\\CrashControl", "DumpFile").resolve());

        // This won't work
        //Assert.assertEquals("%SystemRoot%\\MEMORY.DMP", new RegistryValue(null, regKey, "CrashControl\\DumpFile").resolve());
        regKey = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\DOES_NOT_EXIST";
        regValue = "ImagePath";
        Assert.assertEquals(null, new RegistryValue(null, regKey, regValue).resolve());
    }
}
