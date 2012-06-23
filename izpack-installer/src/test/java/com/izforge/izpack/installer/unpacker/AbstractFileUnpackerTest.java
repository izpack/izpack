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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Abstract base class for {@link FileUnpacker} tests.
 *
 * @author Tim Anderson
 */
public abstract class AbstractFileUnpackerTest
{
    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The librarian.
     */
    private Librarian librarian;

    /**
     * Cancellable implementation.
     */
    private Cancellable cancellable;

    /**
     * Sets up the test case.
     *
     * @throws IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        librarian = Mockito.mock(Librarian.class);
        cancellable = new Cancellable()
        {
            @Override
            public boolean isCancelled()
            {
                return false;
            }
        };
    }

    /**
     * Tests unpacking a loose file.
     *
     * @throws Exception throws exception for any error
     */
    @Test
    public void testUnpack() throws Exception
    {
        File baseDir = temporaryFolder.getRoot();
        File sourceDir = baseDir.getAbsoluteFile();

        File source = createSourceFile(baseDir);
        File target = getTargetFile(baseDir);

        FileQueue queue = new FileQueueFactory(Platforms.WINDOWS, librarian).create();
        FileUnpacker unpacker = createUnpacker(sourceDir, queue);

        PackFile file = createPackFile(baseDir, source, target, Blockable.BLOCKABLE_NONE);
        assertFalse(target.exists());

        ObjectInputStream packStream = createPackStream(source);
        unpacker.unpack(file, packStream, target);
        assertTrue(queue.isEmpty());

        checkTarget(source, target);
    }

    /**
     * Verifies that a file that does not have a blockable type of {@link Blockable#BLOCKABLE_NONE} is queued rather
     * than unpacked to its target.
     *
     * @throws Exception for any error
     */
    @Test
    public void testQueue() throws Exception
    {
        checkQueue(Blockable.BLOCKABLE_AUTO);
        checkQueue(Blockable.BLOCKABLE_FORCE);
    }

    /**
     * Creates a new source file.
     *
     * @param baseDir the base directory
     * @return the source file
     * @throws IOException for any I/O error
     */
    protected File createSourceFile(File baseDir) throws IOException
    {
        File file = new File(baseDir, "source.txt");
        PrintWriter writer = new PrintWriter(file);
        writer.println("Here we go");
        writer.close();
        return file;
    }

    /**
     * Returns the target file.
     *
     * @param baseDir the base directory
     * @return the target file
     */
    protected File getTargetFile(File baseDir)
    {
        return new File(baseDir, "target.txt");
    }

    /**
     * Creates a pack file stream.
     *
     * @param source the source
     * @return a new stream
     * @throws IOException for any I/O error
     */
    protected ObjectInputStream createPackStream(File source) throws IOException
    {
        return Mockito.mock(ObjectInputStream.class);
    }

    /**
     * Helper to create an unpacker.
     *
     * @param sourceDir the source directory
     * @param queue     the file queue
     * @return a new unpacker
     * @throws IOException for any I/O error
     */
    protected abstract FileUnpacker createUnpacker(File sourceDir, FileQueue queue) throws IOException;

    /**
     * Helper to create a new pack file.
     *
     * @param baseDir   the base directory
     * @param source    the source file
     * @param target    the target file
     * @param blockable the blockable type
     * @return a new pack file
     * @throws java.io.IOException if the source file doesn't exist
     */
    protected PackFile createPackFile(File baseDir, File source, File target, Blockable blockable) throws IOException
    {
        return new PackFile(baseDir, source, target.getName(), null, OverrideType.OVERRIDE_TRUE, null, blockable);
    }

    /**
     * The cancellable.
     *
     * @return the cancellable
     */
    protected Cancellable getCancellable()
    {
        return cancellable;
    }

    /**
     * The librarian,
     *
     * @return the librarian
     */
    protected Librarian getLibrarian()
    {
        return librarian;
    }

    /**
     * Verifies the target matches the source.
     *
     * @param source the source
     * @param target the target
     * @throws IOException for any I/O error
     */
    protected void checkTarget(File source, File target) throws IOException
    {
        assertTrue(target.exists());
        assertEquals(source.length(), target.length());
        assertEquals(source.lastModified(), target.lastModified());
        byte[] sourceBytes = getContent(source);
        byte[] targetBytes = getContent(target);
        assertArrayEquals(sourceBytes, targetBytes);
    }

    /**
     * Verifies that a blockable file is queued rather than copied to the target directory.
     *
     * @param blockable the blockable type
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer error
     */
    private void checkQueue(Blockable blockable) throws IOException, InstallerException
    {
        File baseDir = temporaryFolder.getRoot();
        File sourceDir = baseDir.getAbsoluteFile();

        File source = createSourceFile(baseDir);
        File target = getTargetFile(baseDir);

        FileQueue queue = new FileQueueFactory(Platforms.WINDOWS, librarian).create();
        FileUnpacker unpacker = createUnpacker(sourceDir, queue);
        PackFile file = createPackFile(baseDir, source, target, blockable);

        unpacker.unpack(file, createPackStream(source), target);
        assertNotNull(queue);
        assertEquals(1, queue.getOperations().size());
        assertFalse(target.exists());
    }

    /**
     * Returns the contents of a file as a byte array.
     *
     * @param file the file
     * @return the contents of the file
     * @throws IOException for any I/O error
     */
    private byte[] getContent(File file) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(file);
        IoHelper.copyStream(in, out);
        in.close();
        out.close();
        return out.toByteArray();
    }

}
