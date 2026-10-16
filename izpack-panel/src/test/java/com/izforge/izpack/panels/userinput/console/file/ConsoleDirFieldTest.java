/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Options.OK_CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Type.WARNING;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.TestDirFieldConfig;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the {@link ConsoleDirField}.
 *
 * @author Tim Anderson
 */
public class ConsoleDirFieldTest extends AbstractConsoleFieldTest
{

    /**
     * Temporary directory.
     */
    @TempDir
    public Path dir;


    /**
     * Verifies that pressing return enters the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        ConsoleDirField field = createField(dir.toString(), true, false);
        checkValid(field, "\n");
        verifyNoMoreInteractions(prompt);

        assertThat(installData.getVariable("dir")).isEqualTo(dir.toAbsolutePath().toString());
    }

    /**
     * Verifies that a valid directory can be entered.
     */
    @Test
    public void testSetValue()
    {
        ConsoleDirField field = createField(null, true, false);
        checkValid(field, dir.toString(), "\n");
        verifyNoMoreInteractions(prompt);

        assertThat(installData.getVariable("dir")).isEqualTo(dir.toAbsolutePath().toString());
    }

    /**
     * Verify that the target directory can be created if it doesn't exist.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testCreateDir() throws IOException
    {
        ConsoleDirField field = createField(null, false, true);

        Path path = dir;
        delete(path);

        String message = "The target directory will be created: \n" + path.toAbsolutePath().toString();

        when(prompt.confirm(eq(WARNING), anyString(), anyString(), eq(OK_CANCEL), eq(OK))).thenReturn(OK);
        checkValid(field, path.toString());

        verify(prompt, times(1)).confirm(WARNING, "Message", message, OK_CANCEL, OK);
        verifyNoMoreInteractions(prompt);

        assertThat(path).exists();
        assertThat(installData.getVariable("dir")).isEqualTo(path.toString());

    }

    /**
     * Verify that validation fails if the entered directory doesn't exist.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testDirNoExists() throws IOException
    {
        ConsoleDirField field = createField(null, true, false);
        checkInvalid(field, "baddir");
        assertThat(installData.getVariable("dir")).isNull();
        verify(prompt).error("Invalid Directory",
                             "The directory you have chosen either does not exist or is not valid.");
    }

    /**
     * Verify that validation fails if the entered path is a file.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testInvalidDir() throws IOException
    {
        ConsoleDirField field = createField(null, false, false);

        Path file = createTempFile(dir, "foo", "bar");
        checkInvalid(field, file.toString());
        assertThat(installData.getVariable("dir")).isNull();

        delete(file);
        verify(prompt).error("Invalid Directory",
                             "The directory you have chosen either does not exist or is not valid.");
    }

    /**
     * Helper to create a field that updates the 'dir' variable.
     *
     * @param initialValue the initial value. May be {@code null}
     * @param mustExist    if {@code true}, the directory must exist
     * @return a new field
     */
    private ConsoleDirField createField(String initialValue, boolean mustExist, boolean create)
    {
        TestDirFieldConfig config = new TestDirFieldConfig("dir");
        config.setLabel("Enter directory: ");
        config.setInitialValue(initialValue);
        config.setMustExist(mustExist);
        config.setCreate(create);
        DirField model = new DirField(config, installData);
        return new ConsoleDirField(model, console, prompt);
    }


}
