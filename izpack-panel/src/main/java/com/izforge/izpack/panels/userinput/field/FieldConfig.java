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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;

import java.util.List;

/**
 * User-input field configuration.
 *
 * @author Tim Anderson
 */
public interface FieldConfig
{

    /**
     * Returns the variable that the field reads and updates.
     *
     * @return the 'variable' attribute, or {@code null} if the variable is optional but not present
     * @throws IzPackException if the 'variable' attribute is mandatory but not present
     */
    String getVariable();

    /**
     * Returns the summary key associated with the field.
     *
     * @return the 'summaryKey' attribute, or {@code null} if the summary attribute is not present
     */
    String getSummaryKey();

    /**
     * Returns if the field should always be displayed on the panel regardless if its conditionid is true or false.
     * If the conditionid is false, display the field but disable it.
     * <p/>
     *
     * @return the 'displayHidden' attribute, or {@code null}
     */
    boolean getDisplayHidden();

    /**
     * Returns the packs that this field applies to.
     *
     * @return the list of pack names
     */
    List<String> getPacks();

    /**
     * Returns the operating systems that this field applies to.
     *
     * @return the operating systems, or an empty list if the field applies to all operating systems
     */
    List<OsModel> getOsModels();

    /**
     * Returns the default value of the field.
     *
     * @return the default value. May be {@code null}
     */
    String getDefaultValue();

    /**
     * Returns the field size.
     *
     * @return the field size, or {@code -1} if no size is specified, or the specified size is invalid
     */
    int getSize();

    /**
     * Returns the validators for the field.
     *
     * @return the validators for the field
     */
    List<FieldValidator> getValidators();


    /**
     * Returns the validators for the given field.
     *
     * @param field
     * @return the validators for the given field
     */
    List<FieldValidator> getValidators(IXMLElement field);

    /**
     * Returns the processor the field.
     *
     * @return the field processor, or {@code null} if none exists
     */
    FieldProcessor getProcessor();

    /**
     * Returns the field description.
     *
     * @return the field description. May be @{code null}
     */
    String getDescription();

    /**
     * Returns the field's tooltip.
     *
     * @return the field tooltip. May be @{code null}
     */
    String getTooltip();

    /**
     * Returns the field label.
     *
     * @return the field label. May be {@code null}
     */
    String getLabel();

    /**
     * Returns the condition that determines if the field is displayed or not.
     *
     * @return the condition. May be {@code null}
     */
    String getCondition();

}
