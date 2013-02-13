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
package com.izforge.izpack.panels.userinput.console.password;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.core.container.DefaultContainer;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.password.PasswordField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.password.TestPasswordGroupFieldConfig;
import com.izforge.izpack.panels.userinput.validator.PasswordEqualityValidator;

/**
 * Tests the {@link ConsolePasswordGroupField}.
 *
 * @author Tim Anderson
 */
public class ConsolePasswordGroupFieldTest extends AbstractConsoleFieldTest
{

    /**
     * The factory for creating validators.
     */
    private ObjectFactory factory;

    /**
     * Default constructor.
     */
    public ConsolePasswordGroupFieldTest()
    {
        factory = new DefaultObjectFactory(new DefaultContainer());
    }

    /**
     * Verifies that a password can be entered.
     */
    @Test
    public void testSetValue()
    {
        ConsolePasswordGroupField field = createField();
        checkValid(field, "ab1234", "ab1234");

        assertEquals("ab1234", installData.getVariable("password"));
    }

    /**
     * Verifies that when an {@link PasswordEqualityValidator} is registered, the passwords must be the same.
     */
    @Test
    public void testSetMismatchValue()
    {
        String message = "Mismatch passwords";
        FieldValidator validator = new FieldValidator(PasswordEqualityValidator.class, message, factory);
        ConsolePasswordGroupField field = createField(validator);

        checkInvalid(field, "ab1234", "ab1235");
        Mockito.verify(prompt).error("Error", message);
        assertNull(installData.getVariable("password"));

        checkValid(field, "ab1234", "ab1234");
        assertEquals("ab1234", installData.getVariable("password"));
        Mockito.verifyNoMoreInteractions(prompt);
    }

    /**
     * Creates a {@link ConsolePasswordGroupField} that updates the 'password' variable.
     *
     * @return a new field
     */
    private ConsolePasswordGroupField createField()
    {
        return createField(null);
    }

    /**
     * Creates a {@link ConsolePasswordGroupField} that updates the 'password' variable.
     *
     * @param validator the field validator. May be {@code null}
     * @return a new field
     */
    private ConsolePasswordGroupField createField(FieldValidator validator)
    {
        List<PasswordField> fields = new ArrayList<PasswordField>();
        fields.add(new PasswordField("Enter password: ", 8, null));
        fields.add(new PasswordField("Re-enter password: ", 8, null));

        TestPasswordGroupFieldConfig config = new TestPasswordGroupFieldConfig("password", fields);
        if (validator != null)
        {
            config.addValidator(validator);
        }
        PasswordGroupField model = new PasswordGroupField(config, installData);
        return new ConsolePasswordGroupField(model, console, prompt);
    }
}
