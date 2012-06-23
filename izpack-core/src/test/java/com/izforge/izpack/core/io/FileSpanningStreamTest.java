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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * Tests the {@link FileSpanningOutputStream} and {@link FileSpanningInputStream}.
 *
 * @author Tim Anderson
 */
public class FileSpanningStreamTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Tests the {@link FileSpanningOutputStream#write(int)} and {@link FileSpanningInputStream#read()} methods.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testReadWrite() throws IOException
    {
        File volume = new File(temporaryFolder.getRoot(), "volume");
        String basePath = volume.getPath();
        int maxSize = 32;
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, maxSize);

        // write out some data
        for (int i = 0; i < 1000; ++i)
        {
            assertEquals(i, spanningOutputStream.getFilePointer());
            spanningOutputStream.write(i & 0xFF);
        }
        spanningOutputStream.close();

        int volumes = spanningOutputStream.getVolumes();
        assertTrue(volumes > 2);
        checkVolumes(basePath, maxSize, volumes);

        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        for (int i = 0; i < 1000; ++i)
        {
            assertEquals(i, spanningInputStream.getFilePointer());
            assertEquals(i & 0xFF, spanningInputStream.read());
        }
        assertEquals(-1, spanningInputStream.read());
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
        File volume = new File(temporaryFolder.getRoot(), "volume");
        String basePath = volume.getPath();
        int maxSize = 32;
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, maxSize);

        byte[] written = new byte[1024];
        for (int i = 0; i < written.length; ++i)
        {
            written[i] = (byte) i;
        }
        spanningOutputStream.write(written);
        assertEquals(written.length, spanningOutputStream.getFilePointer());
        spanningOutputStream.close();

        int volumes = spanningOutputStream.getVolumes();
        assertTrue(volumes > 2);
        checkVolumes(basePath, maxSize, volumes);

        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        byte[] read = new byte[written.length];
        assertEquals(written.length, spanningInputStream.read(read));
        assertArrayEquals(written, read);
        assertEquals(read.length, spanningInputStream.getFilePointer());

        assertEquals(-1, spanningInputStream.read(read));
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
        File volume = new File(temporaryFolder.getRoot(), "volume");
        FileSpanningOutputStream spanningOutputStream = new FileSpanningOutputStream(volume, 1024);

        // write 100K of random data
        byte[] written = new byte[100000];
        new Random().nextBytes(written);
        spanningOutputStream.write(written);
        spanningOutputStream.close();

        // open the volumes
        int volumes = spanningOutputStream.getVolumes();
        FileSpanningInputStream spanningInputStream = new FileSpanningInputStream(volume, volumes);
        assertEquals(0, spanningInputStream.getFilePointer());

        // skip half of the data
        int skip = written.length / 2;
        assertEquals(skip, spanningInputStream.skip(skip));
        assertEquals(skip, spanningInputStream.getFilePointer());

        // read the remaining half
        byte[] read = new byte[written.length - skip];
        assertEquals(read.length, spanningInputStream.read(read));
        assertEquals(written.length, spanningInputStream.getFilePointer());

        // verify the read data matches that expected
        for (int i = 0; i < read.length; ++i)
        {
            assertEquals(written[i + skip], read[i]);
        }

        // check that there is nothing left to read
        assertEquals(-1, spanningInputStream.read(read));
        spanningInputStream.close();
    }

    /**
     * Writes 10GB of random data and verifies it can be read back in.
     *
     * @throws IOException for any I/O exception
     */
    @Ignore("This is a long running test. It should be run when making changes to FileSpanningInputStream or "
                    + "FileSpanningOutputStream")
    @Test
    public void testLargeFiles() throws IOException
    {
        File volume = new File(temporaryFolder.getRoot(), "volume");
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
            assertEquals(written.length, spanningInputStream.read(read));
            byte id = (byte) (i & 0xFF);
            assertEquals(id, read[0]);
            assertEquals(id, read[read.length - 1]);
        }

        assertEquals(-1, spanningInputStream.read(read));
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
        assertTrue(volumes > 1);

        for (int i = 0; i < volumes; ++i)
        {
            File volume = (i == 0) ? new File(basePath) : new File(basePath + "." + i);
            assertTrue(volume.exists());
            if (i != volumes - 1)
            {
                // verify the length of all but the last volume, whose length is unpredictable
                assertEquals(maxSize, volume.length());
            }
        }
    }
}
