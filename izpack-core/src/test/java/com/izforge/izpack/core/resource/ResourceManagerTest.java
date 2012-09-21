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

package com.izforge.izpack.core.resource;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Tests the {@link ResourceManager}.
 *
 * @author Tim Anderson
 */
public class ResourceManagerTest
{

    /**
     * Verifies images can be retrieved for each of the supported countries and languages.
     */
    @Test
    public void testImages()
    {
        ResourceManager resources = new ResourceManager()
        {
            @Override
            public Object getObject(String name)
            {
                if (name.equals("langpacks.info"))
                {
                    return DefaultLocalesTest.ISO_CODES;

                }
                return super.getObject(name);
            }
        };
        resources.setResourceBasePath("/com/izforge/izpack/bin/langpacks/flags/");
        for (String code : DefaultLocalesTest.ISO_CODES)
        {
            assertNotNull(resources.getImageIcon(code + ".gif"));
        }
    }

}
