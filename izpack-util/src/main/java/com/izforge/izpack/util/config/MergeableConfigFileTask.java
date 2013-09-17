/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.MergeableFileTask;
import com.izforge.izpack.util.file.types.FileSet;

public abstract class MergeableConfigFileTask extends ConfigFileTask implements MergeableFileTask {
	
    private static final Logger logger = Logger.getLogger(MergeableConfigFileTask.class.getName());

    protected List<File> filesToMerge = new LinkedList<File>();
	protected boolean useMerge = false;

	@Override
	public void useMerge(boolean useMerge)
	{
		this.useMerge = useMerge;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Respects the setting of {@link #useMerge(boolean) useMerge}. If 
	 * {@code true}, attempts to merge all available source files (from
	 * single-file task attributes, and from filesets). Otherwise, calls
	 * {@link ConfigFileTask#execute()} to do one or more single-file merges.
	 */
	@Override
	public void execute() throws Exception
	{
		if (useMerge)
		{
	    	File savedSrcFile = srcFile; //may be altered in validateAttributes
	        File savedFile = file;
	        File savedDestFile = destFile;
	        File savedDestDir = destDir;
	        FileSet savedFileSet = null;
	        if (file == null && destFile != null && filesets.size() == 1)
	        {
	            // will be removed in validateAttributes
	            savedFileSet = filesets.elementAt(0);
	        }

	        validateAttributes();
	        
			try
			{
	        	addSingleFilesToMerge(srcFile, file, destFile);
			
		        for (FileSet fs : filesets) {
		            DirectoryScanner ds = fs.getDirectoryScanner();
		            String[] includedFiles = ds.getIncludedFiles();
		            for (String includedFile : includedFiles)
		            {
		                filesToMerge.add(new File(ds.getBasedir(), includedFile));
		            }
		        }
	
		        if (filesToMerge.size() < 2)
		        {
		            logger.warning("Merge skipped, not enough input files to merge");
		        }
		        else
		        {
					doFileMergeOperation();
			        if (cleanup)
			        {
			            for (File file : filesToMerge)
			            {
			                if (file.exists() && !file.equals(destFile))
			                {
			                    if (!file.delete())
			                    {
			                        logger.warning("Merge source file " + file + " could not be cleaned up");
			                    }
			                }
			            }
			        }
					
		        }
			}
            catch (Exception e)
            {
                if (failonerror)
                {
                    throw e;
                }
                else
                {
                    logger.warning(e.getMessage());
                }
            }
			finally
			{
	            // clean up again, so this instance can be used a second
	            // time
	    		srcFile = savedSrcFile;
	            file = savedFile;
	            destFile = savedDestFile;
	            destDir = savedDestDir;
	            if (savedFileSet != null)
	            {
	                filesets.insertElementAt(savedFileSet, 0);
	            }

	            filesToMerge.clear();
			}
		}
		else
		{
			super.execute();
		}
	}
	
	@Override
	protected void doFileOperation(File srcFile, File patchFile, File destFile) throws Exception {
		addSingleFilesToMerge(srcFile, patchFile, destFile);
		try
		{
			doFileMergeOperation();
		}
		finally
		{
			filesToMerge.clear();
		}
		
	}
	
	private void addSingleFilesToMerge(File srcFile, File patchFile, File destFile)
	{
		if (srcFile != null && srcFile.exists())
		{
			filesToMerge.add(srcFile);
		}
		if (patchFile != null && patchFile.exists())
		{
			filesToMerge.add(patchFile);
		}		
		if (destFile.exists())
		{
			filesToMerge.add(destFile);
		}
	}
	
	protected abstract void doFileMergeOperation() throws Exception;
}
