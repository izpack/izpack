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
package com.izforge.izpack.installer.container.impl;

import com.izforge.izpack.core.handler.AutomatedPrompt;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.ConsolePanelAutomationHelper;
import com.izforge.izpack.installer.container.provider.AutomatedInstallDataProvider;
import com.izforge.izpack.installer.container.provider.AutomatedPanelsProvider;
import com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpackerAutomationHelper;
import com.izforge.izpack.installer.unpacker.ConsolePackResources;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Installer container for automated installation mode.
 *
 * @author Tim Anderson
 */
public class AutomatedInstallerContainer extends InstallerContainer
{

    /**
     * Constructs a <tt>AutomatedInstallerContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public AutomatedInstallerContainer()
    {
        initialise();
    }

    /**
     * Constructs a <tt>AutomatedInstallerContainer</tt>.
     * <p/>
     * This constructor is provided for testing purposes.
     *
     * @param container the underlying container
     * @throws ContainerException if initialisation fails
     */
    protected AutomatedInstallerContainer(MutablePicoContainer container)
    {
        initialise(container);
    }

    /**
     * Registers components with the container.
     *
     * @param container the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer container)
    {
        super.registerComponents(container);

        container
                .addAdapter(new ProviderAdapter(new AutomatedInstallDataProvider()))
                .addAdapter(new ProviderAdapter(new AutomatedPanelsProvider()));

        container
                .addComponent(AutomatedPrompt.class)
                .addComponent(AutomatedInstaller.class)
                .addComponent(ConsolePanelAutomationHelper.class)
                .addComponent(ConsolePackResources.class)
                .addComponent(MultiVolumeUnpackerAutomationHelper.class);
    }
}
