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

package com.izforge.izpack.core.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the {@link FileSpanningOutputStream} and {@link FileSpanningInputStream}.
 *
 * @author Tim Anderson
 */
public class FileSpanningStreamTest
{
    @TempDir
    public Path temporaryFolder;

    /**
     * Tests the {@link FileSpanningOutputStream#write(int)} and {@link FileSpanningInputStream#read()} methods.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testReadWrite() throws IOException
    {
        File volume = temporaryFolder.resolve("volume").toFile();
        String basePath = volume.getPath();
        int maxSize = 32;
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, maxSize);

        // write out some data
        for (int i = 0; i < 1000; ++i)
        {
            assertThat(spanningOutputStream.getFilePointer()).isEqualTo(i);
            spanningOutputStream.write(i & 0xFF);
        }
        spanningOutputStream.close();

        int volumes = spanningOutputStream.getVolumes();
        assertThat(volumes).isGreaterThan(2);
        checkVolumes(basePath, maxSize, volumes);

        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        for (int i = 0; i < 1000; ++i)
        {
            assertThat(spanningInputStream.getFilePointer()).isEqualTo(i);
            assertThat(spanningInputStream.read()).isEqualTo(i & 0xFF);
        }
        assertThat(spanningInputStream.read()).isEqualTo(-1);
        spanningInputStream.close();
    }

    /**
     * Tests the {@link FileSpanningOutputStream#write(byte[])} and {@link FileSpanningInputStream#read(byte[])}
     * methods.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testByteArrayReadWrite() throws IOException
    {
        File volume = temporaryFolder.resolve("volume").toFile();
        String basePath = volume.getPath();
        int maxSize = 32;
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, maxSize);

        byte[] written = new byte[1024];
        for (int i = 0; i < written.length; ++i)
        {
            written[i] = (byte) i;
        }
        spanningOutputStream.write(written);
        assertThat(spanningOutputStream.getFilePointer()).isEqualTo(written.length);
        spanningOutputStream.close();

        int volumes = spanningOutputStream.getVolumes();
        assertThat(volumes).isGreaterThan(2);
        checkVolumes(basePath, maxSize, volumes);

        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        byte[] read = new byte[written.length];
        assertThat(spanningInputStream.read(read)).isEqualTo(written.length);
        assertThat(read).isEqualTo(written);
        assertThat(spanningInputStream.getFilePointer()).isEqualTo(read.length);

        assertThat(spanningInputStream.read(read)).isEqualTo(-1);
        spanningInputStream.close();
    }

    /**
     * Tests the {@link FileSpanningInputStream#skip(long)} method.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testSkip() throws IOException
    {
        File volume = temporaryFolder.resolve("volume").toFile();
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, 1024);

        // write 100K of random data
        byte[] written = new byte[100000];
        new Random().nextBytes(written);
        spanningOutputStream.write(written);
        spanningOutputStream.close();

        // open the volumes
        int volumes = spanningOutputStream.getVolumes();
        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        assertThat(spanningInputStream.getFilePointer()).isEqualTo(0);

        // skip half of the data
        int skip = written.length / 2;
        assertThat(spanningInputStream.skip(skip)).isEqualTo(skip);
        assertThat(spanningInputStream.getFilePointer()).isEqualTo(skip);

        // read the remaining half
        byte[] read = new byte[written.length - skip];
        assertThat(spanningInputStream.read(read)).isEqualTo(read.length);
        assertThat(spanningInputStream.getFilePointer()).isEqualTo(written.length);

        // verify the read data matches that expected
        for (int i = 0; i < read.length; ++i)
        {
            assertThat(read[i]).isEqualTo(written[i + skip]);
        }

        // check that there is nothing left to read
        assertThat(spanningInputStream.read(read)).isEqualTo(-1);
        spanningInputStream.close();
    }

    /**
     * Writes 10GB of random data and verifies it can be read back in.
     *
     * @throws IOException for any I/O exception
     */
    @Disabled("This is a long running test. It should be run when making changes to FileSpanningInputStream or "
                    + "FileSpanningOutputStream")
    @Test
    public void testLargeFiles() throws IOException
    {
        File volume = temporaryFolder.resolve("volume").toFile();
        long maxSize = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, maxSize);

        byte[] written = new byte[(int) FileSpanningOutputStream.MB];
        int count = 10000;
        for (int i = 0; i < count; ++i)
        {
            new Random().nextBytes(written);
            byte id = (byte) (i & 0xFF);
            written[0] = id;
            written[written.length - 1] = id;
            System.out.println("Writing " + i);
            spanningOutputStream.write(written);
        }
        spanningOutputStream.close();

        System.out.println("Volume: " + volume.getPath() + ", compressed size=" + volume.length());

        int volumes = spanningOutputStream.getVolumes();
        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        byte[] read = new byte[written.length];

        for (int i = 0; i < count; ++i)
        {
            System.out.println("Reading " + i);
            assertThat(spanningInputStream.read(read)).isEqualTo(written.length);
            byte id = (byte) (i & 0xFF);
            assertThat(read[0]).isEqualTo(id);
            assertThat(read[read.length - 1]).isEqualTo(id);
        }

        assertThat(spanningInputStream.read(read)).isEqualTo(-1);
        spanningInputStream.close();
    }

    /**
     * Checks the existence of volumes and their expected size.
     *
     * @param basePath the volume base path
     * @param maxSize  the maximum volume size
     * @param volumes  the no. of volumes
     */
    private void checkVolumes(String basePath, int maxSize, int volumes)
    {
        assertThat(volumes).isGreaterThan(1);

        for (int i = 0; i < volumes; ++i)
        {
            File volume = (i == 0) ? new File(basePath) : new File(basePath + "." + i);
            assertThat(volume).exists();
            if (i != volumes - 1)
            {
                // verify the length of all but the last volume, whose length is unpredictable
                assertThat(volume).hasSize(maxSize);
            }
        }
    }
}
