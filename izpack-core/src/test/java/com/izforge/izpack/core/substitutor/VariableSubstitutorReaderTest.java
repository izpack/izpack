package com.izforge.izpack.core.substitutor;

import static com.izforge.izpack.api.substitutor.SubstitutionType.TYPE_PLAIN;
import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.core.data.DefaultVariables;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.junit.jupiter.api.Test;

public class VariableSubstitutorReaderTest {

    @Test
    public void testLineSeparatorSubstitution() throws IOException {
        Properties properties = new Properties();
        properties.put("VAR", "value1\nvalue2\r\nvalue3\r");
        Variables variables = new DefaultVariables(properties);

        String input = "Start\n${VAR}\r\nSome line\rEnd";
        VariableSubstitutorReader reader = new VariableSubstitutorReader(new StringReader(input), variables, TYPE_PLAIN);

        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            sb.append((char) c);
        }

        String ls = lineSeparator();
        String expected = "Start" + ls + "value1" + ls + "value2" + ls + "value3" + ls + ls + "Some line" + ls + "End";
        assertThat(sb).hasToString(expected);
    }
}
