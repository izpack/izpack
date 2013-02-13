/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.panels.userinput.field.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldProcessor;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;


/**
 * Rule field.
 *
 * @author Tim Anderson
 */
public class RuleField extends Field
{
    /**
     * The rule field layout.
     */
    private final FieldLayout layout;

    /**
     * The rule format.
     */
    private final RuleFormat format;

    /**
     * The field separator.
     */
    private final String separator;

    /**
     * The initial values.
     */
    private final String[] defaultValues;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(RuleField.class.getName());


    /**
     * Constructs a {@code RuleField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @param factory     the factory for creating {@link Processor} instances
     * @throws IzPackException if the field cannot be read
     */
    public RuleField(RuleFieldConfig config, InstallData installData, ObjectFactory factory)
    {
        super(config, installData);
        this.layout = new FieldLayout(config.getLayout());
        this.defaultValues = parseSet(getSet(), factory);
        this.format = config.getFormat();
        this.separator = config.getSeparator();
    }

    /**
     * Returns the field layout.
     *
     * @return the field layout
     */
    public FieldLayout getLayout()
    {
        return layout;
    }

    /**
     * Returns the initial value of the field.
     *
     * @return the initial value. May be {@code null}
     */
    @Override
    public String getInitialValue()
    {
        if (hasDefaultValues())
        {
            return format(getDefaultValues());
        }
        return null;
    }

    /**
     * Determines if the field has default values.
     * <p/>
     * It has default values if at least one sub-field has a value
     *
     * @return {@code true} if the field has default values
     */
    public boolean hasDefaultValues()
    {
        for (String value : defaultValues)
        {
            if (value != null && !value.equals(""))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the default values for each sub-field.
     *
     * @return the default values
     */
    public String[] getDefaultValues()
    {
        return defaultValues;
    }

    /**
     * Formats the values according to the field format.
     *
     * @param values the values to format
     * @return the formatted values
     * @throws IzPackException if formatting fails
     */
    public String format(String[] values)
    {
        String result;

        switch (format)
        {
            case PLAIN_STRING:
                result = formatPlain(values);
                break;
            case SPECIAL_SEPARATOR:
                result = formatSpecialSeparator(values);
                break;
            case PROCESSED:
                result = formatProcessed(values);
                break;
            default:
                result = formatDisplay(values);
        }

        return result;
    }

    /**
     * Validates the field values against the field layout and any associated validators.
     *
     * @param values the values to validate
     * @return the status of the validation
     */
    @Override
    public ValidationStatus validate(String... values)
    {
        String value = formatDisplay(values); // format the values into one long string, and validate it.
        return validateFormatted(value);
    }

    /**
     * Validates a formatted value.
     *
     * @param value the value to validate
     * @return the status of the validation
     */
    public ValidationStatus validateFormatted(String value)
    {
        ValidationStatus status = layout.validate(value);
        if (status.isValid())
        {
            status = super.validate(status.getValues());
        }
        return status;
    }

    /**
     * Specifies to return the contents of all fields together with all separators as specified in the field format
     * concatenated into one long string.
     * In this case the resulting string looks just like the user saw it during data entry.
     *
     * @param values the values to format
     * @return the formatted string
     */
    private String formatDisplay(String[] values)
    {
        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Object item : layout.getLayout())
        {
            if (item instanceof String)
            {
                result.append(item);
            }
            else
            {
                if (index < values.length)
                {
                    result.append(values[index]);
                    ++index;
                }
            }
        }
        return result.toString();
    }

    /**
     * Plain formatting. Concatenates the values with no separators.
     *
     * @param values the values to format
     * @return the formatted string
     */
    private String formatPlain(String[] values)
    {
        StringBuilder result = new StringBuilder();
        for (String value : values)
        {
            result.append(value);
        }
        return result.toString();
    }

    /**
     * Concatenates the values with the separator in between.
     *
     * @param values the values to format
     * @return the formatted string
     */
    private String formatSpecialSeparator(String[] values)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; ++i)
        {
            if (i > 0)
            {
                result.append(separator);
            }
            result.append(values[i]);
        }
        return result.toString();
    }

    /**
     * Formats values using an {@link FieldProcessor}.
     *
     * @param values the values to format
     * @return the formatted string
     * @throws IzPackException if formatting fails
     */
    private String formatProcessed(String[] values)
    {
        String result;
        FieldProcessor processor = getProcessor();
        if (processor != null)
        {
            result = processor.process(values);
        }
        else
        {
            logger.warning("Rule field has " + format + " type, but no processor is registered");
            // fallback to display format
            result = formatDisplay(values);
        }
        return result;
    }

    /**
     * Parses default values.
     *
     * @param set     the value of the 'set' attribute
     * @param factory the factory for creating {@link Processor} instances
     * @return the default values for each field
     */
    private String[] parseSet(String set, ObjectFactory factory)
    {
        StringTokenizer tokenizer = new StringTokenizer(set);
        String[] result = new String[layout.getFieldSpecs().size()];

        List<String> processors = new ArrayList<String>();

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            String[] values = token.split(":");
            if (values.length > 1 && values.length <= 3)
            {
                try
                {
                    int index = Integer.parseInt(values[0]);
                    if (index >= 0 && index < result.length)
                    {
                        result[index] = replaceVariables(values[1]);
                        String className = (values.length == 3) ? values[2] : null;
                        if (className != null && !className.equals(""))
                        {
                            processors.add(className);
                        }
                    }
                    else
                    {
                        logger.warning("Field index: " + index + " in '" + token + "' in 'set' attribute: '"
                                               + set + "' not in the range [0.." + result.length + "]");
                    }
                }
                catch (NumberFormatException exception)
                {
                    logger.warning("Non-numeric field index: " + values[0] + " in '" + token + "' in 'set' attribute:"
                                           + " '" + set + "'");
                }
            }
            else
            {
                logger.warning("Expected 2..3 fields in '" + token + "' in 'set' attribute: '" + set + "' but got "
                                       + values.length);
            }
        }

        if (!processors.isEmpty())
        {
            result = processValues(result, processors, factory);
        }
        return result;
    }

    /**
     * Processes values using a list of {@link Processor}s specified by their class names.
     *
     * @param values     the values to process. One for each field
     * @param processors the processor class names
     * @param factory    the factory for creating processors
     * @return the processed values, one for each field
     */
    private String[] processValues(String[] values, List<String> processors, ObjectFactory factory)
    {
        String[] result = values;
        for (String className : processors)
        {
            Processor processor = factory.create(className, Processor.class);
            String processed = processor.process(new ValuesProcessingClient(values));
            if (processed != null)
            {
                values = processed.split(" ");
                if (values.length == result.length)
                {
                    result = values;
                }
                else
                {
                    logger.warning("Cannot use result of processor: " + processor.getClass().getName() + ". Expected "
                                           + result.length + " fields but got " + values.length);
                }
            }
            else
            {
                logger.warning("Processor: " + processor.getClass().getName() + " returned null. Expected "
                                       + result.length + " fields");
            }
        }
        return result;
    }

}