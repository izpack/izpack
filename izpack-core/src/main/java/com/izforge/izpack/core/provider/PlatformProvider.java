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

package com.izforge.izpack.core.provider;


import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;


/**
 * Injection provider for the current {@link Platform}.
 *
 * @author Tim Anderson
 */
@ApplicationScoped
public class PlatformProvider
{
    /**
     * Provides the current platform.
     *
     * @param platforms the platform factory
     * @return the current platform
     */
    @Produces
    @ApplicationScoped
    public Platform provide(Platforms platforms)
    {
        return platforms.getCurrentPlatform();
    }

    @Produces
    @ApplicationScoped
    public Platforms platforms()
    {
        return new Platforms();
    }
}
