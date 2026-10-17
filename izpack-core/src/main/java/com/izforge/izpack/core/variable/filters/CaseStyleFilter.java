/*
 * IzPack - Copyright 2026 Julien Ponge, All Rights Reserved.
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
package com.izforge.izpack.core.variable.filters;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import java.util.Objects;

public class CaseStyleFilter implements ValueFilter
{

    private static final long serialVersionUID = 1L;

    public enum Style {
        LOWER, UPPER
    };

    private Style style;

    public CaseStyleFilter(Style style)
    {
        this.style = style;
    }

    public CaseStyleFilter(String style)
    {
        try
        {
            this.style = Style.valueOf(style.toUpperCase());
        }
        catch (RuntimeException e) // IllegalArgumentException || NullPointerException
        {
            // Do nothing, will be reported by validate()
        }
    }

    public Style getStyle()
    {
        return style;
    }

    @Override
    public void validate() throws Exception
    {
        if (style == null)
        { throw new CompilerException("case Filter has been initialized with unknown style"); }
    }

    @Override
    public String filter(String value, VariableSubstitutor... substitutors) throws Exception
    {
        switch (style)
        {
        case LOWER:
            return value.toLowerCase();
        case UPPER:
            return value.toUpperCase();
        default:
            throw new CompilerException(
                    "case Filter has been initialized with unimplemented style");
        }
    }

    @Override
    public String toString()
    {
        return "(style: " + style.toString() + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof CaseStyleFilter))
        { return false; }
        return style.equals(((CaseStyleFilter) obj).getStyle());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(style);
    }
}
