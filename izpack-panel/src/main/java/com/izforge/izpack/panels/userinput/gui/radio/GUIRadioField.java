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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

/**
 * Radio field view.
 *
 * @author Tim Anderson
 */
public class GUIRadioField extends GUIField implements ActionListener
{

    /**
     * The choices.
     */
    private final List<RadioChoiceView> choices = new ArrayList<RadioChoiceView>();

    private ButtonGroup buttonGroup;

    /**
     * Constructs a {@code GUIRadioField}.
     *
     * @param field the field
     */
    public GUIRadioField(RadioField field)
    {
        super(field);

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.indent = true;
        constraints.stretch = true;

        if (getField().getDescription() != null)
        {
            addDescription();
        }

        String variable = field.getVariable();
        buttonGroup = new ButtonGroup();

        int id = 1;

        for (Choice choice : field.getChoices())
        {
            JRadioButton button = new JRadioButton();
            button.setName(variable + "." + id);
            ++id;
            button.setText(choice.getValue());
            button.addActionListener(this);

            button.addItemListener(new ItemListener()
            {

                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        // Fire a change action if the arrow keys are used to select a radiobutton.
                        // This is important for condition-driven dynamic changes on the UserInputPanel,
                        // otherwise they happen just on mouse clicks.
                        ((JRadioButton) e.getSource()).doClick();
                    }
                }
            });

            buttonGroup.add(button);
            boolean selected = field.getSelectedIndex() == buttonGroup.getButtonCount() - 1;

            if (selected)
            {
                button.setSelected(true);
            }

            choices.add(new RadioChoiceView(choice, button));
            addComponent(button, constraints);
        }
        addTooltip();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        notifyUpdateListener();
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
     * @param skipValidation set to true when wanting to save field data without
     * validating
     * @return {@code true} if the field was updated, {@code false} if the view
     * is invalid
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
        refreshChoices();

        boolean result = super.updateView();
        RadioField field = getField();
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
        for (RadioChoiceView view : choices)
        {
            if (value.equals(view.choice.getTrueValue()))
            {
                view.button.setSelected(true);
                return true;
            }
            else
            {
                view.button.setSelected(false);
            }
        }
        return false;
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

        public Choice getChoice()
        {
            return choice;
        }

        public JRadioButton getButton()
        {
            return button;
        }
    }

    /**
     * Reassemble choices according to current conditions and processor results
     * when the panel changes
     */
    private void refreshChoices()
    {

        RadioField field = getField();

        int index = 0;
        for (RadioChoiceView radioChoiceView : choices)
        {
            String conditionId = radioChoiceView.getChoice().getConditionId();
            JRadioButton radioButton = radioChoiceView.getButton();
            if (conditionId == null || getInstallData().getRules().isConditionTrue(conditionId))
            {
                radioButton.setVisible(true);

                boolean selected = field.getSelectedIndex() == index;
                if (selected)
                {
                    radioButton.setSelected(true);
                }
            }
            else
            {
                radioButton.setVisible(false);
            }
            index++;
        }
    }

    @Override
    public JComponent getFirstFocusableComponent()
    {
        // Return first selected radiobutton
        for (RadioChoiceView radioChoiceView : choices)
        {
            JRadioButton radioButton = radioChoiceView.getButton();
            if (radioButton.isSelected())
            {
                return radioButton;
            }
        }
        // No choice selected
        if (choices.size() > 0)
        {
            return choices.get(0).getButton();
        }
        // No radiobutton at all
        return null;
    }
}
