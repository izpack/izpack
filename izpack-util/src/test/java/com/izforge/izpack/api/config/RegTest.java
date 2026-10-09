package com.izforge.izpack.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.izforge.izpack.api.config.Registry.Key;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import java.io.IOException;
import org.junit.jupiter.api.Test;

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
            assertThat(key.get("CurrentUser")).isEqualTo("USERNAME");
        } catch (IOException e) {
            assertThat(e).as("Failed to read registry: " + e.getMessage()).isNull();
        }
    }
}
