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

package com.izforge.izpack.uninstaller.event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.handler.ProgressHandler;
import com.izforge.izpack.event.SimpleUninstallerListener;


/**
 * A container for {@link UninstallerListener}s that supports notifying each registered listener.
 *
 * @author Tim Anderson
 */
public class UninstallerListeners
{

    /**
     * The listeners.
     */
    private final List<UninstallerListener> listeners = new ArrayList<UninstallerListener>();

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Determines if any of the listeners should be notified of file and directory events.
     */
    private boolean fileListener;


    /**
     * Constructs an {@code UninstallerListeners}.
     *
     * @param prompt the prompt
     */
    public UninstallerListeners(Prompt prompt)
    {
        this.prompt = prompt;
    }

    /**
     * Registers a listener.
     *
     * @param listener the listener to add
     */
    public void add(UninstallerListener listener)
    {
        listeners.add(listener);
        if (!fileListener && listener.isFileListener())
        {
            fileListener = true;
        }
    }

    /**
     * Initialises the listeners.
     *
     * @throws IzPackException for any error
     */
    public void initialise()
    {
        for (UninstallerListener listener : listeners)
        {
            listener.initialise();
        }
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files    all files which should be deleted
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeDeletion(List<File> files, ProgressListener listener)
    {
        ProgressHandler handler = new ProgressHandler(listener, prompt);
        for (UninstallerListener l : listeners)
        {
            try
            {
                if (listener instanceof SimpleUninstallerListener)
                {
                    ((SimpleUninstallerListener) listener).setHandler(handler);
                }
                l.beforeDelete(files);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Exception exception)
            {
                throw new IzPackException(exception);
            }
        }
    }

    /**
     * Invoked before a file is deleted.
     * <p/>
     * This implementation only invokes those listeners whose {@link UninstallerListener#isFileListener()} returns
     * <tt>true</tt>.
     *
     * @param file     the file which will be deleted
     * @param listener the UI progress handler
     * @throws IzPackException if a listener throws an exception
     */
    public void beforeDelete(File file, ProgressListener listener)
    {
        ProgressHandler handler = new ProgressHandler(listener, prompt);
        if (fileListener)
        {
            for (UninstallerListener l : listeners)
            {
                if (l.isFileListener())
                {
                    if (listener instanceof SimpleUninstallerListener)
                    {
                        ((SimpleUninstallerListener) listener).setHandler(handler);
                    }
                    l.beforeDelete(file);
                }
            }
        }
    }

    /**
     * Invoked after a file is deleted.
     * <p/>
     * This implementation only invokes those listeners whose {@link UninstallerListener#isFileListener()}
     * returns <tt>true</tt>.
     *
     * @param file     the file which was deleted
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterDelete(File file, ProgressListener listener)
    {
        ProgressHandler handler = new ProgressHandler(listener, prompt);
        if (fileListener)
        {
            for (UninstallerListener l : listeners)
            {
                if (l.isFileListener())
                {
                    if (listener instanceof SimpleUninstallerListener)
                    {
                        ((SimpleUninstallerListener) listener).setHandler(handler);
                    }
                    l.afterDelete(file);
                }
            }
        }
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files    the files which where deleted
     * @param listener the progress listener
     * @throws IzPackException if a listener throws an exception
     */
    public void afterDeletion(List<File> files, ProgressListener listener)
    {
        ProgressHandler handler = new ProgressHandler(listener, prompt);
        for (UninstallerListener l : listeners)
        {
            try
            {
                if (listener instanceof SimpleUninstallerListener)
                {
                    ((SimpleUninstallerListener) listener).setHandler(handler);
                }
                l.afterDelete(files, listener);
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Throwable exception)
            {
                throw new IzPackException(exception);
            }
        }
    }
}
