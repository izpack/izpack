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
package com.izforge.izpack.panels.userinput.gui.combo;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

/**
 * Combo field view.
 *
 * @author Tim Anderson
 */
public class GUIComboField extends GUIField
{

    /**
     * The combo.
     */
    private final JComboBox combo;

    /**
     * Constructs a {@code GUIComboField}.
     *
     * @param field the field
     */
    public GUIComboField(ComboField field)
    {
        super(field);
        combo = new JComboBox();
        combo.setName(field.getVariable());
        for (Choice choice : field.getChoices())
        {
            combo.addItem(choice);
        }
        combo.setSelectedIndex(field.getSelectedIndex());
        combo.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                notifyUpdateListener();
            }
        });

        addField(combo);
        addTooltip();
    }

    /**
     * Updates the field from the view.
     *
     * @param prompt the prompt to display messages
     * @param skipValidation set to true when wanting to save field data without
     * validating
     * @return {@code true} if the field was updated, {@code false} if the view
     * is invalid
     */
    @Override
    public boolean updateField(Prompt prompt, boolean skipValidation)
    {
        Choice selected = (Choice) combo.getSelectedItem();
        String value = (selected != null) ? selected.getKey() : null;
        getField().setValue(value);
        return true;
    }

    /**
     * Updates the view from the field.
     *
     * @return {@code true} if the view was updated
     */
    @Override
    public boolean updateView()
    {
        refreshChoices();

        boolean result = super.updateView();
        ComboField field = (ComboField) getField();
        String value = field.getInitialValue();

        if (value != null)
        {
            result = splitValue(value);
        }

        if (!result) // fallback for invalid values
        {
            // Set default value here for getting current variable values replaced
            String defaultValue = field.getDefaultValue();
            if (defaultValue != null)
            {
                result = splitValue(defaultValue);
            }
        }
        return result;
    }

    private boolean splitValue(String value)
    {
        for (int i = 0; i < combo.getItemCount(); i++)
        {
            Choice item = (Choice) combo.getItemAt(i);
            if (value.equals(item.getTrueValue()))
            {
                combo.setSelectedIndex(i);
                notifyUpdateListener();
                return true;
            }
        }
        return false;
    }

    /**
     * Reassemble choices according to current conditions and processor results
     * when the panel changes
     */
    private void refreshChoices()
    {
        ComboField field = (ComboField) getField();
        combo.removeAllItems();
        int index = 0;
        for (Choice choice : field.getChoices())
        {
            String conditionId = choice.getConditionId();
            if (conditionId == null || getInstallData().getRules().isConditionTrue(conditionId))
            {
                combo.addItem(choice);
            }

            boolean selected = field.getSelectedIndex() == index;
            if (selected)
            {
                combo.setSelectedItem(choice);
            }
            index++;
        }
    }

    @Override
    public JComponent getFirstFocusableComponent()
    {
        return combo;
    }
}
