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

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.Console;

/**
 * Abstract class implementing basic functions needed by all panel console helpers.
 *
 * @author Mounir El Hajj
 * @deprecated use {@link AbstractConsolePanel}
 */
@Deprecated
abstract public class PanelConsoleHelper extends AbstractConsolePanel implements PanelConsole
{

    /**
     * Generates a properties file for each input field or variable.
     * <p/>
     * This implementation is a no-op.
     *
     * @param installData the installation data
     * @param printWriter the properties file to write to
     * @return {@code true}
     */
    @Override
    public boolean generateProperties(InstallData installData, PrintWriter printWriter)
    {
        return runGeneratePropertiesFile(installData, printWriter);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return {@code true} if the installation is successful, otherwise {@code false}
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
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        return runConsole(installData, console);
    }

    /**
     * Runs the panel in interactive console mode.
     *
     * @param installData the installation data
     * @deprecated use {@link #run(InstallData, Console)}
     */
    @Override
    @Deprecated
    public boolean runConsole(InstallData installData)
    {
        return runConsole(installData, new Console());
    }

    /**
     * Prompts to end the console panel.
     *
     * @return <tt>1</tt> to continue, <tt>2</tt> to quit, <tt>3</tt> to redisplay
     * @see {@link #promptEndPanel(InstallData, Console)}
     * @deprecated
     */
    @Deprecated
    public int askEndOfConsolePanel()
    {
        return new Console().prompt("press 1 to continue, 2 to quit, 3 to redisplay", 1, 3, 2);
    }

}
