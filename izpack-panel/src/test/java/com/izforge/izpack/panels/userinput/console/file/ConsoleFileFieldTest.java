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

package com.izforge.izpack.panels.userinput.console.file;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.TestFileFieldConfig;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the {@link ConsoleFileField}.
 *
 * @author Tim Anderson
 */
public class ConsoleFileFieldTest extends AbstractConsoleFieldTest
{

    /**
     * Test file.
     */
    @TempDir
    Path directory;

    private Path file;


    /**
     * Sets up the test.
     *
     * @throws IOException for any error
     */
    @BeforeEach
    public void aetUp() throws IOException
    {
        file = createTempFile(directory, "foo", "bar");
    }

    /**
     * Cleans up after the test.
     */
    /**
     * Verifies that pressing return enters the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        ConsoleFileField field = createField(file.toString());
        checkValid(field, "\n");

        assertThat(installData.getVariable("file")).isEqualTo(file.toAbsolutePath().toString());
    }

    @Test
    public void testSetValue()
    {
        ConsoleFileField field = createField(null);
        checkValid(field, file.toString(), "\n");

        assertThat(installData.getVariable("file")).isEqualTo(file.toAbsolutePath().toString());
    }

    /**
     * Verify that validation fails if the entered file doesn't exist.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testFileNoExists() throws IOException
    {
        ConsoleFileField field = createField(null);
        checkInvalid(field, "badfile");
        assertThat(installData.getVariable("file")).isNull();
    }

    /**
     * Verify that validation fails if the entered path is a directory.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testInvalidDir() throws IOException
    {
        ConsoleFileField field = createField(null);

        Path dir = createTempDirectory(directory, "foobar");
        checkInvalid(field, dir.toString());
        assertThat(installData.getVariable("file")).isNull();

        delete(dir);
    }

    /**
     * Helper to create a field that updates the 'file' variable.
     *
     * @param initialValue the initial value. May be {@code null}
     * @return a new field
     */
    private ConsoleFileField createField(String initialValue)
    {
        TestFileFieldConfig config = new TestFileFieldConfig("file");
        config.setLabel("Enter file: ");
        config.setInitialValue(initialValue);
        FileField model = new FileField(config, installData);
        return new ConsoleFileField(model, console, prompt);
    }


}
