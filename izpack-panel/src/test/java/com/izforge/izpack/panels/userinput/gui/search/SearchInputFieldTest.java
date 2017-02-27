package com.izforge.izpack.panels.userinput.gui.search;

import com.izforge.izpack.panels.userinput.field.search.SearchField;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SearchInputFieldTest
{

    @Test
    public void testResolveEnvValue()
    {
        Map<String, String> env = new HashMap<String, String>();
        env.put("JAVA_HOME", "C:\\Program Files\\Java\\jdk1.7.0");
        env.put("PUBLIC", "C:\\Users\\Public");

        assertEquals("C:\\Program Files\\Java\\jdk1.7.0", SearchField.resolveEnvValue("%JAVA_HOME%", env));
        assertEquals("--C:\\Program Files\\Java\\jdk1.7.0++", SearchField.resolveEnvValue("--%JAVA_HOME%++", env));
        assertEquals("1;C:\\Program Files\\Java\\jdk1.7.0;C:\\Users\\Public;3",
                SearchField.resolveEnvValue("1;%JAVA_HOME%;%PUBLIC%;3", env));
    }

}
