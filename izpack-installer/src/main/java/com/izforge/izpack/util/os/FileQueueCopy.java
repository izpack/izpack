package com.izforge.izpack.util.os;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * File queue copy operation (Windows Setup API)
 */
public class FileQueueCopy implements FileQueueOperation
{

    private static final Logger LOGGER = Logger.getLogger(FileQueueCopy.class.getName());

    protected File fromFile = null; // source file
    protected File toFile = null; // destination file

    protected int copyStyle = WinSetupAPIBase.SP_COPY_NOOVERWRITE;

    public FileQueueCopy(File fromFile, File toFile)
    {
        this(fromFile, toFile, false, false);
    }

    public FileQueueCopy(File fromFile, File toFile, boolean deleteSource, boolean forceInUse)
    {
        this.fromFile = fromFile;
        this.toFile = toFile;
        setDeleteSource(deleteSource);
        setForceInUse(forceInUse);
    }

    // --- Copy Styles ---------------------------------------------------------
    public void setDeleteSource(boolean flag)
    {
        if (flag)
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_DELETESOURCE;
        }
        else
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_DELETESOURCE);
        }
    }

    public void setForceInUse(boolean flag)
    {
        if (flag)
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_FORCE_IN_USE;
        }
        else
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_FORCE_IN_USE);
        }
    }

    public void setInUseNeedsReboot(boolean flag)
    {
        if (flag)
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_IN_USE_NEEDS_REBOOT;
        }
        else
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_IN_USE_NEEDS_REBOOT);
        }
    }

    public void setLanguageAware(boolean flag)
    {
        if (flag)
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_LANGUAGEAWARE;
        }
        else
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_LANGUAGEAWARE);
        }
    }

    public void setNewerOrSame(boolean flag)
    {
        if (flag)
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_NEWER_OR_SAME;
        }
        else
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_NEWER_OR_SAME);
        }
    }

    public void setNewerOnly(boolean flag)
    {
        if (flag)
        {
            this.copyStyle &= WinSetupAPIBase.SP_COPY_NEWER_ONLY;
        }
    }

    public void setReplaceOnly(boolean flag)
    {
        if (flag)
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_REPLACEONLY;
        }
        else
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_REPLACEONLY);
        }
    }

    /**
     * Overwrite any existing destination file(s).
     *
     * @param overwrite if true force overwriting of destination file(s) even if
     * the destination file(s) are younger than the corresponding source file.
     * Default is false.
     */
    public void setOverwrite(boolean flag)
    {
        if (flag)
        {
            this.copyStyle &= (~WinSetupAPIBase.SP_COPY_NOOVERWRITE);
        }
        else
        {
            this.copyStyle |= WinSetupAPIBase.SP_COPY_NOOVERWRITE;
        }
    }

    public void addTo(WinSetupFileQueue filequeue) throws IOException
    {
        if (fromFile.equals(toFile))
        {
            LOGGER.warning("Skipping self-copy of " + fromFile);
        }
        else
        {
            try
            {
                LOGGER.fine("Enqueueing copying " + fromFile + " to " + toFile
                        + " (0x" + Integer.toHexString(copyStyle) + ")");
                filequeue.addCopy(fromFile, toFile, copyStyle);
            }
            catch (IOException ioe)
            {
                String msg = "Failed to enqueue copying " + fromFile + " to " + toFile
                        + " due to " + ioe.getMessage();
                throw new IOException(msg);
            }
        }
    }

}
