/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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
 * <p>
 * This class implements all methods of interface UninstallerListener, but do not do enything. It
 * can be used as base class to save implementation of unneeded methods.
 * </p>
 *
 * @author Klaus Bartz
 * @deprecated use {@code com.izforge.izpack.event.AbstractUninstallerListener}. This class will be removed in IzPack
 *             6.0
 */
@Deprecated
public class SimpleUninstallerListener implements UninstallerListener
{

    /**
     * The progress handler.
     */
    private AbstractUIProgressHandler handler;

    /**
     *
     */
    public SimpleUninstallerListener()
    {
        super();
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
    }

    public void setHandler(AbstractUIProgressHandler handler)
    {
        this.handler = handler;
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
        try
        {
            beforeDeletion(files, handler);
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
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
        try
        {
            beforeDelete(file, handler);
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
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
        try
        {
            afterDelete(file, handler);
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
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
        try
        {
            afterDeletion(files, handler);
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDeletion(java.util.List,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void beforeDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#beforeDelete(java.io.File,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void beforeDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDelete(java.io.File,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void afterDelete(File file, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#afterDeletion(java.util.List,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    public void afterDeletion(List files, AbstractUIProgressHandler handler) throws Exception
    {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.uninstaller.UninstallerListener#isFileListener()
     */

    public boolean isFileListener()
    {
        return false;
    }

}
