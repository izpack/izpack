/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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

import com.izforge.izpack.util.file.FileCopyTask;
import com.izforge.izpack.util.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

public abstract class ConfigurableFileCopyTask extends FileCopyTask implements ConfigurableTask
{

    private static final Logger LOGGER = Logger.getLogger(ConfigurableFileCopyTask.class.getName());

    private boolean patchPreserveEntries = true;

    private boolean patchPreserveValues = true;

    private boolean patchResolveVariables = false;

    protected boolean cleanup;

    /**
     * Whether to preserve equal entries but not necessarily their values from
     * an old configuration, if they can be found (default: true).
     *
     * @param preserveEntries - true to preserve equal entries from an old
     * configuration
     */
    public void setPatchPreserveEntries(boolean preserveEntries)
    {
        this.patchPreserveEntries = preserveEntries;
    }

    /**
     * Whether to preserve the values of equal entries from an old
     * configuration, if they can be found (default: true). Set false to
     * overwrite old configuration values by default with the new ones,
     * regardless whether they have been already set in an old configuration.
     * Values from an old configuration can only be preserved, if the
     * appropriate entries exist in an old configuration.
     *
     * @param preserveValues - true to preserve the values of equal entries from
     * an old configuration
     */
    public void setPatchPreserveValues(boolean preserveValues)
    {
        patchPreserveValues = preserveValues;
    }

    /**
     * Whether variables should be resolved during patching.
     *
     * @param resolve - true to resolve in-value variables
     */
    public void setPatchResolveExpressions(boolean resolve)
    {
        patchResolveVariables = resolve;
    }

    /**
     * Whether to delete the patchfiles after the operation
     *
     * @param cleanup True, if the patchfiles should be deleted after the
     * operation
     */
    public void setCleanup(boolean cleanup)
    {
        this.cleanup = cleanup;
    }

    /**
     * Do a patch operation.
     *
     * @param oldFile original file to patch from
     * @param newFile newer reference file to patch certain values or entries to
     * @param toFile output file of the patched result
     * @param patchPreserveEntries set true to reserve old entries
     * @param patchPreserveValues set true to reserver old values
     * @param patchResolveVariables set true to resolve in-text variables during
     * patching
     */
    protected abstract void doFileOperation(File oldFile, File newFile, File toFile,
            boolean patchPreserveEntries, boolean patchPreserveValues, boolean patchResolveVariables)
            throws Exception;

    @Override
    protected void doFileOperations() throws Exception
    {
        if (fileCopyMap.size() > 0)
        {
            LOGGER.fine("Merge/copy " + fileCopyMap.size() + " file"
                    + (fileCopyMap.size() == 1 ? "" : "s") + " in " + destDir.getAbsolutePath());

            Enumeration<String> e = fileCopyMap.keys();
            while (e.hasMoreElements())
            {
                String fromFile = e.nextElement();
                String[] toFiles = fileCopyMap.get(fromFile);

                for (String toFile : toFiles)
                {
                    if (fromFile.equals(toFile))
                    {
                        LOGGER.warning("Skipping self-merge/copy of " + fromFile);
                        continue;
                    }

                    LOGGER.fine("Merge/copy " + fromFile + " into " + toFile);

                    File to = new File(toFile);
                    File parent = to.getParentFile();
                    if (parent != null && !parent.exists())
                    {
                        parent.mkdirs();
                    }
                    if (!to.exists())
                    {
                        to.createNewFile();
                    }

                    File toTmp = File.createTempFile("tmp-", null, parent);

                    try
                    {
                        // The target file to copy to is the original (old) file to
                        // take preservations of old entries and values from
                        // The source file to copy from is the new file which contains
                        // the reference entries and values which might be patched from
                        // the original ones
                        File from = new File(fromFile);
                        doFileOperation(from, to, toTmp, patchPreserveEntries,
                                patchPreserveValues, patchResolveVariables);

                        FileUtils.copyFile(toTmp, to, forceOverwrite, preserveLastModified);
                        if (cleanup && from.exists())
                        {
                            if (!from.delete())
                            {
                                LOGGER.warning("File " + from + " could not be cleant up");
                            }
                        }
                    }
                    catch (IOException be)
                    {
                        String msg = "Failed to merge/copy " + fromFile + " into " + toFile
                                + " due to " + be.getMessage();
                        File targetFile = new File(toFile);
                        if (targetFile.exists() && !targetFile.delete())
                        {
                            msg += " and I couldn't delete the corrupt " + toFile;
                        }
                        throw new Exception(msg, be);
                    }
                    finally
                    {
                        toTmp.delete();
                    }
                }
            }
        }
    }

}
