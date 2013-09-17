/*
 * Copyright  2000-2005 The Apache Software Foundation
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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.file.types.Mapper;

/**
 * Copies a file or directory to a new file
 * or directory.  Files are only copied if the source file is newer
 * than the destination file, or when the destination file does not
 * exist.  It is possible to explicitly overwrite existing files.</p>
 * <p/>
 * <p>This implementation is based on Arnout Kuiper's initial design
 * document, the following mailing list discussions, and the
 * copyfile/copydir tasks.</p>
 */
public class FileCopyTask
{
    private static final Logger logger = Logger.getLogger(FileCopyTask.class.getName());

    protected File file = null;     // the source file
    protected File destFile = null; // the destination file
    protected File destDir = null;  // the destination directory
    protected Vector<FileSet> filesets = new Vector<FileSet>();

    private boolean enableMultipleMappings = false;
    protected boolean preserveLastModified = false;
    protected boolean forceOverwrite = false;
    protected boolean flatten = false;
    protected boolean includeEmpty = true;
    protected boolean failonerror = true;

    protected Hashtable<String, String[]> fileCopyMap = new Hashtable<String, String[]>();
    protected Hashtable<String, String[]> dirCopyMap = new Hashtable<String, String[]>();
    protected Hashtable<File, File> completeDirMap = new Hashtable<File, File>();

    protected Mapper mapperElement = null;
    protected FileUtils fileUtils;
    private long granularity = 0;

    /**
     * Copy task constructor.
     */
    public FileCopyTask()
    {
        fileUtils = FileUtils.getFileUtils();
        granularity = fileUtils.getFileTimestampGranularity();
    }

    /**
     * @return the fileutils object
     */
    protected FileUtils getFileUtils()
    {
        return fileUtils;
    }

    /**
     * Sets a single source file to copy.
     *
     * @param file the file to copy
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Sets the destination file.
     *
     * @param destFile the file to copy to
     */
    public void setToFile(File destFile)
    {
        this.destFile = destFile;
    }

    /**
     * Sets the destination directory.
     *
     * @param destDir the destination directory
     */
    public void setToDir(File destDir)
    {
        this.destDir = destDir;
    }

    /**
     * Give the copied files the same last modified time as the original files.
     *
     * @param preserve if true perverse the modified time, default is false
     */
    public void setPreserveLastModified(boolean preserve)
    {
        preserveLastModified = preserve;
    }

    /**
     * Whether to give the copied files the same last modified time as
     * the original files.
     *
     * @return the preserveLastModified attribute
     */
    public boolean getPreserveLastModified()
    {
        return preserveLastModified;
    }

    /**
     * Overwrite any existing destination file(s).
     *
     * @param overwrite if true force overwriting of destination file(s)
     *                  even if the destination file(s) are younger than
     *                  the corresponding source file. Default is false.
     */
    public void setOverwrite(boolean overwrite)
    {
        this.forceOverwrite = overwrite;
    }

    /**
     * When copying directory trees, the files can be "flattened"
     * into a single directory.  If there are multiple files with
     * the same name in the source directory tree, only the first
     * file will be copied into the "flattened" directory, unless
     * the forceoverwrite attribute is true.
     *
     * @param flatten if true flatten the destination directory. Default
     *                is false.
     */
    public void setFlatten(boolean flatten)
    {
        this.flatten = flatten;
    }

    /**
     * Used to copy empty directories.
     *
     * @param includeEmpty if true copy empty directories. Default is true.
     */
    public void setIncludeEmptyDirs(boolean includeEmpty)
    {
        this.includeEmpty = includeEmpty;
    }

    /**
     * Attribute to handle mappers that return multiple
     * mappings for a given source path.
     *
     * @param enableMultipleMappings If true the task will
     *                               copy to all the mappings for a given source path, if
     *                               false, only the first file or directory is
     *                               processed.
     *                               By default, this setting is false to provide backward
     *                               compatibility with earlier releases.
     */
    public void setEnableMultipleMappings(boolean enableMultipleMappings)
    {
        this.enableMultipleMappings = enableMultipleMappings;
    }

    /**
     * @return the value of the enableMultipleMapping attribute
     */
    public boolean isEnableMultipleMapping()
    {
        return enableMultipleMappings;
    }

    /**
     * If false, note errors to the output but keep going.
     *
     * @param failonerror true or false
     */
    public void setFailOnError(boolean failonerror)
    {
        this.failonerror = failonerror;
    }

    /**
     * Adds a set of files to copy.
     *
     * @param set a set of files to copy
     */
    public void addFileSet(FileSet set)
    {
        filesets.addElement(set);
    }

    /**
     * Defines the mapper to map source to destination files.
     *
     * @return a mapper to be configured
     * @throws Exception if more than one mapper is defined
     */
    public Mapper createMapper() throws Exception
    {
        if (mapperElement != null)
        {
            throw new Exception("Cannot define more than one mapper");
        }
        mapperElement = new Mapper();
        return mapperElement;
    }

    /**
     * A nested filenamemapper
     *
     * @param fileNameMapper the mapper to add
     */
    public void add(FileNameMapper fileNameMapper) throws Exception
    {
        createMapper().add(fileNameMapper);
    }

    /**
     * The number of milliseconds leeway to give before deciding a
     * target is out of date.
     * <p>Default is 0 milliseconds, or 2 seconds on DOS systems.</p>
     */
    public void setGranularity(long granularity)
    {
        this.granularity = granularity;
    }

    /**
     * Performs the copy operation.
     *
     * @throws Exception if an error occurs
     */
    public void execute() throws Exception
    {
        File savedFile = file; // may be altered in validateAttributes
        File savedDestFile = destFile;
        File savedDestDir = destDir;
        FileSet savedFileSet = null;
        if (file == null && destFile != null && filesets.size() == 1)
        {
            // will be removed in validateAttributes
            savedFileSet = filesets.elementAt(0);
        }

        // make sure we don't have an illegal set of options
        validateAttributes();

        try
        {

            // deal with the single file
            if (file != null)
            {
                if (file.exists())
                {
                    if (destFile == null)
                    {
                        destFile = new File(destDir, file.getName());
                    }

                    if (shouldAllowFileOperation(file, destFile))
                    {
                        fileCopyMap.put(file.getAbsolutePath(),
                                new String[]{destFile.getAbsolutePath()});
                    }
                    else
                    {
                        logger.fine(file + " omitted as " + destFile + " is up to date.");
                    }
                }
                else
                {
                    String message = "Warning: Could not find file "
                            + file.getAbsolutePath() + " to copy.";
                    if (!failonerror)
                    {
                        logger.fine(message);
                    }
                    else
                    {
                        throw new Exception(message);
                    }
                }
            }

            // deal with the filesets
            for (int i = 0; i < filesets.size(); i++)
            {
                FileSet fs = filesets.elementAt(i);
                DirectoryScanner ds = null;
                try
                {
                    ds = fs.getDirectoryScanner();
                }
                catch (Exception e)
                {
                    if (failonerror
                            || !e.getMessage().endsWith(" not found."))
                    {
                        throw e;
                    }
                    else
                    {
                        logger.log(Level.WARNING,
                                "Warning: " + e.getMessage(),
                                e);
                        continue;
                    }
                }

                File fromDir = fs.getDir();

                String[] srcFiles = ds.getIncludedFiles();
                String[] srcDirs = ds.getIncludedDirectories();
                boolean isEverythingIncluded = ds.isEverythingIncluded()
                        && (!fs.hasSelectors() && !fs.hasPatterns());
                if (isEverythingIncluded
                        && !flatten && mapperElement == null)
                {
                    completeDirMap.put(fromDir, destDir);
                }
                scan(fromDir, destDir, srcFiles, srcDirs);
            }

            // do all the copy operations now...
            try
            {
                doFileOperations();
            }
            catch (Exception e)
            {
                if (!failonerror)
                {
                    System.err.println("Warning: " + e.getMessage());
                }
                else
                {
                    throw e;
                }
            }
        }
        finally
        {
            // clean up again, so this instance can be used a second
            // time
            file = savedFile;
            destFile = savedDestFile;
            destDir = savedDestDir;
            if (savedFileSet != null)
            {
                filesets.insertElementAt(savedFileSet, 0);
            }

            fileCopyMap.clear();
            dirCopyMap.clear();
            completeDirMap.clear();
        }
    }

    /************************************************************************
     **  protected and private methods
     ************************************************************************/

    /**
     * Whether to allow a specified destination file to be overwritten with 
     * the contents of the specified source file. Functionality extracted
     * from {@link #execute()} to allow sub-classes to define consistent
     * rules for allowing an operation without overriding {@code execute()}.
     * This method is called only once in {@code FileCopyTask}, when no 
     * filesets are specified (i.e. a single copy operation, {@code file} 
     * &rarr; {@code destFile}).
     * 
     * @param file the source file
     * @param destFile the destination file
     * @return true if {@code destFile} can be overwritten
     */
    protected boolean shouldAllowFileOperation(File file, File destFile)
    {
    	return forceOverwrite || !destFile.exists()
    			|| (file.lastModified() - granularity
    					> destFile.lastModified());
    }

    /**
     * Ensure we have a consistent and legal set of attributes, and set
     * any internal flags necessary based on different combinations
     * of attributes.
     *
     * @throws Exception if an error occurs
     */
    protected void validateAttributes() throws Exception
    {
        if (file == null && filesets.size() == 0)
        {
            throw new Exception("Specify at least one source "
                    + "- a file or a fileset.");
        }

        if (destFile != null && destDir != null)
        {
            throw new Exception("Only one of tofile and todir "
                    + "may be set.");
        }

        if (destFile == null && destDir == null)
        {
            throw new Exception("One of tofile or todir must be set.");
        }

        if (file != null && file.exists() && file.isDirectory())
        {
            throw new Exception("Use a fileset to copy directories.");
        }

        if (destFile != null && filesets.size() > 0)
        {
            if (filesets.size() > 1)
            {
            	validateMergeMultipleFiles();
            }
            else
            {
                FileSet fs = filesets.elementAt(0);
                DirectoryScanner ds = fs.getDirectoryScanner(/*getProject()*/);
                String[] srcFiles = ds.getIncludedFiles();

                if (srcFiles.length == 0)
                {
                    throw new Exception(
                            "Cannot perform operation from directory to file.");
                }
                else if (srcFiles.length == 1)
                {
                    if (file == null)
                    {
                        file = new File(ds.getBasedir(), srcFiles[0]);
                        filesets.removeElementAt(0);
                    }
                    else
                    {
                    	validateMergeMultipleFiles();
                    }
                }
                else
                {
                	validateMergeMultipleFiles();
                }
            }
        }

        if (destFile != null)
        {
            destDir = fileUtils.getParentFile(destFile);
        }

    }
    
    /**
     * Supports merge-capable sub-classes by setting 
     * {@link MergeableFileTask#useMerge(boolean) useMerge(true)}, or throws
     * an exception if this task is not a merge-capable sub-class.
     * 
     * @throws Exception if this task does not implement 
     * {@code MergeableFileTask}; otherwise, sets {@code useMerge(true)} 
     * @see MergeableFileTask
     * @see MergeableConfigFileTask
     */
    private void validateMergeMultipleFiles() throws Exception
    {
    	if (this instanceof MergeableFileTask)
    	{
    		((MergeableFileTask) this).useMerge(true);
    	}
    	else
    	{
            throw new Exception(
                    "Cannot concatenate multiple files into a single file.");            		
    	}    	
    }

    /**
     * Compares source files to destination files to see if they should be
     * copied.
     *
     * @param fromDir The source directory
     * @param toDir   The destination directory
     * @param files   A list of files to copy
     * @param dirs    A list of directories to copy
     */
    protected void scan(File fromDir, File toDir, String[] files,
                        String[] dirs) throws Exception
    {
        FileNameMapper mapper = null;
        if (mapperElement != null)
        {
            mapper = mapperElement.getImplementation();
        }
        else if (flatten)
        {
            mapper = new FlatFileNameMapper();
        }
        else
        {
            mapper = new IdentityMapper();
        }

        buildMap(fromDir, toDir, files, mapper, fileCopyMap);

        if (includeEmpty)
        {
            buildMap(fromDir, toDir, dirs, mapper, dirCopyMap);
        }
    }

    /**
     * Add to a map of files/directories to copy
     *
     * @param fromDir the source directory
     * @param toDir   the destination directory
     * @param names   a list of filenames
     * @param mapper  a <code>FileNameMapper</code> value
     * @param map     a map of source file to array of destination files
     */
    protected void buildMap(File fromDir, File toDir, String[] names,
                            FileNameMapper mapper, Hashtable<String, String[]> map)
            throws Exception
    {
        String[] toCopy = null;
        if (forceOverwrite)
        {
            Vector<String> v = new Vector<String>();
            for (String name : names)
            {
                if (mapper.mapFileName(name) != null)
                {
                    v.addElement(name);
                }
            }
            toCopy = new String[v.size()];
            v.copyInto(toCopy);
        }
        else
        {
            SourceFileScanner ds = new SourceFileScanner();
            toCopy = ds.restrict(names, fromDir, toDir, mapper, granularity);
        }

        for (String elementToCopy : toCopy)
        {
            File src = new File(fromDir, elementToCopy);

            String[] mappedFiles = mapper.mapFileName(elementToCopy);

            if (!enableMultipleMappings)
            {
                map.put(src.getAbsolutePath(),
                        new String[]{new File(toDir, mappedFiles[0]).getAbsolutePath()});
            }
            else
            {
                // reuse the array created by the mapper
                for (int k = 0; k < mappedFiles.length; k++)
                {
                    mappedFiles[k] = new File(toDir, mappedFiles[k]).getAbsolutePath();
                }

                map.put(src.getAbsolutePath(), mappedFiles);
            }
        }
    }

    /**
     * Actually does the file (and possibly empty directory) copies.
     * This is a good method for subclasses to override.
     */
    protected void doFileOperations() throws Exception
    {
        if (fileCopyMap.size() > 0)
        {
            logger.fine("Copying " + fileCopyMap.size()
                    + " file" + (fileCopyMap.size() == 1 ? "" : "s")
                    + " to " + destDir.getAbsolutePath());

            Enumeration<String> e = fileCopyMap.keys();
            while (e.hasMoreElements())
            {
                String fromFile = e.nextElement();
                String[] toFiles = fileCopyMap.get(fromFile);

                for (String toFile : toFiles)
                {
                    if (fromFile.equals(toFile))
                    {
                        logger.warning("Skipping self-copy of " + fromFile);
                        continue;
                    }

                    try
                    {
                        logger.fine("Copying " + fromFile + " to " + toFile);
                        fileUtils.copyFile(fromFile, toFile, forceOverwrite,
                                preserveLastModified);
                    }
                    catch (IOException ioe)
                    {
                        String msg = "Failed to copy " + fromFile + " to " + toFile
                                + " due to " + ioe.getMessage();
                        File targetFile = new File(toFile);
                        if (targetFile.exists() && !targetFile.delete())
                        {
                            msg += " and I couldn't delete the corrupt " + toFile;
                        }
                        throw new Exception(msg, ioe);
                    }
                }
            }
        }

        if (includeEmpty)
        {
            Enumeration<String[]> e = dirCopyMap.elements();
            int createCount = 0;
            while (e.hasMoreElements())
            {
                String[] dirs = e.nextElement();
                for (String dir : dirs)
                {
                    File d = new File(dir);
                    if (!d.exists())
                    {
                        if (!d.mkdirs())
                        {
                            System.err.println("Unable to create directory "
                                    + d.getAbsolutePath());
                        }
                        else
                        {
                            createCount++;
                        }
                    }
                }
            }
            if (createCount > 0)
            {
                logger.fine("Copied " + dirCopyMap.size()
                        + " empty director"
                        + (dirCopyMap.size() == 1 ? "y" : "ies")
                        + " to " + createCount
                        + " empty director"
                        + (createCount == 1 ? "y" : "ies") + " under "
                        + destDir.getAbsolutePath());
            }
        }
    }
}
