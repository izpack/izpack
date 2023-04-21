package com.izforge.izpack.panels.userinput.console.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.UserInterruptException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.custom.Column;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.field.custom.CustomFieldType;
import com.izforge.izpack.util.Console;

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

    private final static int CONTINUE = 1;
    private final static int REDISPLAY = 2;

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
        for (int count = minRow; count >= 1; count--)
        {
            addRow();
        }
    }

    private void showInitialRows()
    {
        for (int i = 1; i <= numberOfRows; i++)
        {
            editRow(i, true);
        }
    }

    private boolean editRow(int rowNumber, boolean initial)
    {
        List<ConsoleField> fields = consoleFields.get(Integer.valueOf(rowNumber));
        print("--> Row " + rowNumber + ": ");
        for (ConsoleField field : fields)
        {
            field.setDisplayed(true);
            final boolean ro = field.isReadonly();
            if (initial)
            {
                // Switch display mode temporarily
                field.setReadonly(true);
                // Avoid initial null values after initializing the first row
                field.getField().setValue(field.getField().getInitialValue());
            }
            while (!field.display())
            {
                // loop unless we have a valid value
            }
            if (initial)
            {
                field.setReadonly(ro);
            }
        }

        return true;
    }

    private boolean canAddRow()
    {
        return numberOfRows < maxRow;
    }

    /**
     * Add a new row to the panel.
     *
     * @return true if the row could be added
     */
    private boolean addRow()
    {
        if (!canAddRow())
        {
            return false;
        }
        numberOfRows++;
        List<ConsoleField> fields = new ArrayList<ConsoleField>();

        for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
        {
            field.setVariable(field.getVariable() + "." + numberOfRows);
            ConsoleField consoleField = createField.createConsoleField(field);
            consoleField.setReadonly(isReadonly());
            fields.add(consoleField);
        }

        consoleFields.put(Integer.valueOf(numberOfRows), fields);

        return true;
    }

    private boolean canRemoveRow()
    {
        return numberOfRows > minRow;
    }

    private boolean removeRow()
    {
        if (!canRemoveRow())
        {
            return false;
        }
        consoleFields.remove(Integer.valueOf(numberOfRows));
        numberOfRows--;
        return true;
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
        consoleFields = new HashMap<>();

        addInitialRows();

        int value;
        do
        {
            int userValue;
            showInitialRows();
            if (isReadonly())
            {
                String prompt = getMessage("ConsoleInstaller.continueQuitRedisplay");
                userValue = getConsole().prompt(prompt, 1, 3, 3, 2);
                switch (userValue)
                {
                    case 1:
                        value = CONTINUE;
                        break;
                    case 2:
                        throw new UserInterruptException(getMessage("ConsoleInstaller.aborted.PressedQuit"));
                    default:
                        value = REDISPLAY;
                        break;
                }
            }
            else
            {
                userValue = getConsole().prompt(getPrompt(),0, numberOfRows + 4, numberOfRows + 2, 0);
                if (userValue == 0) {
                    throw new UserInterruptException(getMessage("ConsoleInstaller.aborted.PressedQuit"));
                }
                if (userValue <= numberOfRows)
                {
                    editRow(userValue, false);
                    value = REDISPLAY;
                }
                else if (userValue == numberOfRows + 1)
                {
                    value = CONTINUE;
                }
                else if (userValue == numberOfRows + 3)
                {
                    if (addRow())
                    {
                        editRow(numberOfRows, false);
                    }
                    value = REDISPLAY;
                }
                else if (userValue == numberOfRows + 4)
                {
                    removeRow();
                    value = REDISPLAY;
                }
                else
                {
                    value = REDISPLAY;
                }
            }

        } while (value != CONTINUE);

        customInfoField.setValue(Integer.toString(numberOfRows));

        if (!columnsAreValid())
        {
            this.display();
        }

        return true;
    }

    private String getPrompt() {
        InstallData installData = getField().getInstallData();
        Messages messages = installData.getMessages();
        if (canAddRow() && canRemoveRow()) {
            return messages.get("UserInputPanel.custom.console.add.and.remove", numberOfRows, numberOfRows + 1, numberOfRows + 2, numberOfRows + 3, numberOfRows + 4, 0);
        }
        if (canAddRow()) {
            return messages.get("UserInputPanel.custom.console.add", numberOfRows, numberOfRows + 1, numberOfRows + 2, numberOfRows + 3, 0);
        }
        if (canRemoveRow()) {
            return messages.get("UserInputPanel.custom.console.remove", numberOfRows, numberOfRows + 1, numberOfRows + 2, numberOfRows + 4, 0);
        }
        return messages.get("UserInputPanel.custom.console.add.and.remove", numberOfRows, numberOfRows + 1, numberOfRows + 2, 0);
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
                ConsoleField consoleField = consoleFields.get(Integer.valueOf(row)).get(col);
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
    @Override
    public List<String> getVariables()
    {
        List<String> countedVariables = new ArrayList<String>();

        for (int i = 1; i <= numberOfRows; i++)
        {
            for(ConsoleField consoleField : consoleFields.get(Integer.valueOf(i)))
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
