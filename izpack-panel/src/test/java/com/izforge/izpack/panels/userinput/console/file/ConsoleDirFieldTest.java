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

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.TestDirFieldConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.izforge.izpack.api.handler.Prompt.Option.OK;
import static com.izforge.izpack.api.handler.Prompt.Options.OK_CANCEL;
import static com.izforge.izpack.api.handler.Prompt.Type.WARNING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link ConsoleDirField}.
 */
public class ConsoleDirFieldTest extends AbstractConsoleFieldTest
{
    /**
     * Temporary directory injected by JUnit 5.
     */
    @TempDir
    public Path tempDir;


    @Test
    public void testSelectDefaultValue()
    {
        ConsoleDirField field = createField(tempDir.toFile().getPath(), true, false);
        checkValid(field, "\n");
        verifyNoMoreInteractions(prompt);

        assertEquals(tempDir.toFile().getAbsolutePath(), installData.getVariable("dir"));
    }

    @Test
    public void testSetValue()
    {
        ConsoleDirField field = createField(null, true, false);
        checkValid(field, tempDir.toFile().getPath(), "\n");
        verifyNoMoreInteractions(prompt);

        assertEquals(tempDir.toFile().getAbsolutePath(), installData.getVariable("dir"));
    }

    @Test
    public void testCreateDir() throws IOException
    {
        ConsoleDirField field = createField(null, false, true);

        File path = tempDir.toFile();
        assertTrue(path.delete());

        String message = "The target directory will be created: \n" + path.getAbsolutePath();

        when(prompt.confirm(eq(WARNING), anyString(), anyString(), eq(OK_CANCEL), eq(OK))).thenReturn(OK);
        checkValid(field, path.getPath());

        verify(prompt, times(1)).confirm(WARNING, "Message", message, OK_CANCEL, OK);
        verifyNoMoreInteractions(prompt);

        assertTrue(path.exists());
        assertEquals(path.getPath(), installData.getVariable("dir"));
    }

    @Test
    public void testDirNoExists()
    {
        ConsoleDirField field = createField(null, true, false);
        checkInvalid(field, "baddir");
        assertNull(installData.getVariable("dir"));
        verify(prompt).error("Invalid Directory",
                             "The directory you have chosen either does not exist or is not valid.");
    }

    @Test
    public void testInvalidDir() throws IOException
    {
        ConsoleDirField field = createField(null, false, false);

        File file = File.createTempFile("foo", "bar", FileUtils.getTempDirectory());
        checkInvalid(field, file.getPath());
        assertNull(installData.getVariable("dir"));

        assertTrue(file.delete());
        verify(prompt).error("Invalid Directory",
                             "The directory you have chosen either does not exist or is not valid.");
    }

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
