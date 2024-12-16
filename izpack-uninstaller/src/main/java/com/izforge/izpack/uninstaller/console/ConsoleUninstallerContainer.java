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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.uninstaller.container.UninstallerContainer;
import com.izforge.izpack.util.Console;


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
    protected void fillContainer()
    {
        super.fillContainer();

        ConsolePrefs consolePrefs = new ConsolePrefs();
        consolePrefs.enableConsoleReader = false;
        addComponent(ConsolePrefs.class, consolePrefs);
        addComponent(Variables.class, DefaultVariables.class);
        addComponent(InstallData.class, AutomatedInstallData.class);
        addComponent(Console.class);
        addComponent(Prompt.class, ConsolePrompt.class);
        addComponent(ConsoleDestroyerListener.class);
        addComponent(ConsoleUninstaller.class);
    }
}
