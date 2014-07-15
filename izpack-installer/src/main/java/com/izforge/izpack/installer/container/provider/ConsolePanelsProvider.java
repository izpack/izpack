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

package com.izforge.izpack.installer.container.provider;

import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.installer.console.ConsolePanelView;
import com.izforge.izpack.installer.console.ConsolePanels;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Provider of {@link ConsolePanels}.
 *
 * @author Tim Anderson
 */
public class ConsolePanelsProvider extends PanelsProvider
{

    /**
     * Creates the panels.
     * <p/>
     * This invokes any pre-construction actions associated with them.
     *
     * @param factory     the factory
     * @param installData the installation data
     * @param console     the console
     * @param matcher     the platform-model matcher
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    public ConsolePanels provide(ObjectFactory factory, AutomatedInstallData installData, Console console,
                                 PlatformModelMatcher matcher)
    {
        List<ConsolePanelView> panels = new ArrayList<ConsolePanelView>();

        for (Panel panel : prepare(installData, matcher))
        {
            ConsolePanelView panelView = new ConsolePanelView(panel, factory, installData, console);
            panels.add(panelView);
        }
        return new ConsolePanels(panels, installData);
    }

}
