/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Marcus Stursberg
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

package com.izforge.izpack.core.provider;

import java.io.IOException;
import java.util.function.Predicate;

import com.izforge.izpack.api.data.InstallDataHandler;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.factory.InstallDataFactory;
import com.izforge.izpack.core.rules.ConditionContainer;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.PlatformModelMatcher;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Abstract base class for providers of {@link InstallData}.
 */
@ApplicationScoped
public class InstallDataProvider
{
    @Produces
    public InstallData provide(
            InstallDataHandler dataHandler, Resources resources, Locales locales, ConditionContainer conditionContainer,
            Variables variables, Housekeeper housekeeper, PlatformModelMatcher matcher)
        throws IOException, ClassNotFoundException
    {
        Platform platform = matcher.getCurrentPlatform();
        Predicate<Pack> availablePackPredicate = pack -> matcher.matchesCurrentPlatform(pack.getOsConstraints());
        InstallData installData = dataHandler.create(resources, variables, platform, locales, availablePackPredicate);

        InstallDataFactory.initializeRules(installData, variables, conditionContainer, resources);
        InstallDataFactory.initializeTempDirectores(installData, housekeeper);
        return installData;
    }
}
