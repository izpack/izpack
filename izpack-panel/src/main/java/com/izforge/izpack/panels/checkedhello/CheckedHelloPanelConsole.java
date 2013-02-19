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

package com.izforge.izpack.panels.checkedhello;


import static com.izforge.izpack.api.handler.Prompt.Option.YES;
import static com.izforge.izpack.api.handler.Prompt.Options.YES_NO;
import static com.izforge.izpack.api.handler.Prompt.Type.ERROR;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.panels.hello.HelloPanelConsoleHelper;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of the {@link CheckedHelloPanel}.
 *
 * @author Tim Anderson
 */
public class CheckedHelloPanelConsole extends HelloPanelConsoleHelper
{

    /**
     * The registry helper.
     */
    private final RegistryHelper registryHelper;

    /**
     * Determines if the application is already installed.
     */
    private boolean registered;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(CheckedHelloPanelConsole.class.getName());

    /**
     * Constructs a <tt>CheckedHelloPanelConsole</tt>.
     *
     * @param handler     the registry handler
     * @param installData the installation data
     * @param prompt      the prompt
     * @throws NativeLibException for any native library error
     */
    public CheckedHelloPanelConsole(RegistryDefaultHandler handler, InstallData installData, Prompt prompt)
            throws NativeLibException
    {
        registryHelper = new RegistryHelper(handler, installData);
        this.prompt = prompt;
        registered = registryHelper.isRegistered();
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean runConsole(InstallData installData, Console console)
    {
        boolean result = true;
        if (registered)
        {
            result = multipleInstall(installData);
            if (result)
            {
                try
                {
                    registryHelper.updateUninstallName();
                    registered = false;
                }
                catch (NativeLibException exception)
                {
                    result = false;
                    log.log(Level.SEVERE, exception.getMessage(), exception);
                }
            }
        }
        installData.setVariable("UNINSTALL_NAME", registryHelper.getUninstallName());
        if (result)
        {
            display(installData, console);
            result = promptEndPanel(installData, console);
        }
        return result;
    }

    /**
     * Invoked if the product is already installed.
     * <p/>
     * This prompts the user whether to install twice or quit.
     *
     * @param installData the installation data
     * @return <tt>true</tt> to install again, <tt>false</tt> to quit
     */
    protected boolean multipleInstall(InstallData installData)
    {
        boolean result;
        try
        {
            String path = registryHelper.getInstallationPath();
            if (path == null)
            {
                path = "<not found>";
            }
            Messages messages = installData.getMessages();
            String message = messages.get("CheckedHelloPanel.productAlreadyExist0") + path + "\n"
                    + messages.get("CheckedHelloPanel.productAlreadyExist1");
            result = prompt.confirm(ERROR, message, YES_NO) == YES;
        }
        catch (NativeLibException exception)
        {
            result = false;
        }
        return result;
    }

}
