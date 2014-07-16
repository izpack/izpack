package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Field;

import java.util.List;

public class CustomField extends Field
{
    private final List<Column> columns;
    private final List<Field> fields;
    private final int minRow;
    private final int maxRow;
    /**
     * Constructs an {@code CustomField}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the field cannot be read
     */
    public CustomField(CustomFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.columns = config.getColumns();
        this.fields = config.getFields();
        this.minRow = config.getMinRow();
        this.maxRow = config.getMaxRow();
    }

    public List<Field> getFields()
    {
        return fields;
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public int getMinRow()
    {
        return minRow;
    }

    public int getMaxRow()
    {
        return maxRow;
    }
}
