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

import java.util.List;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.panel.AbstractPanels;
import com.izforge.izpack.installer.panel.Panels;


/**
 * Implementation of {@link Panels} for {@link ConsolePanelView}.
 *
 * @author Tim Anderson
 */
public class ConsolePanels extends AbstractPanels<ConsolePanelView, ConsolePanel>
{

    /**
     * The container to add {@link ConsolePanel}s to.
     */
    private final Container container;

    /**
     * The action to run when switching panels.
     */
    private ConsoleAction action;

    /**
     * Constructs a {@code ConsolePanels}.
     *
     * @param panels    the panels
     * @param container the container to get instances from
     * @param installData install data
     */
    public ConsolePanels(List<ConsolePanelView> panels, Container container, InstallData installData)
    {
        super(panels, installData);
        this.container = container;
    }

    /**
     * Initialises the {@link ConsolePanelView} instances.
     */
    public void initialise()
    {
        for (ConsolePanelView panel : getPanelViews())
        {
            // Prefetch extra panel conditions for UserInputPanel (os, createForPack)
            ConsolePanel view = panel.getView();
            String panelId = panel.getPanelId();
            if (panelId == null)
            {
                panelId = view.getClass().getName();
            }
            container.addPanel(panelId, view);
        }
    }

    /**
     * Sets the action to invoke when switching panels.
     *
     * @param action the action
     */
    public void setAction(ConsoleAction action)
    {
        this.action = action;
    }

    /**
     * Switches panels.
     *
     * @param newPanel the panel to switch to
     * @param oldPanel the panel to switch from, or {@code null} if there was no prior panel
     * @return {@code true} if the switch was successful
     */
    @Override
    protected boolean switchPanel(ConsolePanelView newPanel, ConsolePanelView oldPanel)
    {
        boolean result = false;
        newPanel.executePreActivationActions();
        if (action != null)
        {
            result = action.run(newPanel);
        }

        return result;
    }

}
