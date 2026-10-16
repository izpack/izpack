package com.izforge.izpack.core.regex;

import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.regex.RegularExpressionProcessor;
import org.junit.jupiter.api.Test;

public class RegularExpressionProcessorImplTest
{

    @Test
    public void testRegexReplaceNotMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("jre\\sun\\1.6.0_21");
        processor.setRegexp("([^%]*)%JAVA_HOME%(.*)");
        processor.setReplace("\1../jre/sun/1.6.0_21\2");
        assertThat(processor.execute()).isEqualTo("jre\\sun\\1.6.0_21");
    }

    @Test
    public void testRegexReplaceMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("a\\a/a%JAVA_HOME%b\\b/b");
        processor.setRegexp("([^%]*)%JAVA_HOME%(.*)");
        processor.setReplace("\\1../jre/sun/1.6.0_21\\2");
        assertThat(processor.execute()).isEqualTo("a\\a/a../jre/sun/1.6.0_21b\\b/b");
    }

    @Test
    public void testRegexReplaceWithDefault()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("jre\\sun\\1.6.0_21");
        processor.setRegexp("([^%]*)%JAVA_HOME%(.*)");
        processor.setReplace("\1../jre/sun/1.6.0_21\2");
        processor.setDefaultValue("xxx");
        assertThat(processor.execute()).isEqualTo("xxx");
    }

    @Test
    public void testRegexSelectNotMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("java version \"unknown\"");
        processor.setRegexp("java version[^\\d]+([\\d\\._]+)");
        processor.setSelect("\\1");
        assertThat(processor.execute()).isNull();
    }

    @Test
    public void testRegexSelectMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("java version \"1.6.0_33\"");
        processor.setRegexp("java version[^\\d]+([\\d\\._]+)");
        processor.setSelect("\\1");
        assertThat(processor.execute()).isEqualTo("1.6.0_33");
    }

}
