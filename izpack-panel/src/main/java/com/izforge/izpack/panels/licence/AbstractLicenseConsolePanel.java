/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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
import com.izforge.izpack.installer.console.AbstractTextConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

/**
 * Abstract panel for displaying license text to the console.
 *
 * @author Tim Anderson
 */
public abstract class AbstractLicenseConsolePanel extends AbstractTextConsolePanel
{

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(AbstractLicenseConsolePanel.class.getName());

    /**
     * Constructs a {@code AbstractLicenseConsolePanel}.
     *
     * @param panel     the parent panel/view. May be {@code null}
     * @param resources the resources
     */
    public AbstractLicenseConsolePanel(PanelView<Console> panel, Resources resources)
    {
        super(panel);
        this.resources = resources;
    }

    /**
     * Returns the named text resource
     *
     * @param resourceName the resource name
     * @return the text resource, or {@code null} if it cannot be found
     */
    protected String getText(String resourceName)
    {
        String result = resources.getString(resourceName, null, null);
        if (result == null)
        {
            logger.log(Level.WARNING, "No licence text for resource: " + resourceName);
        }
        return result;
    }

    /**
     * Prompts to end the license panel.
     * <p/>
     * This displays a prompt to accept, reject, or redisplay. On redisplay, it invokes
     * {@link #run(InstallData, Console)}.
     *
     * @param installData the installation date
     * @param console     the console to use
     * @return {@code true} to accept, {@code false} to reject. If redisplaying the panel, the result of
     *         {@link #run(InstallData, Console)} is returned
     */
    @Override
    protected boolean promptEndPanel(InstallData installData, Console console)
    {
        boolean result;
        String prompt = installData.getMessages().get("ConsoleInstaller.acceptRejectRedisplay");
        console.println();
        int value = console.prompt(prompt, 1, 3, 2);
        result = value == 1 || value != 2 && run(installData, console);
        return result;
    }

}
