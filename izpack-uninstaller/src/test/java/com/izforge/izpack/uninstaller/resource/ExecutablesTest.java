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

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.data.ExecutableFile;


/**
 * Tests the {@link Executables} class.
 *
 * @author Tim Anderson
 */
public class ExecutablesTest
{
    /**
     * The resources.
     */
    private Resources resources;

    /**
     * Sets up the test case.
     *
     * @throws IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        ExecutableFile file1 = new ExecutableFile("file1", ExecutableFile.UNINSTALL, ExecutableFile.ABORT, null, false);
        ExecutableFile file2 = new ExecutableFile("file2", ExecutableFile.POSTINSTALL, ExecutableFile.ABORT, null,
                                                  false);
        ExecutableFile file3 = new ExecutableFile("file3", ExecutableFile.NEVER, ExecutableFile.ABORT, null, false);
        ExecutableFile file4 = new ExecutableFile("file4", ExecutableFile.UNINSTALL, ExecutableFile.ABORT, null, false);
        InputStream executables = createExecutables(file1, file2, file3, file4);

        resources = Mockito.mock(Resources.class);
        when(resources.getInputStream("executables")).thenReturn(executables);
    }

    /**
     * Verifies that the correct executables are selected for execution.
     * TODO - need to test executable selection based on platform.
     */
    @Test
    public void testExecutables()
    {
        final List<String> paths = new ArrayList<String>();
        Executables executables = new Executables(resources, Mockito.mock(Prompt.class))
        {
            @Override
            protected boolean run(ExecutableFile file)
            {
                paths.add(file.path);
                return true;
            }
        };
        assertTrue(executables.run());
        assertEquals(2, paths.size());
        assertEquals("file1", paths.get(0));
        assertEquals("file4", paths.get(1));
    }

    /**
     * Helper to create an executables resource stream.
     *
     * @param files the executable files
     * @return a resource stream
     * @throws IOException for any I/O error
     */
    private InputStream createExecutables(ExecutableFile... files) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeInt(files.length);
        for (ExecutableFile file : files)
        {
            objOut.writeObject(file);
        }
        objOut.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}
