/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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
package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.api.config.Options;

import java.util.Properties;

/**
 * Abstract class implementing basic functions needed by all panel console
 * helpers.
 *
 * @author Mounir El Hajj
 * @deprecated use {@link AbstractConsolePanel}
 */
@Deprecated
abstract public class PanelConsoleHelper extends AbstractConsolePanel implements PanelConsole
{

    /**
     * Constructs a {@code PanelConsoleHelper}.
     */
    public PanelConsoleHelper()
    {
        this(null);
    }

    /**
     * Constructs a {@code PanelConsoleHelper}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public PanelConsoleHelper(PanelView<ConsolePanel> panel)
    {
        super(panel);
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        return runGeneratePropertiesFile(installData, options);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties the properties
     * @return {@code true} if the installation is successful, otherwise
     * {@code false}
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        return runConsoleFromProperties(installData, properties);
    }

    /**
     * Runs the panel in an interactive console.
     *
     * @param installData the installation data
     * @param console the console
     * @return {@code true} if the panel ran successfully, otherwise
     * {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        return runConsole(installData, console);
    }

}
