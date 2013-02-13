/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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
package com.izforge.izpack.panels.userinput.console.text;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.TestFieldConfig;
import com.izforge.izpack.panels.userinput.field.text.TextField;


/**
 * Tests the {@link ConsoleTextField}.
 *
 * @author Tim Anderson
 */
public class ConsoleTextFieldTest extends AbstractConsoleFieldTest
{

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        String defaultValue = "default value";
        ConsoleTextField field = createField(defaultValue);
        checkValid(field, "\n");
        assertEquals(defaultValue, installData.getVariable("text"));
        verifyNoMoreInteractions(prompt);
    }

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSetValue()
    {
        ConsoleTextField field = createField(null);
        String expected = "new value";
        checkValid(field, expected);
        assertEquals(expected, installData.getVariable("text"));
        verifyNoMoreInteractions(prompt);
    }

    /**
     * Creates a new {@link ConsoleTextField}.
     *
     * @param defaultValue the default value. May be {@code null}
     * @return a new field
     */
    private ConsoleTextField createField(String defaultValue)
    {
        TestFieldConfig config = new TestFieldConfig("text");
        config.setLabel("Enter value:");
        config.setDefaultValue(defaultValue);
        return new ConsoleTextField(new TextField(config, installData), console, prompt);
    }
}
