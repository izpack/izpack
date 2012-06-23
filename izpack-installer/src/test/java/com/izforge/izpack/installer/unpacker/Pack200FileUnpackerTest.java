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

package com.izforge.izpack.installer.unpacker;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.ZipEntry;

import org.mockito.Mockito;

import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.os.FileQueue;

/**
 * Tests the {@link Pack200FileUnpacker} class.
 *
 * @author Tim Anderson
 */
public class Pack200FileUnpackerTest extends AbstractFileUnpackerTest
{

    /**
     * Verifies the target matches the source.
     *
     * @param source the source
     * @param target the target
     */
    @Override
    protected void checkTarget(File source, File target) throws IOException
    {
        assertTrue(target.exists());
        assertEquals(source.lastModified(), target.lastModified());

        // for pack200 can't do a size comparison as it modifies the jar structure, so compare the jar contents
        byte[] sourceBytes = getEntry(source, "source.txt");
        byte[] targetBytes = getEntry(target, "source.txt");
        assertArrayEquals(sourceBytes, targetBytes);
    }

    /**
     * Helper to create an unpacker.
     *
     *
     * @param sourceDir the source directory
     * @param queue the file queue. May be {@code null}
     * @return a new unpacker
     */
    @Override
    protected FileUnpacker createUnpacker(File sourceDir, FileQueue queue) throws IOException
    {
        PackResources resources = Mockito.mock(PackResources.class);
        JarInputStream stream = new JarInputStream(new FileInputStream(new File(sourceDir, "installer.jar")));
        JarEntry entry;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while ((entry = stream.getNextJarEntry()) != null)
        {
            if (entry.getName().endsWith("packs/pack200-1"))
            {
                IoHelper.copyStream(stream, bytes);
                break;
            }
        }
        when(resources.getInputStream("packs/pack200-1")).thenReturn(new ByteArrayInputStream(bytes.toByteArray()));
        return new Pack200FileUnpacker(getCancellable(), resources, Pack200.newUnpacker(), queue);
    }

    /**
     * Creates a new source file.
     *
     * @param baseDir the base directory
     * @return the source file
     * @throws java.io.IOException for any I/O error
     */
    @Override
    protected File createSourceFile(File baseDir) throws IOException
    {
        File source = super.createSourceFile(baseDir);
        Pack200.Packer packer = Pack200.newPacker();
        File src = new File(baseDir, "source.jar");
        JarOutputStream srcJar = new JarOutputStream(new FileOutputStream(src));

        FileInputStream stream = new FileInputStream(source);
        IoHelper.copyStreamToJar(stream, srcJar, source.getName(), source.lastModified());
        srcJar.close();

        JarOutputStream installerJar = new JarOutputStream(
                new FileOutputStream(new File(baseDir, "installer.jar")));
        installerJar.putNextEntry(new ZipEntry("/resources/packs/pack200-1"));
        JarFile jar = new JarFile(src);
        packer.pack(jar, installerJar);
        jar.close();
        installerJar.closeEntry();
        installerJar.close();
        return src;
    }

    /**
     * Returns the target file.
     *
     * @param baseDir the base directory
     * @return the target file
     */
    protected File getTargetFile(File baseDir)
    {
        return new File(baseDir, "target.jar");
    }

    /**
     * Creates a pack file stream.
     *
     * @param source the source file
     * @return a new stream
     * @throws IOException for any I/O error
     */
    @Override
    protected ObjectInputStream createPackStream(File source) throws IOException
    {
        ObjectInputStream stream = Mockito.mock(ObjectInputStream.class);
        when(stream.readInt()).thenReturn(1);
        return stream;
    }

    /**
     * Returns a file from a jar as a byte array.
     *
     * @param file the jar file
     * @param name the entry name
     * @return the file content
     * @throws IOException for any I/O error
     */
    private byte[] getEntry(File file, String name) throws IOException
    {
        JarInputStream stream = new JarInputStream(new FileInputStream(file));
        try
        {
            JarEntry entry;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            while ((entry = stream.getNextJarEntry()) != null)
            {
                if (entry.getName().endsWith(name))
                {
                    IoHelper.copyStream(stream, bytes);
                    return bytes.toByteArray();
                }
            }
            fail("Entry not found: " + name);
        }
        finally
        {
            stream.close();
        }
        return null;
    }

}
