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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.xmlmerge.AbstractXmlMergeException;
import com.izforge.izpack.util.xmlmerge.ConfigurationException;
import com.izforge.izpack.util.xmlmerge.XmlMerge;
import com.izforge.izpack.util.xmlmerge.config.ConfigurableXmlMerge;
import com.izforge.izpack.util.xmlmerge.config.PropertyXPathConfigurer;

public class SingleXmlFileMergeTask implements ConfigurableTask
{

    private static final Logger LOGGER = Logger.getLogger(SingleXmlFileMergeTask.class.getName());

    protected File origfile;
    protected File patchfile;
    protected File tofile;
    protected File conffile;
    protected boolean cleanup;

    protected Properties confProps = new Properties();

    public void setOriginalFile(File origfile)
    {
        this.origfile = origfile;
    }

    public void setPatchFile(File patchfile)
    {
        this.patchfile = patchfile;
    }

    public void setToFile(File tofile)
    {
        this.tofile = tofile;
    }

    public void setConfigFile(File confFile)
    {
        this.conffile = confFile;
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
     * List of file sets.
     */
    List<FileSet> filesets = new ArrayList<FileSet>();

    /**
     * Adds a file set.
     *
     * @param fileset The file set to add
     */
    public void addFileSet(FileSet fileset)
    {
        filesets.add(fileset);
    }

    /**
     * Adds a XML merge configuration property (XPath)
     *
     * @param key The property key
     * @param value The property value
     */
    public void addProperty(String key, String value)
    {
        confProps.setProperty(key, value);
    }

    /**
     * Validates the configuration and destination files and the file sets.
     */
    public void validate() throws Exception
    {
        if (tofile == null)
        {
            throw new Exception("XML merge output file not set");
        }
        if (filesets.isEmpty() && patchfile == null)
        {
            throw new Exception("No XML merge patch files given at all");
        }
        if (origfile == null)
        {
            throw new Exception("No XML merge patch files given at all");
        }
        if (!confProps.isEmpty() && conffile != null)
        {
            throw new Exception("Using both XML merge configuration file and explicit merge properties not allowed");
        }
    }

    @Override
    public void execute() throws Exception
    {
        validate();

        // Get the files to merge
        LinkedList<File> filesToMerge = new LinkedList<File>();

        if (origfile != null)
        {
            if (origfile.exists())
            {
                filesToMerge.add(origfile);
            }
            else
            {
                LOGGER.warning("XML merge skipped, target file " + origfile + " not found");
                return;
            }
        }
        else
        {
            LOGGER.warning("XML merge skipped, target file not defined");
            return;
        }

        if (patchfile != null && patchfile.exists())
        {
            filesToMerge.add(patchfile);
        }

        for (FileSet fs : filesets)
        {
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] includedFiles = ds.getIncludedFiles();
            for (String includedFile : includedFiles)
            {
                filesToMerge.add(new File(ds.getBasedir(), includedFile));
            }
        }

        if (filesToMerge.size() < 2)
        {
            LOGGER.warning("XML merge skipped, not enough XML input files to merge");
            return;
        }

        if (conffile != null)
        {
            InputStream configIn = null;
            try
            {
                configIn = new FileInputStream(conffile);
                confProps.load(configIn);
            }
            catch (IOException e)
            {
                throw new Exception(e);
            }
            finally
            {
                if (configIn != null)
                {
                    try
                    {
                        configIn.close();
                    }
                    catch (IOException e)
                    {
                        LOGGER.log(Level.WARNING,
                                "Error closing file '" + conffile + "': " + e.getMessage(),
                                e);
                    }
                }
            }
        }

        // Create the XmlMerge instance and execute the merge
        XmlMerge xmlMerge;
        try
        {
            xmlMerge = new ConfigurableXmlMerge(new PropertyXPathConfigurer(confProps));
        }
        catch (ConfigurationException e)
        {
            throw new Exception(e);
        }

        try
        {
            xmlMerge.merge(filesToMerge.toArray(new File[filesToMerge.size()]), tofile);
        }
        catch (AbstractXmlMergeException e)
        {
            throw new Exception(e);
        }

        if (cleanup)
        {
            for (File file : filesToMerge)
            {
                if (file.exists() && !file.equals(tofile))
                {
                    if (!file.delete())
                    {
                        LOGGER.warning("File " + file + " could not be cleant up");
                    }
                }
            }
        }
    }
}
