/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link FieldConfig} for testing purposes.
 *
 * @author Tim Anderson
 */
public class TestFieldConfig implements FieldConfig
{

    /**
     * The variable.
     */
    private String variable;

    /**
     * The default value.
     */
    private String defaultValue;

    /**
     * The label.
     */
    private String label;

    /**
     * The tooltip.
     */
    private String tooltip;

    /**
     * The validators.
     */
    private List<FieldValidator> validators = new ArrayList<FieldValidator>();


    /**
     * Constructs a {@code TestFieldConfig}.
     *
     * @param variable the variable
     */
    public TestFieldConfig(String variable)
    {
        this.variable = variable;
    }

    /**
     * Returns the variable that the field reads and updates.
     *
     * @return the 'variable' attribute, or {@code null} if the variable is optional but not present
     * @throws IzPackException if the 'variable' attribute is mandatory but not present
     */
    @Override
    public String getVariable()
    {
        return variable;
    }

    @Override
    public String getSummaryKey()
    {
        return null;
    }

    @Override
    public boolean getDisplayHidden()
    {
        return false;
    }

    /**
     * Returns the packs that this field applies to.
     *
     * @return the list of pack names
     */
    @Override
    public List<String> getPacks()
    {
        return Collections.emptyList();
    }

    /**
     * Returns the operating systems that this field applies to.
     *
     * @return the operating systems, or an empty list if the field applies to all operating systems
     */
    @Override
    public List<OsModel> getOsModels()
    {
        return Collections.emptyList();
    }

    /**
     * Returns the default value of the field.
     *
     * @return the default value. May be {@code null}
     */
    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the field size.
     *
     * @return the field size, or {@code -1} if no size is specified, or the specified size is invalid
     */
    @Override
    public int getSize()
    {
        return 0;
    }

    /**
     * Returns the validators for the field.
     *
     * @return the validators for the field
     */
    @Override
    public List<FieldValidator> getValidators()
    {
        return validators;
    }

    /**
     * Adds a validator.
     *
     * @param validator the validator to add
     */
    public void addValidator(FieldValidator validator)
    {
        validators.add(validator);
    }

    /**
     * Returns the processor the field.
     *
     * @return the field processor, or {@code null} if none exists
     */
    @Override
    public FieldProcessor getProcessor()
    {
        return null;
    }

    /**
     * Returns the field description.
     *
     * @return the field description. May be @{code null}
     */
    @Override
    public String getDescription()
    {
        return null;
    }

    /**
     * Returns the field label.
     *
     * @return the field label. May be {@code null}
     */
    @Override
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns the field tooltip.
     *
     * @return the field tooltip. Maybe {@code null}
     */
    @Override
    public String getTooltip() { return tooltip; }

    /**
     * Sets the field label.
     *
     * @param label the label. May be {@code null}
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * Returns the condition that determines if the field is displayed or not.
     *
     * @return the condition. May be {@code null}
     */
    @Override
    public String getCondition()
    {
        return null;
    }
}
