package com.izforge.izpack.panels.userinput.field.button;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.api.exception.IzPackException;

import java.util.List;

public class ButtonField extends Field
{
    private final String sucessMsg;
    private final String buttonName;
    private final List<ButtonAction> buttonActions;
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
        this.buttonActions = config.getButtonActions();
        this.sucessMsg = config.getSuccessMsg();
    }

    public String getButtonName()
    {
        return buttonName;
    }

    public List<ButtonAction> getButtonActions()
    {
        return buttonActions;
    }

    public String getSucessMsg()
    {
        return sucessMsg;
    }
}
