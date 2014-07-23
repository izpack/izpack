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

package com.izforge.izpack.panels.userinput.gui.radio;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Radio field view.
 *
 * @author Tim Anderson
 */
public class GUIRadioField extends GUIField
{
    /**
     * The choices.
     */
    private final List<RadioChoiceView> choices = new ArrayList<RadioChoiceView>();

    /**
     * Constructs a {@code GUIRadioField}.
     *
     * @param field       the field
     */
    public GUIRadioField(RadioField field)
    {
        super(field);
        String variable = field.getVariable();

        ButtonGroup group = new ButtonGroup();

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.indent = true;
        constraints.stretch = true;


        int id = 1;
        ActionListener l = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                notifyUpdateListener();
            }
        };

        if (getField().getDescription() != null)
        {
            addDescription();
        }

        for (Choice choice : field.getChoices())
        {
            JRadioButton button = new JRadioButton();
            button.setName(variable + "." + id);
            ++id;
            button.setText(choice.getValue());
            button.addActionListener(l);

            group.add(button);
            boolean selected = field.getSelectedIndex() == group.getButtonCount() - 1;

            if (selected)
            {
                button.setSelected(true);
            }

            choices.add(new RadioChoiceView(choice, button));
            addComponent(button, constraints);
        }
        addTooltip();
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public RadioField getField()
    {
        return (RadioField) super.getField();
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
        for (RadioChoiceView view : choices)
        {
            if (view.button.isSelected())
            {
                RadioField field = getField();
                field.setValue(view.choice.getTrueValue());
                break;
            }
        }
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
        boolean result = false;
        RadioField field = getField();
        String value = field.getValue();

        if (value != null)
        {
            splitValue(value);
            result = true;
        }
        else
        {
            // Set default value here for getting current variable values replaced
            Field f = getField();
            String defaultValue = f.getDefaultValue();
            if (defaultValue != null)
            {
                splitValue(defaultValue);
            }
        }
        return result;
    }

    private void splitValue(String value)
    {
        for (RadioChoiceView view : choices)
        {
            if (value.equals(view.choice.getTrueValue()))
            {
                view.button.setSelected(true);
            }
            else
            {
                view.button.setSelected(false);
            }
        }
    }

    /**
     * Associates a choice with its button.
     */
    private class RadioChoiceView
    {
        private Choice choice;

        private JRadioButton button;

        public RadioChoiceView(Choice choice, JRadioButton button)
        {
            this.choice = choice;
            this.button = button;
        }
    }

}
