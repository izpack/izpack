package com.izforge.izpack.panels.userinput.console.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.custom.Column;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.field.custom.CustomFieldType;
import com.izforge.izpack.util.Console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleCustomField extends ConsoleField implements CustomFieldType
{
    private final UserInputPanelSpec userInputPanelSpec;

    private final IXMLElement spec;

    private final FieldCommand createField;

    private int numberOfRows = 0;

    private int numberOfColumns = 0;

    private final CustomField customInfoField;

    private final int maxRow;

    private final int minRow;

    private final static int INVALID = -1;

    private final static int CONTINUE = 1;

    private final static int ADD_MODULE = 2;

    private final static int REDISPLAY = 3;

    Map<Integer, List<ConsoleField>> consoleFields;

    /**
     * Constructs a {@code ConsoleField}.
     *
     * @param customField the field
     * @param console     the console
     * @param prompt      the prompt
     */
    public ConsoleCustomField(CustomField customField, Console console, Prompt prompt,
                              FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        super(customField, console, prompt);
        this.spec = spec;
        this.userInputPanelSpec = userInputPanelSpec;
        this.createField = createField;
        this.customInfoField = customField;
        this.maxRow = customField.getMaxRow();
        this.minRow = customField.getMinRow();
        this.numberOfColumns = customField.getFields().size();
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    @Override
    public CustomField getField()
    {
        return (CustomField) super.getField();
    }

    /**
     * Ensure to display the minimum amount of rows required.
     */
    private void addInitialRows()
    {
        for (int count = minRow; count > 1; count--)
        {
            addRow(true);
        }
    }

    /**
     * Display the fields within the row to the console.
     * At the end prompt to add another module, redisplay the module, or continue with the installation.
     *
     * @param initial If it is part of the initial rows then do no prompt to add another module
     *                As there must be at least one more module to follow.
     * @return true if the user requested another row, false if the user wants to continue with the installation
     */
    public boolean addRow(boolean initial)
    {
        numberOfRows++;
        boolean onModule = true;
        List<ConsoleField> fields = new ArrayList<ConsoleField>();

        for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
        {
            field.setVariable(field.getVariable() + "." + numberOfRows);
            ConsoleField consoleField = createField.createConsoleField(field);
            fields.add(consoleField);
        }

        consoleFields.put(numberOfRows, fields);

        int value = INVALID;
        while (onModule)
        {
            value = INVALID;
            for (ConsoleField field : fields)
            {
                field.setDisplayed(true);
                while (!field.display())
                {
                    //Continue to ask for input if it was invalid
                }
            }


            while (value == INVALID)
            {
                // Only give options to continue or redisplay when you need to meet the minimum amount of rows
                // or you are at the max amount of rows
                if (initial || numberOfRows == maxRow)
                {
                    value = getConsole().prompt("Enter 1 continue, or 2 to redisplay", 1, 2, -1, -1);
                    if (value == 2)
                    {
                        value = REDISPLAY;
                    }
                } else
                {
                    value = getConsole().prompt("Enter 1 continue, or 2 to add another module, 3 to redisplay", 1, 3, -1, -1);
                }
            }
            if (value != REDISPLAY)
            {
                onModule = false;
            }
        }

        if (value == ADD_MODULE)
        {
            return true;
        }

        return false;
    }

    public boolean addRow()
    {
        return addRow(false);
    }

    /**
     * Display the custom field.
     *
     * @return
     */
    @Override
    public boolean display()
    {
        numberOfRows = 0;
        consoleFields = new HashMap<Integer, List<ConsoleField>>();

        addInitialRows();
        while (addRow())
        {
            //Keep adding rows until the user is done or max limit is reached
        }
        customInfoField.setValue(numberOfRows + "");

        if (!columnsAreValid())
        {
            this.display();
        }

        return true;
    }

    private boolean columnsAreValid()
    {
        List<Column> columns = customInfoField.getColumns();
        String[] columnVariables = getVariablesByColumn();
        for (int i = 0; i < columnVariables.length; i++)
        {
            ValidationStatus status = columns.get(i).validate(columnVariables[i]);
            if (!status.isValid())
            {
                System.out.println(status.getMessage());
                return false;
            }
        }
        return true;
    }

    //TODO: Refactor duplicated code form CustomInputRows
    private String[] getVariablesByColumn()
    {
        String[] columnVariables = new String[numberOfColumns];

        for (int col = 0; col < numberOfColumns; col++)
        {
            columnVariables[col] = "";
            for (int row = 1; row <= numberOfRows; row++)
            {
                ConsoleField consoleField = consoleFields.get(row).get(col);
                if (consoleField.isDisplayed())
                {
                    columnVariables[col] += getField().getInstallData().getVariable(consoleField.getVariable()) + ",";
                }
            }
        }
        for (int i = 0; i < columnVariables.length; i++)
        {
            String v = columnVariables[i];
            columnVariables[i] = v.substring(0, v.length() - 1);
        }
        return columnVariables;
    }

    /**
     * Get the variables associated with this custom field.
     * @return
     */
    public List<String> getVariables()
    {
        List<String> countedVariables = new ArrayList<String>();

        for (int i = 1; i <= numberOfRows; i++)
        {
            for(ConsoleField consoleField : consoleFields.get(i))
            {
                if (consoleField.isDisplayed())
                {
                    countedVariables.add(consoleField.getVariable());
                }
            }
        }
        return countedVariables;
    }

    /**
     * Generate a new custom field.
     * @return
     */
    private CustomField createCustomField(UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        List<Field> fields = userInputPanelSpec.createFields(spec);
        for (Field field : fields)
        {
            if (field instanceof CustomField)
            {
                return (CustomField) field;
            }
        }
        return  null;
    }
}
