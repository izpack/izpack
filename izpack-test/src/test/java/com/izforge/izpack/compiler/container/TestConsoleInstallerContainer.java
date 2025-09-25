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
import com.izforge.izpack.core.container.CdiInitializationContext;
import com.izforge.izpack.installer.console.ConsoleInstaller;
import com.izforge.izpack.installer.console.TestConsoleInstaller;
import com.izforge.izpack.installer.container.impl.ConsoleInstallerContainer;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.Housekeeper;


/**
 * Test installer container for console based installers.
 * <p/>
 * This returns:
 * <ul>
 * <li>a {@link TestConsoleInstaller} instead of a {@link ConsoleInstaller}</li>
 * <li>a {@link TestConsole} instead of a {@link Console}</li>
 * <li>a {@link TestHousekeeper} instead of a {@link Housekeeper}</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class TestConsoleInstallerContainer extends ConsoleInstallerContainer
{

    public TestConsoleInstallerContainer()
    {
    }

    public TestConsoleInstallerContainer(CdiInitializationContext container)
    {
        super(container);
    }

    /**
     * Registers components with the container.
     */
    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
        context.removeComponent(ConsoleInstaller.class);
        context.addComponent(TestConsoleInstaller.class);
        context.removeComponent(ConsolePrefs.class);
        context.removeComponent(Console.class);
        context.addComponent(TestConsole.class);
        context.removeComponent(Housekeeper.class);
        context.addComponent(TestHousekeeper.class);
    }
}
