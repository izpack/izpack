/*
 * IzPack - Copyright 2001-2011 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2011 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.util;

import static com.izforge.izpack.util.Platform.Arch;
import static com.izforge.izpack.util.Platform.Name;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract base class for {@link Platform} and {@link Platforms} test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPlatformTest
{

    /**
     * Verifies that a platform matches that expected.
     *
     * @param expected the expected platform
     * @param platform the platform to check
     */
    protected void checkPlatform(Platform expected, Platform platform)
    {
        checkPlatform(platform, expected.getName(), expected.getSymbolicName(), expected.getVersion(),
                expected.getArch());
    }

    /**
     * Verifies that a platform matches that expected.
     *
     * @param platform     the platform to check
     * @param name         the expected name
     * @param symbolicName the expected symbolic name
     * @param version      the expected version
     * @param arch         the expected architecture
     */
    protected void checkPlatform(Platform platform, Name name, String symbolicName, String version, Arch arch)
    {
        assertEquals(name, platform.getName());
        assertEquals(symbolicName, platform.getSymbolicName());
        assertEquals(version, platform.getVersion());
        assertEquals(arch, platform.getArch());
    }
}
