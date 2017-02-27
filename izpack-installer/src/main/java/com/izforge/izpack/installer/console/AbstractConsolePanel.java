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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.UserInterruptException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.api.config.Options;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        return true;
    }

    /**
     * Prompts to end the console panel.
     * <p/>
     * If the panel is valid, this displays a prompt to continue, quit, or
     * redisplay. On redisplay, it invokes {@link #run(InstallData, Console)}.
     * <br/>
     * If the panel is invalid, this invokes
     * {@link #promptRerunPanel(InstallData, Console)}.<br/>
     *
     * @param installData the installation date
     * @param console the console to use
     * @return {@code true} to continue, {@code false} to quit. If redisplaying
     * the panel, the result of {@link #run(InstallData, Console)} is returned
     */
    protected boolean promptEndPanel(InstallData installData, Console console)
    {
        boolean result;
        final Messages messages = installData.getMessages();
        String prompt = messages.get("ConsoleInstaller.continueQuitRedisplay");
        console.println();
        int value = console.prompt(prompt, 1, 3, 2);
        switch (value)
        {
            case 1:
                result = true;
                break;

            case 2:
                throw new UserInterruptException(messages.get("ConsoleInstaller.aborted.PressedQuit"));

            default:
                result = run(installData, console);
                break;
        }
        return result;
    }

    /**
     * Prompts to re-run the panel or quit.
     * <p/>
     * This displays a prompt to redisplay the panel or quit. On redisplay, it
     * invokes {@link #run(InstallData, Console)}.
     *
     * @param installData the installation date
     * @param console the console to use
     * @return {@code true} to re-display, {@code false} to quit. If
     * redisplaying the panel, the result of {@link #run(InstallData, Console)}
     * is returned
     */
    protected boolean promptRerunPanel(InstallData installData, Console console)
    {
        boolean result;
        final Messages messages = installData.getMessages();
        String prompt = messages.get("ConsoleInstaller.redisplayQuit");
        console.println();
        int value = console.prompt(prompt, 1, 2, 2);
        switch (value)
        {
            case 2:
                throw new UserInterruptException(messages.get("ConsoleInstaller.aborted.PressedQuit"));

            default:
                result = run(installData, console);
                break;
        }
        return result;
    }

    /**
     * Returns the panel.
     *
     * @return the panel, or {@code null} if no panel/view was supplied at
     * construction
     */
    protected Panel getPanel()
    {
        return (panel != null) ? panel.getPanel() : null;
    }

    @Override
    public void createInstallationRecord(IXMLElement rootElement)
    {
        // Default method, override to record panel contents
    }

    @Override
    public boolean run(InstallData installData, Console console)
    {
        printHeadLine(installData, console);
        return true;
    }

    protected void printHeadLine(InstallData installData, Console console)
    {
        final String headline = getI18nStringForClass("headline", installData);
        if (headline != null)
        {
            console.println();
            console.printFilledLine('\u2500');
            console.println(headline);
            console.printFilledLine('\u2500');
            console.println();
        }
    }

    @Override
    public boolean handlePanelValidationResult(boolean valid)
    {
        // Do nothing by default - to be overwritten in console panel implementations if necessary
        return valid;
    }

    /**
     * Search for a proper translation key belonging to the panel
     * implementation.
     *
     * @param subkey the subkey for the string which should be returned
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey, InstallData installData)
    {
        return getI18nStringForClass(subkey, null, installData);
    }

    /**
     * Search for a proper translation key belonging to the panel
     * implementation.
     *
     * @param subkey the subkey for the string which should be returned
     * @param alternateClass the short name of the class which should be used if
     * no string is present with the runtime class name
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey, String alternateClass, InstallData installData)
    {
        String retval = null;

        List<String> prefixes = new ArrayList<String>();
        Panel panel = getPanel();
        String panelId = null;
        if (panel != null)
        {
            panelId = getPanel().getPanelId();
        }

        Class<?> clazz = PanelHelper.getIzPanel(this.getClass().getName());

        String fullClassname = alternateClass == null ? clazz.getName() : alternateClass;
        String simpleClassname = alternateClass == null ? clazz.getSimpleName() : alternateClass;

        do
        {
            if (panelId != null)
            {
                prefixes.add(fullClassname + "." + panelId);
                prefixes.add(simpleClassname + "." + panelId);
            }
            prefixes.add(fullClassname);
            prefixes.add(simpleClassname);

            clazz = clazz.getSuperclass();
            if (clazz != null)
            {
                fullClassname = clazz.getName();
                simpleClassname = clazz.getSimpleName();
            }
        }
        while ((alternateClass == null && !(clazz == null || clazz.equals(AbstractConsolePanel.class))));
        if (panelId != null)
        {
            prefixes.add(2, panelId);
        }

        for (String prefix : prefixes)
        {
            String searchkey = prefix + "." + subkey;
            if (installData.getMessages().getMessages().containsKey(searchkey))
            {
                retval = installData.getMessages().get(searchkey);
            }
            if (retval != null)
            {
                break;
            }
        }

        if (retval != null && retval.indexOf('$') > -1)
        {
            retval = installData.getVariables().replace(retval);
        }
        return (retval);
    }

}
