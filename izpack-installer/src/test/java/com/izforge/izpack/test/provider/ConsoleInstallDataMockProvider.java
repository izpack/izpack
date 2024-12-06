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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.izforge.izpack.api.data.ConsolePrefs;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.util.Platforms;

import java.io.IOException;

/**
 * Test provider for {@link ConsoleInstallData}.
 *
 * @author Tim Anderson
 */
@Singleton
public class ConsoleInstallDataMockProvider extends AbstractInstallDataMockProvider<ConsoleInstallData>
{
    private final Variables variables;
    private final Locales locales;

    @Inject
    public ConsoleInstallDataMockProvider(Variables variables, Locales locales)
    {
        this.variables = variables;
        this.locales = locales;
    }

    /**
     * Provides an {@link ConsoleInstallData}.
     *
     * @return an {@link ConsoleInstallData}
     */
    @Override
    public ConsoleInstallData loadInstallData()
    {
        try {
            ConsoleInstallData result = createInstallData(variables);
            populate(result, locales);
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create ConsoleInstallData", exception);
        }
    }

    /**
     * Creates a new {@link ConsoleInstallData}.
     *
     * @param variables the variables
     * @return a new {@link ConsoleInstallData}
     */
    @Override
    protected ConsoleInstallData createInstallData(Variables variables)
    {
        ConsoleInstallData result = new ConsoleInstallData(variables, Platforms.MAC_OSX);

        ConsolePrefs consolePrefs = new ConsolePrefs();
        consolePrefs.enableConsoleReader = false;
        result.consolePrefs = consolePrefs;

        return result;
    }

}
