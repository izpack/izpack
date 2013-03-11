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

import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.injectors.ProviderAdapter;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.base.InstallDataConfiguratorWithRules;
import com.izforge.izpack.installer.container.provider.IconsProvider;
import com.izforge.izpack.test.provider.GUIInstallDataMockProvider;


/**
 * Container for GUI panel testing.
 *
 * @author Tim Anderson
 */
public class TestGUIPanelContainer extends AbstractTestPanelContainer
{

    /**
     * Constructs a {@code TestGUIPanelContainer}.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestGUIPanelContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer(MutablePicoContainer container)
    {
        super.fillContainer(container);
        addComponent(InstallDataConfiguratorWithRules.class);
        addComponent(Log.class, Mockito.mock(Log.class));
        addComponent(GUIPrompt.class);

        container.addAdapter(new ProviderAdapter(new GUIInstallDataMockProvider()));
        container.addAdapter(new ProviderAdapter(new IconsProvider()));
    }
}
