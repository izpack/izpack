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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.izforge.izpack.core.resource.ResourceManager;

/**
 * Tests the {@link LangPackChecker}.
 *
 * @author Tim Anderson
 */
public class LangPackCheckerTest
{

    /**
     * Tests the {@link LangPackChecker}.
     */
    @Test
    public void testLangPackChecker()
    {
        final List<String> langPacks = new ArrayList<String>();
        ResourceManager resources = new ResourceManager()
        {
            @Override
            public List<String> getAvailableLangPacks()
            {
                return langPacks;
            }
        };

        LangPackChecker checker = new LangPackChecker(resources);

        // no lang packs - should evaluate false
        assertFalse(checker.check());

        // add a lang pack - should evaluate true
        langPacks.add("eng");
        assertTrue(checker.check());
    }
}
