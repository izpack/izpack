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
package com.izforge.izpack.compiler.container;

import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.installer.console.TestConsolePrefsProvider;
import com.izforge.izpack.installer.container.impl.AutomatedInstallerContainer;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Housekeeper;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Test installer container for automated installation mode.
 */
public class TestAutomatedInstallerContainer extends AutomatedInstallerContainer
{

    /**
     * Default constructor.
     */
    public TestAutomatedInstallerContainer()
    {
        super();
    }

    /**
     * Constructs a <tt>TestAutomatedInstallerContainer</tt>.
     *
     * @param container the container to use
     */
    public TestAutomatedInstallerContainer(MutablePicoContainer container)
    {
        super(container);
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
        container.removeComponent(ConsolePrefs.class);
        container.addAdapter(new ProviderAdapter(new TestConsolePrefsProvider()));
        container.removeComponent(Housekeeper.class);
        container.addComponent(TestHousekeeper.class);
    }
}
