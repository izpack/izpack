/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Daniel Abson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util.config;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.izforge.izpack.util.config.MultiMapConfigEntry.Operation;
import com.izforge.izpack.util.config.MultiMapConfigEntry.Type;
import com.izforge.izpack.util.config.MultiMapConfigEntry.Unit;

public class MultiMapConfigEntryTest {
	
	private static final String DATE_VALUE = "2000/01/01 20:00:01.010";
	private static final String DATE_CONTROL = "1970/01/01 00:00:00.000";
	private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
	
	private static DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
	
	private MultiMapConfigEntry entry;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	@Before
	public void setUp() throws Exception
	{
		entry = new MultiMapConfigEntry();
	}

	@Test
	public void testValidateAttributes()
	{
		tryValidation(false, "Expected validation failure (no attributes set)");
		
		//test valid key/section & value/default combinations 
		entry.setKey("bar");
		entry.setValue("baz");
		tryValidation(true, "Unexpected validation failure (key & value set)");
		entry.setKey(null);
		entry.setSection("foo");
		tryValidation(true, "Unexpected validation failure (section & value set");
		entry.setValue(null);
		entry.setDefault("qux");
		tryValidation(true, "Unexpected validation failure (section & default set");
		
		//test invalid no value/default
		entry.setDefault(null);
		tryValidation(false, "Expected validation failure (no value or default set)");
		
		//test valid type operation/pattern/unit combinations
		testValidOperation(Operation.DECREMENT, Type.DATE, Type.INTEGER);
		testValidOperation(Operation.INCREMENT, Type.DATE, Type.INTEGER, Type.STRING);
		testValidOperation(Operation.KEEP, Type.DATE, Type.INTEGER, Type.STRING);
		testValidOperation(Operation.SET, Type.DATE, Type.INTEGER, Type.STRING);
		testValidOperation(Operation.REMOVE, Type.DATE, Type.INTEGER, Type.STRING);
		//test valid type/pattern combo (i.e. any type but STRING)
		testValidPattern(Type.DATE, Type.INTEGER);
		entry.setPattern(null);
		//test valid type/unit combo (i.e. only DATE)
		entry.setType(Type.DATE);
		entry.setUnit(Unit.DAY);
		tryValidation(true, "Unexpected validation failure (DATE & HOUR set)");
		entry.setUnit(null);
		
		//test invalid type operation/pattern/unit combinations
		entry.setOperation(Operation.DECREMENT);
		entry.setType(Type.STRING);
		tryValidation(false, "Expected validation failure (STRING & DECREMENT set)");
		entry.setOperation(Operation.SET);
		entry.setPattern("quux");
		tryValidation(false, "Expected validation failure (STRING & pattern set");
		entry.setPattern(null);
		//test invalid unit/type cominations (i.e. any type except DATE)
		testInvalidUnit(Type.STRING, Type.INTEGER);
		
	}

	/* NON-JAVADOC
	 * Set entry operation as specified, and repeat tryValidation with entry type set to specified types.
	 * Entry is left with section, key, value, operation, and type set 
	 */
	private void testValidOperation(Operation op, Type... types)
	{
		entry.setSection("foo");
		entry.setKey("bar");
		entry.setValue("baz");
		entry.setOperation(op);
		for (Type type : types)
		{
			entry.setType(type);
			tryValidation(true, "Unexpected validation failure (" + type + " & " + op + " set)");
		}
	}
	
	/* NON-JAVADOC
	 * Set pattern to a nonsense value, and repeat tryValidation with entry type set to
	 * specified types. Entry is left with section, key, value, operation, type, and pattern set.
	 */
	private void testValidPattern(Type... types)
	{
		entry.setSection("foo");
		entry.setKey("bar");
		entry.setValue("baz");
		entry.setOperation(Operation.SET);
		entry.setPattern("quux");
		for (Type type : types)
		{
			entry.setType(type);
			tryValidation(true, "Unexpected validation failure (" + type + " & " + entry.pattern + " set)");
		}		
	}
	
	/* NON-JAVADOC
	 * Set pattern to a nonsense value, and repeat tryValidation with entry type set to
	 * specified types. Entry is left with section, key, value, operation, type, and unit set.
	 */
	private void testInvalidUnit(Type... types)
	{
		entry.setSection("foo");
		entry.setKey("bar");
		entry.setValue("baz");
		entry.setUnit(Unit.DAY);
		for (Type type : types)
		{
			entry.setType(type);
			tryValidation(false, "Expected validation failure (" + type + " & " + entry.unit + " set)");
		}
	}
	
	@Test
	public void testGetCurrentValue()
	{
		//test valid combinations for operations except SET
		testNonSetOperation(Operation.DECREMENT);
		testNonSetOperation(Operation.INCREMENT);	
		testNonSetOperation(Operation.KEEP);	
		testNonSetOperation(Operation.REMOVE);	
		
		//test valid combinations for Operation.SET
		entry.setOperation(Operation.SET);
		entry.setValue("foo");
		entry.setDefault(null);
		assertEquals("foo", entry.getCurrentValue("baz"));	//value, oldValue
		assertEquals("foo", entry.getCurrentValue(null));	//value
		entry.setDefault("bar");
		assertEquals("foo", entry.getCurrentValue("baz"));	//value, default, oldValue
		assertEquals("bar", entry.getCurrentValue(null));	//value, default
		entry.setValue(null);		
		assertEquals("baz", entry.getCurrentValue("baz"));	//default, oldValue 
		assertEquals("bar", entry.getCurrentValue(null));	//default
	}
	
	/* NON-JAVADOC
	 * Tests currentValue() for all valid combinations of oldValue/value/defaultValue
	 * for the specified operation (not Operation.SET) 
	 */
	private void testNonSetOperation(Operation operation)
	{
		if (operation.equals(Operation.SET))
		{
			throw new RuntimeException("Cannot use this test method with Operation.SET");
		}
		entry.setOperation(operation);
		
		for (String value : new String[] {null, "", "foo"})
		{
			entry.setValue(value);
			entry.setDefault("bar");
			assertEquals("bar", entry.getCurrentValue(null));
			assertEquals("baz", entry.getCurrentValue("baz"));
			entry.setDefault(null);
			assertNull(entry.getCurrentValue(null));
		}
	}
	
	@Test
	public void testUnit() throws Exception
	{
		assertEquals(Calendar.YEAR, Unit.YEAR.getCalendarField());
		assertEquals(Calendar.MONTH, Unit.MONTH.getCalendarField());
		assertEquals(Calendar.WEEK_OF_YEAR, Unit.WEEK.getCalendarField());
		assertEquals(Calendar.DAY_OF_MONTH, Unit.DAY.getCalendarField());
		assertEquals(Calendar.HOUR_OF_DAY, Unit.HOUR.getCalendarField());
		assertEquals(Calendar.MINUTE, Unit.MINUTE.getCalendarField());
		assertEquals(Calendar.SECOND, Unit.SECOND.getCalendarField());
		assertEquals(Calendar.MILLISECOND, Unit.MILLISECOND.getCalendarField());
	}
	
	@Test
	public void testCalculateDateValue() throws Exception 
	{
		Calendar now = Calendar.getInstance();
		now.setTimeZone(TimeZone.getTimeZone("GMT"));
		int thisYear = now.get(Calendar.YEAR);
		
		entry.setPattern("yyyy/MM/dd HH:mm:ss.SSS");
		entry.setType(Type.DATE);
		entry.setUnit(Unit.YEAR);
		
		//test valid combinations for Operation.INCREMENT for exemplar tests where unit is YEAR
		entry.setOperation(Operation.INCREMENT);
		entry.setValue("10");
		entry.setDefault(null);
		assertEquals("2010/01/01 20:00:01.010", entry.calculateValue("2000/01/01 20:00:01.010"));	//value, oldValue			= oldValue+value
		assertEquals(String.valueOf(thisYear + 10), entry.calculateValue(null).substring(0, 4));						//value						= "now"+value
		entry.setDefault("1970/01/01 00:00:00.000");
		assertEquals("2010/01/01 20:00:01.010", entry.calculateValue("2000/01/01 20:00:01.010"));	//value, default, oldValue	= oldValue+value
		assertEquals("1980/01/01 00:00:00.000", entry.calculateValue(null));						//value, default			= default+value
		entry.setValue(null);
		assertEquals("2001/01/01 20:00:01.010", entry.calculateValue("2000/01/01 20:00:01.010"));	//default, oldValue			= oldValue+1
		assertEquals("1971/01/01 00:00:00.000", entry.calculateValue(null));						//default					= default+1		

		//test valid combinations for Operation.DECREMENT for exemplar tests where unit is YEAR
		entry.setOperation(Operation.DECREMENT);
		entry.setValue("10");
		entry.setDefault(null);
		assertEquals("1990/01/01 20:00:01.010", entry.calculateValue("2000/01/01 20:00:01.010"));	//value, oldValue			= oldValue-value
		assertEquals(String.valueOf(thisYear - 10), entry.calculateValue(null).substring(0, 4));						//value						= "now"-value
		entry.setDefault("1970/01/01 00:00:00.000");
		assertEquals("1990/01/01 20:00:01.010", entry.calculateValue("2000/01/01 20:00:01.010"));	//value, default, oldValue	= oldValue-value
		assertEquals("1960/01/01 00:00:00.000", entry.calculateValue(null));						//value, default			= default-value
		entry.setValue(null);
		assertEquals("1999/01/01 20:00:01.010", entry.calculateValue("2000/01/01 20:00:01.010"));	//default, oldValue			= oldValue-1
		assertEquals("1969/01/01 00:00:00.000", entry.calculateValue(null));						//default					= default-1		
	}
	
	@Test
	public void testCalculateIntegerValue() throws Exception 
	{
		//test valid combinations for Operation.INCREMENT
		entry.setType(Type.INTEGER);
		entry.setOperation(Operation.INCREMENT);
		entry.setValue("10");
		entry.setDefault(null);
		assertEquals("40", entry.calculateValue("30"));			//value, oldValue			= oldValue+value
		assertEquals("10", entry.calculateValue(null));			//value						= value
		entry.setDefault("20");
		assertEquals("40", entry.calculateValue("30"));			//value, default, oldValue	= oldValue+value
		assertEquals("30", entry.calculateValue(null));			//value, default			= default+value
		entry.setValue(null);
		assertEquals("31", entry.calculateValue("30"));			//default, oldValue			= oldValue+1
		assertEquals("21", entry.calculateValue(null));			//default					= default+1		

		//test valid combinations for Operation.DECREMENT
		entry.setOperation(Operation.DECREMENT);
		entry.setValue("10");
		entry.setDefault(null);
		assertEquals("20", entry.calculateValue("30"));			//value, oldValue			= oldValue-value
		assertEquals("-10", entry.calculateValue(null));		//value						= value
		entry.setDefault("20");
		assertEquals("20", entry.calculateValue("30"));			//value, default, oldValue	= oldValue-value
		assertEquals("10", entry.calculateValue(null));			//value, default			= default-value
		entry.setValue(null);
		assertEquals("29", entry.calculateValue("30"));			//default, oldValue			= oldValue-1
		assertEquals("19", entry.calculateValue(null));			//default					= default-1		

		//test valid combinations for Operation.SET
		entry.setOperation(Operation.SET);
		entry.setValue("10");
		entry.setDefault(null);
		assertEquals("10", entry.calculateValue("30"));			//value, oldValue			= value
		assertEquals("10", entry.calculateValue(null));			//value						= value
		entry.setDefault("20");
		assertEquals("10", entry.calculateValue("30"));			//value, default, oldValue	= value
		assertEquals("20", entry.calculateValue(null));			//value, default			= default
		entry.setValue(null);
		assertEquals("30", entry.calculateValue("30"));			//default, oldValue			= oldValue
		assertEquals("20", entry.calculateValue(null));			//default					= default		
	}

	@Test
	public void testCalculateStringValue() throws Exception
	{
		//test valid combinations for Operation.INCREMENT
		entry.setType(Type.STRING);
		entry.setOperation(Operation.INCREMENT);
		entry.setValue("New");
		entry.setDefault(null);
		assertEquals("OldNew", entry.calculateValue("Old"));	//value, oldValue			= oldValue+value
		assertEquals("New", entry.calculateValue(null));		//value						= value
		entry.setDefault("Default");
		assertEquals("OldNew", entry.calculateValue("Old"));	//value, default, oldValue	= oldValue+value
		assertEquals("DefaultNew", entry.calculateValue(null));	//value, default			= default+value
		entry.setValue(null);
		assertEquals("Old", entry.calculateValue("Old"));		//default, oldValue			= oldValue
		assertEquals("Default", entry.calculateValue(null));	//default					= default
		
		//test valid combinations for Operation.SET
		entry.setOperation(Operation.SET);
		entry.setValue("New");
		entry.setDefault(null);
		assertEquals("New", entry.calculateValue("Old"));		//value, oldValue			= value
		assertEquals("New", entry.calculateValue(null));		//value						= value
		entry.setDefault("Default");
		assertEquals("New", entry.calculateValue("Old"));		//value, default, oldValue	= value
		assertEquals("Default", entry.calculateValue(null));	//value, default			= default
		entry.setValue(null);
		assertEquals("Old", entry.calculateValue("Old"));		//default, oldValue			= oldValue
		assertEquals("Default", entry.calculateValue(null));	//default					= default
	}
	
	/* NON-JAVADOC
	 * Wrap validateRegAttributes in try/catch, and fail if exception-is-null does 
	 * not match value of shouldValidate (null exception mean successful validation). 
	 */
	private void tryValidation(boolean shouldValidate, String errMessage)
	{
		Exception exception = null;
		try
		{
			entry.validateAttributes();
		}
		catch (Exception e)
		{
			exception = e;
		}
		if (!(exception == null == shouldValidate))
		{
			fail(errMessage + (exception == null ? "" : ": " + exception.getMessage()));
		}
	}
}
