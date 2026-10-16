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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.izforge.izpack.api.resource.Locales;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocaleDatabaseTest
{

    private LocaleDatabase db;

    @BeforeEach
    public void setUp() throws Exception
    {
        db = new LocaleDatabase(LocaleDatabaseTest.class.getResourceAsStream("testing-langpack.xml"),
                                mock(Locales.class));

    }

    @Test
    public void testGet()
    {
        assertThat(db.get("string")).isEqualTo("String Text");
        assertThat(db.get("none")).isEqualTo("none");
    }

    @Test
    public void testGetWithArgs()
    {
        assertThat(db.get("string.with.arguments", "one", "two")).isEqualTo("Argument1: one, Argument2: two");
        assertThat(db.get("string.with.quoted.arguments", "one", "two")).isEqualTo("Argument1: 'one', Argument2: 'two'");
    }

    @Test
    public void testNpeHandling()
    {
        assertThat(db.getString(
                "string.with.arguments", new String[]{"one", null})).isEqualTo("Argument1: one, Argument2: N/A");
    }

    @Test
    public void testQuotedPlaceholder()
    {
        assertThat(db.getString(
                "string.with.quoted.arguments", new String[]{"one", null})).isEqualTo("Argument1: 'one', Argument2: 'N/A'");
    }

}
