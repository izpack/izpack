package com.izforge.izpack.core.variable.filters;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

public class CaseStyleFilter implements ValueFilter
{

    private static final long serialVersionUID = 1L;

    public enum Style
    {
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
        catch (RuntimeException e)  //    IllegalArgumentException || NullPointerException
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
        {
            throw new CompilerException("case Filter has been initialized with unknown style");
        }
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
                throw new CompilerException("case Filter has been initialized with unimplemented style");
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
        {
            return false;
        }
        return style.equals(((CaseStyleFilter) obj).getStyle());
    }
}
