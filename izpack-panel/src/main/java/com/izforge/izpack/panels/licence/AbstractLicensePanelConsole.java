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

package com.izforge.izpack.panels.licence;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractTextPanelConsole;
import com.izforge.izpack.util.Console;

/**
 * Abstract panel for displaying license text to the console.
 *
 * @author Tim Anderson
 */
public abstract class AbstractLicensePanelConsole extends AbstractTextPanelConsole
{
    private static final Logger logger = Logger.getLogger(AbstractLicensePanelConsole.class.getName());

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * Constructs a <tt>AbstractLicensePanelConsole</tt>.
     *
     * @param resources the resources
     */
    public AbstractLicensePanelConsole(Resources resources)
    {
        this.resources = resources;
    }

    /**
     * Returns the named text resource
     *
     * @param resourceName the resource name
     * @return the text resource, or <tt>null</tt> if it cannot be found
     */
    protected String getText(String resourceName)
    {
        String result = resources.getString(resourceName, null, null);
        if (result == null)
        {
            logger.log(Level.WARNING, "No  licence text for resource: " + resourceName);
        }
        return result;
    }

    /**
     * Prompts to end the license panel.
     * <p/>
     * This displays a prompt to accept, reject, or redisplay. On redisplay, it invokes
     * {@link #runConsole(InstallData, Console)}.
     *
     * @param installData the installation date
     * @param console     the console to use
     * @return <tt>true</tt> to accept, <tt>false</tt> to reject. If redisplaying the panel, the result of
     *         {@link #runConsole(InstallData, Console)} is returned
     */
    @Override
    protected boolean promptEndPanel(InstallData installData, Console console)
    {
        boolean result;
        int value = console.prompt("Press 1 to accept, 2 to reject, 3 to redisplay", 1, 3, 2);
        result = value == 1 || value != 2 && runConsole(installData, console);
        return result;
    }

}
