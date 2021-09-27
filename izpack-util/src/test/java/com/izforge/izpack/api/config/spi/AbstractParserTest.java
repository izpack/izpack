package com.izforge.izpack.api.config.spi;

import org.junit.Assert;
import org.junit.Test;


public class AbstractParserTest extends AbstractParser {
	
    private static final String COMMENTS = ";#";
    private static final String OPERATORS = ":=";


    public AbstractParserTest() {
    	super(OPERATORS, COMMENTS);
    }

    @Test
    public void indexOfOperator() {
    	String line0 = "\"";
    	Assert.assertEquals(-1, indexOfOperator(line0));
    	
    	String line1 = "\"NextInstance\"=dword:00000001";
    	Assert.assertEquals(14, indexOfOperator(line1));
    	
    	String line2 = "\"4\"=\"IPBusEnumRoot\\UMB\\2&ba1ffa4&0&uuid:12345678-0000-0000-0000-00000000abcd\"";
    	Assert.assertEquals(3, indexOfOperator(line2));
    	
    	String line3 = "\"0\"=\"uuid:12345678-0000-0000-0000-00000000abcd\\UMB\\3&44ecbc&0&uuid:12345678-0000-0000-0000-00000000abcd\"";
    	Assert.assertEquals(3, indexOfOperator(line3));
    }
}
