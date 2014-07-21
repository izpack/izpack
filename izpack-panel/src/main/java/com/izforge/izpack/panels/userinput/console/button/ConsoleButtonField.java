package com.izforge.izpack.panels.userinput.console.button;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.button.ButtonField;
import com.izforge.izpack.util.Console;

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

    @Override
    public boolean display()
    {
        println(getField().getLabel());
        String value = getConsole().prompt(field.getButtonName() + " [y/n]: [n]", "n", "n");
        if(value.equalsIgnoreCase("y"))
        {

        }
        else
        {

        }
        return true;
    }
}
