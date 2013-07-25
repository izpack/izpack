package com.izforge.izpack.util.config.base.spi;

import org.junit.Test;

import junit.framework.Assert;

public class IniParserTest extends IniParser {

	@Test
	public void testIndexOfOperator() {
		//                                         012345678901 23
		Assert.assertEquals( 13, indexOfOperator("\"DisplayName\"=\"@%SystemRoot%\\system32") );
		//                                         012345678901234 56
		Assert.assertEquals( 16, indexOfOperator("\"http://*:2869/\"=hex:01,00") );
		//                                         01234567890123456 7 8 90
		Assert.assertEquals( 20, indexOfOperator("\"back-slash-quote\\\"\"=1") );	
        //                                         0123456789012345678 9 0 12
		Assert.assertEquals( 22, indexOfOperator("\"double-back-slash-\\\\\"=1") );
	}
}
