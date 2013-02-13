/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.panels.userinput.field.file;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

/**
 * Directory field.
 *
 * @author Tim Anderson
 */
public class DirField extends AbstractFileField
{

    /**
     * Determines if directories must exist.
     */
    private final boolean mustExist;

    /**
     * Determines if directories can be created if they don't exist.
     */
    private final boolean create;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(DirField.class.getName());


    /**
     * Constructs a {@code DirField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public DirField(DirFieldConfig config, InstallData installData)
    {
        super(config, installData);
        mustExist = config.getMustExist();
        create = config.getCreate();
    }

    /**
     * Determines if directories must exist.
     *
     * @return {@code true} if the directories must exist; otherwise {@code false}
     */
    public boolean getMustExist()
    {
        return mustExist;
    }

    /**
     * Determines if directories can be created if they don't exist.
     *
     * @return {@code true} if directories can be created if they don't exist
     */
    public boolean getCreate()
    {
        return create;
    }

    /**
     * Determines if a directory is writable.
     *
     * @return {@code true} if the the directory is writable
     */
    public boolean isWritable(File dir)
    {
        boolean result = false;
        File parent = IoHelper.existingParent(dir);
        if (parent != null)
        {
            Platform platform = getInstallData().getPlatform();
            if (platform.isA(Platforms.WINDOWS))
            {
                // On windows we cannot use canWrite because it looks to the DOS flags which are not valid on NT or 2k
                // XP
                File tmpFile;
                try
                {
                    tmpFile = File.createTempFile("izWrTe", ".tmp", parent);
                    if (!tmpFile.delete())
                    {
                        tmpFile.deleteOnExit();
                    }
                    result = true;
                }
                catch (IOException exception)
                {
                    logger.log(Level.WARNING, exception.getMessage(), exception);
                }
            }
            else
            {
                result = parent.canWrite();
            }
        }
        return result;
    }
}
