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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import com.izforge.izpack.api.event.AbstractUninstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.exception.WrappedNativeLibException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;

/**
 * Uninstaller custom action for handling registry entries. The needed configuration data are
 * written at installation time from the corresponding installer custom action. An external
 * definiton is not needed.
 *
 * @author Klaus Bartz
 */
public class RegistryUninstallerListener extends AbstractUninstallerListener
{

    /**
     * The registry handler.
     */
    private final RegistryHandler handler;

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The localised messages.
     */
    private final Messages messages;

    /**
     * The uninstall actions.
     */
    private List actions;

    /**
     * Constructs a <tt>RegistryUninstallerListener</tt>.
     *
     * @param handler   the handler
     * @param resources the resources
     * @param messages  the messages
     */
    public RegistryUninstallerListener(RegistryDefaultHandler handler, Resources resources, Messages messages)
    {
        this.handler = handler.getInstance();
        this.resources = resources;
        this.messages = messages;
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        // Load the defined actions.
        try
        {
            InputStream in = resources.getInputStream("registryEntries");
            ObjectInputStream objIn = new ObjectInputStream(in);
            actions = (List) objIn.readObject();
            objIn.close();
            in.close();
        }
        catch (ResourceNotFoundException ignore)
        {
            // do nothing
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
        if (actions == null || actions.isEmpty())
        {
            return;
        }

        if (handler != null)
        {
            try
            {
                handler.activateLogging();
                handler.setLoggingInfo(actions);
                handler.rewind();
            }
            catch (NativeLibException e)
            {
                throw new WrappedNativeLibException(e, messages);
            }
        }
    }

}
