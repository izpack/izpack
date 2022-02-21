/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.installer.requirement;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Option;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.util.FileUtil;

/**
 * Determines if another installation is in progress, by checking for the existence of a lock file.
 *
 * @author Tim Anderson
 */
public class LockFileChecker implements RequirementChecker
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(LockFileChecker.class.getName());


    /**
     * Constructs a <tt>LockFileChecker</tt>.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
    public LockFileChecker(AutomatedInstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean result = true;
        Info installationInfo = installData.getInfo();

        if (installationInfo.isSingleInstance() && !Boolean.getBoolean("MULTIINSTANCE"))
        {
            String appName = installationInfo.getAppName();
            File file = FileUtil.getLockFile(appName);
            if (file.exists())
            {
                result = handleLockFile(file);
            }
            else
            {
                try
                {
                    // Create the new lock file
                    if (file.createNewFile())
                    {
                        logger.fine("Created lock file:" + file.getPath());
                        file.deleteOnExit();
                    }
                    else
                    {
                        logger.warning("Failed to create lock file: " + file.getPath());
                        logger.warning("*** Multiple instances of installer will be allowed ***");
                    }
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Lock file could not be created: " + e.getMessage(), e);
                    logger.warning("*** Multiple instances of installer will be allowed ***");
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * Invoked when the lock file already exists.
     *
     * @param file the lock file
     * @return <tt>true</tt> if the user wants to proceed with installation, <tt>false</tt> if they want to cancel
     */
    protected boolean handleLockFile(File file)
    {
        boolean result = false;
        if (Installer.getInstallerMode() == Installer.INSTALLER_AUTO)
        {
            logger.fine("Lock file exists.");
            try
            {
                installData.setVariable("LOCK_FILE", file.getCanonicalPath());
            }
            catch (IOException ignored)
            {
            }
            String msg = installData.getMessages().get("LockFile.exists.message", "Lock file exist.");
            System.out.println(installData.getVariables().replace(msg));
            // Leave the file as it is.
            logger.fine("Leaving temp file alone and exiting");
        }
        else
        {
            logger.fine("Lock file exists, asking user for permission to proceed.");
            String msg = installData.getMessages().get("LockFile.exists.prompt", "Lock file exist.");
            Option selected = prompt.confirm(Prompt.Type.WARNING, installData.getVariables().replace(msg), Prompt.Options.YES_NO);
            if (selected == Option.YES)
            {
                // Take control of the file so it gets deleted after this installer instance exits.
                logger.fine("Setting temp file to delete on continue");
                // FIXME Avoid deleting lock for other running instances by using some pool of locks
                // (this is just a workaround to clean up)
                file.deleteOnExit();
                result = true;
            }
            else
            {
                // Leave the file as it is.
                logger.fine("Leaving temp file alone and exiting");
            }
        }
        return result;
    }
}
