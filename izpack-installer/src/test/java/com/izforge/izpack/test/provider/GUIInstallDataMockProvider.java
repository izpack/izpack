/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.test.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.GUIPrefs;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.util.Platforms;

import java.io.IOException;

/**
 * Mock provider for guiInstallData
 */
@Singleton
public class GUIInstallDataMockProvider extends AbstractInstallDataMockProvider<GUIInstallData>
{
    private final Variables variables;
    private final Locales locales;

    @Inject
    public GUIInstallDataMockProvider(Variables variables, Locales locales) {
        this.variables = variables;
        this.locales = locales;
    }

    /**
     * Provides an {@link GUIInstallData}.
     *
     * @return an {@link GUIInstallData}
     */
    public GUIInstallData loadInstallData()
    {
        try {
            GUIInstallData result = createInstallData(variables);
            populate(result, locales);
            result.configureGuiButtons();
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create GUIInstallData", ex);
        }
    }

    /**
     * Creates a new {@link AutomatedInstallData}.
     *
     * @param variables the variables
     * @return a new {@link AutomatedInstallData}
     */
    @Override
    protected GUIInstallData createInstallData(Variables variables)
    {
        GUIInstallData result = new GUIInstallData(variables, Platforms.MAC_OSX);
        GUIPrefs guiPrefs = new GUIPrefs();
        guiPrefs.height = 600;
        guiPrefs.width = 480;
        result.guiPrefs = guiPrefs;
        return result;
    }
}
