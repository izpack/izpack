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

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanelView;
import com.izforge.izpack.installer.gui.IzPanels;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Provider of {@link IzPanels}.
 *
 * @author Tim Anderson
 */
public class IzPanelsProvider extends PanelsProvider
{
    /**
     * Creates the panels.
     * <p/>
     * This invokes any pre-construction actions associated with them.
     *
     * @param factory     the factory for constructing panels
     * @param container   the installer container
     * @param installData the installation data
     * @param matcher     the platform-model matcher
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    public IzPanels provide(ObjectFactory factory, InstallerContainer container, GUIInstallData installData,
                            PlatformModelMatcher matcher)
    {
        List<IzPanelView> panels = new ArrayList<IzPanelView>();

        for (Panel panel : prepare(installData, matcher))
        {
            IzPanelView panelView = new IzPanelView(panel, factory, installData);
            panels.add(panelView);
        }
        return new IzPanels(panels, container, installData);
    }

}
