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
package com.izforge.izpack.test.provider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import org.mockito.Mockito;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.installer.container.provider.AbstractInstallDataProvider;
import com.izforge.izpack.util.Platforms;

/**
 * Test provider for {@link InstallData}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInstallDataMockProvider extends AbstractInstallDataProvider
{

    /**
     * Populates an {@link AutomatedInstallData}.
     *
     * @param installData the installation data to populate
     * @throws IOException if the default messages cannot be found
     */
    protected void populate(AutomatedInstallData installData) throws IOException
    {
        Info info = new Info();
        installData.setInfo(info);

        URL resource = getClass().getResource("/com/izforge/izpack/bin/langpacks/installer/eng.xml");
        Locales locales = Mockito.mock(Locales.class);
        when(locales.getMessages(anyString())).thenThrow(new ResourceNotFoundException("Resource not found"));
        Messages messages = new LocaleDatabase(resource.openStream(), locales);
        installData.setMessages(messages);
        installData.setLocale(Locale.getDefault(), "eng");
        setStandardVariables(installData, null);
    }

    /**
     * Creates a new {@link com.izforge.izpack.api.data.AutomatedInstallData}.
     *
     * @param variables the variables
     * @return a new {@link com.izforge.izpack.api.data.AutomatedInstallData}
     */
    protected AutomatedInstallData createInstallData(Variables variables)
    {
        return new AutomatedInstallData(variables, Platforms.MAC_OSX);
    }
}
