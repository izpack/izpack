package com.izforge.izpack.core.variable.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Properties;

import com.izforge.izpack.core.data.DefaultVariables;
import org.junit.jupiter.api.Test;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;

public class LocationFilterTest
{

    @Test
    public void testOneDirUp()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(System.getProperties()));
        ValueFilter filter = new LocationFilter("C:\\Program Files\\MyApp\\subdir");
        try
        {
            assertEquals(
                    "C:\\Program Files\\MyApp\\app.exe".replace('\\', File.separatorChar),
                    filter.filter("..\\app.exe", subst));
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
            assertEquals(
                    "C:\\Program Files\\MyApp\\app.exe".replace('\\', File.separatorChar),
                    filter.filter("..\\app.exe", subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }
}
