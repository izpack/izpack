/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.util;

import static com.izforge.izpack.util.Platform.Arch.SPARC;
import static com.izforge.izpack.util.Platform.Arch.X64;
import static com.izforge.izpack.util.Platform.Arch.X86;
import static com.izforge.izpack.util.Platform.Name.SUNOS;
import static com.izforge.izpack.util.Platform.Name.UNIX;
import static com.izforge.izpack.util.Platform.Name.WINDOWS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.izforge.izpack.api.data.binding.OsModel;

/**
 * Tests {@link PlatformModelMatcher}.
 *
 * @author Tim Anderson
 */
public class PlatformModelMatcherTest
{

    /**
     * The platform factory.
     */
    private final Platforms platforms;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;


    /**
     * Constructs a {@code PlatformModelMatcherTest}.
     */
    public PlatformModelMatcherTest()
    {
        platforms = new Platforms();
        matcher = new PlatformModelMatcher(platforms, Platforms.WINDOWS);
    }

    /**
     * Tests the {@link PlatformModelMatcher#match(Platform, OsModel)} method when only the architecture is specified.
     */
    @Test
    public void testArchitectureMatch()
    {
        OsModel x86 = new OsModel("x86", null, null, null, null);
        OsModel x64 = new OsModel("x64", null, null, null, null);
        OsModel sparc = new OsModel("SPARC", null, null, null, null);

        checkMatch(new Platform(UNIX, X86), x86, x64, sparc);
        checkMatch(new Platform(WINDOWS, X64), x64, x86, sparc);
        checkMatch(new Platform(SUNOS, SPARC), sparc, x86, x64);
    }

    /**
     * Tests the {@link PlatformModelMatcher#match(Platform, OsModel)} method when only the family is specified.
     */
    @Test
    public void testFamilyMatch()
    {
        OsModel unix = new OsModel(null, "unix", null, null, null);
        OsModel windows = new OsModel(null, "windows", null, null, null);
        OsModel mac = new OsModel(null, "mac", null, null, null);

        checkMatch(Platforms.UNIX, unix, windows, mac);
        checkMatch(Platforms.WINDOWS, windows, unix, mac);
        checkMatch(Platforms.MAC, mac, unix, windows);
    }

    /**
     * Tests the {@link PlatformModelMatcher#match(Platform, OsModel)} method when only the family and name is
     * specified.
     */
    @Test
    public void testFamilyNameMatch()
    {
        OsModel mac = new OsModel(null, "mac", null, "Mac OS", null);
        OsModel osx = new OsModel(null, "mac", null, "Mac OS X", null);

        checkMatch(Platforms.MAC, mac, osx);
        checkMatch(Platforms.MAC_OSX, osx, mac);
    }


    /**
     * Tests the {@link PlatformModelMatcher#match(Platform, OsModel)} method when only the name is specified.
     */
    @Test
    public void testNameMatch()
    {
        OsModel sunos = new OsModel(null, null, null, "SunOS", null);
        OsModel osx = new OsModel(null, null, null, "Mac OS X", null);
        OsModel os2 = new OsModel(null, null, null, "OS/2", null);

        checkMatch(Platforms.SUNOS, sunos, osx, os2);
        checkMatch(Platforms.MAC_OSX, osx, sunos, os2);
        checkMatch(Platforms.OS_2, os2, sunos, osx);
    }

    /**
     * Tests the {@link PlatformModelMatcher#match(Platform, OsModel)} method when only the version is specified.
     */
    @Test
    public void testVersionMatch()
    {
        OsModel xp = new OsModel(null, null, null, null, OsVersionConstants.WINDOWS_XP_VERSION);
        OsModel vista = new OsModel(null, null, null, null, OsVersionConstants.WINDOWS_VISTA_VERSION);
        OsModel windows7 = new OsModel(null, null, null, null, OsVersionConstants.WINDOWS_7_VERSION);

        checkMatch(Platforms.WINDOWS_XP, xp, vista, windows7);
        checkMatch(Platforms.WINDOWS_VISTA, vista, xp, windows7);
        checkMatch(Platforms.WINDOWS_7, windows7, xp, vista);
    }

    /**
     * Tests the {@link PlatformModelMatcher#match(Platform, OsModel)} method when only the java version is specified.
     */
    @Test
    public void testJavaVersionMatch()
    {
        OsModel v14 = new OsModel(null, null, "1.4", null, null);
        OsModel v15 = new OsModel(null, null, "1.5", null, null);
        OsModel v16 = new OsModel(null, null, "1.6", null, null);

        Platform platform14 = platforms.getPlatform(OsVersionConstants.WINDOWS, null, null, "1.4.0");
        Platform platform15 = platforms.getPlatform(OsVersionConstants.WINDOWS, null, null, "1.5.0");
        Platform platform16 = platforms.getPlatform(OsVersionConstants.WINDOWS, null, null, "1.6.0_30");
        checkMatch(platform14, v14, v15, v16);
        checkMatch(platform15, v15, v14, v16);
        checkMatch(platform16, v16, v14, v15);
    }

    /**
     * Tests {@link PlatformModelMatcher#matches(Platform, java.util.List)} when only the family is specified.
     */
    @Test
    public void testMatchesByFamily()
    {
        Platform platform = Platforms.WINDOWS;
        OsModel unix = new OsModel(null, "unix", null, null, null);
        OsModel windows = new OsModel(null, "windows", null, null, null);
        OsModel mac = new OsModel(null, "mac", null, null, null);

        assertTrue(matcher.matches(platform, null));
        assertTrue(matcher.matches(platform, Collections.<OsModel>emptyList()));

        assertTrue(matcher.matches(platform, Arrays.asList(unix, windows, mac)));
        assertFalse(matcher.matches(platform, Arrays.asList(unix, mac)));
    }


    /**
     * Tests {@link PlatformModelMatcher#matches(Platform, java.util.List)} when only the name is specified.
     */
    @Test
    public void testMatchesByName()
    {
        Platform platform = Platforms.MAC_OSX;
        OsModel osx = new OsModel(null, null, null, "Mac OS X", null);
        OsModel sunos = new OsModel(null, null, null, "SunOS", null);
        OsModel os2 = new OsModel(null, null, null, "OS/2", null);

        assertTrue(matcher.matches(platform, null));
        assertTrue(matcher.matches(platform, Collections.<OsModel>emptyList()));

        assertTrue(matcher.matches(platform, Arrays.asList(osx, os2, sunos)));
        assertFalse(matcher.matches(platform, Arrays.asList(sunos, os2)));
    }
    /**
     * Verifies that a platform matches the expected model.
     *
     * @param platform  the platform
     * @param match     the model that is expected to match
     * @param noMatches the models that aren't expected to match
     */
    private void checkMatch(Platform platform, OsModel match, OsModel... noMatches)
    {
        assertTrue(matcher.match(platform, match));
        for (OsModel model : noMatches)
        {
            assertFalse(matcher.match(platform, model));
        }
    }

}
