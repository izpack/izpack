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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.exception.IzPackException;


/**
 * Rule field specification.
 *
 * @author Elmar Grom
 * @author Tim Anderson
 */
public class FieldSpec
{

    /**
     * Field type.
     */
    public enum Type
    {
        NUMERIC,       // field only accepts numeric input
        ALPHA,         // field only accepts alphabetic input
        ALPHANUMERIC,  // field accepts alphanumeric input
        HEX,           // field only accepts hexadecimal input
        OPEN           // field accepts any character
    }

    /**
     * The field type.
     */
    private final Type type;

    /**
     * The number of columns (characters) to display.
     */
    private final int columns;

    /**
     * The edit length of the field, or {@code -1} if it is unlimited.
     */
    private final int length;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FieldSpec.class.getName());

    /**
     * Minimum number of tokens.
     */
    private static final int MIN_TOKENS = 2;

    /**
     * Maximum number of tokens.
     */
    private static final int MAX_TOKENS = 3;


    /**
     * Constructs a {@code FieldSpec}.
     *
     * @param spec the field spec
     */
    public FieldSpec(String spec)
    {
        StringTokenizer tokenizer = new StringTokenizer(spec, ":");

        int tokens = tokenizer.countTokens();
        if (tokens < MIN_TOKENS || tokens > MAX_TOKENS)
        {
            throw new IzPackException("Invalid no. of tokens: " + tokens + " in rule: " + spec);
        }

        String token = tokenizer.nextToken().toUpperCase();
        type = getType(token, spec);

        // test for a valid integer to define the size of the field
        token = tokenizer.nextToken();
        columns = getColumns(token, spec);

        if (tokens == MAX_TOKENS)
        {
            // determine the edit length of the field
            token = tokenizer.nextToken().toUpperCase();
            if ("U".equals(token))
            {
                length = -1;
            }
            else
            {
                length = getLength(token, spec);
            }
        }
        else
        {
            length = -1;
        }
    }

    /**
     * Creates a {@code FieldSpec}.
     *
     * @param type    the field type
     * @param columns the number of columns to display.
     * @param length  the length of the field, or {@code -1} if its unlimited
     */
    public FieldSpec(Type type, int columns, int length)
    {
        this.type = type;
        this.columns = columns;
        this.length = length;
    }

    /**
     * Parses a field specification.
     *
     * @param spec the field specification
     * @return the corresponding FieldSpec, or {@code null} if the spec is not a valid field specification
     */
    public static FieldSpec parse(String spec)
    {
        FieldSpec result = null;
        try
        {
            result = new FieldSpec(spec);
        }
        catch (IzPackException exception)
        {
            logger.log(Level.FINE, "Failed to parse: " + spec, exception);
        }
        return result;
    }

    /**
     * Returns the field type.
     *
     * @return the field type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Returns the number to columns to display.
     *
     * @return the number of columns to display
     */
    public int getColumns()
    {
        return columns;
    }

    /**
     * Returns the maximum size of the field.
     *
     * @return the maximum size, or {@code -1} if it is unlimited
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Determines if the field has unlimited length.
     *
     * @return {@code true} if the field has unlimited length, {@code false} if it is limited by {@link #getLength}
     */
    public boolean isUnlimitedLength()
    {
        return length == -1;
    }

    /**
     * Validates a value against the field specification.
     *
     * @param value the value to validate
     * @return {@code true} if the value is valid, otherwise {@code false}
     */
    public boolean validate(String value)
    {
        boolean result = true;
        if (value != null)
        {
            if (length == -1 || value.length() <= length)
            {
                // test for numeric type
                if (type == FieldSpec.Type.NUMERIC)
                {
                    for (int i = 0; i < value.length(); i++)
                    {
                        if (!Character.isDigit(value.charAt(i)))
                        {
                            result = false;
                            break;
                        }
                    }
                }
                else if (type == FieldSpec.Type.HEX)
                {
                    for (int i = 0; i < value.length(); i++)
                    {
                        char ch = Character.toUpperCase(value.charAt(i));
                        if (!Character.isDigit(ch) && (ch != 'A') && (ch != 'B') && (ch != 'C') && (ch != 'D')
                                && (ch != 'E') && (ch != 'F'))
                        {
                            result = false;
                            break;
                        }
                    }
                }
                else if (type == FieldSpec.Type.ALPHA)
                {
                    for (int i = 0; i < value.length(); i++)
                    {
                        if (!Character.isLetter(value.charAt(i)))
                        {
                            result = false;
                            break;
                        }
                    }
                }
                else if (type == FieldSpec.Type.ALPHANUMERIC)
                {
                    for (int i = 0; i < value.length(); i++)
                    {
                        if (!Character.isLetterOrDigit(value.charAt(i)))
                        {
                            result = false;
                            break;
                        }
                    }
                }
                else if (type == FieldSpec.Type.OPEN)
                {
                    // no limiting rule
                }
                else
                {
                    logger.warning("Invalid field type: " + type);
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Returns the type of the field.
     *
     * @param type the type as a string
     * @param spec the rule, for error reporting purposes
     * @return the corresponding type
     */
    private Type getType(String type, String spec)
    {
        Type result;
        if ("N".equals(type))
        {
            result = Type.NUMERIC;
        }
        else if ("H".equals(type))
        {
            result = Type.HEX;
        }
        else if ("A".equals(type))
        {
            result = Type.ALPHA;
        }
        else if ("O".equals(type))
        {
            result = Type.OPEN;
        }
        else if ("AN".equals(type))
        {
            result = Type.ALPHANUMERIC;
        }
        else
        {
            throw new IzPackException("Invalid field type: " + type + " in rule: " + spec);
        }
        return result;
    }

    /**
     * Returns the number of columns to display of the field.
     *
     * @param values the columns as a string
     * @param spec   the rule, for error reporting purposes
     * @return the corresponding column
     */
    private int getColumns(String values, String spec)
    {
        int result;
        try
        {
            result = Integer.parseInt(values);
        }
        catch (NumberFormatException exception)
        {
            throw new IzPackException("Unable to determine the size of the field in rule:" + spec, exception);
        }
        return result;
    }

    /**
     * Returns the length of the field.
     *
     * @param value the length as a string
     * @param spec  the rule, for error reporting purposes
     * @return the corresponding length
     */
    private int getLength(String value, String spec)
    {
        int result;
        try
        {
            result = Integer.parseInt(value);
        }
        catch (NumberFormatException exception)
        {
            throw new IzPackException("Unable to determine the length of the field in rule:" + spec, exception);
        }
        return result;
    }

}
