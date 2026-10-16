package com.izforge.izpack.core.variable.filters;

import static java.lang.System.getProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import org.junit.jupiter.api.Test;

public class RegularExpressionFilterTest
{

    @Test
    public void testSelectNumberValue()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(getProperties()));
        ValueFilter filter = new RegularExpressionFilter("^(\\d+)$", "\\1", "3000", true);
        try
        {
            assertThat(filter.filter("10", subst)).isEqualTo("10");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testSelectDefaultValue()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(getProperties()));
        ValueFilter filter = new RegularExpressionFilter("^(\\d+)$", "\\1", "3000", true);
        try
        {
            assertThat(filter.filter("xxx", subst)).isEqualTo("3000");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testReplaceNumberValueGlobal()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(getProperties()));
        ValueFilter filter = new RegularExpressionFilter("\\d+", ".", "abc", true, true);
        try
        {
            assertThat(filter.filter("1x2x300", subst)).isEqualTo(".x.x.");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testReplaceNumberValueOnce()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(getProperties()));
        ValueFilter filter = new RegularExpressionFilter("\\d+", ".", "abc", true, false);
        try
        {
            assertThat(filter.filter("1x2x300", subst)).isEqualTo(".x2x300");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testReplaceDefaultValue()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(getProperties()));
        ValueFilter filter = new RegularExpressionFilter("\\d+", ".", "abc", true, true);
        try
        {
            assertThat(filter.filter("xxx", subst)).isEqualTo("abc");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }
}
