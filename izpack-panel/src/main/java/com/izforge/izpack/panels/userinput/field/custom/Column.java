package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

import java.util.List;

/**
 * Holds data on a column.
 * Add logic here when added mor attributes to the column
 */
public class Column
{
    private final List<FieldValidator> validators;

    public Column(List<FieldValidator> validators)
    {
        this.validators = validators;
    }

    /**
     * Validate based on column.
     * @param values
     * @return
     */
    public ValidationStatus validate(String... values)
    {
        for (FieldValidator validator : validators)
        {
            if(!validator.validate(new ValuesProcessingClient(values)))
            {
                return ValidationStatus.failed(validator.getMessage());
            }
        }
        return ValidationStatus.success(values);
    }
}
