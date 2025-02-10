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

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.CdiInitializationContext;
import com.izforge.izpack.util.Platforms;


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
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
//        addComponent(DefaultResources.class);
//        addComponent(Housekeeper.class);
//        addComponent(Librarian.class);
//        addComponent(TargetFactory.class);
//        addComponent(DefaultObjectFactory.class);
//        addComponent(DefaultTargetPlatformFactory.class);
//        addComponent(RegistryDefaultHandler.class);
//        addComponent(Properties.class);
//        addComponent(ResourceManager.class);
//        addComponent(DefaultLocales.class);
//        addComponent(DefaultObjectFactory.class);
//        addComponent(InstallLog.class);
//        addComponent(Executables.class);
//        addComponent(RootScripts.class);
//        addComponent(PlatformModelMatcher.class);
//        addComponent(Destroyer.class);
//        addComponent(PlatformProvider.class);
//        addComponent(UninstallerListenersProvider.class);
//        addComponent(MessagesProvider.class);
    }
}
