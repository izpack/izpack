package com.izforge.izpack.core.variable.filters;

import static java.lang.System.getProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class LocationFilterTest
{

    @Test
    public void testOneDirUp()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(getProperties()));
        ValueFilter filter = new LocationFilter("C:\\Program Files\\MyApp\\subdir");
        try
        {
            assertThat(filter.filter("..\\app.exe", subst)).isEqualTo("C:\\Program Files\\MyApp\\app.exe".replace('\\', File.separatorChar));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testOneDirUpWithSubstitution()
    {
        Properties props = new Properties();
        props.setProperty("INSTALL_PATH", "C:\\Program Files\\MyApp");
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(props));
        ValueFilter filter = new LocationFilter("${INSTALL_PATH}\\subdir");
        try
        {
            assertThat(filter.filter("..\\app.exe", subst)).isEqualTo("C:\\Program Files\\MyApp\\app.exe".replace('\\', File.separatorChar));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }
}
