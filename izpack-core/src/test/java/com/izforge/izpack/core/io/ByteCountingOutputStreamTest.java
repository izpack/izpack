/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.core.io;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.size;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ByteCountingOutputStreamTest
{
    @TempDir
    Path directory;

    @Test
    public void testWriting() throws IOException
    {
        Path temp = createTempFile(directory, "foo", "bar");
        ByteCountingOutputStream out;
        try (ByteCountingOutputStream stream = new ByteCountingOutputStream(newOutputStream(temp)))
        {
            out = stream;
            byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            out.write(data);
            out.write(data, 3, 2);
            out.write(1024);
        }
        assertThat(out.getByteCount()).isEqualTo(size(temp));
    }
}
