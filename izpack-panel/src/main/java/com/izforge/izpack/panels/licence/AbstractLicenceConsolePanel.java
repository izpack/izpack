/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels.licence;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.exception.UserInterruptException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractTextConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.util.Console;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Abstract panel for displaying license text to the console.
 *
 * @author Tim Anderson
 */
public abstract class AbstractLicenceConsolePanel extends AbstractTextConsolePanel
{

    private static final String DEFAULT_SUFFIX = ".licence";

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractLicenceConsolePanel.class.getName());

    /**
     * Constructs a {@code AbstractLicenseConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     * @param resources the resources
     */
    public AbstractLicenceConsolePanel(PanelView<ConsolePanel> panel, Resources resources)
    {
        super(panel);
        this.resources = resources;
    }

    /**
     * Loads the license document URL.
     *
     * @return The license text URL.
     */
    protected URL loadLicence()
    {
        URL url = null;
        final Class<? extends AbstractLicenceConsolePanel> thisClazz = getClass();
        final Class<IzPanel> izClass = PanelHelper.getIzPanel(thisClazz.getName());
        if (izClass != null)
        {
            final String resNamePrefix = izClass.getSimpleName();
            String resNameStr = resNamePrefix + DEFAULT_SUFFIX;
            Panel panel = getPanel();
            if (panel != null)
            {
                String panelId = panel.getPanelId();
                if (panelId != null)
                {
                    String panelSpecificResName = resNamePrefix + '.' + panelId;
                    try
                    {
                        url = resources.getURL(panelSpecificResName);
                    }
                    catch (ResourceNotFoundException e)
                    {
                        try
                        {
                            url = resources.getURL(resNameStr);
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                }
            }
            if (url == null)
            {
                String panelId = panel != null ? panel.getPanelId() : null;
                LOGGER.warning("Cannot open any of the possible license document resources ("
                        + (panelId != null ? resNamePrefix + '.' + panelId + ", " : "")
                        + resNamePrefix + DEFAULT_SUFFIX
                        + ") for panel type '" + resNamePrefix + "");
            }
        }
        else
        {
            LOGGER.warning("No IzPanel implementation found for " + thisClazz.getSimpleName());
        }

        return url;
    }

    protected String loadLicenceAsString()
    {
        return loadLicenceAsString("UTF-8");
    }

    protected String loadLicenceAsString(final String encoding)
    {
        URL url = null;
        String result = null;
        try
        {
            url = loadLicence();

            InputStream in = url.openStream();
            try
            {
                result = IOUtils.toString(in, Charsets.toCharset(encoding));
            }
            finally
            {
                IOUtils.closeQuietly(in);
            }
        }
        catch (IOException e)
        {
            LOGGER.warning("Cannot convert license document from resource " + url.getFile() + " to text: " + e.getMessage());
        }
        return result;
    }

    /**
     * Prompts to end the license panel.
     * <p/>
     * This displays a prompt to accept, reject, or redisplay. On redisplay, it
     * invokes {@link #run(InstallData, Console)}.
     *
     * @param installData the installation date
     * @param console the console to use
     * @return {@code true} to accept, {@code false} to reject. If the panel is
     * displayed again, the result of {@link #run(InstallData, Console)} is
     * returned
     */
    @Override
    protected boolean promptEndPanel(InstallData installData, Console console)
    {
        boolean result;
        final Messages messages = installData.getMessages();
        String prompt = messages.get("ConsoleInstaller.acceptRejectRedisplay");
        console.println();
        int value = console.prompt(prompt, 1, 3, 2);
        switch (value)
        {
            case 1:
                result = true;
                break;

            case 2:
                throw new UserInterruptException(messages.get("ConsoleInstaller.aborted.LicenseRejected"));

            default:
                result = run(installData, console);
                break;
        }
        return result;
    }

}
