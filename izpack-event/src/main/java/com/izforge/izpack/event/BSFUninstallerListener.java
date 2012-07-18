/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
 * Copyright 2004 Thomas Guenter
 * Copyright 2009 Matthew Inger
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

import com.izforge.izpack.api.event.AbstractUninstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;


public class BSFUninstallerListener extends AbstractUninstallerListener
{

    /**
     * The resources.
     */
    private final Resources resources;

    private List<BSFAction> actions;

    public BSFUninstallerListener(Resources resources)
    {
        this.resources = resources;
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        InputStream in;
        try
        {
            in = resources.getInputStream("bsfActions");
            ObjectInputStream objIn = new ObjectInputStream(in);
            actions = (List<BSFAction>) objIn.readObject();
            if (actions == null)
            {
                actions = Collections.emptyList();
            }
            else
            {
                for (BSFAction action : actions)
                {
                    action.init();
                }
            }
            objIn.close();
            in.close();
        }
        catch (ResourceNotFoundException ignore)
        {
            // do nothing
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

    /**
     * Invoked before files are deleted.
     *
     * @param files all files which should be deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(List<File> files)
    {
        for (BSFAction action : actions)
        {
            action.executeUninstall(BSFAction.BEFOREDELETION, files);
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
        for (BSFAction action : actions)
        {
            action.executeUninstall(BSFAction.AFTERDELETION, files, listener);
            action.destroy();
        }
    }

    /**
     * Invoked after a file is deleted.
     *
     * @param file the file which was deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(File file)
    {
        for (BSFAction action : actions)
        {
            action.executeUninstall(BSFAction.BEFOREDELETE, file);
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
        for (BSFAction action : actions)
        {
            action.executeUninstall(BSFAction.AFTERDELETE, file);
        }
    }

    public boolean isFileListener()
    {
        return true;
    }

}
