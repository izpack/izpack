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

import static com.izforge.izpack.util.IoHelper.copyStreamToJar;
import static java.nio.file.Files.createTempFile;
import static org.apache.commons.compress.java.util.jar.Pack200.newPacker;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.IOUtils.buffer;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.util.os.FileQueue;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.*;
import org.apache.commons.compress.java.util.jar.Pack200;
import org.apache.commons.io.output.CountingOutputStream;

/**
 * Tests the {@link Pack200FileUnpacker} class.
 *
 * @author Tim Anderson
 */
public class Pack200FileUnpackerTest extends AbstractFileUnpackerTest
{
    private PackFile packFile;
    private File sourceFile;

    /**
     * Verifies the target matches the source.
     *
     * @param source the source
     * @param target the target
     */
    @Override
    protected void checkTarget(File source, File target) throws IOException
    {
        assertThat(target).exists();
        assertThat(target.lastModified()).isEqualTo(source.lastModified());

        // for pack200 can't do a size comparison as it modifies the jar structure, so compare the jar contents
        byte[] sourceBytes = getEntry(source, "source.txt");
        byte[] targetBytes = getEntry(target, "source.txt");
        assertThat(targetBytes).isEqualTo(sourceBytes);
    }

    @Override
    protected InputStream createPackStream(File source) throws IOException
    {
        File tmpfile = null;
        JarFile jar = null;

        try
        {
            tmpfile = createTempFile(temporaryFolder, "izpack-compress", ".pack200").toFile();
            CountingOutputStream proxyOutputStream = new CountingOutputStream(openOutputStream(tmpfile));
            OutputStream bufferedStream = buffer(proxyOutputStream);

            Pack200.Packer packer = createPack200Packer(this.packFile);
            jar = new JarFile(this.packFile.getFile());
            packer.pack(jar, bufferedStream);

            bufferedStream.flush();
            this.packFile.setSize(proxyOutputStream.getByteCount());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(openInputStream(tmpfile), out);
            out.close();
            return new ByteArrayInputStream(out.toByteArray());
        }
        finally
        {
            if (jar != null)
            {
                jar.close();
            }
            deleteQuietly(tmpfile);
        }
    }

    private Pack200.Packer createPack200Packer(PackFile packFile)
    {
        Pack200.Packer packer = newPacker();
        Map<String, String> defaultPackerProperties = packer.properties();
        Map<String,String> localPackerProperties = packFile.getPack200Properties();
        if (localPackerProperties != null)
        {
            defaultPackerProperties.putAll(localPackerProperties);
        }
        return packer;
    }

    /**
     * Helper to create an unpacker.
     *
     * @param sourceDir the source directory
     * @param queue the file queue. May be {@code null}
     * @return a new unpacker
     */
    @Override
    protected FileUnpacker createUnpacker(File sourceDir, FileQueue queue) throws IOException
    {
        PackResources resources = mock(PackResources.class);
        return new Pack200FileUnpacker(getCancellable(), resources, queue);
    }

    /**
     * Creates a new source file.
     *
     * @param baseDir the base directory
     * @return the source file
     * @throws IOException for any I/O error
     */
    @Override
    protected File createSourceFile(File baseDir) throws IOException
    {
        File source = super.createSourceFile(baseDir);

        this.sourceFile = new File(baseDir, "source.jar");
        JarOutputStream srcJar = new JarOutputStream(new FileOutputStream(this.sourceFile));
        FileInputStream stream = new FileInputStream(source);
        try
        {
            copyStreamToJar(stream::transferTo, srcJar, source.getName(), source.lastModified());
        }
        finally
        {
            closeQuietly(stream);
            closeQuietly(srcJar);
        }

        return this.sourceFile;
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
                    copy(stream, bytes);
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

    @Override
    protected PackFile createPackFile(File baseDir, File source, File target, Blockable blockable) throws IOException
    {
        PackFile packFile = new PackFile(baseDir, source, target.getName(), null, OverrideType.OVERRIDE_TRUE,
                        null, blockable, new HashMap<String, String>());
        packFile.setStreamResourceName("packs/pack200-" + packFile.getId());
        packFile.setStreamOffset(0);
        return (this.packFile = packFile);
    }
}
