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
import com.izforge.izpack.util.os.FileQueue;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;


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
        File tmpfile = File.createTempFile("izpack-uncompress", null, FileUtils.getTempDirectory());
        final long fileSize = file.size();
        final long fileLength = file.length();
        final long backReferenceFileLength = file.isBackReference() ? file.getLinkedPackFile().size() : fileSize;

        OutputStream fo = null;
        InputStream finalStream = null;

        try
        {
            fo = IOUtils.buffer(FileUtils.openOutputStream(tmpfile));
            final long bytesUnpacked = IOUtils.copyLarge(packInputStream, fo, 0, fileSize);
            fo.flush();
            fo.close();

            if (!(bytesUnpacked == fileSize || bytesUnpacked == backReferenceFileLength))
            {
                throw new IOException("File size mismatch when reading from pack: " + file.getRelativeSourcePath());
            }

            InputStream in = IOUtils.buffer(FileUtils.openInputStream(tmpfile));

            if (compressionFormat == PackCompression.DEFLATE)
            {
                DeflateParameters deflateParameters = new DeflateParameters();
                deflateParameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
                finalStream = new DeflateCompressorInputStream(in, deflateParameters);
            }
            else
            {
                finalStream = new CompressorStreamFactory().createCompressorInputStream(compressionFormat.toName(), in);
            }

            final long bytesUncompressed = copy(file, finalStream, target);

            if (bytesUncompressed != fileLength)
            {
                throw new IOException("File size mismatch when uncompressing from pack: " + file.getRelativeSourcePath());
            }

        }
        catch (CompressorException e)
        {
            throw new IOException("An exception occurred whilst unpacking: " + file.getRelativeSourcePath() + ": " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.closeQuietly(fo);
            IOUtils.closeQuietly(finalStream);
            FileUtils.deleteQuietly(tmpfile);
        }
    }
}
