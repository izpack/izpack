/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012
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


import com.izforge.izpack.panels.userinput.processor.Processor;

/**
 * Describes the format of an {@link RuleField}.
 *
 * @author Tim Anderson
 */
public enum RuleFormat
{
    /**
     * Specifies to return the contents of all fields concatenated into one long string, with separation between
     * each component.
     */
    PLAIN_STRING("plainString"),

    /**
     * Specifies to return the contents of all fields together with all separators as specified in the field format
     * concatenated into one long string.
     * In this case the resulting string looks just like the user saw it during data entry.
     */
    DISPLAY_FORMAT("displayFormat"),

    /**
     * Specifies to return the contents of all fields concatenated into one long string, with a special separator
     * string inserted in between the individual components.
     */
    SPECIAL_SEPARATOR("specialSeparator"),

    /**
     * Specifies to return the contents of all fields using an {@link Processor}.
     */
    PROCESSED("processed");

    /**
     * Constructs a {@code RuleFormat}.
     *
     * @param format the format
     */
    private RuleFormat(String format)
    {
        this.format = format;
    }

    /**
     * Returns a string representation of this.
     *
     * @return the format
     */
    public String toString()
    {
        return format;
    }

    /**
     * Returns a {@code RuleFormat} for the specified value.
     *
     * @param value the value
     * @return the corresponding {@code RuleFormat}, or {@code null} if none is found
     */
    public static RuleFormat fromString(String value)
    {
        for (RuleFormat f : values())
        {
            if (f.format.equals(value))
            {
                return f;
            }
        }
        return null;
    }

    /**
     * The format identifier.
     */
    private final String format;
}
