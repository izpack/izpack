package com.izforge.izpack.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.core.variable.filters.LocationFilter;

public class DynamicVariableImplTest
{

    @Test
    public void testSimple()
    {
        Properties props = new Properties();
        props.setProperty("INSTALL_PATH", "C:\\Program Files\\MyApp");
        VariableSubstitutor subst = new VariableSubstitutorImpl(props);
        ValueFilter filter = new LocationFilter("${INSTALL_PATH}\\subdir");

        DynamicVariable dynvar = new DynamicVariableImpl();
        dynvar.setValue(new PlainValue("..\\app.exe"));
        dynvar.addFilter(filter);
        try
        {
            assertEquals("C:\\Program Files\\MyApp\\app.exe".replace('\\', File.separatorChar),
                         dynvar.evaluate(subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

}
