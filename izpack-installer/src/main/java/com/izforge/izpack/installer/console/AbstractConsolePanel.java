/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012-2013 Tim Anderson
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
 * Abstract implementation of the {@link ConsolePanel} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractConsolePanel implements ConsolePanel, PanelConsole
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
        return true;
    }

    /**
     * Generates a properties file for each input field or variable.
     *
     * @param installData the installation data
     * @param printWriter the properties file to write to
     * @return {@code true} if the generation is successful, otherwise {@code false}
     * @deprecated use {@link #generateProperties(InstallData, PrintWriter)}
     */
    @Override
    @Deprecated
    public boolean runGeneratePropertiesFile(InstallData installData, PrintWriter printWriter)
    {
        return generateProperties(installData, printWriter);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return {@code true} if the installation is successful, otherwise {@code false}
     * @deprecated use {@link #run(InstallData, Properties)}
     */
    @Override
    public boolean runConsoleFromProperties(InstallData installData, Properties properties)
    {
        return run(installData, properties);
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
        return run(installData, new Console());
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     * @deprecated use {@link #run(InstallData, Console)}
     */
    @Override
    public boolean runConsole(InstallData installData, Console console)
    {
        return run(installData, console);
    }

    /**
     * Prompts to end the console panel.
     * <p/>
     * This displays a prompt to continue, quit, or redisplay. On redisplay, it invokes
     * {@link #run(InstallData, Console)}.
     *
     * @param installData the installation date
     * @param console     the console to use
     * @return {@code true} to continue, {@code false} to quit. If redisplaying the panel, the result of
     *         {@link #run(InstallData, Console)} is returned
     */
    protected boolean promptEndPanel(InstallData installData, Console console)
    {
        boolean result;
        String prompt = installData.getMessages().get("ConsoleInstaller.continueQuitRedisplay");
        console.println();
        int value = console.prompt(prompt, 1, 3, 2);
        result = value == 1 || value != 2 && run(installData, console);
        return result;
    }
}
