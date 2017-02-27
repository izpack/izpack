/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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
package com.izforge.izpack.api.data;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.resource.Locales;

public class LocaleDatabaseTest
{

    private LocaleDatabase db;

    @Before
    public void setUp() throws Exception
    {
        db = new LocaleDatabase(LocaleDatabaseTest.class.getResourceAsStream("testing-langpack.xml"),
                Mockito.mock(Locales.class));

    }

    @Test
    public void testGet()
    {
        assertEquals("String Text", db.get("string"));
        assertEquals("none", db.get("none"));
    }

    @Test
    public void testGetWithArgs()
    {
        assertEquals("Argument1: one, Argument2: two", db.get("string.with.arguments", "one", "two"));
        assertEquals("Argument1: 'one', Argument2: 'two'", db.get("string.with.quoted.arguments", "one", "two"));
    }

    @Test
    public void testNpeHandling()
    {
        assertEquals("Argument1: one, Argument2: N/A", db.getString(
                "string.with.arguments", new String[]
                {
                    "one", null
                }));
    }

    @Test
    public void testQuotedPlaceholder()
    {
        assertEquals("Argument1: 'one', Argument2: 'N/A'", db.getString(
                "string.with.quoted.arguments", new String[]
                {
                    "one", null
                }));
    }

}
