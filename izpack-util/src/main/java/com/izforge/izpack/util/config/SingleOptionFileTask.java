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
import java.io.IOException;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.Options;

public class SingleOptionFileTask extends ConfigFileTask
{
    private static final Logger logger = Logger.getLogger(SingleOptionFileTask.class.getName());

    @Override
    protected void readSourceConfigurable() throws Exception
    {
        if (oldFile != null)
        {
            try
            {
                if (!oldFile.exists())
                {
                    logger.warning("Options file " + oldFile.getAbsolutePath()
                            + " to patch from could not be found, no patches will be applied");
                    return;
                }
                logger.fine("Loading options file: " + oldFile.getAbsolutePath());
                // Configuration file type must be the same as the target type
                fromConfigurable = new Options(this.oldFile);
            }
            catch (IOException ioe)
            {
                throw new Exception(ioe.toString());
            }
        }
    }

    @Override
    protected void readConfigurable() throws Exception
    {
        if (newFile != null && newFile.exists())
        {
            try
            {
                logger.fine("Loading original configuration file: " + newFile.getAbsolutePath());
                configurable = new Options(newFile);
            }
            catch (IOException ioe)
            {
                throw new Exception("Error opening original configuration file: " + ioe.toString());
            }
        }
        else if (toFile != null && toFile.exists())
        {
            try
            {
                logger.fine("Loading target configuration file: " + toFile.getAbsolutePath());
                configurable = new Options(toFile);
            }
            catch (IOException ioe)
            {
                throw new Exception("Error opening target configuration file: " + ioe.toString());
            }
        }
        else
        {
            configurable = new Options();
        }
    }

    @Override
    protected void writeConfigurable() throws Exception
    {

        try
        {
            if (!toFile.exists())
            {
                if (createConfigurable)
                {
                    File parent = toFile.getParentFile();
                    if (parent != null && !parent.exists())
                    {
                        parent.mkdirs();
                    }
                    logger.fine("Creating empty properties file: " + toFile.getAbsolutePath());
                    toFile.createNewFile();
                }
                else
                {
                    logger.warning("Options file " + toFile.getAbsolutePath()
                            + " did not exist and is not allowed to be created");
                    return;
                }
            }
            Options opts = (Options) configurable;
            opts.setFile(toFile);
            opts.setHeaderComment(getComment());
            opts.store();
        }
        catch (IOException ioe)
        {
            throw new Exception(ioe);
        }

        if (cleanup && oldFile.exists())
        {
            if (!oldFile.delete())
            {
                logger.warning("File " + oldFile + " could not be cleant up");
            }
        }
    }
}
