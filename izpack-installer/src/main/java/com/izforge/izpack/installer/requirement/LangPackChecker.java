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

package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.core.resource.ResourceManager;

/**
 * Verifies that a language pack is available.
 *
 * @author Tim Anderson
 */
public class LangPackChecker implements RequirementChecker
{
    /**
     * The resources.
     */
    private final ResourceManager resources;

    /**
     * Constructs a <tt>LangPackChecker</tt>.
     *
     * @param resources the resources
     */
    public LangPackChecker(ResourceManager resources)
    {
        this.resources = resources;
    }

    /**
     * Determines if installation requirements are met.
     *
     * @return <tt>true</tt> if requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        return !resources.getAvailableLangPacks().isEmpty();
    }
}
