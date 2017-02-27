/*
 * Copyright  2001-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file;

import com.izforge.izpack.util.OsVersion;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * This class also encapsulates methods which allow Files to be referred to
 * using abstract path names which are translated to native system file paths at
 * runtime as well as copying files or setting their last modification time.
 */
public class FileUtils
{

    /**
     * The granularity of timestamps under FAT.
     */
    public static final long FAT_FILE_TIMESTAMP_GRANULARITY = 2000;

    /**
     * The granularity of timestamps under Unix.
     */
    public static final long UNIX_FILE_TIMESTAMP_GRANULARITY = 1000;

    /**
     * Convenience method to copy a file from a source to a destination. No
     * filtering is performed.
     *
     * @param sourceFile Name of file to copy from. Must not be
     * <code>null</code>.
     * @param destFile Name of file to copy to. Must not be <code>null</code>.
     * @throws IOException if the copying fails.
     */
    public static void copyFile(String sourceFile, String destFile,
            boolean overwrite, boolean preserveLastModified)
            throws IOException
    {
        copyFile(new File(sourceFile), new File(destFile), overwrite, preserveLastModified);
    }

    /**
     * Convenience method to copy a file from a source to a destination
     * specifying if token filtering must be used, if filter chains must be
     * used, if source files may overwrite newer destination files and the last
     * modified time of <code>destFile</code> file should be made equal to the
     * last modified time of <code>sourceFile</code>.
     *
     * @param sourceFile the file to copy from. Must not be <code>null</code>.
     * @param destFile the file to copy to. Must not be <code>null</code>.
     * @param overwrite Whether or not the destination file should be
     * overwritten if it already exists.
     * @param preserveLastModified Whether or not the last modified time of the
     * resulting file should be set to that of the source file.
     * @throws IOException if the copying fails.
     */
    public static void copyFile(File sourceFile, File destFile, boolean overwrite, boolean preserveLastModified)
            throws IOException
    {

        if (overwrite || !destFile.exists()
                || destFile.lastModified() < sourceFile.lastModified())
        {
            org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile, preserveLastModified);
        }
    }

    /**
     * Interpret the filename as a file relative to the given file unless the
     * filename already represents an absolute filename.
     *
     * @param file the "reference" file for relative paths. This instance must
     * be an absolute file and must not contain &quot;./&quot; or
     * &quot;../&quot; sequences (same for \ instead of /). If it is null, this
     * call is equivalent to <code>new java.io.File(filename)</code>.
     * @param filename a file name.
     * @return an absolute file that doesn't contain &quot;./&quot; or
     * &quot;../&quot; sequences and uses the correct separator for the current
     * platform.
     */
    public static File resolveFile(File file, String filename) throws Exception
    {
        return new File(FilenameUtils.concat(file == null ? null : file.getPath(), filename));
    }

    /**
     * This was originally an emulation of {@link File#getParentFile} for JDK
     * 1.1, but it is now implemented using that method (Ant 1.6.3 onwards).
     *
     * @param f the file whose parent is required.
     * @return the given file's parent, or null if the file does not have a
     * parent.
     */
    public static File getParentFile(File f)
    {
        return (f == null) ? null : f.getParentFile();
    }

    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>
     * It doesn't really test for symbolic links but whether the canonical and
     * absolute paths of the file are identical--this may lead to false
     * positives on some platforms.</p>
     *
     * @param parent the parent directory of the file to test
     * @param name the name of the file to test.
     * @return true if the file is a symbolic link.
     * @throws IOException on error.
     */
    public static boolean isSymbolicLink(File parent, String name)
            throws IOException
    {
        if (parent == null)
        {
            File f = new File(name);
            parent = f.getParentFile();
            name = f.getName();
        }
        File toTest = new File(parent.getCanonicalPath(), name);
        return org.apache.commons.io.FileUtils.isSymlink(toTest);
    }

    /**
     * Removes a leading path from a second path.
     *
     * @param leading The leading path, must not be null, must be absolute.
     * @param path The path to remove from, must not be null, must be absolute.
     * @return path's normalized absolute if it doesn't start with leading;
     * path's path with leading's path removed otherwise.
     */
    public static String removeLeadingPath(File leading, File path) throws Exception
    {
        String l = FilenameUtils.normalize(leading.getAbsolutePath());
        String p = FilenameUtils.normalize(path.getAbsolutePath());
        if (l.equals(p))
        {
            return "";
        }

        // ensure that l ends with a /
        // so we never think /foo was a parent directory of /foobar
        if (!l.endsWith(File.separator))
        {
            l += File.separator;
        }
        return (p.startsWith(l)) ? p.substring(l.length()) : p;
    }

    /**
     * Get the granularity of file timestamps. The choice is made based on OS,
     * which is incorrect--it should really be by filesystem. We do not have an
     * easy way to probe for file systems, however.
     *
     * @return the difference, in milliseconds, which two file timestamps must
     * have in order for the two files to be given a creation order.
     */
    public static long getFileTimestampGranularity()
    {
        return OsVersion.IS_WINDOWS
                ? FAT_FILE_TIMESTAMP_GRANULARITY : UNIX_FILE_TIMESTAMP_GRANULARITY;
    }

    /**
     * Create a temporary directory.
     *
     * @param prefix the prefix string to be used in generating the directory's
     * name; must be at least three characters long
     * @param directory the directory in which the file is to be created, or
     * <code>null</code> if the default temporary-file directory is to be used
     *
     * @return a new temporary directory
     *
     * @throws IOException if the creation of the directory fails
     */
    public static File createTempDirectory(String prefix, File directory) throws IOException
    {
        // create a unique temporary file name
        File tempFile = File.createTempFile(prefix, "", directory);
        tempFile.delete();
        // append a "d" suffix to the temporary directory name as the temp file may not be
        // immediately deleted by the JVM
        File tempDirectory = new File(tempFile.getPath() + ".tmp");
        if (!tempDirectory.mkdirs())
        {
            throw new IOException("Failed to create temporary directory: " + tempDirectory);
        }
        return tempDirectory;
    }

}
