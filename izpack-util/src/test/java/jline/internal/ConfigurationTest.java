/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright (c) 2002-2012, the original authors of JLine
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

package jline.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for {@link Configuration}.
 */
public class ConfigurationTest
{
    @Test
    public void initFromSystemProperty() {
        System.setProperty(Configuration.JLINE_CONFIGURATION, getClass().getResource("jlinerc1").toExternalForm());
        Configuration.reset();
        String value = Configuration.getString("a");
        assertEquals("b", value);
    }

    @Test
    public void getBooleanFromSystemProperty() {
        System.setProperty("test", "false");
        boolean value = Configuration.getBoolean("test", true);
        assertEquals(false, value);
    }

    @Test
    public void getIntegerFromSystemProperty() {
        System.setProperty("test", "1234");
        int value = Configuration.getInteger("test", 5678);
        assertEquals(1234, value);
    }

    @Test
    public void getIntegerUsingDefault() {
        System.getProperties().remove("test");
        int value = Configuration.getInteger("test", 1234);
        assertEquals(1234, value);
    }

    @Test
    public void resetReconfigures() {
        System.setProperty(Configuration.JLINE_CONFIGURATION, getClass().getResource("jlinerc1").toExternalForm());
        Configuration.reset();
        String value1 = Configuration.getString("a");
        assertEquals("b", value1);

        System.setProperty(Configuration.JLINE_CONFIGURATION, getClass().getResource("jlinerc2").toExternalForm());
        Configuration.reset();
        String value2 = Configuration.getString("c");
        assertEquals("d", value2);
    }

    @Test
    public void parseCtypeNull() {
        assertNull(Configuration.extractEncodingFromCtype(null));
    }

    @Test
    public void parseCtypeBlank() {
        assertNull(Configuration.extractEncodingFromCtype(""));
    }

    @Test
    public void parseCtypeNoEncoding() {
        assertNull(Configuration.extractEncodingFromCtype("fr_FR"));
    }

    @Test
    public void parseCtypeNoEncodingWithModifier() {
        assertNull(Configuration.extractEncodingFromCtype("fr_FR@euro"));
    }

    @Test
    public void parseCtypeWithEncoding() {
        assertEquals("UTF-8", Configuration.extractEncodingFromCtype("fr_FR.UTF-8"));
    }

    @Test
    public void parseCtypeWithEncodingAndModifier() {
        assertEquals("UTF-8", Configuration.extractEncodingFromCtype("fr_FR.UTF-8@euro"));
    }


}