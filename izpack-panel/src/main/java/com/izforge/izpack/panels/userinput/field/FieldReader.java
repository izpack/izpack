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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;

import java.util.ArrayList;
import java.util.List;

/**
 * Field reader.
 *
 * @author Tim Anderson
 */
public class FieldReader extends ElementReader implements FieldConfig
{
    /**
     * The field element.
     */
    private final IXMLElement field;

    /**
     * The field specification. May be {@code null}.
     */
    private final IXMLElement spec;

    /**
     * Variable attribute name.
     */
    protected static final String VARIABLE = "variable";

    /**
     * Variable attribute name.
     */
    protected static final String SUMMARY_KEY = "summaryKey";


    /**
     * Variable attribute name.
     */
    protected static final String DISPLAY_HIDDEN = "displayHidden";


    /**
     * Text size attribute name.
     */
    private static final String TEXT_SIZE = "size";

    /**
     * The field specification element name.
     */
    protected static final String SPEC = "spec";

    /**
     * The validator element name.
     */
    private static final String VALIDATOR = "validator";

    /**
     * The tooltip attribute name.
     */
    private static final String TOOLTIP = "tooltip";

    /**
     * Constructs a {@code FieldReader}.
     *
     * @param field  the field element to read
     * @param config the configuration
     */
    public FieldReader(IXMLElement field, Config config)
    {
        super(config);
        this.field = field;
        this.spec = getSpec(field, config);
    }

    /**
     * Returns the 'field' element.
     *
     * @return the 'field' element
     */
    public IXMLElement getField()
    {
        return field;
    }

    /**
     * Returns the 'spec' element.
     *
     * @return the 'spec' element, or {@code null} if none is present
     */
    public IXMLElement getSpec()
    {
        return spec;
    }

    /**
     * Returns the variable that the field reads and updates.
     * <p/>
     * This implementation throws an exception if the variable is not present; subclasses override and return
     * {@code null} if the variable is optional.
     *
     * @return the 'variable' attribute, or {@code null} if the variable is optional but not present
     * @throws IzPackException if the 'variable' attribute is mandatory but not present
     */
    @Override
    public String getVariable()
    {
        return getConfig().getAttribute(getField(), VARIABLE);
    }

    /**
     * Returns the summaryKey that the field is associated with.
     * <p/>
     *
     * @return the 'summaryKey' attribute, or {@code null}
     */
    @Override
    public String getSummaryKey()
    {
        boolean optional = true;
        return getConfig().getAttribute(getField(), SUMMARY_KEY, optional);
    }

    /**
     * Returns if the field should always be displayed on the panel regardless if its conditionid is true or false.
     * If the conditionid is false, display the field but disable it.
     * <p/>
     *
     * @return the 'displayHidden' attribute, or {@code null}
     */
    @Override
    public boolean getDisplayHidden()
    {
        boolean displayHidden = false;
        boolean optional = true;
        String displayHiddenValue = getConfig().getAttribute(getField(), DISPLAY_HIDDEN, optional);
        try
        {
            displayHidden = Boolean.parseBoolean(displayHiddenValue);
            return displayHidden;
        }
        catch(Exception ignore)
        {
            return false;
        }
    }

    /**
     * Returns the packs that this field applies to.
     *
     * @return the list of pack names
     */
    @Override
    public List<String> getPacks()
    {
        return getPacks(field);
    }

    /**
     * Returns the operating systems that this field applies to.
     *
     * @return the operating systems, or an empty list if the field applies to all operating systems
     */
    @Override
    public List<OsModel> getOsModels()
    {
        return getOsModels(field);
    }

    /**
     * Returns the default value of the field without replacing variables.
     * <p/>
     * This is obtained from the 'set' attribute of the 'spec' element.
     *
     * @return the default value. May be {@code null}
     */
    @Override
    public String getDefaultValue()
    {
        return (spec != null) ? getConfig().getRawString(spec, "set", null) : null;
    }

    /**
     * Returns the field size.
     *
     * @return the field size, or {@code -1} if no size is specified, or the specified size is invalid
     */
    @Override
    public int getSize()
    {
        int result = -1;
        if (spec != null)
        {
            result = getConfig().getInt(spec, TEXT_SIZE, result);
        }
        return result;
    }

    /**
     * Returns the validators for the field.
     *
     * @return the validators for the field
     */
    @Override
    public List<FieldValidator> getValidators(IXMLElement field)
    {
        List<FieldValidator> result = new ArrayList<FieldValidator>();
        Config config = getConfig();
        for (IXMLElement element : field.getChildrenNamed(VALIDATOR))
        {
            FieldValidatorReader reader = new FieldValidatorReader(element, config);
            result.add(new FieldValidator(reader.getClassName(), reader.getParameters(), reader.getMessage(),
                                          config.getFactory()));
        }
        return result;
    }

    @Override
    public List<FieldValidator> getValidators()
    {
        return getValidators(this.field);
    }
    /**
     * Returns the processor the field.
     *
     * @return the field processor, or {@code null} if none exists
     */
    @Override
    public FieldProcessor getProcessor()
    {
        IXMLElement element = (spec != null) ? spec.getFirstChildNamed("processor") : null;
        return element != null ? new FieldProcessor(element, getConfig()) : null;
    }

    /**
     * Returns the field description.
     *
     * @return the field description. May be @{code null}
     */
    @Override
    public String getDescription()
    {
        return getText(field.getFirstChildNamed("description"));
    }

    /**
     * Returns the field's tooltip.
     *
     * @return the field tooltip. May be @{code null}
     */
    @Override
    public String getTooltip() {
        return getConfig().getAttribute(field, TOOLTIP, true);
    }

    /**
     * Returns the field label.
     *
     * @return the field label. May be {@code null}
     */
    @Override
    public String getLabel()
    {
        return getText(getSpec());
    }

    /**
     * Returns the condition that determines if the field is displayed or not.
     *
     * @return the condition. May be {@code null}
     */
    @Override
    public String getCondition()
    {
        return getConfig().getString(getField(), "conditionid", null);
    }

    /**
     * Returns the 'spec' element.
     *
     * @param field  the parent field element
     * @param config the configuration
     * @return the 'spec' element, or {@code null} if the spec element is optional and not found
     * @throws IzPackException if the 'spec' element is mandatory but not present
     */
    protected IXMLElement getSpec(IXMLElement field, Config config)
    {
        return config.getElement(field, SPEC);
    }

    /**
     * Extracts the text from an element. The text can be defined:
     * <ol>
     * <li>in the locale's messages, under the key defined by the {@code id} attribute; or
     * <li>as value of the attribute {@code txt}.
     * </ol>
     *
     * @param element the element from which to extract the text
     * @return the text, or {@code null} if none can be found
     */
    protected String getText(IXMLElement element)
    {
        return getConfig().getText(element);
    }

}
