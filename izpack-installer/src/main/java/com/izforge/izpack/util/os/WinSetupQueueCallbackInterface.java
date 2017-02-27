package com.izforge.izpack.util.os;

public interface WinSetupQueueCallbackInterface
{

    // ------------------------ Callback return codes --------------------------
    /**
     * Callback return code to abort the current file queue commit
     */
    public final static int FILEOP_ABORT = 0;

    /**
     * Callback return code to continue with the current operation, same as
     * <i>FILEOP_RETRY</i>
     */
    public final static int FILEOP_DOIT = 1;

    /**
     * Callback return code to skip the current operation
     */
    public final static int FILEOP_SKIP = 2;

    /**
     * Callback return code to retry the current operation, same as
     * <i>FILEOP_DOIT</i>
     */
    public final static int FILEOP_RETRY = FILEOP_DOIT;

    /**
     * Callback return code to addionally return a new source path to the
     * current operation (media needed or blocked source file)
     */
    // TODO: Implement returning of an alternate source path by the callback routine
    public final static int FILEOP_NEWPATH = 4;

    // -------------------------- Callback methods -----------------------------
    /**
     * Handles a need media notification occuring during a file queue commit. In
     * that case, a common problem with a source file happened (blocked or does
     * not exist).
     *
     * @param tagfile
     * @param description
     * @param sourcePath
     * @param sourceFile
     * @return One of the allowed callback return codes
     */
    public int handleNeedMedia(String tagfile, String description, String sourcePath,
            String sourceFile);

    /**
     * Handles a copy error notification occuring during a file queue commit.
     *
     * @param source Source file
     * @param target Target file
     * @return One of the allowed callback return codes
     */
    public int handleCopyError(String source, String target, int errCode, String errMsg);

    /**
     * Handles a delete error notification occuring during a file queue commit.
     *
     * @param target Target file
     * @return One of the allowed callback return codes
     */
    public int handleDeleteError(String target, int errCode, String errMsg);

    /**
     * Handles a rename error notification occuring during a file queue commit.
     *
     * @param source Source file
     * @param target Target file
     * @return One of the allowed callback return codes
     */
    public int handleRenameError(String source, String target, int errCode, String errMsg);

}
