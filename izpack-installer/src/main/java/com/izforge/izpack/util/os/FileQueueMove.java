package com.izforge.izpack.util.os;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * File queue move operation (Windows Setup API)
 */
public class FileQueueMove extends FileQueueCopy
{

    private static final Logger LOGGER = Logger.getLogger(FileQueueMove.class.getName());

    public FileQueueMove(File fromFile, File toFile)
    {
        this(fromFile, toFile, false);
    }

    public FileQueueMove(File fromFile, File toFile, boolean forceInUse)
    {
        super(fromFile, toFile, true, forceInUse);
    }

    @Override
    public void addTo(WinSetupFileQueue filequeue) throws IOException
    {
        try
        {
            LOGGER.fine("Enqueueing moving " + fromFile + " to " + toFile
                    + " (0x" + Integer.toHexString(copyStyle) + ")");
            filequeue.addMove(fromFile, toFile);
        }
        catch (IOException e)
        {
            throw new IOException(
                    "Failed to enqueue moving " + fromFile + " to " + toFile
                    + " due to " + e.getMessage());
        }
    }

}
