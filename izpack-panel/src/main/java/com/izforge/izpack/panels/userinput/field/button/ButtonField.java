package com.izforge.izpack.panels.userinput.field.button;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.api.exception.IzPackException;

public class ButtonField extends Field
{
    private final String buttonName;
    /**
     * Constructs a {@code Field}.
     *
     * @param config      the field configuration
     * @param installData the installation data
     * @throws IzPackException if the configuration is invalid
     */
    public ButtonField(ButtonFieldConfig config, InstallData installData)
    {
        super(config, installData);
        this.buttonName = config.getButtonName();
    }

    public String getButtonName()
    {
        return buttonName;
    }
}
