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

package com.izforge.izpack.installer.multiunpacker;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.XPackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.core.io.FileSpanningInputStream;
import com.izforge.izpack.installer.unpacker.Cancellable;
import com.izforge.izpack.installer.unpacker.FileUnpacker;
import com.izforge.izpack.util.os.FileQueue;


/**
 * A multi-volume file unpacker.
 *
 * @author Tim Anderson
 */
public class MultiVolumeFileUnpacker extends FileUnpacker
{
    /**
     * The volumes.
     */
    private final FileSpanningInputStream volumes;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumeFileUnpacker.class.getName());

    /**
     * Constructs a <tt>MultiVolumeFileUnpacker</tt>.
     *
     * @param volumes     the input stream
     * @param cancellable determines if unpacking should be cancelled
     * @param queue       the file queue. May be {@code null}
     */
    public MultiVolumeFileUnpacker(FileSpanningInputStream volumes, Cancellable cancellable, FileQueue queue)
    {
        super(cancellable, queue);
        this.volumes = volumes;
    }

    /**
     * Unpacks a pack file.
     *
     * @param file            the pack file meta-data
     * @param packInputStream the pack input stream
     * @param target          the target
     * @throws InterruptedIOException if the unpack is cancelled
     * @throws IOException            for any I/O error
     * @throws InstallerException     for any installer exception
     */
    @Override
    public void unpack(PackFile file, ObjectInputStream packInputStream, File target)
            throws IOException, InstallerException
    {
        // read in the position of this file
        long position = ((XPackFile) file).getArchiveFilePosition();

        if (volumes.getFilePointer() < position)
        {
            // need to skip to the correct position
            logger.fine("Skipping bytes to get to file " + target.getName()
                                + " (" + volumes.getFilePointer() + "<" + position
                                + ") target is: " + (position - volumes.getFilePointer()));
            skip(position - volumes.getFilePointer());
        }

        if (volumes.getFilePointer() > position)
        {
            throw new IOException("Error, can't access file in pack.");
        }

        copy(file, volumes, target);
    }

    /**
     * Skips bytes in a stream.
     *
     * @param bytes the no. of bytes to skip
     * @throws IOException for any I/O error, or if the no. of bytes skipped doesn't match that expected
     */
    protected void skip(long bytes) throws IOException
    {
        long skipped = volumes.skip(bytes);
        if (skipped != bytes)
        {
            throw new IOException("Expected to skip: " + bytes + " in stream but skipped: " + skipped);
        }
    }
}
