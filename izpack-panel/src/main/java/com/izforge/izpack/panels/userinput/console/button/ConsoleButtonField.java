package com.izforge.izpack.panels.userinput.console.button;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.button.ButtonField;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of a button
 */
public class ConsoleButtonField  extends ConsoleField
{
    private final ButtonField field;
    /**
     * Constructs a {@code ConsoleField}.
     *
     * @param field   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleButtonField(ButtonField field, Console console, Prompt prompt)
    {
        super(field, console, prompt);
        this.field = field;
    }

    /**
     * Show button information.
     * If the user selects to press the button execute all its actions, unless one of the actions 'fail'.
     * Otherwise continue program as usual.
     * @return
     */
    @Override
    public boolean display()
    {
        boolean proceed = true;

        println(getField().getLabel());
        String value = getConsole().prompt(field.getButtonName() + " [y/n]: [n]", "n", "n");

        if(value.equalsIgnoreCase("y"))
        {
            for(ButtonAction buttonAction : field.getButtonActions())
            {
                proceed = buttonAction.execute(getConsole());
                if (!proceed)
                {
                    break;
                }
            }
            if(proceed)
            {
                println(field.getSucessMsg());
            }
        }
        return true;
    }
}
