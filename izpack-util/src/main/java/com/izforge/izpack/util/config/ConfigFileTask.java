/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
 * Copyright 2012 Daniel Abson
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

package com.izforge.izpack.util.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.FileCopyTask;

public abstract class ConfigFileTask extends FileCopyTask implements ConfigurableTask
{
    private static final Logger logger = Logger.getLogger(ConfigFileTask.class.getName());

	protected File srcFile;
    protected boolean cleanup = false;
    protected boolean create = true;
    protected String headerComment;
    
    private List<File> cleanFiles = new ArrayList<File>();
    private List<File> deleteFiles = new ArrayList<File>();
    
    public ConfigFileTask()
    {
        super();
        forceOverwrite = true;
    }

    /**
     * Set location of the configuration file to be updated; required.
     * Implementations may wish to define separate methods to control 
     * preservation of old data where {@code toFile} already exists.
     */
    public void setToFile(File toFile)
    {
        this.file = toFile;
    }

    /**
     * Location of the configuration file to be patched from; optional. 
     * Implementations are also expected to provide discrete methods to add
     * new config data to a task. 
     */
    public void setSrcFile(File srcFile)
    {
        this.srcFile = srcFile;
    }

    /**
     * Location of the resulting output file; optional. If not set, defaults
     * to {@link #setToFile(File) toFile}; in this case, operation will fail
     * if {@code toFile} exists, and {@code overwrite} is false.
     * 
     * @param destFile the resulting output file
     */
    public void setTargetFile(File destFile)
    {
        this.destFile = destFile;
    }

    /**
     * Set true to delete source file(s) after the operation. Default is 
     * false.
     * 
     * @param cleanup true, if the source file(s) should be deleted after the 
     * operation
     */
    public void setCleanup(boolean cleanup)
    {
        this.cleanup = cleanup;
    }
    
    /**
     * Set true to create the destination file if it doesn't exist. Default is
     * true. If set to false, and the destination file doesn't exist, the task
     * will be skipped. 
     * 
     * @param create
     */
    public void setCreate(boolean create)
    {
    	this.create = create;
    }

    /**
     * Optional header comment for the file. Implementations may ignore
     * this attribute if comments are not supported, but should document the 
     * fact.
     * 
     * @param headerComment optional header comment for the file
     */
    public void setHeaderComment(String headerComment)
    {
        this.headerComment = headerComment;
    }
    
    @Override
    public void execute() throws Exception
    {
    	File savedSrcFile = srcFile; //may be altered in validateAttributes
    	try
    	{
    		super.execute();
    	}
    	finally
    	{ 
    		// clean up again, so this instance can be used a second time  
    		srcFile = savedSrcFile;
    		deleteFilesSilently(deleteFiles);
    		if (cleanup)
    		{
    			deleteFilesSilently(cleanFiles);
    		}
    		cleanFiles.clear();
    	}
    }
    
    private void deleteFilesSilently(List<File> files)
    {
    	for (File file : files)
    	{
    		if (file != null && file.exists())
    		{
    			if(!file.delete())
    			{
    				logger.log(Level.SEVERE, "Failed to delete temporary or obsolete file " + file.getPath());
    			}
    		}
    	}
    }
    
    @Override
    protected boolean shouldAllowFileOperation(File file, File destFile)
    {
    	 return destFile.exists() ? forceOverwrite : create;
    }
    

    /**
     * {@inheritDoc}
     * 
     * Implementations may override this method to add extra checks to 
     * validate implementation-specific attributes, and call
     * {@code super.validateAttributes()} before returning. Not calling up to
     * this method will also skip checks (including fileset validation) done 
     * by {@code FileCopyTask.validateAttributes()}. 
     * 
     */
    @Override
    protected void validateAttributes() throws Exception
    {
    	if(!forceOverwrite && !create)
    	{
    		throw new Exception("Only one of overwrite and create can be false.");
    	}
        if (srcFile != null && srcFile.equals(file))
    	{ //'from' and 'to' are the same file - no need to patch
    		srcFile = null;
    	}
        if (file != null)
        {
	        if (destFile == null)
	        {
	        	destFile = file;
	        }
	    	if (!file.exists())
	    	{ //allow patching to a file that may or may not exist
	    		file.createNewFile();
	    		if (!file.equals(destFile))
	    		{ //if dummy 'to' is not also the destination file, clean it up at the end
	    			deleteFiles.add(file);    				
	    		}
	    		else if (!create)
	    		{ //dummy 'to' is also the destination file, but create="false"
	    			throw new Exception("Not allowed to create new file " + file.getPath());
	    		}
	    	}
        }
        super.validateAttributes();
    }

    @Override
    protected void doFileOperations() throws Exception
    {
        if (fileCopyMap.size() > 0)
        {
            logger.fine("Update " + fileCopyMap.size() + " file"
                    + (fileCopyMap.size() == 1 ? "" : "s") + " in " + destDir.getAbsolutePath());

            for (Map.Entry<String,String[]> entry : fileCopyMap.entrySet())
            {
                File patchFile = new File(entry.getKey());
                String[] toFiles = entry.getValue();
                boolean cleanPatchFile = true;

                for (String toFile : toFiles)
                {
                    logger.fine("Merge/copy " + patchFile + " into " + toFile);

                    File to = null;
                    File toTmp = null;
                    File cleanFile = null;
                    
                    try
                    {
	                    to = new File(toFile);
	                    File parent = to.getParentFile();
	                    if (parent != null && !parent.exists())
	                    {
	                        parent.mkdirs();
	                    }
	                    if (!to.exists())
	                    {
	                    	if (create)
	                    	{
	                    		to.createNewFile();
	                    	}
	                    	else
	                    	{
	                    		throw new FileNotFoundException("Destination file " + to.getAbsolutePath()
	                    				+ " does not exist, and is not allowed to be created.");
	                    	}
	                    }
	                    
	                    toTmp = File.createTempFile("tmp-", null, parent);
                	
	                	/*
	                	 * TODO: Make single- and multi-file operations consistent.
	                	 * If multi-file ops could have an alternative target 
	                	 * (i.e. src/patch/target), the call to doFileOperation
	                	 * would always be the same 
	                	 */
	                	if (srcFile == null)
	                	{
	                		//Single-file, two-way merge in multi-file task
	                		//	patchFile is source, to is destination & target
	                		//OR 
	                		//Single-file task with no file to merge (implies
	                		//extra, implementation-specific config is set)
	                		//  patchFile is destination, to is target 
	                		//  (may be the same)
	                		doFileOperation(patchFile, to, toTmp);
	                        cleanFile = patchFile;
	                	}
	                	else
	                	{
	                		//Single-file, two-way merge with optional 
	                		//separate target file (single-file task)
	                		//	srcFile is source, patchFile is destination,
	                		//  toTmp will later overwrite target
	                		doFileOperation(srcFile, patchFile, toTmp);
	                        cleanFile = srcFile;
	                	}
                	
                        getFileUtils().copyFile(toTmp, to, forceOverwrite, preserveLastModified);
	                    deleteFiles.add(toTmp);
	                    cleanFiles.add(cleanFile);
                    }
                    catch (Exception e)
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("Failed to replace ");
                        msg.append(to.getPath());
                        msg.append(" with new config");
                        if(toTmp != null && toTmp.exists())
                        {
                    		msg.append(" in temp file ");
                    		msg.append(toTmp.getPath()); 
                     		msg.append("; temp file will not be deleted");                        	
                        }
                        if(cleanup && cleanFile != null)
                        {
                        	msg.append("; source file ");
                        	msg.append(cleanFile.getPath());
                        	msg.append(" will not be cleaned up");
                        }
                        
                    	if (failonerror)
                    	{
                            throw new Exception(msg.toString(), e);                    		
                    	}
                    	else
                    	{
                    		logger.log(Level.SEVERE, msg.toString());
                    		cleanPatchFile = false;
                    	}
                    }                    
                }
                
                if(!cleanPatchFile)
                { //if at least one merge/copy in a multi-file operation failed, don't delete the original file
                	cleanFiles.remove(patchFile);
                }
            }
        }
    }
    
    /**
     * Perform the task of merging config from {@code srcFile} to 
     * {@code patchFile}, and store the result in {@code destFile}. May 
     * optionally patch in config from an implementation-specific source.
     * 
     * Only {@code destFile} must be non-null, and either of the other two
     * arguments may refer to an empty or non-existent file.
     * 
     * @param srcFile the original configuration file
     * @param patchFile the new configuration file
     * @param destFile destination file to which the merge should be stored
     * @throws Exception in the event of any processing error
     */
    protected abstract void doFileOperation(File srcFile, File patchFile, File destFile) throws Exception;
}
