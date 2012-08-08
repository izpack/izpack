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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link JVMHelper}.
 *
 * @author Tim Anderson
 */
public class JVMHelperTest
{

    /**
     * Tests {@link JVMHelper#getJVMArguments()}.
     */
    @Test
    public void testGetJVMArguments()
    {
        JVMHelper helper = new JVMHelper()
        {
            @Override
            protected List<String> getInputArguments()
            {
                // simulate the JVM input arguments. Note that some of these are mutually exclusive (agentlib,
                // runjdwp), but just need to verify they are removed by getJVMArguments()
                return Arrays.asList("-DDEBUG=true",
                                     "-DTRACE=true",
                                     "-Xmx512M",
                                     "-Xms64M",
                                     "-XX:MaxPermSize=64m",
                                     "-Xdebug",
                                     "-Dself.mod.base=/tmp",
                                     "-Dself.mod.jar=install.jar",
                                     "-Dizpack.mode=privileged",
                                     "-Xrunjdwp:transport=dt_shmem",
                                     "-agentlib:jdwp=transport=dt_socket",
                                     "-javaagent:gragent.jar");
            }
        };
        // verify that java debug, SelfModifier and PrivilegedRunner properties are excluded.
        List<String> args = helper.getJVMArguments();
        assertEquals(5, args.size());
        assertTrue(args.contains("-DDEBUG=true"));
        assertTrue(args.contains("-DTRACE=true"));
        assertTrue(args.contains("-Xmx512M"));
        assertTrue(args.contains("-Xms64M"));
        assertTrue(args.contains("-XX:MaxPermSize=64m"));
    }


    /**
     * Tests {@link JVMHelper#getJVMArguments()} for arguments that contain spaces.
     * <p/>
     * Due to a bug in {@link java.lang.management.RuntimeMXBean#getInputArguments()}, arguments with spaces are
     * split. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459832 for more details.
     */
    @Test
    public void testArgumentWithSpaces()
    {
        JVMHelper helper = new JVMHelper()
        {
            @Override
            protected List<String> getInputArguments()
            {
                return Arrays.asList("-Dsomepath=C:\\Program",
                                     "Files\\IzPack",
                                     "-Dsomeotherpath=C:\\Program",
                                     "Files",
                                     "(x86)\\MyApp",
                                     "5.0");
            }
        };
        // verify that java debug, SelfModifier and PrivilegedRunner properties are excluded.
        List<String> args = helper.getJVMArguments();
        assertEquals(2, args.size());
        assertTrue(args.contains("-Dsomepath=C:\\Program Files\\IzPack"));
        assertTrue(args.contains("-Dsomeotherpath=C:\\Program Files (x86)\\MyApp 5.0"));
    }

}
