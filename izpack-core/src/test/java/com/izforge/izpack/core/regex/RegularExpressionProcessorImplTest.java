package com.izforge.izpack.core.regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.izforge.izpack.api.regex.RegularExpressionProcessor;

public class RegularExpressionProcessorImplTest
{

    @Test
    public void testRegexReplaceNotMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("jre\\sun\\1.6.0_21");
        processor.setRegexp("([^%]*)%JAVA_HOME%(.*)");
        processor.setReplace("\1../jre/sun/1.6.0_21\2");
        assertEquals("jre\\sun\\1.6.0_21", processor.execute());
    }

    @Test
    public void testRegexReplaceMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("a\\a/a%JAVA_HOME%b\\b/b");
        processor.setRegexp("([^%]*)%JAVA_HOME%(.*)");
        processor.setReplace("\\1../jre/sun/1.6.0_21\\2");
        assertEquals("a\\a/a../jre/sun/1.6.0_21b\\b/b", processor.execute());
    }

    @Test
    public void testRegexReplaceWithDefault()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("jre\\sun\\1.6.0_21");
        processor.setRegexp("([^%]*)%JAVA_HOME%(.*)");
        processor.setReplace("\1../jre/sun/1.6.0_21\2");
        processor.setDefaultValue("xxx");
        assertEquals("xxx", processor.execute());
    }

    @Test
    public void testRegexSelectNotMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("java version \"unknown\"");
        processor.setRegexp("java version[^\\d]+([\\d\\._]+)");
        processor.setSelect("\\1");
        assertNull(processor.execute());
    }

    @Test
    public void testRegexSelectMatching()
    {
        RegularExpressionProcessor processor = new RegularExpressionProcessorImpl();
        processor.setInput("java version \"1.6.0_33\"");
        processor.setRegexp("java version[^\\d]+([\\d\\._]+)");
        processor.setSelect("\\1");
        assertEquals("1.6.0_33", processor.execute());
    }

}
