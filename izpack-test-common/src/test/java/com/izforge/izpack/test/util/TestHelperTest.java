/*
 * IzPack - Copyright 2001-2026 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.test.util;

import static com.izforge.izpack.test.util.TestHelper.assertFileEquals;
import static com.izforge.izpack.test.util.TestHelper.assertFileExists;
import static com.izforge.izpack.test.util.TestHelper.assertFileNotExists;
import static com.izforge.izpack.test.util.TestHelper.createFile;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.size;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestHelperTest
{
    @TempDir
    Path directory;

    @Test
    void pathAndFileAdaptersCreateAndCompareFixtures() throws Exception
    {
        Path source = createFile(directory, "source", 1024);
        assertThat(size(source)).isEqualTo(1024);
        assertThat(createFile(directory.toFile(), "legacy", 7).length()).isEqualTo(7);
        Path copy = copy(source, directory.resolve("copy"));
        assertFileEquals(source, copy);
        assertFileEquals(source.toFile(), directory.toFile(), "copy");
        assertFileExists(directory, "source");
        assertFileExists(source.toFile());
        assertFileNotExists(directory, "missing");
        assertFileNotExists(directory.toFile(), "missing");
        assertThatThrownBy(() -> assertFileEquals(source, source))
                .isInstanceOf(AssertionError.class).hasMessageContaining("Path differs");
        write(copy, new byte[1024]);
        assertThatThrownBy(() -> assertFileEquals(source, copy))
                .isInstanceOf(AssertionError.class).hasMessageContaining("Checksum differs");
        assertThatThrownBy(() -> assertFileExists(directory.resolve("missing")))
                .isInstanceOf(AssertionError.class).hasMessageContaining("expected but not found");
    }

    @Test
    void fixtureCreationTruncatesWithoutCreatingParents() throws Exception
    {
        Path file = createFile(directory, "fixture", 32);
        createFile(file, 3);
        assertThat(size(file)).isEqualTo(3);
        assertThatThrownBy(() -> createFile(directory.resolve("missing/fixture"), 3))
                .isInstanceOf(IOException.class);
        assertThat(directory.resolve("missing")).doesNotExist();
    }
}
