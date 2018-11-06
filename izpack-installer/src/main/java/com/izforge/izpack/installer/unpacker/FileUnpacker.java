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

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.os.FileQueue;
import com.izforge.izpack.util.os.FileQueueMove;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * Unpacks a file from a pack.
 * <p/>
 * This manages queueing files that are blocked.
 *
 * @author Tim Anderson
 */
public abstract class FileUnpacker
{

    /**
     * Determines if unpacking should be cancelled.
     */
    private final Cancellable cancellable;

    /**
     * The target.
     */
    private File target;

    /**
     * Temporary target, used if the target file is blockable.
     */
    private File tmpTarget;

    /**
     * The file queue.
     */
    private final FileQueue queue;

    /**
     * Determines if the file was queued.
     */
    private boolean queued;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FileUnpacker.class.getName());


    /**
     * Constructs a <tt>FileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param queue       the file queue. May be {@code null}
     */
    public FileUnpacker(Cancellable cancellable, FileQueue queue)
    {
        this.cancellable = cancellable;
        this.queue = queue;
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
    public abstract void unpack(PackFile file, InputStream packInputStream, File target)
            throws IOException, InstallerException;

    /**
     * Determines if the file was queued.
     *
     * @return <tt>true</tt> if the file was queued
     */
    public boolean isQueued()
    {
        return queued;
    }

    /**
     * Copies an input stream to a target, setting its timestamp to that of the pack file.
     * <p/>
     * If the target is a blockable file, then a temporary file will be created, and the file queued.
     *
     * @param file   the pack file
     * @param in     the pack file stream
     * @param target the file to write to
     * @return the number of bytes actually copied
     * @throws InterruptedIOException if the copy operation is cancelled
     * @throws IOException            for any I/O error
     */
    protected long copy(PackFile file, InputStream in, File target) throws IOException
    {
        OutputStream out = getTarget(file, target);
        byte[] buffer = new byte[5120];
        long bytesCopied = 0;
        long bytesToCopy = (file.isBackReference() ? file.getLinkedPackFile().length() : file.length());
        logger.fine("|- Copying to file system (size: " + bytesToCopy + " bytes)");
        try
        {
            while (bytesCopied < bytesToCopy)
            {
                if (cancellable.isCancelled())
                {
                    // operation cancelled
                    throw new InterruptedIOException("Copy operation cancelled");
                }
                bytesCopied = copy(file, buffer, in, out, bytesCopied);
            }
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }

        postCopy(file);

        if (file.getAdditionals() != null) {
            Map fullAdditionalsMap = file.getAdditionals();
            HashMap<String, Object> filePropertiesMap = (HashMap<String, Object>) fullAdditionalsMap.get(file.getTargetPath());
            if (filePropertiesMap != null) setAdditionalData(target.getPath(), filePropertiesMap);
        }

        return bytesCopied;
    }

    /**
     * Sets the file permissions and type in accordance with the initial values.
     * MacOS not supported yet.
     *
     * @param target the full path to file
     * @param additionals the collection which contains full information about current file
     */
    private void setAdditionalData(String target, HashMap<String, Object> additionals)
    {
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        ArrayList<String> execCommands = new ArrayList<String>();

        if (isLinux) {
            if ((Boolean) additionals.get("isSymbolicLink")) {
                execCommands.add("ln -sf " + additionals.get("linkName") + " " + target);
            } else if ((Boolean) additionals.get("isLink")) {
                String pathToLink = target.substring(0, target.length() - ((String) additionals.get("entryName")).length());
                execCommands.add("ln -fv " + pathToLink + additionals.get("linkName") + " " + target);
            }
            execCommands.add("chmod " + additionals.get("unixMode") + " " + target);
        }
        else if (isWindows) {
            if ((Boolean) additionals.get("isSymbolicLink")) {
                execCommands.add("cmd.exe /c del " + target);
                execCommands.add("cmd.exe /c mklink " + target + " " + ((String) additionals.get("linkName")).replace('/', '\\'));
            } else if ((Boolean) additionals.get("isLink")) {
                execCommands.add("cmd.exe /c del " + target);
                String pathToLink = target.substring(0, target.length() - ((String) additionals.get("entryName")).length());
                execCommands.add("cmd.exe /c mklink /H " + target + " " + pathToLink + ((String) additionals.get("linkName")).replace('/', '\\'));
            }
        }

        for (String command : execCommands) {
            try {
                Process pr = Runtime.getRuntime().exec(command);
                pr.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Invoked after copying is complete to set the last modified timestamp, and queue blockable files.
     *
     * @param file the pack file meta-data
     */
    protected void postCopy(PackFile file)
    {
        setLastModified(file);
        if (isBlockable(file))
        {
            queue();
        }
    }

    /**
     * Copies from the input stream to the output stream.
     *
     * @param file        the pack file
     * @param buffer      the buffer to use
     * @param in          the stream to read from
     * @param out         the stream to write to
     * @param bytesCopied the current no. of bytes copied
     * @return the number of bytes actually copied
     * @throws IOException for any I/O error
     */
    protected long copy(PackFile file, byte[] buffer, InputStream in, OutputStream out, long bytesCopied)
            throws IOException
    {
        int maxBytes = (int) Math.min(file.length() - bytesCopied, buffer.length);
        int read = read(buffer, in, maxBytes);
        if (read == -1)
        {
            throw new IOException("Unexpected end of stream (installer corrupted?)");
        }
        out.write(buffer, 0, read);
        bytesCopied += read;

        return bytesCopied;
    }

    /**
     * Reads up to <tt>maxBytes</tt> bytes to the specified buffer.
     *
     * @param buffer   the buffer
     * @param in       the input stream
     * @param maxBytes the maximum no. of bytes to read
     * @return the no. of bytes read
     * @throws IOException for any I/O error
     */
    protected int read(byte[] buffer, InputStream in, int maxBytes) throws IOException
    {
        return in.read(buffer, 0, maxBytes);
    }

    /**
     * Returns a stream to the target file.
     * <p/>
     * If the target file is blockable, then a temporary file will be created, and a stream to this returned instead.
     *
     * @param file   the pack file meta-data
     * @param target the requested target
     * @return a stream to the actual target
     * @throws IOException an I/O error occurred
     */
    protected OutputStream getTarget(PackFile file, File target) throws IOException
    {
        this.target = target;
        OutputStream result;
        if (isBlockable(file))
        {
            // If target file might be blocked the output file must first refer to a temporary file, because
            // Windows Setup API doesn't work on streams but only on physical files
            tmpTarget = File.createTempFile("__FQ__", null, target.getParentFile());
            result = FileUtils.openOutputStream(tmpTarget);
        }
        else
        {
            try {
                result = FileUtils.openOutputStream(target);
            }
            catch (IOException e) { //if target file cannot be opened for writing, we delete him and create new
                    target.delete();
                    result = FileUtils.openOutputStream(target);
            }
        }
        return result;
    }

    /**
     * Sets the last-modified timestamp of a file from the pack-file meta-data.
     *
     * @param file the pack file meta-data
     */
    protected void setLastModified(PackFile file)
    {
        // Set file modification time if specified
        if (file.lastModified() >= 0)
        {
            File f = (tmpTarget != null) ? tmpTarget : target;
            if (!f.setLastModified(file.lastModified()))
            {
                logger.warning("Failed to set last modified timestamp for: " + target);
            }
        }
    }

    /**
     * Determines if a pack file is blockable.
     * <p/>
     * Blockable files must be queued using {@link #queue()}.
     *
     * @param file the pack file
     * @return <tt>true</tt> if the file is blockable, otherwise <tt>false</tt>
     */
    private boolean isBlockable(PackFile file)
    {
        return queue != null && (file.blockable() != Blockable.BLOCKABLE_NONE);
    }

    /**
     * Queues the target file.
     */
    private void queue()
    {
        FileQueueMove move = new FileQueueMove(tmpTarget, target);
        move.setForceInUse(true);
        move.setOverwrite(true);
        queue.add(move);
        logger.fine(tmpTarget.getAbsolutePath() + " -> " + target.getAbsolutePath()
                            + " added to file queue for being copied after reboot");
        // The temporary file must not be deleted until the file queue will be committed
        tmpTarget.deleteOnExit();
        queued = true;
    }

}
