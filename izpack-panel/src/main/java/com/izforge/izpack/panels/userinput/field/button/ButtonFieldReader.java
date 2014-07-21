package com.izforge.izpack.panels.userinput.field.button;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.SimpleFieldReader;

import java.util.Map;

public class ButtonFieldReader extends SimpleFieldReader implements ButtonFieldConfig
{
    /**
     * Constructs a {@code FieldReader}.
     *
     * @param field  the field element to read
     * @param config the configuration
     */
    public ButtonFieldReader(IXMLElement field, Config config)
    {
        super(field, config);
    }

    /**
     * Allow retrival of specification
     * @param field  the parent field element
     * @param config the configuration
     * @return
     */
    @Override
    protected IXMLElement getSpec(IXMLElement field, Config config)
    {
        return config.getElement(field, SPEC);
    }

    /**
     * Returns the text label.
     *
     * @return the text label
     */
    @Override
    public String getLabel()
    {
        return getText(getField());
    }

    /**
     * Get the button's name
     * @return
     */
    public String getButtonName()
    {
        return getText(getSpec());
    }
}
