package com.izforge.izpack.panels.path;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Link common functionality for PathInputPanels here.
 * Help to keep features and bug fixes in sync between console and GUI installations for PathInputPanel (Ex. TargetPanel).
 *
 * @author Miles Tjandrawidjaja
 */
public class PathInputBase
{
    private static InstallData installData;
    private static final Logger logger = Logger.getLogger(PathInputPanel.class.getName());

    /**
     * ShowCreateDirectoryMessage configuration option<br>
     * If 'ShowCreateDirectoryMessage' configuration option set 'false' then don't show
     * then don't show "directory will be created" dialog
     */
    public static final String SHOWCREATEDIRECTORYMESSAGE = "ShowCreateDirectoryMessage";

    /**
     * ShowExistingDirectoryWarning configuration option<br>
     * If 'ShowExistingDirectoryWarning' configuration option set 'false' then don't show
     * "The directory already exists! Are you sure you want to install here and possibly overwrite existing files?"
     * warning dialog
     */
    public static final String SHOWEXISTINGDIRECTORYWARNING = "ShowExistingDirectoryWarning";

    public static void setInstallData(InstallData installData)
    {
        PathInputBase.installData = installData;
    }

    /**
     * Verify that the given path to a file or directory is writable.
     * Ex. /home/user/example/test //Check to see if we can write "test" into /home/user/example/
     *
     * @param path path to file or directory to be written
     * @return true if location is writable else false
     */
    public static boolean isWritable(String path)
    {
        return isWritable(new File(path));
    }

    /**
     * Verify that the given path to a file or directory is writable.
     * Ex. /home/user/example/test //Check to see if we can write "test" into /home/user/example/
     *
     * @param path path to file or directory to be written
     * @return true if location is writable else false
     */
    public static boolean isWritable(File path)
    {
        boolean result = false;
        File existParent = IoHelper.existingParent(path);
        if (existParent != null)
        {
            // On windows we cannot use canWrite because it looks to the dos flags which are not valid
            // on NT or 2k XP or ...
            if (installData.getPlatform().isA(Platforms.WINDOWS))
            {
                File tmpDir;
                File tmpFile;
                try
                {
                    tmpDir = new File(existParent.getAbsoluteFile() + File.separator + "iZwrite" + System.nanoTime());
                    tmpDir.mkdir();
                    tmpFile = new File(tmpDir.getAbsoluteFile() + File.separator + "izWrite.tmp");
                    tmpFile.createNewFile();
                    result = true;
                    if (!tmpFile.delete())
                    {
                        tmpFile.deleteOnExit();
                    }
                    if (!tmpDir.delete())
                    {
                        tmpDir.deleteOnExit();
                    }
                }
                catch (IOException e)
                {
                    logger.log(Level.WARNING, e.toString(), e);
                }
            }
            else
            {
                result = existParent.canWrite();
            }
        }
        return result;
    }

    /**
     * Normalize a path. Compresses consecutive file separators and replace the "~" with the users
     * home directory if installation is being run on a UNIX platform.
     *
     * @param path path to normalize
     * @return normalized path
     */
    public static String normalizePath(String path)
    {
        if (path == null)
        {
            return "";
        }
        else if (path.startsWith("~") && installData.getPlatform().isA(Platform.Name.UNIX))
        {
            String home = System.getProperty("user.home");
            path = home + path.substring(1);
        }

        return new File(path).getAbsolutePath();
    }
}
