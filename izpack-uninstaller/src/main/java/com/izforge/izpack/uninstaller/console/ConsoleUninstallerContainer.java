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

package com.izforge.izpack.uninstaller.console;

import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.CdiInitializationContext;
import com.izforge.izpack.installer.container.provider.ConsoleInstallDataFactory;
import com.izforge.izpack.uninstaller.container.UninstallerContainer;
import com.izforge.izpack.util.Platform;

import java.util.function.Predicate;


/**
 * Console uninstaller container.
 *
 * @author Tim Anderson
 */
public class ConsoleUninstallerContainer extends UninstallerContainer
{

    /**
     * Constructs a <tt>ConsoleUninstallerContainer</tt>.
     */
    public ConsoleUninstallerContainer()
    {
        initialise();
    }

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
        ConsolePrefs consolePrefs = new ConsolePrefs();
        consolePrefs.enableConsoleReader = false;
        context.addComponent(ConsolePrefs.class, consolePrefs);

//        addComponent(DefaultVariables.class);
//        addComponent(AutomatedInstallData.class);
//        addComponent(Console.class);
//        addComponent(ConsolePrompt.class);
//        addComponent(ConsoleDestroyerListener.class);
//        addComponent(ConsoleUninstaller.class);
    }

    @Override
    public InstallData create(Resources resources, Variables variables, Platform platform, Locales locales,
                              Predicate<Pack> availablePackPredicate)
    {
        return ConsoleInstallDataFactory.create(resources, variables, platform, locales, availablePackPredicate);
    }
}
