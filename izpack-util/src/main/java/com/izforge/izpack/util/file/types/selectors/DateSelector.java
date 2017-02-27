/*
 * Copyright  2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file.types.selectors;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.file.types.Parameter;

/**
 * Selector that chooses files based on their last modified date.
 */
public class DateSelector extends BaseExtendSelector
{

    private long millis = -1;
    private String dateTime = null;
    private boolean includeDirs = false;
    private int granularity = 0;
    private TimeComparisons cmp = TimeComparisons.EQUAL;
    private String pattern;
    /**
     * Key to used for parameterized custom selector
     */
    public static final String MILLIS_KEY = "millis";
    /**
     * Key to used for parameterized custom selector
     */
    public static final String DATETIME_KEY = "datetime";
    /**
     * Key to used for parameterized custom selector
     */
    public static final String CHECKDIRS_KEY = "checkdirs";
    /**
     * Key to used for parameterized custom selector
     */
    public static final String GRANULARITY_KEY = "granularity";
    /**
     * Key to used for parameterized custom selector
     */
    public static final String WHEN_KEY = "when";
    /**
     * Key to used for parameterized custom selector
     */
    public static final String PATTERN_KEY = "pattern";

    /**
     * Creates a new <code>DateSelector</code> instance.
     */
    public DateSelector()
    {
        if (OsVersion.IS_WINDOWS)
        {
            granularity = 2000;
        }
    }

    /**
     * @return a string describing this object
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("{dateselector date: ");
        buf.append(dateTime);
        buf.append(" compare: ");
        buf.append(cmp.getAttribute());
        buf.append(" granularity: ");
        buf.append(granularity);
        if (pattern != null)
        {
            buf.append(" pattern: ").append(pattern);
        }
        buf.append("}");
        return buf.toString();
    }

    /**
     * For users that prefer to express time in milliseconds since 1970
     *
     * @param millis the time to compare file's last modified date to, expressed
     * in milliseconds
     */
    public void setMillis(long millis)
    {
        this.millis = millis;
    }

    /**
     * Returns the millisecond value the selector is set for.
     *
     * @return the millisecond value
     */
    public long getMillis() throws Exception
    {
        if (dateTime != null)
        {
            validate();
        }
        return millis;
    }

    /**
     * Sets the date. The user must supply it in MM/DD/YYYY HH:MM AM_PM format
     *
     * @param dateTime a string in MM/DD/YYYY HH:MM AM_PM format
     */
    public void setDatetime(String dateTime)
    {
        this.dateTime = dateTime;
    }

    /**
     * Should we be checking dates on directories?
     *
     * @param includeDirs whether to check the timestamp on directories
     */
    public void setCheckdirs(boolean includeDirs)
    {
        this.includeDirs = includeDirs;
    }

    /**
     * Sets the number of milliseconds leeway we will give before we consider a
     * file not to have matched a date.
     *
     * @param granularity the number of milliconds leeway
     */
    public void setGranularity(int granularity)
    {
        this.granularity = granularity;
    }

    /**
     * Sets the type of comparison to be done on the file's last modified date.
     *
     * @param cmp The comparison to perform, an EnumeratedAttribute
     */
    public void setWhen(TimeComparisons cmp)
    {
        this.cmp = cmp;
    }

    /**
     * Sets the pattern to be used for the SimpleDateFormat
     *
     * @param pattern the pattern that defines the date format
     */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    /**
     * When using this as a custom selector, this method will be called. It
     * translates each parameter into the appropriate setXXX() call.
     *
     * @param parameters the complete set of parameters for this selector
     */
    public void setParameters(Parameter[] parameters)
    {
        super.setParameters(parameters);
        if (parameters != null)
        {
            for (Parameter parameter : parameters)
            {
                String paramname = parameter.getName();
                if (MILLIS_KEY.equalsIgnoreCase(paramname))
                {
                    try
                    {
                        setMillis(new Long(parameter.getValue()
                        ).longValue());
                    }
                    catch (NumberFormatException nfe)
                    {
                        setError("Invalid millisecond setting "
                                + parameter.getValue());
                    }
                }
                else if (DATETIME_KEY.equalsIgnoreCase(paramname))
                {
                    setDatetime(parameter.getValue());
                }
                else if (CHECKDIRS_KEY.equalsIgnoreCase(paramname))
                {
                    setCheckdirs(Boolean.parseBoolean(parameter.getValue()));
                }
                else if (GRANULARITY_KEY.equalsIgnoreCase(paramname))
                {
                    try
                    {
                        setGranularity(new Integer(parameter.getValue()
                        ).intValue());
                    }
                    catch (NumberFormatException nfe)
                    {
                        setError("Invalid granularity setting "
                                + parameter.getValue());
                    }
                }
                else if (WHEN_KEY.equalsIgnoreCase(paramname))
                {
                    TimeComparisons cmp = TimeComparisons.getFromAttribute(parameter.getValue());
                    if (cmp != null)
                    {
                        setWhen(cmp);
                    }
                    else
                    {
                        setError("Invalid " + WHEN_KEY + " setting " + parameter.getValue());
                    }
                }
                else if (PATTERN_KEY.equalsIgnoreCase(paramname))
                {
                    setPattern(parameter.getValue());
                }
                else
                {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }

    /**
     * This is a consistency check to ensure the selector's required values have
     * been set.
     */
    public void verifySettings()
    {
        if (dateTime == null && millis < 0)
        {
            setError("You must provide a datetime or the number of "
                    + "milliseconds.");
        }
        else if (millis < 0 && dateTime != null)
        {
            // check millis and only set it once.
            DateFormat df = ((pattern == null)
                    ? DateFormat.getDateTimeInstance(
                            DateFormat.SHORT, DateFormat.SHORT, Locale.US)
                    : new SimpleDateFormat(pattern));

            try
            {
                setMillis(df.parse(dateTime).getTime());
                if (millis < 0)
                {
                    setError("Date of " + dateTime
                            + " results in negative milliseconds value"
                            + " relative to epoch (January 1, 1970, 00:00:00 GMT).");
                }
            }
            catch (ParseException pe)
            {
                setError("Date of " + dateTime
                        + " Cannot be parsed correctly. It should be in"
                        + ((pattern == null)
                                ? " MM/DD/YYYY HH:MM AM_PM" : pattern) + " format.");
            }
        }
    }

    /**
     * The heart of the matter. This is where the selector gets to decide on the
     * inclusion of a file in a particular fileset.
     *
     * @param basedir the base directory the scan is being done from
     * @param filename is the name of the file to check
     * @param file is a java.io.File object the selector can use
     * @return whether the file should be selected or not
     */
    public boolean isSelected(InstallData idata, File basedir, String filename, File file)
            throws Exception
    {

        validate();

        if (file.isDirectory() && (!includeDirs))
        {
            return true;
        }
        switch (cmp)
        {
            case BEFORE:
                return ((file.lastModified() - granularity) < millis);

            case AFTER:
                return ((file.lastModified() + granularity) > millis);

            default:
                return (Math.abs(file.lastModified() - millis) <= granularity);
        }
    }

    public enum TimeComparisons
    {
        BEFORE("before"), AFTER("after"), EQUAL("equal");

        private static Map<String, TimeComparisons> lookup;

        private String attribute;

        TimeComparisons(String attribute)
        {
            this.attribute = attribute;
        }

        static
        {
            lookup = new HashMap<String, TimeComparisons>();
            for (TimeComparisons mapperType : EnumSet.allOf(TimeComparisons.class))
            {
                lookup.put(mapperType.getAttribute(), mapperType);
            }
        }

        public String getAttribute()
        {
            return attribute;
        }

        public static TimeComparisons getFromAttribute(String attribute)
        {
            if (attribute != null && lookup.containsKey(attribute))
            {
                return lookup.get(attribute);
            }
            return null;
        }
    }

}
