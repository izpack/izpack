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

package com.izforge.izpack.installer.event;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.handler.ProgressHandler;
import com.izforge.izpack.event.SimpleInstallerListener;


/**
 * A container for {@link InstallerListener}s that supports notifying each registered listener.
 *
 * @author Tim Anderson
 */
public class InstallerListeners
{

    /**
     * The listeners.
     */
    private final List<InstallerListener> listeners = new ArrayList<InstallerListener>();

    /**
     * The file listeners, i.e. those listeners for who {@link InstallerListener#isFileListener() isFileListener()}
     * returns {@code true}.
     */
    private final List<InstallerListener> fileListeners = new ArrayList<InstallerListener>();

    /**
     * The installation data.
     */
    private final AutomatedInstallData installData;

    /**
     * The prompt.
     */
    private final Prompt prompt;


    /**
     * Constructs an {@code InstallerListeners}.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
    public InstallerListeners(AutomatedInstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void add(InstallerListener listener)
    {
        if (listener instanceof SimpleInstallerListener)
        {
            ((SimpleInstallerListener) listener).setInstalldata(installData);
        }
        listeners.add(listener);
    }

    /**
     * Returns the number of registered listeners.
     *
     * @return the number of registered listeners
     */
    public int size()
    {
        return listeners.size();
    }

    /**
     * Determines if there are no registered listeners.
     *
     * @return <tt>true</tt> if there are no registered listeners
     */
    public boolean isEmpty()
    {
        return listeners.isEmpty();
    }

    /**
     * Returns the listener at the specified index in the collection.
     *
     * @param index the index into the collection
     * @return the corresponding listener
     */
    public InstallerListener get(int index)
    {
        return listeners.get(index);
    }

    /**
     * Returns the installer listeners.
     *
     * @return the installer listeners
     */
    public List<InstallerListener> getInstallerListeners()
    {
        return listeners;
    }

    /**
     * Initialises the listeners.
     *
     * @throws IzPackException if a listener throws an exception
     */
    public void initialise()
    {
        for (InstallerListener listener : listeners)
        {
            listener.initialise();
            if (listener.isFileListener())
            {
                fileListeners.add(listener);
            }
        }
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs    the packs to install
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforePacks(List<Pack> packs, ProgressListener listener)
    {
        for (InstallerListener l : listeners)
        {
            if (l instanceof SimpleInstallerListener)
            {
                ((SimpleInstallerListener) l).setHandler(new ProgressHandler(listener, prompt));
            }
            l.beforePacks(packs);
        }
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack     the pack
     * @param i        the pack number
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforePack(Pack pack, int i, ProgressListener listener)
    {
        for (InstallerListener l : listeners)
        {
            if (l instanceof SimpleInstallerListener)
            {
                ((SimpleInstallerListener) l).setHandler(new ProgressHandler(listener, prompt));
            }
            l.beforePack(pack, i);
        }
    }

    /**
     * Determines if the listener should be notified of every file and directory installation.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise <tt>false</tt>
     */
    public boolean isFileListener()
    {
        return !fileListeners.isEmpty();
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeDir(File dir, PackFile packFile, Pack pack)
    {
        for (InstallerListener l : fileListeners)
        {
            l.beforeDir(dir, packFile, pack);
        }
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException if a listener throws an exception
     */
    public void afterDir(File dir, PackFile packFile, Pack pack)
    {
        for (InstallerListener l : fileListeners)
        {
            l.afterDir(dir, packFile, pack);
        }
    }

    /**
     * Invoked before a file is installed.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeFile(File file, PackFile packFile, Pack pack)
    {
        for (InstallerListener l : fileListeners)
        {
            l.beforeFile(file, packFile, pack);
        }
    }

    /**
     * Invoked after a file is installed.
     * <p/>
     * This implementation only invokes those listeners whose {@link #isFileListener()} returns <tt>true</tt>.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException if a listener throws an exception
     */
    public void afterFile(File file, PackFile packFile, Pack pack)
    {
        for (InstallerListener l : fileListeners)
        {
            l.afterFile(file, packFile, pack);
        }
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack     current pack object
     * @param i        current pack number
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterPack(Pack pack, int i, ProgressListener listener)
    {
        for (InstallerListener l : listeners)
        {
            if (l instanceof SimpleInstallerListener)
            {
                ((SimpleInstallerListener) l).setHandler(new ProgressHandler(listener, prompt));
            }
            l.afterPack(pack, i);
        }
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        for (InstallerListener l : listeners)
        {
            if (l instanceof SimpleInstallerListener)
            {
                ((SimpleInstallerListener) l).setHandler(new ProgressHandler(listener, prompt));
            }
            l.afterPacks(packs, listener);
        }
    }

}
