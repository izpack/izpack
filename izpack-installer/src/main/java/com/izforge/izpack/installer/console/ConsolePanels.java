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

import com.izforge.izpack.api.data.Variables;
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
     * The action to run when switching panels.
     */
    private ConsoleAction action;


    /**
     * Constructs a {@code ConsolePanels}.
     *
     * @param panels    the panels
     * @param variables the variables. These are refreshed prior to each panel switch
     */
    public ConsolePanels(List<ConsolePanelView> panels, Variables variables)
    {
        super(panels, variables);
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
            do
            {
                result = action.run(newPanel);
                if (!result)
                {
                    break;
                }
            } while (!newPanel.isValid());
        }

        return result;
    }

}
