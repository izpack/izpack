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

package com.izforge.izpack.uninstaller.resource;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;


/**
 * Tests the {@link RootScripts} class.
 *
 * @author Tim Anderson
 */
public class RootScriptsTest
{

    /**
     * The resources.
     */
    private Resources resources;

    /**
     * Sets up the test case.
     *
     * @throws java.io.IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        // Set up a mock Resources implementation.
        // This will return 3 root scripts
        InputStream script1 = createRootScript("echo script1");
        InputStream script2 = createRootScript("echo script2");
        InputStream script3 = createRootScript("echo script3");

        resources = Mockito.mock(Resources.class);
        when(resources.getInputStream(UninstallData.ROOTSCRIPT + "0")).thenReturn(script1);
        when(resources.getInputStream(UninstallData.ROOTSCRIPT + "1")).thenReturn(script2);
        when(resources.getInputStream(UninstallData.ROOTSCRIPT + "2")).thenReturn(script3);
        when(resources.getInputStream(UninstallData.ROOTSCRIPT + "3")).thenThrow(new ResourceNotFoundException(""));
    }

    /**
     * Verifies that the expected root scripts are run on Unix.
     * <p/>
     * NOTE: This doesn't perform the actual execution, just that execution would be performed.
     */
    @Test
    public void testRootScriptsOnUnix()
    {
        final List<String> run = new ArrayList<String>();
        RootScripts scripts = new TestRootScripts(resources, Platforms.UNIX, run);
        scripts.run();
        assertEquals(3, run.size());

        assertEquals("echo script1", run.get(0));
        assertEquals("echo script2", run.get(1));
        assertEquals("echo script3", run.get(2));
    }

    /**
     * Verifies that root scripts are not run on non-Unix platforms.
     */
    @Test
    public void testRootScriptsOnNonUnix()
    {
        final List<String> run = new ArrayList<String>();
        RootScripts scripts = new TestRootScripts(resources, Platforms.WINDOWS, run);
        scripts.run();
        assertTrue(run.isEmpty());
    }


    /**
     * Helper to create a root script.
     *
     * @param script the script
     * @return a stream of the root script
     * @throws IOException for any I/O error
     */
    private InputStream createRootScript(String script) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeUTF(script);
        objOut.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * An implementation of {@link RootScripts} that collects, rather than executes, root scripts.
     */
    private static class TestRootScripts extends RootScripts
    {

        /**
         * The scripts that would be executed.
         */
        private final List<String> scripts;


        /**
         * Constructs a <tt>RootScripts</tt>.
         *
         * @param resources used to locate the root scripts
         * @param platform  the current platform
         * @param scripts   collects the scripts that would be executed
         * @throws IzPackException if the root scripts cannot be read
         */
        public TestRootScripts(Resources resources, Platform platform, List<String> scripts)
        {
            super(resources, platform);
            this.scripts = scripts;
        }

        /**
         * Removes the given files as root for the given Users
         *
         * @param script The Script to exec at uninstall time by root.
         */
        @Override
        protected void run(String script)
        {
            scripts.add(script);
        }
    }
}
