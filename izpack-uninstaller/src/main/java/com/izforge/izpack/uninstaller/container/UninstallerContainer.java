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

import java.util.Properties;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.DefaultResources;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.uninstaller.Destroyer;
import com.izforge.izpack.uninstaller.resource.Executables;
import com.izforge.izpack.uninstaller.resource.InstallLog;
import com.izforge.izpack.uninstaller.resource.RootScripts;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;


/**
 * Uninstaller container.
 *
 * @author Tim Anderson
 */
public abstract class UninstallerContainer extends AbstractContainer
{

    /**
     * Invoked by {@link #initialise} to fill the container.
     * <p/>
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        addComponent(Resources.class, DefaultResources.class);
        addComponent(Housekeeper.class);
        addComponent(Librarian.class);
        addComponent(TargetFactory.class);
        addComponent(DefaultObjectFactory.class);
        addComponent(DefaultTargetPlatformFactory.class);
        addComponent(RegistryDefaultHandler.class);
        addComponent(Container.class, this);
        addComponent(Properties.class);
        addComponent(ResourceManager.class);
        addComponent(DefaultLocales.class);
        addComponent(Platforms.class);
        addComponent(ObjectFactory.class, DefaultObjectFactory.class);
        addComponent(InstallLog.class);
        addComponent(Executables.class);
        addComponent(RootScripts.class);
        addComponent(PlatformModelMatcher.class);
        addComponent(Destroyer.class);

        container.addAdapter(new ProviderAdapter(new PlatformProvider()));
        container.addAdapter(new ProviderAdapter(new UninstallerListenersProvider()));
        container.addAdapter(new ProviderAdapter(new MessagesProvider()));
    }
}
