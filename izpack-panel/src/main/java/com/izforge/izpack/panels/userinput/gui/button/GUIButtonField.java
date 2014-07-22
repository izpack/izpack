package com.izforge.izpack.panels.userinput.gui.button;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.GUIPrompt;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.panels.userinput.field.button.ButtonField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GUIButtonField extends GUIField implements ActionListener
{
    private final Prompt prompt;
    private final JButton button;
    private final String successMsg;
    private final List<ButtonAction> buttonActions;

    /**
     * Constructs a {@code GUIField}.
     *
     * @param field the field
     */
    public GUIButtonField(ButtonField field)
    {
        super(field);
        this.prompt = new GUIPrompt();
        this.successMsg = field.getSucessMsg();
        buttonActions = field.getButtonActions();
        button = new JButton(field.getButtonName());
        button.addActionListener(this);
        addField(button);
        addTooltip();
    }

    public void actionPerformed(ActionEvent e)
    {
        boolean proceed = true;
        for(ButtonAction buttonAction : buttonActions)
        {
            proceed = buttonAction.execute(prompt);
            if(!proceed)
            {
                break;
            }
        }
        if (proceed)
        {
            prompt.message(Prompt.Type.INFORMATION, successMsg);
        }
    }
}
