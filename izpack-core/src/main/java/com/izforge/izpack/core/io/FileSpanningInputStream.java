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

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * An <tt>InputStream</tt> which transparently spans over multiple volumes.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @author Tim Anderson
 * @see FileSpanningOutputStream
 */
public class FileSpanningInputStream extends InputStream
{

    /**
     * The spanning input stream. This sits between the volume file input stream
     * and {@link #zippedInputStream}.
     */
    private final SpanningInputStream spanningInputStream;

    /**
     * The sip stream.
     */
    private GZIPInputStream zippedInputStream;

    /**
     * The absolute offset into the volumes.
     */
    private long filePointer;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FileSpanningInputStream.class.getName());

    /**
     * Constructs a <tt>FileSpanningInputStream</tt>.
     *
     * @param volume the first volume to read
     * @param volumes the no. of volumes
     * @throws CorruptVolumeException if the volume magic no. cannot be read
     * @throws IOException for any other I/O exception
     */
    public FileSpanningInputStream(File volume, int volumes) throws IOException
    {
        spanningInputStream = new SpanningInputStream(volume, volumes);
        zippedInputStream = new GZIPInputStream(spanningInputStream);
    }

    /**
     * Sets the volume locator.
     *
     * @param locator the locator. May be <tt>null</tt>
     */
    public void setLocator(VolumeLocator locator)
    {
        spanningInputStream.setLocator(locator);
    }

    /**
     * (non-Javadoc)
     *
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException
    {
        return zippedInputStream.available();
    }

    /**
     * (non-Javadoc)
     *
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException
    {
        zippedInputStream.close();
        spanningInputStream.close();
    }

    /**
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException
    {
        int read = zippedInputStream.read();
        if (read != -1)
        {
            ++filePointer;
        }
        return read;
    }

    /**
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        int count = -1;
        while (len != 0)
        {
            int read = zippedInputStream.read(b, off, len);
            if (read == -1)
            {
                break;
            }
            else
            {
                off += read;
                len -= read;
                count = (count == -1) ? read : count + read;
            }
        }
        if (count != -1)
        {
            filePointer += count;
        }
        return count;
    }

    /**
     * (non-Javadoc)
     *
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip(long n) throws IOException
    {
        long skipped = zippedInputStream.skip(n);
        long count = skipped;
        while (skipped != -1 && skipped < n)
        {
            n -= skipped;
            skipped = zippedInputStream.skip(n);
            if (skipped != -1)
            {
                count += skipped;
            }
        }
        if (count != -1)
        {
            filePointer += count;
        }
        return count;
    }

    /**
     * Returns the volume being read.
     *
     * @return the volume being read
     */
    public File getVolume()
    {
        return spanningInputStream.getVolume();
    }

    /**
     * Returns the current position in the file. This is the absolute offset
     * into the volumes.
     *
     * @return the current position in the file
     */
    public long getFilePointer()
    {
        return filePointer;
    }

    private static final class SpanningInputStream extends InputStream
    {

        /**
         * The current volume stream.
         */
        private InputStream stream;

        /**
         * The base path to each volume.
         */
        private String basePath;

        /**
         * The index of the current volume.
         */
        private int index = 0;

        /**
         * The total no. of volumes.
         */
        private final int volumes;

        /**
         * The first volume magic number. All subsequent volumes must start with
         * this.
         */
        private final byte[] magicNumber;

        /**
         * The volume locator. May be <tt>null</tt>
         */
        private VolumeLocator locator;

        /**
         * The current volume.
         */
        private File current;

        /**
         * Constructs a <tt>SpanningInputStream</tt>.
         *
         * @param volume the first volume
         * @param volumes the number of volumes
         * @throws IOException for any I/O error
         */
        public SpanningInputStream(File volume, int volumes) throws IOException
        {
            basePath = volume.getAbsolutePath();
            stream = new FileInputStream(volume);
            current = volume;
            this.volumes = volumes;

            // read magic number
            magicNumber = new byte[FileSpanningOutputStream.MAGIC_NUMBER_LENGTH];
            if (stream.read(magicNumber) != FileSpanningOutputStream.MAGIC_NUMBER_LENGTH)
            {
                IOUtils.closeQuietly(stream);
                throw new CorruptVolumeException();
            }
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.fine("Opened volume=" + volume + ", magic=" + FileSpanningOutputStream.formatMagic(magicNumber));
            }
        }

        /**
         * Sets the volume locator.
         *
         * @param locator the locator. May be <tt>null</tt>
         */
        public void setLocator(VolumeLocator locator)
        {
            this.locator = locator;
        }

        /**
         * Reads up to <code>len</code> bytes of data from the input stream into
         * an array of bytes.
         * <p/>
         * An attempt is made to read as many as <code>len</code> bytes, but a
         * smaller number may be read.
         *
         * @param b the buffer into which the data is read.
         * @param off the start offset in array <code>b</code> at which the data
         * is written.
         * @param len the maximum number of bytes to read.
         * @return the total number of bytes read into the buffer, or
         * <code>-1</code> if there is no more data because the end of the
         * stream has been reached.
         * @throws IOException for any I/O error
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            int count = -1;
            while (len != 0)
            {
                int read = stream.read(b, off, len);
                if (read == -1)
                {
                    if (!openNextVolume())
                    {
                        break;
                    }
                }
                else
                {
                    off += read;
                    len -= read;
                    count = (count == -1) ? read : count + read;
                }
            }
            return count;
        }

        /**
         * Reads the next byte of data from the input stream.
         *
         * @return the next byte of data, or <code>-1</code> if the end of the
         * stream is reached.
         * @throws java.io.IOException if an I/O error occurs.
         */
        @Override
        public int read() throws IOException
        {
            int read = stream.read();
            if (read == -1 && openNextVolume())
            {
                // read from the next volume
                read = stream.read();
            }
            return read;
        }

        /**
         * Returns the volume being read.
         *
         * @return the volume being read
         */
        public File getVolume()
        {
            return current;
        }

        /**
         * Closes this input stream and releases any system resources associated
         * with the stream.
         *
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void close() throws IOException
        {
            stream.close();
        }

        /**
         * Opens the next volume.
         *
         * @return <tt>true</tt> if the next volume was opened, or
         * <tt>false</tt> if there are no more volumes
         * @throws CorruptVolumeException if the magic no. of the next volume
         * does not match that expected
         * @throws VolumeNotFoundException if the next volume was not found
         */
        private boolean openNextVolume() throws IOException
        {
            boolean result;
            if (index + 1 >= volumes)
            {
                LOGGER.fine("Last volume reached");
                result = false;
            }
            else
            {
                // the next volume name
                String volumePath = basePath + "." + (index + 1);
                File volume = new File(volumePath);
                boolean found = false;
                while (!found)
                {
                    if (volume.exists())
                    {
                        try
                        {
                            // try to open new stream to next volume
                            IOUtils.closeQuietly(stream);
                            stream = new FileInputStream(volume);
                            current = volume;
                            checkMagicNumber();
                            found = true;
                        }
                        catch (CorruptVolumeException exception)
                        {
                            if (locator == null)
                            {
                                throw exception;
                            }
                            else
                            {
                                volume = locator.getVolume(volume.getAbsolutePath(), true);
                            }
                        }
                    }
                    else if (locator != null)
                    {
                        volume = locator.getVolume(volume.getAbsolutePath(), false);
                    }
                    else
                    {
                        throw new VolumeNotFoundException("Volume not found: " + volume.getAbsolutePath(),
                                volume.getAbsolutePath());
                    }
                }

                ++index;
                result = true;
            }
            return result;
        }

        /**
         * Checks if the magic number if the current volume is valid.
         *
         * @throws CorruptVolumeException if the magic number doesn't match that
         * expected
         * @throws IOException for any I/O error
         */
        private void checkMagicNumber() throws IOException
        {
            LOGGER.fine("Trying to read magic number");
            byte[] volumeMagicNo = new byte[FileSpanningOutputStream.MAGIC_NUMBER_LENGTH];
            try
            {
                if (stream.read(volumeMagicNo) != volumeMagicNo.length)
                {
                    LOGGER.fine("Failed to read magic number");
                    throw new CorruptVolumeException();
                }

                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.fine("Magic number is " + FileSpanningOutputStream.formatMagic(volumeMagicNo));
                    if (!Arrays.equals(magicNumber, volumeMagicNo))
                    {
                        throw new CorruptVolumeException();
                    }
                }
            }
            catch (IOException exception)
            {
                IOUtils.closeQuietly(stream);
                throw exception;
            }
        }

    }
}
