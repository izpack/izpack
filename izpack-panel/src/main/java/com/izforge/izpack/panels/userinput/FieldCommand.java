package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.gui.GUIField;

/**
 * Wrapper to allow passing the create method from the FieldFactory classes.
 * This easily generate new fields, without cloning.
 * Cloning is not available with JComponent classes, and using serialization to clone can to unexpected error.
 */
public abstract class FieldCommand
{
    public GUIField createGuiField(Field field)
    {
        return null;
    }

    public ConsoleField createConsoleField(Field field)
    {
        return null;
    }
}
