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
package com.izforge.izpack.compiler.packager.impl;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.writeString;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackInfo;
import com.izforge.izpack.merge.MergeManager;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPackagerTest
{
    @TempDir
    Path fixtureDirectory;

    /**
     * The merge manager.
     */
    private MergeManager mergeManager;

    @BeforeEach
    public void setUp()
    {
        mergeManager = mock(MergeManager.class);
    }

    @Test
    public void noSplash() throws IOException
    {
        PackagerBase packager = createPackager(mock(JarOutputStream.class), mergeManager);
        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void noGuiPrefs() throws IOException
    {
        PackagerBase packager = createPackager(mock(JarOutputStream.class), mergeManager);
        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), anyString());
    }

    /**
     * Verifies that the pack size can be specified.
     * <p/>
     * Given:
     * <ul>
     * <li>{@code size} = the specified pack size; and</li>
     * <li>{@code fileSize} = the total size of all files in the pack</li>
     * </ul>
     * The Pack.getSize() method will return:
     * <ul>
     * <li>{@code size} if {@code size &gt; fileSize}</li>
     * <li>{@code fileSize} if {@code size &lt; fileSize}</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    @Test
    public void testSize() throws Exception
    {
        File file = createTextFile("This is a test");
        long size = 1000000;
        long fileSize = file.length();

        // pack size not specified - should be set to fileSize
        checkSize(fileSize, fileSize, 0, file);

        // pack size specified, and > file size. Should be set to pack size
        checkSize(size, 0, size);

        // pack size specified, and > file size. Should be set to pack size
        checkSize(size, fileSize, size, file);

        // pack size specified, and < file size. Should be set to fileSize
        long tooSmall = fileSize - 1;
        checkSize(fileSize, fileSize, tooSmall, file);

        assertThat(file.delete()).isTrue();
    }

    /**
     * Helper to create a packager that writes to the provided jar.
     *
     * @param jar          the jar stream
     * @param mergeManager the merge manager
     * @return a new packager
     */
    protected abstract PackagerBase createPackager(JarOutputStream jar, MergeManager mergeManager);

    /**
     * Verifies that the pack size is calculated correctly.
     *
     * @param expectedSize     the expected pack size
     * @param expectedFileSize the expected total file size
     * @param size             the pack size. May be {@code 0}
     * @param files            the pack files
     * @throws Exception for any error
     */
    private void checkSize(long expectedSize, long expectedFileSize, long size, File... files) throws Exception
    {
        Path jar = createTempFile(fixtureDirectory, "installer", ".jar");

        JarOutputStream output = new JarOutputStream(newOutputStream(jar));
        PackagerBase packager = createPackager(output, mergeManager);

        PackInfo packInfo = new PackInfo("Core", "Core", null, true, false, null, true, size);
        long fileSize = 0;
        for (File file : files)
        {
            packInfo.addFile(file.getParentFile(), file, "$INSTALL_PATH/" + file.getName(), null,
                             OverrideType.OVERRIDE_TRUE, null, Blockable.BLOCKABLE_NONE, null, null, null);
            fileSize += file.length();
        }
        packager.addPack(packInfo);
        packager.createInstaller();

        InputStream jarEntry = getJarEntry("resources/packs.info", jar);

        ObjectInputStream packStream = new ObjectInputStream(jarEntry);
        List<PackInfo> packsInfo = (List<PackInfo>) packStream.readObject();
        assertThat(packsInfo).hasSize(1);
        Pack pack = packsInfo.get(0).getPack();
        assertThat(pack.getSize()).isEqualTo(expectedSize);
        assertThat(fileSize).isEqualTo(expectedFileSize);

        closeQuietly(jarEntry);
        closeQuietly(packStream);
        delete(jar);
    }

    /**
     * Helper to return a stream to the content of a jar entry.
     *
     * @param name the name of the entry
     * @param jar  the jar
     * @return a stream to the content
     * @throws IOException for any I/O error
     */
    private InputStream getJarEntry(String name, Path jar) throws IOException
    {
        JarInputStream input = new JarInputStream(newInputStream(jar));
        JarEntry entry;
        while ((entry = input.getNextJarEntry()) != null)
        {
            if (entry.getName().equals(name))
            {
                return input;
            }
        }
        fail("Failed to find jar entry: " + name);
        return null;
    }

    /**
     * Helper to create a temporary text file containing the specified text.
     *
     * @param text the text
     * @return the new file
     * @throws IOException for any I/O error
     */
    private File createTextFile(String text) throws IOException
    {
        Path file = createTempFile(fixtureDirectory, "data", ".txt");
        writeString(file, text, defaultCharset());
        return file.toFile();
    }

    public static File getBaseDir()
    {
        File path = null;
        try
        {
            URL url = AbstractPackagerTest.class.getClassLoader().getResource("");
            if (url != null)
            {
                URI uri = url.toURI();
                path = new File(uri);
                // path: <root>/target/test-classes
                path = path.getParentFile(); // <root>/target/
                path = path.getParentFile(); // <root>
            }
            else
            {
                fail("Resource not found");
            }
        }
        catch (URISyntaxException e)
        {
            fail(e.getMessage());
        }
        return path;
    }
}
