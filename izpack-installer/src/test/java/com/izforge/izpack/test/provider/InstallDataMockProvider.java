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

import java.io.IOException;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;

/**
 * Test provider for {@link InstallData}.
 *
 * @author Tim Anderson
 */
public class InstallDataMockProvider extends AbstractInstallDataMockProvider
{

    /**
     * Provides an {@link AutomatedInstallData}.
     *
     * @param variables the variables
     * @param locales   the locales
     * @return an {@link AutomatedInstallData}
     * @throws IOException if the default messages cannot be found
     */
    public AutomatedInstallData provide(Variables variables, Locales locales) throws IOException
    {
        AutomatedInstallData result = createInstallData(variables);
        populate(result, locales);
        return result;
    }

}
