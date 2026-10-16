package com.izforge.izpack.panels.userinput.gui.search;

import static com.izforge.izpack.panels.userinput.field.search.SearchField.resolveEnvValue;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;


public class SearchInputFieldTest {

    @Test
    public void testResolveEnvValue() {
        Map<String, String> env = new HashMap<String, String>();
        env.put("JAVA_HOME", "C:\\Program Files\\Java\\jdk1.7.0");
        env.put("PUBLIC", "C:\\Users\\Public");

        assertThat(resolveEnvValue("%JAVA_HOME%", env)).isEqualTo("C:\\Program Files\\Java\\jdk1.7.0");
        assertThat(resolveEnvValue("--%JAVA_HOME%++", env)).isEqualTo("--C:\\Program Files\\Java\\jdk1.7.0++");
        assertThat(resolveEnvValue("1;%JAVA_HOME%;%PUBLIC%;3", env)).isEqualTo("1;C:\\Program Files\\Java\\jdk1.7.0;C:\\Users\\Public;3");
    }

}
