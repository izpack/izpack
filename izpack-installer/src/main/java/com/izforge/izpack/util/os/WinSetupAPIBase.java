package com.izforge.izpack.util.os;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.NativeLibraryClient;

import java.io.IOException;

public class WinSetupAPIBase implements NativeLibraryClient
{

    protected static final int INVALID_HANDLE_VALUE = -1;

    // ----------------------------- Copy styles -------------------------------
    /**
     * Delete source file on successful copy
     */
    public static final int SP_COPY_DELETESOURCE = 0x0000001;

    /**
     * Copy only if target file already present
     */
    public static final int SP_COPY_REPLACEONLY = 0x0000002;

    /**
     * Copy only if source newer than or same as target
     *
     * @see #SP_COPY_NEWER_OR_SAME
     */
    public static final int SP_COPY_NEWER = 0x0000004;

    /**
     * Copy only if source newer than or same as target
     *
     * @see #SP_COPY_NEWER
     */
    public static final int SP_COPY_NEWER_OR_SAME = SP_COPY_NEWER;

    /**
     * Copy only if target doesn't exist
     *
     * @see #SP_COPY_FORCE_NOOVERWRITE
     */
    public static final int SP_COPY_NOOVERWRITE = 0x0000008;

    /**
     * Don't decompress source file while copying
     */
    public static final int SP_COPY_NODECOMP = 0x0000010;

    /**
     * Don't overwrite file of different language
     */
    public static final int SP_COPY_LANGUAGEAWARE = 0x0000020;

    /**
     * Parameter SourceFile is a full source path
     */
    public static final int SP_COPY_SOURCE_ABSOLUTE = 0x0000040;

    /**
     * Parameter SourcePathRoot is the full path
     */
    public static final int SP_COPY_SOURCEPATH_ABSOLUTE = 0x0000080;

    /**
     * System needs reboot if file in use
     */
    public static final int SP_COPY_IN_USE_NEEDS_REBOOT = 0x0000100;

    /**
     * Force target-in-use behavior
     */
    public static final int SP_COPY_FORCE_IN_USE = 0x0000200;

    /**
     * Skip is disallowed for this file or section
     */
    public static final int SP_COPY_NOSKIP = 0x0000400;

    /**
     * Used with need media notification
     */
    public static final int SP_FLAG_CABINETCONTINUATION = 0x0000800;

    /**
     * Like NOOVERWRITE but no callback nofitication
     *
     * @see #SP_COPY_NOOVERWRITE
     */
    public static final int SP_COPY_FORCE_NOOVERWRITE = 0x0001000;

    /**
     * Like NEWER but no callback nofitication
     */
    public static final int SP_COPY_FORCE_NEWER = 0x0002000;

    /**
     * System critical file: warn if user tries to skip
     */
    public static final int SP_COPY_WARNIFSKIP = 0x0004000;

    /**
     * Browsing is disallowed for this file or section
     */
    public static final int SP_COPY_NOBROWSE = 0x0008000;

    /**
     * Copy only if source file newer than target
     */
    public static final int SP_COPY_NEWER_ONLY = 0x0010000;

    /**
     * Source is single-instance store master
     */
    public static final int SP_COPY_SOURCE_SIS_MASTER = 0x0020000;

    /**
     * (SetupCopyOEMInf only) don't copy INF--just catalog
     */
    public static final int SP_COPY_OEMINF_CATALOG_ONLY = 0x0040000;

    /**
     * File must be present upon reboot (i.e., it's needed by the loader); this
     * flag implies a reboot
     */
    public static final int SP_COPY_REPLACE_BOOT_FILE = 0x0080000;

    /**
     * Never prune this file
     */
    public static final int SP_COPY_NOPRUNE = 0x0100000;

    // -- Flags that are returned by SetupPromptReboot -------------------------
    public static final int SPFILEQ_FILE_IN_USE = 0x00000001;

    public static final int SPFILEQ_REBOOT_RECOMMENDED = 0x00000002;

    public static final int SPFILEQ_REBOOT_IN_PROGRESS = 0x00000004;

    // ----------------------------- Constructor -------------------------------
    /**
     * Constructs a <tt>WinSetupAPIBase</tt>.
     *
     * @param librarian the librarian
     * @throws IzPackException if the WinSetupAPI library cannot be loaded
     */
    public WinSetupAPIBase(Librarian librarian)
    {
        try
        {
            librarian.loadLibrary("WinSetupAPI", this);
        }
        catch (UnsatisfiedLinkError error)
        {
            throw new IzPackException("Failed to load WinSetupAPI", error);
        }
    }

    // ----------------------------- Interface NativeLibraryClient -------------
    public void freeLibrary(String name)
    {
    }

    ;

    // ----------------------------- Native part -------------------------------

    /**
     * Create a file queue
     *
     * @param handler Optional instance of WinSetupQueueCallbackInterface handler. If set to null,
     *                use the Setup API's SetupDefaultCallbackHandler.
     * @return Handle to the new file queue
     */
    protected native int /* HSPFILEQ */ SetupOpenFileQueue(Object handler) throws IOException;

    /**
     * Closes a file queue.
     *
     * @param queuehandle Handle to the file queue to close.
     */
    protected native void SetupCloseFileQueue(int /* HSPFILEQ */ queuehandle);

    /**
     * Places an individual file copy operation on a setup file queue.
     *
     * @param queuehandle
     * @param SourceRootPath (optional)
     * @param SourcePath (optional)
     * @param SourceFileName
     * @param SourceDescription (optional)
     * @param SourceTagFile (optional)
     * @param TargetDirectory
     * @param TargetFileName (optional)
     * @param CopyStyle
     */
    protected native void /* BOOL */ SetupQueueCopy(int /* HSPFILEQ */ queuehandle,
                    String /* PCTSTR */ SourceRootPath, String /* PCTSTR */ SourcePath,
                    String /* PCTSTR */ SourceFileName, String /* PCTSTR */ SourceDescription,
                    String /* PCTSTR */ SourceTagFile, String /* PCTSTR */ TargetDirectory,
                    String /* PCTSTR */ TargetFileName, int /* DWORD */ CopyStyle) throws IOException;

    /**
     * Places an individual file delete operation on a setup file queue.
     *
     * @param queuehandle Handle to a setup file queue, as returned by
     * SetupOpenFileQueue.
     * @param PathPart1 String that specifies the first part of the path of the
     * file to be deleted. If <i>PathPart2</i> is NULL, <i>PathPart1</i> is the
     * full path of the file to be deleted.
     * @param PathPart2 String that specifies the second part of the path of the
     * file to be deleted. This parameter may be NULL. This is appended to
     * <i>PathPart1</i> to form the full path of the file to be deleted. The
     * function checks for and collapses duplicated path separators when it
     * combines <i>PathPart1</i> and <i>PathPart2</i>.
     */
    protected native void /* BOOL */ SetupQueueDelete(int /* HSPFILEQ */ queuehandle,
                    String /* PCTSTR */ PathPart1, String /* PCTSTR */ PathPart2) throws IOException;

    /**
     * Places an individual file rename operation on a setup file queue.
     *
     * @param queuehandle Handle to a setup file queue, as returned by
     * SetupOpenFileQueue.
     * @param SourcePath String that specifies the source path of the file to be
     * renamed. If SourceFileName is not specified, SourcePath is assumed to be
     * the full path.
     * @param SourceFileName String that specifies the file name part of the
     * file to be renamed. If not specified, SourcePath is the full path.
     * @param TargetPath String that specifies the target directory. When this
     * parameter is specified, the rename operation is actually a move
     * operation. If TargetPath is not specified, the file is renamed but
     * remains in its current location.
     * @param TargetFileName String that specifies the new name for the source
     * file.
     */
    protected native void /* BOOL */ SetupQueueRename(int /* HSPFILEQ */ queuehandle,
                    String /* PCTSTR */ SourcePath, String /* PCTSTR */ SourceFileName,
                    String /* PCTSTR */ TargetPath, String /* PCTSTR */ TargetFileName) throws IOException;

    /**
     * Commits the actions stored in the file queue.
     *
     * @param queuehandle File queue handle
     * @return If the function succeeds, the return value is a nonzero value. If
     * the function fails, the return value is zero.
     */
    protected native boolean /* BOOL */ SetupCommitFileQueue(int /* HSPFILEQ */ queuehandle)
            throws IOException;

    /**
     * @param queuehandle Optional handle to a setup file queue upon which to
     * base the decision about whether shutdown is necessary. If FileQueue is
     * null, SetupPromptReboot assumes shutdown is necessary and asks the user
     * what to do.
     * @param scanonly Indicates whether or not to prompt the user when
     * SetupPromptReboot is called. If TRUE, the user is never asked about
     * rebooting, and system shutdown is not initiated. In this case, FileQueue
     * must be specified. If FALSE, the user is asked about rebooting, as
     * previously described. Use ScanOnly to determine if shutdown is necessary
     * separately from actually initiating a shutdown.
     */
    protected native int /* INT */ SetupPromptReboot(int /* HSPFILEQ */ queuehandle,
                    boolean /* BOOL */ scanonly) throws IOException;
}
