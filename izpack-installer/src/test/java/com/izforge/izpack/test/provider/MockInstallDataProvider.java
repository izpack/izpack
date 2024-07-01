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

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.core.factory.InstallDataFactory;
import com.izforge.izpack.core.rules.RulesEngineImpl;

/**
 * Test provider for {@link InstallData}.
 *
 * @author Tim Anderson
 */
final class MockInstallDataProvider
{

    /**
     * Populates an {@link InstallData}.
     *
     * @param installData the installation data to populate
     * @param locales     the locales
     */
    static void populate(InstallData installData, Locales locales)
    {
        installData.setInfo(new Info());
        installData.setRules(new RulesEngineImpl(installData,null));
        InstallDataFactory.loadDefaultLocale(installData, locales);
        InstallDataFactory.setStandardVariables(installData, null);
    }
}
