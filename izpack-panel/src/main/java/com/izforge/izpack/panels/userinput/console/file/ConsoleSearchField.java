package com.izforge.izpack.panels.userinput.console.file;

import java.util.List;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.ConsoleChoiceField;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.util.Console;

/**
 * Based on {@link ConsoleChoiceField}
 */
public class ConsoleSearchField extends ConsoleField { // ConsoleComboField { //AbstractConsoleFileField {
	
	/**
     * Constructs a {@link ConsoleSearchField}.
     *
     * @param field   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleSearchField(SearchField field, Console console, Prompt prompt)
    {
        //super(new SearchFieldView(field, prompt), console, prompt);
    	super(field, console, prompt);
    }

    @Override
    public boolean display()
    {
    	SearchField field = (SearchField)getField();
        printDescription();

        List<String> choices = field.getChoices();
        listChoices(choices, field.getSelectedIndex());

        int selected = getConsole().prompt("input selection: ", 0, choices.size() - 1, field.getSelectedIndex(), -1);
        if (selected == -1)
        {
            return false;
        }
        field.setValue(choices.get(selected));
        return true;
    }
    
    /**
     * Displays the choices.
     *
     * @param choices  the choices
     * @param selected the selected choice, or {@code -1} if no choice is selected
     */
    protected void listChoices(List<String> choices, int selected)
    {
        for (int i = 0; i < choices.size(); ++i)
        {
            String choice = choices.get(i);
            println(i + "  [" + (i == selected ? "x" : " ") + "] " + choice);
        }
    }
}
