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

package com.izforge.izpack.uninstaller.container;


import java.io.IOException;
import java.util.List;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.uninstaller.event.UninstallerListeners;


/**
 * A provider of {@link UninstallerListeners}.
 *
 * @author Tim Anderson
 */
public class UninstallerListenersProvider implements Provider
{

    /**
     * Provides an {@link UninstallerListeners} by reading the <em>uninstallerListeners</em> resource.
     *
     * @param resources used to locate the <em>uninstallerListeners</em> resource
     * @param factory   the factory to create the {@link UninstallerListener}s
     * @param prompt    the prompt
     * @return the listeners
     * @throws IOException               for any I/O error
     * @throws ClassNotFoundException    if a class of a serialized object cannot be found
     * @throws ResourceNotFoundException if <em>uninstallerListeners</em> cannot be found
     */
    @SuppressWarnings("unchecked")
    public UninstallerListeners provide(Resources resources, ObjectFactory factory, Prompt prompt)
            throws IOException, ClassNotFoundException
    {
        UninstallerListeners listeners = new UninstallerListeners(prompt);
        List<String> classNames = (List<String>) resources.getObject("uninstallerListeners");

        for (String className : classNames)
        {
            UninstallerListener listener = factory.create(className, UninstallerListener.class);
            listeners.add(listener);
        }
        listeners.initialise();
        return listeners;
    }

}
