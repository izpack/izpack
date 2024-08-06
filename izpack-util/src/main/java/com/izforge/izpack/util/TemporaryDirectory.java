/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Sean O'Loughlin
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


package com.izforge.izpack.util;

import com.izforge.izpack.api.data.Info.TempDir;
import com.izforge.izpack.api.data.InstallData;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the life-cycle of a temporary directory
 */
public class TemporaryDirectory implements CleanupClient
{
    private static final Logger logger = Logger.getLogger(TemporaryDirectory.class.getName());

    private File tempdir;
    private final InstallData installData;
    private final TempDir tempDirDescription;
    private boolean deleteOnExit = false;

    /**
     * The house-keeper.
     */
    private final Housekeeper housekeeper;

    /**
     * Define a temporary directory
     *
     * @param tempDirDescription describes the parameters of the directory to be created
     * @param installData        The install data in to which the temporary directories variable will be written
     * @param housekeeper the house-keeper
     */
    public TemporaryDirectory(TempDir tempDirDescription, InstallData installData, Housekeeper housekeeper)
    {
        if (null == tempDirDescription)
        {
            throw new IllegalArgumentException("Unable to create a temporary directory, the temp directory description may not be null.");
        }
        if (null == installData)
        {
            throw new IllegalArgumentException("Unable to create a temporary directory, the install data may not be null.");
        }
        this.installData = installData;
        this.tempDirDescription = tempDirDescription;
        this.housekeeper = housekeeper;

    }

    /**
     * Creates the temporary directory and sets the install data variable to point to it
     *
     * @throws IOException if creation of the directory fails
     */
    public void create() throws IOException
    {
        try
        {
            tempdir = File.createTempFile(tempDirDescription.getPrefix(), tempDirDescription.getSuffix());
            tempdir.delete();
            tempdir.mkdir();
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE,
                    "Unable to create temporary directory for installation: " + e.getMessage(),
                    e);
            throw e;
        }
        installData.setVariable(tempDirDescription.getVariableName(), tempdir.getAbsolutePath());
        housekeeper.registerForCleanup(this);
    }

    /**
     * Configure this temporary directory to be deleted when the installer exits
     * This will delete the directory and all of its contents
     */
    public void deleteOnExit()
    {
        deleteOnExit = true;
    }

    /**
     * Deletes the temporary directory and all it's contents immediately
     */
    @Override
    public void cleanUp()
    {
        if (null != tempdir)
        {
            if (deleteOnExit)
            {
                try
                {
                    FileUtils.deleteDirectory(tempdir);
                }
                catch (IOException e)
                {
                    logger.warning("Failed to properly clean up files in "
                            + tempdir.getAbsolutePath() + ": " + e.getMessage()
                            + ", manual clean up may be required.");
                }
            }
            else
            {
                logger.warning("Temporary directory has not been cleaned up. Files have been left in: " + tempdir.getAbsolutePath());

            }
        }
        else
        {
            logger.warning("Temporary directory registered for cleanup but there is no such directory");
        }
    }



}