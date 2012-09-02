/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.uninstaller;

import static com.izforge.izpack.api.handler.Prompt.Type.ERROR;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.uninstaller.event.UninstallerListeners;
import com.izforge.izpack.uninstaller.resource.Executables;
import com.izforge.izpack.uninstaller.resource.InstallLog;
import com.izforge.izpack.uninstaller.resource.RootScripts;


/**
 * The files destroyer class.
 *
 * @author Julien Ponge
 * @author Tim Anderson
 */
public class Destroyer implements Runnable
{

    /**
     * The log of installed files.
     */
    private final InstallLog log;

    /**
     * The uninstaller listeners.
     */
    private final UninstallerListeners listeners;

    /**
     * The executables.
     */
    private final Executables executables;

    /**
     * The root scripts.
     */
    private final RootScripts rootScripts;

    /**
     * The prompt.
     */
    private Prompt prompt;

    /**
     * The progress listener. May be {@code null}.
     */
    private ProgressListener listener;

    /**
     * True if the destroyer must force recursive deletion.
     */
    private boolean forceDelete;

    /**
     * Tracks the no. of files that couldn't be deleted.
     */
    private List<File> failed = new ArrayList<File>();

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Destroyer.class.getName());

    /**
     * The constructor.
     *
     * @param log       the installation log
     * @param listeners the uninstaller listeners
     * @param prompt    the prompt
     */
    public Destroyer(InstallLog log, UninstallerListeners listeners,
                     Executables executables, RootScripts rootScripts, Prompt prompt)
    {
        this.log = log;
        this.listeners = listeners;
        this.executables = executables;
        this.rootScripts = rootScripts;
        this.prompt = prompt;
    }

    /**
     * Sets the prompt.
     *
     * @param prompt the prompt
     */
    public void setPrompt(Prompt prompt)
    {
        this.prompt = prompt;
    }

    /**
     * Registers a listener to be notified of progress.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setProgressListener(ProgressListener listener)
    {
        this.listener = listener;
    }

    /**
     * Determines if any remaining files should be removed after the installed files are removed.
     *
     * @param force if <tt>true</tt>, remove remaining files
     */
    public void setForceDelete(boolean force)
    {
        this.forceDelete = force;
    }

    /**
     * Runs the destroyer.
     */
    @Override
    public void run()
    {
        try
        {
            if (!executables.run())
            {
                logger.severe("An executable has failed. Destroyer will not be run");
            }
            else
            {
                destroy();
            }
        }
        catch (Throwable exception)
        {
            if (listener != null)
            {
                listener.stopAction();
            }
            logger.log(Level.SEVERE, exception.getMessage(), exception);

            StringWriter trace = new StringWriter();
            exception.printStackTrace(new PrintWriter(trace));

            prompt.message(ERROR, "exception caught", trace.toString());
        }
    }

    /**
     * Returns any files that could not be removed.
     *
     * @return the files
     */
    public List<File> getFailedToDelete()
    {
        return failed;
    }

    /**
     * Deletes installed files, runs any root scripts, and cleans up remaining files if required.
     *
     * @throws Exception for any error
     */
    private void destroy() throws Exception
    {
        List<File> files = log.getInstalled();
        int size = files.size();
        listeners.beforeDeletion(files, listener);
        if (listener != null)
        {
            listener.startAction("destroy", size);
        }

        for (int i = 0; i < size; i++)
        {
            File file = files.get(i);
            listeners.beforeDelete(file, listener);

            delete(file);

            listeners.afterDelete(file, listener);
            if (listener != null)
            {
                listener.progress(i, file.getAbsolutePath());
            }
        }

        listeners.afterDeletion(files, listener);

        rootScripts.run();

        // We make a complementary cleanup
        if (listener != null)
        {
            listener.progress(log.getInstalled().size(), "[ cleanups ]");
        }

        File installPath = new File(log.getInstallPath());
        cleanup(installPath);

        // verify that the files no longer exist. Check this here, as the root scripts may have performed cleanup.
        checkDeletion(files, installPath);

        if (listener != null)
        {
            listener.stopAction();
        }
    }

    /**
     * Verifies that the installed files have been deleted.
     *
     * @param files       the files to check
     * @param installPath the installation path
     */
    private void checkDeletion(List<File> files, File installPath)
    {
        failed.clear();
        for (File f : files)
        {
            if (f.exists())
            {
                failed.add(f);
            }
        }
        if (installPath.exists())
        {
            failed.add(installPath);
        }
    }

    /**
     * Recursively deletes a directory tree.
     *
     * @param file the file to delete
     */
    private void cleanup(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File child : files)
                {
                    cleanup(child);
                }
            }
            delete(file);
        }
        else if (forceDelete)
        {
            delete(file);
        }
    }

    /**
     * Deletes a file.
     *
     * @param file the file to delete
     */
    private void delete(File file)
    {
        if (file.exists() && !file.delete())
        {
            logger.info("Failed to delete: " + file);
        }
    }

}
