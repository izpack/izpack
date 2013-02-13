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

package com.izforge.izpack.panels.userinput.field.rule;

import static com.izforge.izpack.panels.userinput.field.rule.FieldSpec.Type;
import static com.izforge.izpack.panels.userinput.field.rule.FieldSpec.Type.ALPHA;
import static com.izforge.izpack.panels.userinput.field.rule.FieldSpec.Type.ALPHANUMERIC;
import static com.izforge.izpack.panels.userinput.field.rule.FieldSpec.Type.NUMERIC;
import static com.izforge.izpack.panels.userinput.field.rule.FieldSpec.Type.OPEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.izforge.izpack.panels.userinput.field.ValidationStatus;


/**
 * Tests the {@link FieldLayout} class.
 *
 * @author Tim Anderson
 */
public class FieldLayoutTest
{

    /**
     * Tests a phone number layout.
     */
    @Test
    public void testPhoneNumber()
    {
        FieldLayout layout = new FieldLayout("( N:3:3 ) N:3:3 - N:4:4 x N:5:5");
        checkLayout(layout, "(", field(NUMERIC, 3, 3), ")", field(NUMERIC, 3, 3), "-", field(NUMERIC, 4, 4), "x",
                    field(NUMERIC, 5, 5));

        checkValid("(03)484-7777x139", layout, "03", "484", "7777", "139");
    }

    /**
     * Tests an IP address layout.
     */
    @Test
    public void testIPAddress()
    {
        FieldLayout layout = new FieldLayout("N:3:3 . N:3:3 . N:3:3 . N:3:3");
        checkLayout(layout, field(NUMERIC, 3, 3), ".", field(NUMERIC, 3, 3), ".", field(NUMERIC, 3, 3), ".",
                    field(NUMERIC, 3, 3));

        checkValid("192.168.0.1", layout, "192", "168", "0", "1");
    }

    /**
     * Tests an email address layout.
     */
    @Test
    public void testEmailAddress()
    {
        FieldLayout layout = new FieldLayout("AN:15:U @ AN:10:40 . A:3:3");
        checkLayout(layout, field(ALPHANUMERIC, 15, -1), "@", field(ALPHANUMERIC, 10, 40), ".", field(ALPHA, 3, 3));

        checkValid("foo@bar.com", layout, "foo", "bar", "com");
    }

    /**
     * Tests a serial number layout.
     */
    @Test
    public void testSerialNumber()
    {
        FieldLayout layout = new FieldLayout("H:4:4 - N:6:6 - N:3:3");
        checkLayout(layout, field(Type.HEX, 4, 4), "-", field(NUMERIC, 6, 6), "-", field(NUMERIC, 3, 3));

        checkValid("ABCD-123-456", layout, "ABCD", "123", "456");
    }

    /**
     * Tests layouts with unlimited length fields.
     */
    @Test
    public void testUnlimitedLength()
    {
        FieldLayout layout1 = new FieldLayout("O:5:U");
        FieldLayout layout2 = new FieldLayout("O:5:U O:4");
        FieldLayout layout3 = new FieldLayout("O:5:U O:4:U N:3");
        FieldLayout layout4 = new FieldLayout("N:1:");

        checkLayout(layout1, field(OPEN, 5, -1));
        checkLayout(layout2, field(OPEN, 5, -1), " ", field(OPEN, 4, -1));
        checkLayout(layout3, field(OPEN, 5, -1), " ", field(OPEN, 4, -1), " ", field(NUMERIC, 3, -1));
        checkLayout(layout4, field(NUMERIC, 1, -1));
    }

    /**
     * Tests invalid field layouts.
     */
    @Test
    public void testInvalidLayout()
    {
        // malformed field specs are treated as separators
        FieldLayout layout1 = new FieldLayout("O");
        checkLayout(layout1, "O");

        FieldLayout layout2 = new FieldLayout("Q:5:5");
        checkLayout(layout2, "Q:5:5");
    }

    /**
     * Tests invalid values.
     */
    @Test
    public void testInvalidValues()
    {
        String phoneLayout = "( N:3:3 ) N:3:3 - N:4:4 x N:5:5";
        checkInvalid(phoneLayout, "", "Expected '(' at character 0 but got ''");
        checkInvalid(phoneLayout, "(0A)", "Invalid field at character 1: 0A");
        checkInvalid(phoneLayout, "(03)9484", "Unterminated field at character 4: 9484");
        checkInvalid(phoneLayout, "(03)9484-", "Field too long at character 4. Expected length 3 but got length 4: " +
                "9484");
        checkInvalid(phoneLayout, "(03)9484", "Unterminated field at character 4: 9484");
    }

    /**
     * Helper to create a new {@code FieldSpec}.
     *
     * @param type    the type of field
     * @param columns the no, of columns to display
     * @param length  the field length, or {@code -1} to indicate unlimited length
     * @return a new {@code FieldSpec}.
     */
    private FieldSpec field(Type type, int columns, int length)
    {
        return new FieldSpec(type, columns, length);
    }

    /**
     * Validates a value.
     *
     * @param value    the value to validate
     * @param layout   the field layout
     * @param expected the expected field values
     */
    private void checkValid(String value, FieldLayout layout, String... expected)
    {
        ValidationStatus status = layout.validate(value);
        assertTrue(status.isValid());
        assertEquals(expected.length, status.getValues().length);
        for (int i = 0; i < expected.length; ++i)
        {
            assertEquals(expected[i], status.getValues()[i]);
        }
    }

    /**
     * Ensures validation fails for an incorrect value.
     *
     * @param value    the value to validate
     * @param layout   the field layout
     * @param expected the expected error message
     */
    private void checkInvalid(String layout, String value, String expected)
    {
        FieldLayout l = new FieldLayout(layout);
        ValidationStatus status = l.validate(value);
        assertFalse(status.isValid());
        assertEquals(expected, status.getMessage());
    }

    /**
     * Verifies that a field layout matches that expected.
     *
     * @param layout the layout
     * @param items  the expected items
     */
    private void checkLayout(FieldLayout layout, Object... items)
    {
        int fields = 0;
        int separators = 0;
        for (Object item : items)
        {
            if (item instanceof String)
            {
                ++separators;
            }
            else
            {
                ++fields;
            }
        }
        assertEquals(fields, layout.getFieldSpecs().size());
        assertEquals(fields + separators, layout.getLayout().size());

        for (int i = 0; i < items.length; ++i)
        {
            if (items[i] instanceof String)
            {
                checkSeparator(layout, i, (String) items[i]);
            }
            else
            {
                checkFieldSpec(layout, i, (FieldSpec) items[i]);
            }
        }
    }

    /**
     * Verifies an item in the layout is a separator.
     *
     * @param layout    the layout
     * @param index     the item index
     * @param separator the expected separator
     */
    private void checkSeparator(FieldLayout layout, int index, String separator)
    {
        List<Object> items = layout.getLayout();
        assertTrue(items.get(index) instanceof String);
        assertEquals(separator, items.get(index));
    }

    /**
     * Verifies an item in the layout is a field.
     *
     * @param layout   the layout
     * @param index    the item index
     * @param expected the expected field
     */
    private void checkFieldSpec(FieldLayout layout, int index, FieldSpec expected)
    {
        List<Object> items = layout.getLayout();
        assertTrue(items.get(index) instanceof FieldSpec);
        FieldSpec spec = (FieldSpec) items.get(index);
        assertEquals(expected.getType(), spec.getType());
        assertEquals(expected.getLength(), spec.getLength());
        assertEquals(expected.getColumns(), spec.getColumns());
    }
}
