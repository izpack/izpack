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

package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.core.handler.ConsolePrompt;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.util.Console;


/**
 * Implementation of {@link PanelView} for {@link PanelConsole}s.
 *
 * @author Tim Anderson
 */
public class ConsolePanelView extends PanelView<PanelConsole>
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * The prompt.
     */
    private final ConsolePrompt prompt;


    /**
     * Constructs a {@code ConsolePanelView}.
     *
     * @param panel       the panel
     * @param factory     the factory for creating the view
     * @param installData the installation data
     */
    public ConsolePanelView(Panel panel, ObjectFactory factory, InstallData installData, Console console)
    {
        super(panel, PanelConsole.class, factory, installData);
        this.console = console;
        this.prompt = new ConsolePrompt(console, installData.getMessages());
    }

    /**
     * Returns the PanelConsole class corresponding to the panel's class name
     *
     * @return the corresponding {@link PanelConsole} implementation class, or {@code null} if none is found
     */
    public Class<PanelConsole> getViewClass()
    {
        Panel panel = getPanel();
        return PanelHelper.getConsolePanel(panel.getClassName());
    }

    /**
     * Creates a new view.
     *
     * @param panel     the panel to create the view for
     * @param viewClass the view base class
     * @return the new view
     */
    @Override
    protected PanelConsole createView(Panel panel, Class<PanelConsole> viewClass)
    {
        Class<PanelConsole> impl = getViewClass();
        if (impl == null)
        {
            throw new IzPackException("Console implementation not found for panel: " + panel.getClassName());
        }
        return getFactory().create(impl, panel);
    }

    /**
     * Returns a handler to prompt the user.
     *
     * @return the handler
     */
    @Override
    protected AbstractUIHandler getHandler()
    {
        return new PromptUIHandler(prompt)
        {
            @Override
            public void emitNotification(String message)
            {
                console.println(message);
            }
        };
    }

}
