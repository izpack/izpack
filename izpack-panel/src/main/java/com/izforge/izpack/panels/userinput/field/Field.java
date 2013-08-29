/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.field;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.process.ExistsCondition;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

/**
 * Describes a user input field.
 *
 * @author Tim Anderson
 */
public abstract class Field
{

    /**
     * The variable. May be {@code null}.
     */
    private final String variable;

    /**
     * Specifies the default (or set) value for the field.
     */
    private final String set;

    /**
     * The field size.
     */
    private final int size;

    /**
     * The packs that the field applies to. May be {@code null} or empty to indicate all packs.
     */
    private final List<String> packs;

    /**
     * The the operating systems that the field applies to. An empty list indicates it applies to all operating systems
     */
    private final List<OsModel> models;

    /**
     * The field validators.
     */
    private final List<FieldValidator> validators;

    /**
     * The field processor. May be {@code null}
     */
    private final FieldProcessor processor;

    /**
     * The field label. May be {@code null}
     */
    private final String label;

    /**
     * The field description. May be {@code null}
     */
    private final String description;

    /**
     * Determines if field updates trigger re-validation.
     */
    private final boolean revalidate;

    /**
     * Condition that determines if the field is displayed or not.
     */
    private final String condition;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Field.class.getName());

    /**
     * Constructs a {@code Field}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the configuration is invalid
     */
    public Field(FieldConfig config, InstallData installData)
    {
        variable = config.getVariable();
        set = config.getDefaultValue();
        size = config.getSize();
        packs = config.getPacks();
        models = config.getOsModels();
        validators = config.getValidators();
        processor = config.getProcessor();
        label = config.getLabel();
        description = config.getDescription();
        this.revalidate = config.getRevalidate();
        this.condition = config.getCondition();
        this.installData = installData;

        if (variable != null)
        {
            addExistsCondition();
        }
    }

    /**
     * Returns the variable.
     *
     * @return the variable. May be {@code null}
     */
    public String getVariable()
    {
        return variable;
    }

    /**
     * Returns all variables that this field updates.
     *
     * @return all variables that this field updates
     */
    public List<String> getVariables()
    {
        return variable != null ? Arrays.asList(variable) : Collections.<String>emptyList();
    }

    /**
     * Returns the packs that the field applies to.
     *
     * @return the pack names
     */
    public List<String> getPacks()
    {
        return packs;
    }

    /**
     * Returns the operating systems that the field applies to.
     *
     * @return the OS family names
     */
    public List<OsModel> getOsModels()
    {
        return models;
    }

    /**
     * Returns the default value of the field.
     *
     * @return the default value. May be {@code null}
     */
    public String getDefaultValue()
    {
        if (set != null)
        {
            return installData.getVariables().replace(set);
        }
        return null;
    }

    /**
     * Returns the initial value.
     * <p/>
     * If the field is associated with a variable, and the variable value is non-null, this is returned, otherwise
     * {@link #getDefaultValue} is returned.
     *
     * @return the initial value
     */
    public String getInitialValue()
    {
        String result = getValue();
        if (result == null)
        {
            result = getDefaultValue();
        }
        return result;
    }

    /**
     * Returns the variable value.
     *
     * @return the variable value. May be {@code null}
     */
    public String getValue()
    {
        return installData.getVariable(variable);
    }

    /**
     * Sets the variable value.
     *
     * @param value the variable value. May be {@code null}
     */
    public void setValue(String value)
    {
        value = process(value);
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Field setting variable=" + variable + " to value=" + value);
        }
        installData.setVariable(variable, value);
    }

    /**
     * Returns the field size.
     *
     * @return the field size, or {@code -1} if no size is defined
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Validates values using any validators associated with the field.
     *
     * @param values the values to validate
     * @return the status of the validation
     */
    public ValidationStatus validate(String... values)
    {
        return validate(new ValuesProcessingClient(values));
    }

    /**
     * Validates values using any validators associated with the field.
     *
     * @param format how the values should be formatted into one text
     * @param values the values to validate
     * @return the status of the validation
     */
    public ValidationStatus validate(MessageFormat format, String... values)
    {
        return validate(new ValuesProcessingClient(format, values));
    }

    /**
     * Validates values using any validators associated with the field.
     *
     * @param values the values to validate
     * @return the status of the validation
     */
    public ValidationStatus validate(ValuesProcessingClient values)
    {
        try
        {
            for (FieldValidator validator : validators)
            {
                if (!validator.validate(values))
                {
                    return ValidationStatus.failed(validator.getMessage());
                }
            }
        }
        catch (Throwable exception)
        {
            return ValidationStatus.failed(exception.getMessage());
        }
        return ValidationStatus.success(values.getValues());
    }

    /**
     * Processes a set of values.
     *
     * @param values the values to process
     * @return the result of processing
     * @throws IzPackException if processing fails
     */
    public String process(String... values)
    {
        String result = null;
        if (processor != null)
        {
            result = processor.process(values);
        }
        else if (values.length > 0)
        {
            result = values[0];
        }
        return result;
    }

    /**
     * Returns the field processor.
     *
     * @return the field processor. May be {@code null}
     */
    public FieldProcessor getProcessor()
    {
        return processor;
    }

    /**
     * Returns the field label.
     *
     * @return the field label. May be {@code null}
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns the field description.
     *
     * @return the field description. May be {@code null}
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Determines if the field triggers revalidation on update.
     *
     * @return {@code true} if the field triggers revalidation
     */
    public boolean getRevalidate()
    {
        return revalidate;
    }

    /**
     * Determines if the condition associated with the field is true.
     *
     * @return {@code true} if the condition evaluates {true} or if the field has no condition
     */
    public boolean isConditionTrue()
    {
        RulesEngine rules = getRules();
        return (condition == null || rules.isConditionTrue(condition, installData));
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    public InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the raw value of the 'set' attribute.
     *
     * @return the raw value of the 'set' attribute
     */
    protected String getSet()
    {
        return set;
    }

    /**
     * Returns the rules.
     *
     * @return the rules
     */
    protected RulesEngine getRules()
    {
        return installData.getRules();
    }

    /**
     * Replaces any variables in the supplied value.
     *
     * @param value the value
     * @return the value with variables replaced
     */
    protected String replaceVariables(String value)
    {
        return installData.getVariables().replace(value);
    }

    /**
     * Adds an 'exists' condition for the variable.
     */
    private void addExistsCondition()
    {
        RulesEngine rules = getRules();
        final String conditionId = "izpack.input." + variable;
        if (rules != null)
        {
            if (rules.getCondition(conditionId) == null)
            {
                ExistsCondition existsCondition = new ExistsCondition();
                existsCondition.setContentType(ExistsCondition.ContentType.VARIABLE);
                existsCondition.setContent(variable);
                existsCondition.setId(conditionId);
                existsCondition.setInstallData(installData);
                rules.addCondition(existsCondition);
            }
            else
            {
                logger.fine("Condition '" + conditionId + "' for variable '" + variable + "' already exists");
            }
        }
        else
        {
            logger.fine("Cannot add  condition '" + conditionId + "' for variable '" + variable + "'. Rules not supplied");
        }
    }

}
