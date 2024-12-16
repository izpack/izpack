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

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.core.rules.ConditionContainerProvider;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.container.provider.RulesProvider;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import static org.mockito.Mockito.when;

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
    protected void fillContainer()
    {
        addComponent(Properties.class);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(Resources.class, ResourceManager.class);
        addComponent(UninstallData.class);
        addComponent(Container.class, this);
        addProvider(ConditionContainer.class, ConditionContainerProvider.class);
        addComponent(UninstallDataWriter.class, Mockito.mock(UninstallDataWriter.class));
        addComponent(AutomatedInstaller.class);

        addComponent(ObjectFactory.class, new DefaultObjectFactory(this));
        addComponent(IUnpacker.class, Mockito.mock(IUnpacker.class));
        addComponent(TestHousekeeper.class, Mockito.mock(TestHousekeeper.class));
        addComponent(Platforms.class);
        addComponent(PlatformModelMatcher.class);

        Locales locales = Mockito.mock(Locales.class);
        when(locales.getISOCode()).thenReturn("eng");
        when(locales.getLocale()).thenReturn(Locale.ENGLISH);

        URL resource = getClass().getResource("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        when(locales.getMessages(ArgumentMatchers.anyString())).thenThrow(new ResourceNotFoundException("Resource not found"));
        try
        {
            Messages messages = new LocaleDatabase(resource.openStream(), locales);
            when(locales.getMessages()).thenReturn(messages);
        }
        catch (IOException exception)
        {
            throw new ContainerException(exception);
        }
        addComponent(Locales.class, locales);

        addProvider(RulesEngine.class, RulesProvider.class);
        addProvider(Platform.class, PlatformProvider.class);
    }
}
