package com.izforge.izpack.core.variable.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import com.izforge.izpack.core.data.DefaultVariables;
import org.junit.jupiter.api.Test;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;

public class CaseStyleFilterTest
{

    @Test
    public void testLowerCase()
    {
        final String text = "Some Text";
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(new Properties()));
        try
        {
            assertEquals("some text", new CaseStyleFilter("lower").filter(text, subst));
            assertEquals("some text", new CaseStyleFilter(CaseStyleFilter.Style.LOWER).filter(text, subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testUpperCase()
    {
        final String text = "Some Text";
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(new Properties()));
        try
        {
            assertEquals("SOME TEXT", new CaseStyleFilter("upper").filter(text, subst));
            assertEquals("SOME TEXT", new CaseStyleFilter(CaseStyleFilter.Style.UPPER).filter(text, subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

}
