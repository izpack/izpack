/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.core.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * An <tt>OutputStream</tt> which transparently spans over multiple volumes. The
 * size of the volumes and an additional space for the first volume can be
 * specified.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @author Tim Anderson
 */
public class FileSpanningOutputStream extends OutputStream
{

    /**
     * One kilobyte.
     */
    public static final long KB = 1000;

    /**
     * One megabyte.
     */
    public static final long MB = 1000 * KB;

    /**
     * The default size of a volume.
     */
    public static final long DEFAULT_VOLUME_SIZE = 650 * MB;

    /**
     * The no. of bytes allocated to the magic number written at the start of
     * each volume.
     */
    protected static final int MAGIC_NUMBER_LENGTH = 10;

    /**
     * The minimum volume size. Need to be able to fit at least
     * MAGIC_NUMBER_LENGTH + 1 bytes per volume.
     */
    private static final int MIN_VOLUME_SIZE = MAGIC_NUMBER_LENGTH + 1;

    /**
     * The spanning output stream.
     */
    private SpanningOutputStream spanningOutputStream;

    /**
     * The stream that .
     */
    private GZIPOutputStream gzipOutputStream;

    /**
     * The current offset in the (uncompressed) output stream.
     */
    private long filePointer;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileSpanningOutputStream.class.getName());

    /**
     * Constructs a <tt>FileSpanningOutputStream</tt> with specified initial
     * volume path, a maximum size for the first volume, and a maximum volume
     * size for all subsequent volumes.
     * <p/>
     * The <tt>maxFirstVolumeSize</tt> argument may be used to specify a
     * different maximum volume size for the first volume; this is useful to
     * leave space on media for other files.
     *
     * @param volumePath the path to the first volume
     * @param maxFirstVolumeSize the maximum size of the first volume
     * @param maxVolumeSize the maximum volume size for subsequent volumes
     * @throws IOException for any I/O error
     */
    public FileSpanningOutputStream(String volumePath, long maxFirstVolumeSize, long maxVolumeSize) throws IOException
    {
        this(new File(volumePath), maxFirstVolumeSize, maxVolumeSize);
    }

    /**
     * Constructs a <tt>FileSpanningOutputStream</tt> with specified initial
     * volume and a maximum volume size.
     *
     * @param volume the first volume
     * @param maxVolumeSize the maximum volume size
     * @throws IOException for any I/O error
     */
    public FileSpanningOutputStream(File volume, long maxVolumeSize) throws IOException
    {
        this(volume, maxVolumeSize, maxVolumeSize);
    }

    /**
     * Constructs a <tt>FileSpanningOutputStream</tt> with specified initial
     * volume, a maximum size for the first volume, and a maximum volume size
     * for all subsequent volumes.
     * <p/>
     * The <tt>maxFirstVolumeSize</tt> argument may be used to specify a
     * different maximum volume size for the first volume; this is useful to
     * leave space on media for other files.
     *
     * @param volume the first volume
     * @param maxFirstVolumeSize the maximum size of the first volume
     * @param maxVolumeSize the maximum volume size for subsequent volumes
     * @throws IOException for any I/O error
     */
    public FileSpanningOutputStream(File volume, long maxFirstVolumeSize, long maxVolumeSize) throws IOException
    {
        spanningOutputStream = new SpanningOutputStream(volume, maxFirstVolumeSize, maxVolumeSize);
        gzipOutputStream = new GZIPOutputStream(spanningOutputStream);
    }

    /**
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException
    {
        flush();
        gzipOutputStream.close();
    }

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        gzipOutputStream.write(b, off, len);
        // increase filePointer by written bytes
        filePointer += len;
    }

    /**
     * (non Javadoc)
     *
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * (non Javadoc)
     *
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException
    {
        gzipOutputStream.write(b);
        // increase filePointer by written byte
        filePointer++;
    }

    /**
     * (non Javadoc)
     *
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException
    {
        gzipOutputStream.flush();
    }

    /**
     * Returns the number of volumes spanned.
     *
     * @return the number of volumes
     */
    public int getVolumes()
    {
        return spanningOutputStream.getVolumes();
    }

    /**
     * Returns the current offset in the (uncompressed) output stream.
     *
     * @return the current offset
     */
    public long getFilePointer()
    {
        return filePointer;
    }

    /**
     * Helper to format the volume magic number.
     *
     * @param magic the magic number
     * @return the formatted magic number
     */
    static String formatMagic(byte[] magic)
    {
        StringBuilder builder = new StringBuilder();
        for (byte b : magic)
        {
            if (builder.length() != 0)
            {
                builder.append(' ');
            }
            builder.append(Integer.toHexString((int) b & 0xFF));
        }
        return builder.toString();
    }

    /**
     * The <tt>SpanningOutputStream</tt> sits between the
     * <tt>GZIPOutputStream</tt> and the volume
     * <tt>FileOutputStream</tt>. When a volume fills, it is closed and a new
     * one opened and written to.
     */
    private static class SpanningOutputStream extends ByteCountingOutputStream
    {

        /**
         * The maximum size of each volume.
         */
        private final long maxVolumeSize;

        /**
         * The index of the current volume.
         */
        private int index;

        /**
         * The base path to each volume.
         */
        private String basePath;

        /**
         * The magic number written at the start of each volume.
         */
        private byte[] magic;

        /**
         * The space to leave free on the first volume.
         */
        private final long maxFirstVolumeSize;

        /**
         * Constructs a <tt>SpanningOutputStream</tt>.
         *
         * @param volume the first volume
         * @param maxFirstVolumeSize the maximum size of the first volume
         * @param maxVolumeSize the maximum volume size for subsequent volumes
         * @throws IOException for any I/O error
         */
        public SpanningOutputStream(File volume, long maxFirstVolumeSize, long maxVolumeSize) throws IOException
        {
            super(new FileOutputStream(volume));
            if (maxVolumeSize < MIN_VOLUME_SIZE)
            {
                throw new IllegalArgumentException("Argument 'maxVolumeSize' is invalid: " + maxVolumeSize);
            }
            if (maxFirstVolumeSize < MIN_VOLUME_SIZE)
            {
                throw new IllegalArgumentException("Argument 'maxFirstVolumeSize' is invalid: " + maxFirstVolumeSize);
            }
            basePath = volume.getAbsolutePath();
            this.maxVolumeSize = maxVolumeSize;
            this.maxFirstVolumeSize = maxFirstVolumeSize;
            magic = generateMagicNumber();

            initVolume();
        }

        /**
         * (non Javadoc)
         *
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            // calculate the available bytes
            long available = getAvailable();

            if (available < len)
            {
                // there's not enough space available, so write as much as possible, create the next volume, and
                // call this recursively
                LOGGER.fine("Not enough space left on volume. (available: " + available + ")");
                if (available > 0)
                {
                    super.write(b, off, (int) available);
                    off += available;
                    len -= available;
                }
                createNextVolume();
                write(b, off, len);
            }
            else
            {
                super.write(b, off, len);
            }
        }

        /**
         * (non Javadoc)
         *
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write(int b) throws IOException
        {
            long available = getAvailable();
            if (available == 0)
            {
                createNextVolume();
            }
            super.write(b);
        }

        /**
         * Closes the current volume and creates the next.
         *
         * @throws IOException for any I/O error
         */
        private void createNextVolume() throws IOException
        {
            // close current volume
            close();

            // create the next volume
            ++index;
            String name = basePath + "." + index;
            setOutputStream(new FileOutputStream(name));
            initVolume();
        }

        /**
         * Returns the number of volumes spanned.
         *
         * @return the number of volumes
         */
        public int getVolumes()
        {
            return index + 1;
        }

        /**
         * Initialises the volume.
         * <p/>
         * This writes a random byte array at the start of the volume. Each
         * volume in the collection will have the same bytes at the start, to
         * detect an incorrect volume being used when read back in.
         *
         * @throws IOException
         */
        private void initVolume() throws IOException
        {
            write(magic);
        }

        /**
         * Returns the available space in the volume.
         *
         * @return the available space, in bytes
         */
        private long getAvailable()
        {
            long count = getByteCount();
            return (index == 0) ? maxFirstVolumeSize - count : maxVolumeSize - count;
        }

        /**
         * Generates the magic number to write to each volume.
         *
         * @return the magic number
         */
        private byte[] generateMagicNumber()
        {
            byte[] result = new byte[MAGIC_NUMBER_LENGTH];
            Random random = new Random();
            random.nextBytes(result);
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.fine("Created new magic number for SpanningOutputStream: " + formatMagic(magic));
            }
            return result;
        }
    }

}
