package com.izforge.izpack.core.variable.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class CaseStyleFilterTest
{

    @Test
    public void testLowerCase()
    {
        final String text = "Some Text";
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(new Properties()));
        try
        {
            assertThat(new CaseStyleFilter("lower").filter(text, subst)).isEqualTo("some text");
            assertThat(new CaseStyleFilter(CaseStyleFilter.Style.LOWER).filter(text, subst)).isEqualTo("some text");
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
            assertThat(new CaseStyleFilter("upper").filter(text, subst)).isEqualTo("SOME TEXT");
            assertThat(new CaseStyleFilter(CaseStyleFilter.Style.UPPER).filter(text, subst)).isEqualTo("SOME TEXT");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

}
