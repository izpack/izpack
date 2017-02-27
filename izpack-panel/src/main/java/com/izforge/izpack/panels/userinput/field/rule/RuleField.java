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

import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldProcessor;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;

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
    private String[] initialValues;

    /**
     * The default values.
     */
    private String[] defaultValues;

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RuleField.class.getName());

    /**
     * Constructs a {@code RuleField}.
     *
     * @param config the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public RuleField(RuleFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.layout = new FieldLayout(config.getLayout());
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
        String result = null;

        String value = super.getInitialValue();
        if (value != null)
        {
            ValidationStatus status = validateFormatted(value);
            this.initialValues = status.isValid() ? status.getValues() : null;
        }
        else
        {
            // Maybe an unresolved variable, for example <spec set="${abc}"/>
            this.initialValues = null;
        }

        if (!getInstallData().getVariables().isBlockedVariableName(getVariable()))
        {
            result = getInstallData().getVariables().replace(format(initialValues));
        }

        if (result == null)
        {
            result = getValue();
            if (result == null)
            {
                result = getDefaultValue();
            }
        }
        return result;
    }

    /**
     * Returns the default value of the field.
     *
     * @return the default value. May be {@code null}
     */
    @Override
    public String getDefaultValue()
    {
        String value = super.getDefaultValue();
        if (value != null)
        {
            ValidationStatus status = validateFormatted(value);
            this.defaultValues = status.isValid() ? status.getValues() : null;
        }
        else
        {
            // Maybe an unresolved variable, for example <spec default="${abc}"/>
            this.defaultValues = null;
        }

        if (defaultValues != null)
        {
            return getInstallData().getVariables().replace(format(defaultValues));
        }
        return null;
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

        if (values == null)
        {
            return null;
        }

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
     * Validates the field values against the field layout and any associated
     * validators.
     *
     * @param values the values to validate
     * @return the status of the validation
     */
    @Override
    public ValidationStatus validate(String... values)
    {
        String value = formatDisplay(values); // format the values into one long string, and validate it.
        ValidationStatus status = validateFormatted(value);
        if (status.isValid())
        {
            status = super.validate(value);
        }
        return status;
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
        return status;
    }

    /**
     * Specifies to return the contents of all fields together with all
     * separators as specified in the field format concatenated into one long
     * string. In this case the resulting string looks just like the user saw it
     * during data entry.
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
            LOGGER.warning("Rule field has " + format + " type, but no processor is registered");
            // fallback to display format
            result = formatDisplay(values);
        }
        return result;
    }
}
