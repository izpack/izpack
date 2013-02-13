/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.field.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.izforge.izpack.panels.userinput.field.ValidationStatus;


/**
 * Rule field layout.
 *
 * @author Elmar Grom
 * @author Tim Anderson
 */
public class FieldLayout
{
    /**
     * The field specs.
     */
    private final List<FieldSpec> specs = new ArrayList<FieldSpec>();

    /**
     * The layout items. This is a mixture of field separator strings and FieldSpec instances.
     */
    private final List<Object> items = new ArrayList<Object>();

    /**
     * Constructs a  {@code FieldLayout}.
     *
     * @param layout the layout specifier
     */
    public FieldLayout(String layout)
    {
        StringTokenizer tokenizer = new StringTokenizer(layout);

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            FieldSpec spec = FieldSpec.parse(token);
            int size = items.size();
            if (spec != null)
            {
                // if the previous item is also a field, insert a space as separator
                if (size > 0 && items.get(size - 1) instanceof FieldSpec)
                {
                    items.add(" ");
                }
                specs.add(spec);
                items.add(spec);
            }
            else
            {
                // the token is not a valid field specification, so assume it is a separator
                if (!items.isEmpty() && items.get(size - 1) instanceof String)
                {
                    // if the previous item is also a separator simply concatenate the token with a space
                    // inserted in between, don't add it as new separator.
                    String last = (String) items.get(size - 1);
                    items.set(size - 1, last + " " + token);
                }
                else
                {
                    items.add(token);
                }
            }
        }
    }

    /**
     * Returns the field specifications.
     *
     * @return the field specifications
     */
    public List<FieldSpec> getFieldSpecs()
    {
        return specs;
    }

    /**
     * Returns the layout items.
     *
     * @return a mixture of field separator strings and {@link FieldSpec} instances.
     */
    public List<Object> getLayout()
    {
        return items;
    }

    /**
     * Validates a value against the layout, splitting it into the appropriate fields.
     *
     * @param value the value
     * @return the validation status
     */
    public ValidationStatus validate(String value)
    {
        String[] result = new String[specs.size()];
        int specIndex = 0;
        int pos = 0;
        for (int i = 0; i < items.size(); ++i)
        {
            Object item = items.get(i);
            if (item instanceof String)
            {
                String separator = (String) item;
                if (value.startsWith(separator))
                {
                    value = value.substring(separator.length());
                    pos += separator.length();
                }
                else
                {
                    String actual = value.length() >= separator.length()
                            ? value.substring(0, separator.length()) : value;
                    return ValidationStatus.failed("Expected '" + separator + "' at character " + pos + " but got "
                                                           + "'" + actual + "'");
                }
            }
            else
            {
                FieldSpec spec = (FieldSpec) item;
                String terminator = getTerminator(items, i);
                boolean last = (i == items.size() - 1);
                if (!last && terminator == null)
                {
                    // cannot work out where the field ends
                    return ValidationStatus.failed("Cannot determine field delimiter at character " + pos);
                }
                int end = (last) ? -1 : value.indexOf(terminator);
                String field;
                if (end != -1)
                {
                    field = value.substring(0, end);
                    value = value.substring(end);
                }
                else
                {
                    field = value;
                    value = "";
                }
                if (end == -1 && !last)
                {
                    return ValidationStatus.failed("Unterminated field at character " + pos + ": " + field);
                }
                else if (!spec.isUnlimitedLength() && field.length() > spec.getLength())
                {
                    return ValidationStatus.failed("Field too long at character " + pos + ". Expected length "
                                                           + spec.getLength() + " but got length " + field.length()
                                                           + ": " + field);
                }
                if (!spec.validate(field))
                {
                    return ValidationStatus.failed("Invalid field at character " + pos + ": " + field);
                }
                result[specIndex++] = field;
                pos += field.length();
            }
        }
        return ValidationStatus.success(result);
    }

    /**
     * Returns the field terminator for the specified field.
     *
     * @param items the layout items
     * @param index the field index
     * @return the field terminator, or {@code null} if there is no terminator
     */
    private String getTerminator(List<Object> items, int index)
    {
        if (index < items.size() - 1 && items.get(index + 1) instanceof String)
        {
            return (String) items.get(index + 1);
        }
        return null;
    }
}
