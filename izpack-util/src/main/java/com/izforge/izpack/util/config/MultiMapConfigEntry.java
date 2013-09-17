/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010,2011 Rene Krell
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Instance of this class represents nested elements of a task configuration file. If 
 * {@link #validateAttributes()} is not run on a modified instance before any of the
 * {@code calculateXxxValue} methods, or the {@link #getCurrentValue(String) getCurrentValue}
 * method, those methods may return an unexpected value or null.
 */
public class MultiMapConfigEntry implements Cloneable
{
    private static final Logger logger = Logger.getLogger(MultiMapConfigEntry.class.getName());

    protected static final int DEFAULT_INT_VALUE = 0;

    protected static final String DEFAULT_DATE_VALUE = "now";

    protected static final String DEFAULT_STRING_VALUE = "";
    
    protected static final Unit DEFAULT_UNIT_VALUE = Unit.DAY;
    
    protected static final String DEFAULT_DATE_PATTERN = "yyyy/MM/dd HH:mm";

    protected String section = null;

    protected String key = null;

    protected String value = null;

    protected MultiMapConfigEntry.LookupType lookupType = LookupType.PLAIN;

    protected MultiMapConfigEntry.Type type = Type.STRING;

    protected MultiMapConfigEntry.Operation operation = Operation.SET;

    protected String defaultValue = null;

    protected String pattern = null;

    protected Unit unit = null;

    public MultiMapConfigEntry ()
    {
    	
    }
    
    public MultiMapConfigEntry (String section, String key, String value)
    {
    	this.section = section;
    	this.key = key;
    	this.value = value;
    }

    /**
     * Set name of the section containing the key to update.
     * 
     * @param section the section containing the key to update
     */
    public void setSection(String section)
    {
        this.section = section;
    }

    /**
     * Set name of the key/value pair to update.
     * 
     * @param key the key/value pair to update
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * Set value to apply.
     * 
     * @param value the value to apply
     * @see #setLookupType(LookupType)
     * @see #setOperation(Operation)
     */
    public void setValue(String value)
    {
        this.value = value;
    }
    
    
    /**
     * Return the name of the section containing the key to update.
     * 
     * @return the section containing the key to update
     */
    public String getSection()
    {
    	return section;
    }
    
    /**
     * Return the name of the key/value pair to update.
     * 
     * @return the key/value pair to update
     */
    public String getKey()
    {
    	return key;
    }

    /**
     * Return the value to apply.
     * 
     * @return the value to apply
     * @see #getLookupType()
     * @see #getOperation()
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Set the value lookup method. Defaults to {@link LookupType#PLAIN}.
     * 
     * @return the value lookup method
     */
    public MultiMapConfigEntry.LookupType getLookupType()
    {
        return lookupType;
    }


    /**
     * Get the datatype for the entry.
     * 
     * @return the datatype for the entry
     */
    public MultiMapConfigEntry.Type getType()
    {
        return type;
    }
    
    /**
     * Get the operand to use to apply the configured value to the current 
     * value.
     *
     * @return the operand to use to apply the configured value to the current 
     * value 
     */
    public MultiMapConfigEntry.Operation getOperation()
    {
        return operation;
    }

    /**
     * Set the operand to use to apply the configured value to the current 
     * value. Types {@link Operation#INCREMENT INCREMENT}, 
     * {@link Operation#REMOVE REMOVE}, {@link Operation#KEEP KEEP}, and 
     * {@link Operation#SET SET} (default) are valid for all datatypes; 
     * {@link Operation#DECREMENT DECREMENT} can only be used for date and int
     * types.
     * 
     * @param operation the {@link Operation} to use to apply the configured
     * value to the current value.
     * @see #setValue(String)
     */
    public void setOperation(MultiMapConfigEntry.Operation operation)
    {
        this.operation = operation;
    }

    /**
     * Set the datatype for the entry. Defaults to {@link Type#STRING STRING}.
     * 
     * @param type the datatype for the entry
     */
    public void setType(MultiMapConfigEntry.Type type)
    {
        this.type = type;
    }

    /**
     * Set the value lookup method. Defaults to {@link LookupType#PLAIN}.
     * 
     * @param lookupType the value lookup method
     */
    public void setLookupType(MultiMapConfigEntry.LookupType lookupType)
    {
        this.lookupType = lookupType;
    }

    /**
     * Initial value to set for a key if it is not already defined in the
     * configuration. For type date, an additional keyword is allowed: 
     * &quot;now&quot;.
     * 
     * @param defaultValue the default value for an entry
     */

    public void setDefault(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets a pattern for parsing and formatting int and date types.
     * 
     * @param pattern the pattern to use when parsing and formatting int and
     * date types
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    /**
     * The unit of the value to be applied to date +/- operations. Default is 
     * {@link Unit#DAY}. Only applies to date types using a +/- operation.
     * 
     * @param unit the unit of the value to be applied to date +/- operations
     */
    public void setUnit(Unit unit)
    {
        this.unit = unit;
    }
    
    public String calculateValue(String oldValue) throws Exception
    {
        String newValue = null;

        switch (type)
        {
        case INTEGER:
            newValue = calculateIntegerValue(oldValue);
            break;
        case DATE:
            newValue = calculateDateValue(oldValue);
            break;
        case STRING:
            newValue = calculateStringValue(oldValue);
            break;
        default:
            throw new Exception("Unknown operation type: " + type);
        }

        return newValue == null ? "" : newValue;
    }

    /**
     * Handle operations for type <code>date</code>.
     *
     * For the purposes of {@code Operation.INCREMENT} and {@code Operation.DECREMENT}:
     * <ul><li>if {@code oldValue} is null, it is effectively equivalent to "now" or (if
     * defined) to {@code default};</li>
     * <li>where {@code value} is not a valid integer, a default increment/decrement of 1 will
     * be applied.</li></ul> 
     *
     * @param oldValue the current value read from the configuration file or <code>null</code>
     * if the <code>key</code> was not contained in the configuration file.
     */
    protected String calculateDateValue(String oldValue) throws Exception
    {
        Calendar currentValue = Calendar.getInstance();

        DateFormat fmt = new SimpleDateFormat( (pattern == null ? DEFAULT_DATE_PATTERN : pattern) );

        String currentStringValue = getCurrentValue(oldValue);
        if (currentStringValue == null)
        {
            currentStringValue = DEFAULT_DATE_VALUE;
        }
        Unit currentUnitValue = unit;
        if (currentUnitValue == null)
        {
        	currentUnitValue = DEFAULT_UNIT_VALUE;
        }

        if ("now".equals(currentStringValue))
        {
            currentValue.setTime(new Date());
        }
        else
        {
            try
            {
                currentValue.setTime(fmt.parse(currentStringValue));
            }
            catch (ParseException pe)
            {
                // swallow
            }
        }

        if (operation != Operation.SET)
        {
            int offset = 1;
            try
            {
                offset = Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                // swallow
            }
            if (operation == Operation.DECREMENT)
            {
                offset = -1 * offset;
            }
            currentValue.add(currentUnitValue.getCalendarField(), offset);
        }

        return fmt.format(currentValue.getTime());
    }

    /**
     * Handle operations for type <code>int</code>. 
     * 
     * For the purposes of {@code Operation.INCREMENT} and {@code Operation.DECREMENT}:
     * <ul><li>if {@code oldValue} is null, it is effectively equivalent to 0 or (if defined)
     * to {@code default};</li>
     * <li>where {@code value} is not a valid integer, a default increment/decrement of 1 will
     * be applied.</li></ul> 
     *
     * @param oldValue the current value read from the configuration file or <code>null</code>
     * if the <code>key</code> was not contained in the configuration file.
     */
    protected String calculateIntegerValue(String oldValue) throws Exception
    {
        int currentValue = DEFAULT_INT_VALUE;
        int newValue = DEFAULT_INT_VALUE;

        DecimalFormat fmt = (pattern != null) ? new DecimalFormat(pattern)
                : new DecimalFormat();
        try
        {
            String curval = getCurrentValue(oldValue);
            if (curval != null)
            {
                currentValue = fmt.parse(curval).intValue();
            }
            else
            {
                currentValue = 0;
            }
        }
        catch (NumberFormatException nfe)
        {
            // swallow
        }
        catch (ParseException pe)
        {
            // swallow
        }

        if (operation == Operation.SET)
        {
            newValue = currentValue;
        }
        else
        {
            int operationValue = 1;
            if (value != null)
            {
                try
                {
                    operationValue = fmt.parse(value).intValue();
                }
                catch (NumberFormatException nfe)
                {
                    // swallow
                }
                catch (ParseException pe)
                {
                    // swallow
                }
            }

            if (operation == Operation.INCREMENT)
            {
                newValue = currentValue + operationValue;
            }
            else if (operation == Operation.DECREMENT)
            {
                newValue = currentValue - operationValue;
            }
        }

        return fmt.format(newValue);
    }

    /**
     * Handle operations for type <code>string</code>.
     *
     * @param oldValue the current value read from the configuration file or <code>null</code>
     * if the <code>key</code> was not contained in the configuration file.
     */
    protected String calculateStringValue(String oldValue) throws Exception
    {
        String newValue = DEFAULT_STRING_VALUE;

        String currentValue = getCurrentValue(oldValue);

        if (currentValue == null)
        {
            currentValue = DEFAULT_STRING_VALUE;
        }

        if (operation == Operation.SET)
        {
            newValue = currentValue;
        }
        else if (operation == Operation.INCREMENT)
        {
            newValue = currentValue + (value == null ? "" : value);
        }

        return newValue;
    }

    /**
     * Check if parameter combinations can be supported.
     *
     * @throws Exception in the event of any validation error
     */
    protected void validateAttributes() throws Exception
    {
        if (key == null && section == null)
        {
        	throw new Exception("at least one of \"key\" or \"section\" must be specified");
    	}
        if (value == null && defaultValue == null && operation == Operation.SET) 
        { 
        	throw new Exception("\"value\" and/or \"default\" attribute must be specified (key: " + key+ "; operation: " + operation + ")");
    	}
        if (type == Type.STRING && operation == Operation.DECREMENT)
        {
        	throw new Exception("- is not supported for string properties (key: " + key + ")"); 
    	}
        if (type == Type.STRING && pattern != null) 
        { 
        	throw new Exception("pattern is not supported for string properties (key: " + key + ")"); 
        }
        if (unit != null && type != Type.DATE)
        {
        	throw new Exception("unit is only supported for date properties (key: " + key + ")");
        }
    }

    /**
     * <p>Return the working value of the specified entry before updating. This is determined
     * by whether {@code oldValue} is null (i.e. no previous value found), and by the 
     * {@code operation}, {@code value}, and {@code defaultValue} attributes for the instance.</p>
     * 
     * <p>For any operation <b>except</b> {@code SET}, {@code oldValue} is always returned 
     * when not null. Otherwise, {@code defaultValue} is returned (even if null).</p>
     * 
     * <p>Where the operation is {@code SET}, {@code value} is always returned if 
     * {@code defaultValue} is not defined. Otherwise:
     * <ul><li>if {@code value} is not defined and {@code oldValue} is non-null, 
     * {@code oldValue} is returned unchanged;</li>
     * <li>if {@code oldValue} is null and {@code defaultValue} is defined,
     * {@code defaultValue} returned (i.e. {@code value} is ignored);</li>
     * <li>if {@code oldValue}, {@code value}, and {@code defaultValue} are all defined, 
     * {@code value} is returned.</li><ul></p>
     *  
     * 
     * @param oldValue the previous value of the entry, or null if it didn't 
     * exist
     * @return the working value for the entry, before updating
     */
    protected String getCurrentValue(String oldValue)
    {
        String ret = null;
        if (operation == Operation.SET)
        {
            // If only value is specified, the current value is set to it
            // regardless of its previous value.
            if (value != null && defaultValue == null)
            {
                ret = value;
            }
            // If the value did not exist in the configuration file, the value
            // is set to default, whether value is specified or not.
            else if (oldValue == null)
            {
            	return defaultValue;
            }
            else
            {
            	ret = (value == null ? oldValue : value);
            }
/*            else if (value == null && defaultValue != null)
            {
                // If only default is specified and the value previously
                // existed in the configuration file, it is unchanged.
            	if (oldValue != null){
                    ret = oldValue;            	
            	}
                // If only default is specified and the value did not
                // exist in the configuration file, the value is set to default.
            	else
            	{
                    ret = defaultValue;            		
            	}
            }
            else if (value != null && defaultValue != null)
            {
                // If value and default are both specified and the value
                // previously existed in the configuration file, the value
                // is set to value.
            	if (oldValue != null)
            	{
            		ret = value;
            	}
                // If value and default are both specified and the value
                // did not exist in the configuration file, the value is set
                // to default.
            	else
            	{
            		ret = defaultValue;
            	}
            }
            */
        }
        else
        {
            ret = (oldValue == null ? defaultValue : oldValue);
        }

        return ret;
    }
    
    public MultiMapConfigEntry clone()
    {
    	try {
			return (MultiMapConfigEntry) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("An internal error has occurred: MultiMapConfigEntry instance could not be cloned.", e);
		}
    }

    /**
     * Defines the operations that may be performed on an entry. 
     *
     */
    public enum Operation
    {
        INCREMENT("+"), DECREMENT("-"), SET("="), REMOVE("remove"), KEEP("keep");

        private static Map<String, MultiMapConfigEntry.Operation> lookup;

        private String attribute;

        Operation(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, MultiMapConfigEntry.Operation>();
            for (MultiMapConfigEntry.Operation operation : EnumSet.allOf(MultiMapConfigEntry.Operation.class))
            {
                lookup.put(operation.getAttribute(), operation);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static MultiMapConfigEntry.Operation getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

    /**
     * Defines the data types that an entry value may represent. 
     *
     */
    public enum Type
    {
        INTEGER("int"), DATE("date"), STRING("string");

        private static Map<String, MultiMapConfigEntry.Type> lookup;

        private String attribute;

        Type(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, MultiMapConfigEntry.Type>();
            for (MultiMapConfigEntry.Type type : EnumSet.allOf(MultiMapConfigEntry.Type.class))
            {
                lookup.put(type.getAttribute(), type);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static MultiMapConfigEntry.Type getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

    /**
     * Defines the types of lookup-by-value search that can be performed on an
     * entry. 
     * 
     */
    public enum LookupType
    {
        PLAIN("plain"), REGEXP("regexp");

        private static Map<String, MultiMapConfigEntry.LookupType> lookup;

        private String attribute;

        LookupType(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, MultiMapConfigEntry.LookupType>();
            for (MultiMapConfigEntry.LookupType type : EnumSet.allOf(MultiMapConfigEntry.LookupType.class))
            {
                lookup.put(type.getAttribute(), type);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static MultiMapConfigEntry.LookupType getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }
    
    /**
     * Defines the units by which a date-type entry may be counted. 
     *
     * @see Type#DATE
     */
    public enum Unit
    {
        MILLISECOND("millisecond"), SECOND("second"), MINUTE("minute"), HOUR("hour"),
        DAY("day"), WEEK("week"), MONTH("month"), YEAR("year");

        private static Map<String, Unit> lookup;
        private static Hashtable<Unit, Integer> calendarFields;

        private String attribute;

        Unit(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, Unit>();
            for (Unit unit : EnumSet.allOf(Unit.class))
            {
                lookup.put(unit.getAttribute(), unit);
            }
            calendarFields = new Hashtable<Unit, Integer>();
            calendarFields.put(MILLISECOND, new Integer(Calendar.MILLISECOND));
            calendarFields.put(SECOND, new Integer(Calendar.SECOND));
            calendarFields.put(MINUTE, new Integer(Calendar.MINUTE));
            calendarFields.put(HOUR, new Integer(Calendar.HOUR_OF_DAY));
            calendarFields.put(DAY, new Integer(Calendar.DATE));
            calendarFields.put(WEEK, new Integer(Calendar.WEEK_OF_YEAR));
            calendarFields.put(MONTH, new Integer(Calendar.MONTH));
            calendarFields.put(YEAR, new Integer(Calendar.YEAR));
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static Unit getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }

        public int getCalendarField()
        {
            return calendarFields.get(this);
        }

    }
}