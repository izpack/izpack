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
package com.izforge.izpack.panels.test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import com.izforge.izpack.core.container.CdiInitializationContext;
import com.izforge.izpack.core.provider.LocalesProvider;
import com.izforge.izpack.util.Housekeeper;
import org.mockito.Mockito;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Platforms;

/**
 * Container for testing panels.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTestPanelContainer extends AbstractContainer
{

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
        context.addComponent(UninstallDataWriter.class, Mockito.mock(UninstallDataWriter.class));
//        context.addComponent(ObjectFactory.class, new DefaultObjectFactory(this)); //TODO:WELD: remove container reference if possible
        context.addComponent(IUnpacker.class, Mockito.mock(IUnpacker.class));
        context.addComponent(Housekeeper.class, Mockito.mock(TestHousekeeper.class));
//        context.addComponent(Container.class, this);

        Locales locales = Mockito.mock(Locales.class);
        when(locales.getISOCode()).thenReturn("eng");
        when(locales.getLocale()).thenReturn(Locale.ENGLISH);

        URL resource = getClass().getResource("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        when(locales.getMessages(anyString())).thenThrow(new ResourceNotFoundException("Resource not found"));
        try
        {
            Messages messages = new LocaleDatabase(resource.openStream(), locales);
            when(locales.getMessages()).thenReturn(messages);
        }
        catch (IOException exception)
        {
            throw new ContainerException(exception);
        }

        context.removeComponent(LocalesProvider.class);
        context.addComponent(Locales.class, locales);

//        addComponent(Properties.class);
//        addComponent(DefaultVariables.class);
//        addComponent(ResourceManager.class);
//        addComponent(UninstallData.class);
//        addComponent(ConditionContainer.class);
//        addComponent(AutomatedInstaller.class);
//        addComponent(PlatformModelMatcher.class);
//        addComponent(RulesProvider.class);
//        addComponent(PlatformProvider.class);
    }
}
