package com.izforge.izpack.panels.userinput.gui.button;

import com.izforge.izpack.panels.userinput.field.button.ButtonField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;

public class GUIButtonField extends GUIField
{
    private final JButton button;
    /**
     * Constructs a {@code GUIField}.
     *
     * @param field the field
     */
    public GUIButtonField(ButtonField field)
    {
        super(field);
        button = new JButton(field.getButtonName());

        addField(button);
        addTooltip();
    }
}
