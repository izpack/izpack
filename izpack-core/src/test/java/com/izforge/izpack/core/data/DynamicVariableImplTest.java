package com.izforge.izpack.core.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

import com.izforge.izpack.api.data.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.core.variable.ConfigFileValue;
import com.izforge.izpack.core.variable.PlainConfigFileValue;
import com.izforge.izpack.core.variable.PlainValue;
import com.izforge.izpack.core.variable.filters.LocationFilter;
import com.izforge.izpack.core.variable.filters.RegularExpressionFilter;

public class DynamicVariableImplTest
{
    @TempDir
    static Path tmpDir;

    @Test
    public void testSimple()
    {
        Properties props = new Properties();
        props.setProperty("INSTALL_PATH", "C:\\Program Files\\MyApp");
        Variables variables = new DefaultVariables(props);
        VariableSubstitutor subst = new VariableSubstitutorImpl(variables);
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

    @Test
    public void testBackSlashesWithRegex()
    {
        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(new Properties()));
        ValueFilter filter = new RegularExpressionFilter("[/\\\\]+", "/", null, Boolean.FALSE, Boolean.TRUE);

        DynamicVariable dynvar = new DynamicVariableImpl();
        dynvar.setValue(new PlainValue("C:\\Program Files\\Java\\jdk1.7.0_51\\bin\\java"));
        dynvar.addFilter(filter);
        try
        {
            assertEquals("C:/Program Files/Java/jdk1.7.0_51/bin/java",
                         dynvar.evaluate(subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testBackSlashesWithRegexFromConfigFile()
    {
        InputStream in = getClass().getResourceAsStream("wrapper.conf");
        byte[] buf = new byte[1024];
        File configFile = null;

        try
        {
            configFile = tmpDir.resolve("_wrapper_.conf").toFile();
            OutputStream out = new FileOutputStream(configFile);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

        VariableSubstitutor subst = new VariableSubstitutorImpl(new DefaultVariables(new Properties()));
        ValueFilter filter = new RegularExpressionFilter("[/\\\\]+", "/", null, Boolean.FALSE, Boolean.TRUE);

        DynamicVariable dynvar = new DynamicVariableImpl();
        dynvar.setValue(new PlainConfigFileValue(configFile.getPath(),
                ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "wrapper.java.command", false));
        dynvar.addFilter(filter);
        try
        {
            assertEquals("C:/Program Files/Java/jdk1.7.0_51/bin/java",
                         dynvar.evaluate(subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }
}
