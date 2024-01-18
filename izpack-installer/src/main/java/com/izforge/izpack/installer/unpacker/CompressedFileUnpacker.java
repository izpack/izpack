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

import com.izforge.izpack.api.data.PackCompression;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.StreamSupport;
import com.izforge.izpack.util.os.FileQueue;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Unpacker for compressed files.
 */
public class CompressedFileUnpacker extends FileUnpacker
{
    private final PackCompression compressionFormat;

    /**
     * Constructs a <tt>CompressedFileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param queue       the file queue. May be <tt>null</tt>
     */
    public CompressedFileUnpacker(Cancellable cancellable, FileQueue queue, PackCompression compressionFormat)
    {
        super(cancellable, queue);
        this.compressionFormat = compressionFormat;
    }

    /**
     * Unpacks a pack file.
     *
     * @param file            the pack file meta-data
     * @param packInputStream the pack input stream
     * @param target          the target
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer exception
     */
    @Override
    public void unpack(PackFile file, InputStream packInputStream, File target)
            throws IOException, InstallerException
    {
        final long fileSize = file.size();
        final long fileLength = file.length();
        final long backReferenceFileLength = file.isBackReference() ? file.getLinkedPackFile().size() : fileSize;
        final Path tmpfile = Files.createTempFile("izpack-uncompress", null);
        try
        {
            try (OutputStream fo = Files.newOutputStream(tmpfile))
            {
                final long bytesUnpacked = IOUtils.copyLarge(packInputStream, fo, 0, fileSize);
                if (!(bytesUnpacked == fileSize || bytesUnpacked == backReferenceFileLength))
                {
                    throw new IOException("File size mismatch when reading from pack: " + file.getRelativeSourcePath());
                }
            }
            try (InputStream in = Files.newInputStream(tmpfile);
                 InputStream finalStream = StreamSupport.compressedInput(compressionFormat, in))
            {
                final long bytesUncompressed = copy(file, finalStream, target);
                if (bytesUncompressed != fileLength)
                {
                    throw new IOException("File size mismatch when uncompressing from pack: " + file.getRelativeSourcePath());
                }
            }
        }
        finally
        {
            Files.deleteIfExists(tmpfile);
        }
    }
}
