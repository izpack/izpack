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

package com.izforge.izpack.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;


/**
 * Test helper.
 *
 * @author Tim Anderson
 */
public class TestHelper
{

    /**
     * Helper to create a file of the specified size containing random data.
     *
     * @param dir  the parent directory
     * @param name the file name
     * @param size the file size
     * @return a new file
     * @throws IOException for any I/O error
     */
    public static File createFile(File dir, String name, int size) throws IOException
    {
        return createFile(new File(dir, name), size);
    }

    /**
     * Helper to create a file of the specified size containing random data.
     *
     * @param file the file
     * @param size the file size
     * @return a new file
     * @throws IOException for any I/O error
     */
    public static File createFile(File file, int size) throws IOException
    {
        byte[] data = new byte[size];
        Random random = new Random();
        random.nextBytes(data);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data);
        return file;
    }

    /**
     * Verifies that two files have the same content.
     * <p/>
     * The files must have different paths.
     *
     * @param expected   the expected file
     * @param actualDir  the actual file directory
     * @param actualName the actual file name
     */
    public static void assertFileEquals(File expected, File actualDir, String actualName)
    {
        assertFileEquals(expected, new File(actualDir, actualName));
    }

    /**
     * Verifies that a file exists.
     *
     * @param dir  the directory
     * @param name the file name, relative to the directory
     */
    public static void assertFileExists(File dir, String name)
    {
        assertFileExists(new File(dir, name));
    }

    /**
     * Verifies that a file exists.
     *
     * @param file the file
     */
    public static void assertFileExists(File file)
    {
        assertTrue(file.exists());
    }

    /**
     * Verifies that a file doesn't exist.
     *
     * @param dir  the directory
     * @param name the file name, relative to the directory
     */
    public static void assertFileNotExists(File dir, String name)
    {
        assertFalse(new File(dir, name).exists());
    }

    /**
     * Verifies that a file doesn't exist.
     *
     * @param file the file
     */
    public static void assertFileNotExists(File file)
    {
        assertFalse(file.exists());
    }

    /**
     * Verifies that two files have the same content.
     * <p/>
     * The files must have different paths.
     *
     * @param expected the expected file
     * @param actual   the actual file
     */
    public static void assertFileEquals(File expected, File actual)
    {
        assertTrue(actual.exists());
        assertFalse(actual.getAbsolutePath().equals(expected.getAbsolutePath()));
        assertEquals(expected.length(), actual.length());
        try
        {
            assertEquals(FileUtils.checksumCRC32(expected), FileUtils.checksumCRC32(actual));
        }
        catch (IOException exception)
        {
            fail(exception.getMessage());
        }
    }
}
