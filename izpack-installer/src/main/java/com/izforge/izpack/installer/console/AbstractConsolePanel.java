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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;


/**
 * Abstract implementation of the {@link ConsolePanel} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractConsolePanel implements ConsolePanel
{

    /**
     * The the parent panel/view. May be {@code null}
     */
    private final PanelView<ConsolePanel> panel;


    /**
     * Constructs an {@code AbstractConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public AbstractConsolePanel(PanelView<ConsolePanel> panel)
    {
        this.panel = panel;
    }

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
     * Prompts to end the console panel.
     * <p/>
     * If the panel is valid, this displays a prompt to continue, quit, or redisplay. On redisplay,
     * it invokes {@link #run(InstallData, Console)}. <br/>
     * If the panel is invalid, this invokes {@link #promptRerunPanel(InstallData, Console)}.<br/>
     *
     * @param installData the installation date
     * @param console     the console to use
     * @return {@code true} to continue, {@code false} to quit. If redisplaying the panel, the result of
     *         {@link #run(InstallData, Console)} is returned
     */
    protected boolean promptEndPanel(InstallData installData, Console console)
    {
        boolean result;

        if (panel == null || panel.isValid())
        {
            String prompt = installData.getMessages().get("ConsoleInstaller.continueQuitRedisplay");
            console.println();
            int value = console.prompt(prompt, 1, 3, 2);
            result = value == 1 || value != 2 && run(installData, console);
        }
        else
        {
            result = promptRerunPanel(installData, console);
        }
        return result;
    }

    /**
     * Prompts to re-run the panel or quit.
     * <p/>
     * This displays a prompt to redisplay the panel or quit. On redisplay, it invokes
     * {@link #run(InstallData, Console)}.
     *
     * @param installData the installation date
     * @param console     the console to use
     * @return {@code true} to re-display, {@code false} to quit. If redisplaying the panel, the result of
     *         {@link #run(InstallData, Console)} is returned
     */
    protected boolean promptRerunPanel(InstallData installData, Console console)
    {
        boolean result;
        String prompt = installData.getMessages().get("ConsoleInstaller.redisplayQuit");
        console.println();
        int value = console.prompt(prompt, 1, 2, 2);
        result = value != 2 && run(installData, console);
        return result;
    }

    /**
     * Returns the panel.
     *
     * @return the panel, or {@code null} if no panel/view was supplied at construction
     */
    protected Panel getPanel()
    {
        return (panel != null) ? panel.getPanel() : null;
    }

    public void createInstallationRecord(IXMLElement rootElement)
    {
        // Default method, override to record panel contents
    };
}
