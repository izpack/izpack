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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.installer.panel.Panels;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Base class for {@link Panels} providers.
 *
 * @author Tim Anderson
 */
public abstract class PanelsProvider implements Provider
{

    /**
     * Prepares panels for the current platform.
     * <br/>
     *
     * @param installData the installation data
     * @param matcher     The platform-model matcher
     * @return the panels for the current platform
     * @throws IzPackException if a panel doesn't have unique identifier
     */
    protected List<Panel> prepare(InstallData installData, PlatformModelMatcher matcher)
    {
        List<Panel> result = new ArrayList<Panel>();
        Set<String> ids = new HashSet<String>();
        for (Panel panel : installData.getPanelsOrder())
        {
            if (matcher.matchesCurrentPlatform(panel.getOsConstraints()))
            {
                String panelId = panel.getPanelId();
                String key = (panelId != null) ? panelId : panel.getClassName();
                if (!ids.add(key))
                {
                    throw new IzPackException("Duplicate panel: " + key);
                }


                result.add(panel);
            }
        }
        return result;
    }
}
