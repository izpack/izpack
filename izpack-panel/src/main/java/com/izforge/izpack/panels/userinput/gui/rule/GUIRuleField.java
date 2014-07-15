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

package com.izforge.izpack.panels.userinput.gui.rule;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;


/**
 * Rule field view.
 *
 * @author Tim Anderson
 */
public class GUIRuleField extends GUIField
{

    /**
     * The component.
     */
    private final RuleInputField component;


    /**
     * Constructs a {@code GUIRuleField}.
     *
     * @param field the field
     */
    public GUIRuleField(RuleField field)
    {
        super(field);

        component = new RuleInputField(field);
        int id = 1;
        for (JTextField input : component.getInputFields())
        {
            input.setName(field.getVariable() + "." + id);
            ++id;
        }

        addField(component);
        addTooltip();
    }

    /**
     * Returns the text from the display, according to the field's formatting convention.
     *
     * @return the formatted text
     */
    public String getValue()
    {
        return component.getText();
    }

    /**
     * Returns each sub-field value.
     *
     * @return the sub-field values
     */
    public String[] getValues()
    {
        return component.getValues();
    }

    /**
     * Sets the sub-field values.
     *
     * @param values the sub-field values
     */
    public void setValues(String... values)
    {
        component.setValues(values);
    }

    /**
     * Updates the field from the view.
     *
     * @param prompt the prompt to display messages
     * @param skipValidation set to true when wanting to save field data without validating
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField(Prompt prompt, boolean skipValidation)
    {
        boolean result = false;
        Field field = getField();
        ValidationStatus status = field.validate(component.getValues());
        if (skipValidation || status.isValid())
        {
            field.setValue(component.getText());
            result = true;
        }
        else if (status.getMessage() != null)
        {
            prompt.warn(status.getMessage());
        }
        return result;
    }
}
