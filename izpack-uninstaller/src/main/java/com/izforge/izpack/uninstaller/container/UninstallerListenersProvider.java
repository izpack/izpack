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


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.uninstaller.event.UninstallerListeners;

import java.util.List;


/**
 * A provider of {@link UninstallerListeners}.
 *
 * @author Tim Anderson
 */
public class UninstallerListenersProvider implements Provider<UninstallerListeners>
{

    private final Resources resources;
    private final ObjectFactory factory;
    private final Prompt prompt;

    @Inject
    public UninstallerListenersProvider(Resources resources, ObjectFactory factory, Prompt prompt) {
        this.resources = resources;
        this.factory = factory;
        this.prompt = prompt;
    }

    /**
     * Provides an {@link UninstallerListeners} by reading the <em>uninstallerListeners</em> resource.
     *
     * @return the listeners
     */
    @SuppressWarnings("unchecked")
    @Override
    public UninstallerListeners get()
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
