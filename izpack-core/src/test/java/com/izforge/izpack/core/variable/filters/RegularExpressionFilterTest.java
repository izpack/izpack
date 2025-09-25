package com.izforge.izpack.core.variable.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.izforge.izpack.core.data.DefaultVariables;
import org.junit.jupiter.api.Test;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;

public class RegularExpressionFilterTest
{

    @Test
    public void testSelectNumberValue()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(System.getProperties()));
        ValueFilter filter = new RegularExpressionFilter("^(\\d+)$", "\\1", "3000", true);
        try
        {
            assertEquals(
                    "10",
                    filter.filter("10", subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testSelectDefaultValue()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(System.getProperties()));
        ValueFilter filter = new RegularExpressionFilter("^(\\d+)$", "\\1", "3000", true);
        try
        {
            assertEquals(
                    "3000",
                    filter.filter("xxx", subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testReplaceNumberValueGlobal()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(System.getProperties()));
        ValueFilter filter = new RegularExpressionFilter("\\d+", ".", "abc", true, true);
        try
        {
            assertEquals(
                    ".x.x.",
                    filter.filter("1x2x300", subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testReplaceNumberValueOnce()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(System.getProperties()));
        ValueFilter filter = new RegularExpressionFilter("\\d+", ".", "abc", true, false);
        try
        {
            assertEquals(
                    ".x2x300",
                    filter.filter("1x2x300", subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testReplaceDefaultValue()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(System.getProperties()));
        ValueFilter filter = new RegularExpressionFilter("\\d+", ".", "abc", true, true);
        try
        {
            assertEquals(
                    "abc",
                    filter.filter("xxx", subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }
}
