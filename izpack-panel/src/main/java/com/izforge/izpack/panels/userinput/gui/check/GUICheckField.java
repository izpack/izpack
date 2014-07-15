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

package com.izforge.izpack.panels.userinput.gui.check;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * GUI check field.
 *
 * @author Tim Anderson
 */
public class GUICheckField extends GUIField
{
    /**
     * The checkbox.
     */
    private final JCheckBox checkbox;


    /**
     * Constructs a {@code GUICheckField}.
     *
     * @param field the field
     */
    public GUICheckField(CheckField field)
    {
        super(field);
        checkbox = new JCheckBox(field.getLabel());
        checkbox.setName(field.getVariable());

        checkbox.setSelected(field.getInitialSelection());
        checkbox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    notifyUpdateListener();
                }
            });

        addDescription();

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.stretch = true;
        constraints.indent = true;

        addComponent(checkbox, constraints);
        addTooltip();
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public CheckField getField()
    {
        return (CheckField) super.getField();
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
        CheckField field = getField();
        if (checkbox.isSelected())
        {
            field.setValue(field.getTrueValue());
        }
        else
        {
            field.setValue(field.getFalseValue());
        }
        return true;
    }
}
