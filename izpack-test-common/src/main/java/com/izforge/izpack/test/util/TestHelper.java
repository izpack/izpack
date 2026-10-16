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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        return createFile(new File(dir, name).toPath(), size).toFile();
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
        createFile(file.toPath(), size);
        return file;
    }

    /** Creates a random-data fixture without creating missing parent directories. */
    public static Path createFile(Path dir, String name, int size) throws IOException
    {
        return createFile(dir.resolve(name), size);
    }

    public static Path createFile(Path file, int size) throws IOException
    {
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        Files.write(file, data);
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
        assertFileEquals(expected.toPath(), new File(actualDir, actualName).toPath());
    }

    /**
     * Verifies that a file exists.
     *
     * @param dir  the directory
     * @param name the file name, relative to the directory
     */
    public static void assertFileExists(File dir, String name)
    {
        assertFileExists(new File(dir, name).toPath());
    }

    /**
     * Verifies that a file exists.
     *
     * @param file the file
     */
    public static void assertFileExists(File file)
    {
        assertFileExists(file.toPath());
    }

    public static void assertFileExists(Path dir, String name)
    {
        assertFileExists(dir.resolve(name));
    }

    public static void assertFileExists(Path file)
    {
        assertThat(Files.exists(file)).as("File or directory %s expected but not found", file).isTrue();
    }

    /**
     * Verifies that a file doesn't exist.
     *
     * @param dir  the directory
     * @param name the file name, relative to the directory
     */
    public static void assertFileNotExists(File dir, String name)
    {
        assertFileNotExists(new File(dir, name).toPath());
    }

    /**
     * Verifies that a file doesn't exist.
     *
     * @param file the file
     */
    public static void assertFileNotExists(File file)
    {
        assertFileNotExists(file.toPath());
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
        assertFileEquals(expected.toPath(), actual.toPath());
    }

    public static void assertFileNotExists(Path dir, String name)
    {
        assertFileNotExists(dir.resolve(name));
    }

    public static void assertFileNotExists(Path file)
    {
        // Negated exists preserves the File contract when existence cannot be determined.
        assertThat(Files.exists(file)).as("File or directory %s not expected but found", file).isFalse();
    }

    public static void assertFileEquals(Path expected, Path actualDir, String actualName)
    {
        assertFileEquals(expected, actualDir.resolve(actualName));
    }

    public static void assertFileEquals(Path expected, Path actual)
    {
        assertThat(Files.exists(actual)).as("File not found").isTrue();
        assertThat(actual.toAbsolutePath().toString()).as("Path differs")
                .isNotEqualTo(expected.toAbsolutePath().toString());
        // File.length() returns zero on a missing or unreadable file; retain that behavior.
        assertThat(actual.toFile().length()).as("File length differs").isEqualTo(expected.toFile().length());
        try
        {
            assertThat(FileUtils.checksumCRC32(actual.toFile())).as("Checksum differs")
                    .isEqualTo(FileUtils.checksumCRC32(expected.toFile()));
        }
        catch (IOException exception)
        {
            fail(exception.getMessage());
        }
    }
}
