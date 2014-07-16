package com.izforge.izpack.panels.userinput.field.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldFactory;
import com.izforge.izpack.panels.userinput.field.FieldReader;
import com.izforge.izpack.util.PlatformModelMatcher;

import java.util.ArrayList;
import java.util.List;

public class CustomFieldReader extends FieldReader implements CustomFieldConfig
{
    /**
     * The installation data.
     */
    private InstallData installData;

    private final Config config;

    private final PlatformModelMatcher matcher;

    /**
     * Constructs a {@code FieldReader}.
     *
     * @param field  the field element to read
     * @param config the configuration
     */
    public CustomFieldReader(IXMLElement field, Config config, PlatformModelMatcher matcher, InstallData installData)
    {
        super(field, config);
        this.installData = installData;
        this.config = config;
        this.matcher = matcher;
    }

    @Override
    public List<Column> getColumns()
    {
        List<Column> result = new ArrayList<Column>();

        for (IXMLElement column : getSpec().getChildrenNamed("col"))
        {
            result.add(new Column(getValidators(column)));
        }

        return result;
    }

    @Override
    public List<Field> getFields()
    {
        List<Field> result = new ArrayList<Field>();
        FieldFactory factory = new FieldFactory(config, installData, matcher);

        List<IXMLElement> fieldConfig;
        for (IXMLElement column : getSpec().getChildrenNamed("col"))
        {

            fieldConfig = column.getChildrenNamed("field");
            for (IXMLElement fieldElement : fieldConfig)
            {
                Field field = factory.create(fieldElement);
                result.add(field);
            }
        }
        return  result;
    }

    @Override
    public int getMinRow()
    {
        String minRowString = getConfig().getAttribute(getField(), "minRow", true);
        try
        {
            return Integer.parseInt(minRowString);
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    @Override
    public int getMaxRow()
    {
        String maxRowString = getConfig().getAttribute(getField(), "maxRow", true);
        try
        {
            return Integer.parseInt(maxRowString);
        }
        catch (Exception e)
        {
            return 5;
        }
    }
}
