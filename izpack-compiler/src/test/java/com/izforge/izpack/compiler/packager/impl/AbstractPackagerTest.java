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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.merge.MergeManager;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPackagerTest
{

    /**
     * The merge manager.
     */
    private MergeManager mergeManager;

    @Before
    public void setUp()
    {
        mergeManager = mock(MergeManager.class);
    }

    @Test
    public void noSplash() throws IOException
    {
        PackagerBase packager = createPackager(Mockito.mock(JarOutputStream.class), mergeManager);
        packager.writeManifest();

        verify(mergeManager).addResourceToMerge(anyString(), eq("META-INF/MANIFEST.MF"));
    }

    @Test
    public void noGuiPrefs() throws IOException
    {
        PackagerBase packager = createPackager(Mockito.mock(JarOutputStream.class), mergeManager);
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

        assertTrue(file.delete());
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
        File jar = File.createTempFile("installer", ".jar");

        JarOutputStream output = new JarOutputStream(new FileOutputStream(jar));
        output.setPreventClose(true);
        PackagerBase packager = createPackager(output, mergeManager);

        PackInfo packInfo = new PackInfo("Core", "Core", null, true, false, null, true, size);
        long fileSize = 0;
        for (File file : files)
        {
            packInfo.addFile(file.getParentFile(), file, "$INSTALL_PATH/" + file.getName(), null,
                             OverrideType.OVERRIDE_TRUE, null, Blockable.BLOCKABLE_NONE, null, null);
            fileSize += file.length();
        }
        packager.addPack(packInfo);
        packager.createInstaller();

        InputStream jarEntry = getJarEntry("resources/packs.info", jar);

        ObjectInputStream packStream = new ObjectInputStream(jarEntry);
        int packs = packStream.readInt();
        assertEquals(1, packs);
        Pack pack = (Pack) packStream.readObject();
        assertEquals(expectedSize, pack.getSize());
        assertEquals(expectedFileSize, fileSize);

        jarEntry.close();
        packStream.close();
        assertTrue(jar.delete());
    }

    /**
     * Helper to return a stream to the content of a jar entry.
     *
     * @param name the name of the entry
     * @param jar  the jar
     * @return a stream to the content
     * @throws IOException for any I/O error
     */
    private InputStream getJarEntry(String name, File jar) throws IOException
    {
        JarInputStream input = new JarInputStream(new FileInputStream(jar));
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
        File file = File.createTempFile("data", ".txt");
        PrintStream printStream = new PrintStream(file);
        printStream.print(text);
        printStream.close();
        return file;
    }

    public static File getBaseDir()
    {
        File path = null;
        try
        {
            path = new File(AbstractPackagerTest.class.getClassLoader().getResource("").toURI());
            // path: <root>/target/test-classes
            path = path.getParentFile(); // <root>/target/
            path = path.getParentFile(); // <root>
        }
        catch (URISyntaxException e)
        {
            Assert.fail(e.getMessage());
        }
        return path;
    }
}
