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

package com.izforge.izpack.event;


import java.io.File;
import java.util.List;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;


/**
 * Abstract implementation of {@link UninstallerListener}.
 * <p/>
 * This provides no-op versions of each of the methods, to simplify implementation of listeners that only need
 * some methods.
 *
 * @author Tim Anderson
 */
public abstract class AbstractUninstallerListener implements UninstallerListener
{

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files all files which should be deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(List<File> files)
    {
    }

    /**
     * Invoked before a file is deleted.
     *
     * @param file the file which will be deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(File file)
    {
    }

    /**
     * Invoked after a file is deleted.
     *
     * @param file the file which was deleted
     * @throws IzPackException for any error
     */
    @Override
    public void afterDelete(File file)
    {
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files    the files which where deleted
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterDelete(List<File> files, ProgressListener listener)
    {
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files   all files which should be deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
    }

    /**
     * Determines if the listener should be notified of every file deletion.
     * <p/>
     * If <tt>true</tt>, the {@link #beforeDelete} and {@link #afterDelete} methods will be invoked for each file.
     *
     * @return <tt>true</tt> if this listener would be informed at every delete operation, else <tt>false</tt>
     */
    @Override
    public boolean isFileListener()
    {
        return false;
    }

    /**
     * Invoked before a file is deleted.
     *
     * @param file    the file which will be deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
    }

    /**
     * Invoked after a file is deleted.
     *
     * @param file    the file which was deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files   the files which where deleted
     * @param handler the UI progress handler
     * @throws Exception for any error
     */
    @Override
    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
    }
}
