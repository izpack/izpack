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
package com.izforge.izpack.panels.target;

import static com.izforge.izpack.panels.target.TargetPanelHelper.isIncompatibleInstallation;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newOutputStream;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the {@link TargetPanelHelper} class.
 *
 * @author Tim Anderson
 */
public class TargetPanelHelperTest
{
    @TempDir
    Path directory;
    private String orgUserDir;

    @BeforeEach
    public void initialize() {
        orgUserDir = getProperty("user.dir");
    }

    @AfterEach
    public void cleanup() {
        setProperty("user.dir", orgUserDir);
    }

    /**
     * Tests the {@link TargetPanelHelper#isIncompatibleInstallation(String, Boolean)} method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testIsIncompatibleInstallation() throws IOException
    {
        Path dir = directory.resolve("installation");

        assertThat(dir).doesNotExist();
        assertThat(isIncompatibleInstallation(dir.toString(), true)).isFalse();
        createDirectory(dir);
        assertThat(isIncompatibleInstallation(dir.toString(), true)).isFalse();

        Path file = dir.resolve(InstallData.INSTALLATION_INFORMATION);
        try (ObjectOutputStream stream = new ObjectOutputStream(newOutputStream(file)))
        {
            stream.writeObject(new ArrayList<Pack>());
        }
        assertThat(isIncompatibleInstallation(dir.toString(), true)).isFalse();

        delete(file);
        try (ObjectOutputStream stream = new ObjectOutputStream(newOutputStream(file)))
        {
            stream.writeObject(Integer.valueOf(1));
        }
        assertThat(isIncompatibleInstallation(dir.toString(), true)).isTrue();
    }
}
