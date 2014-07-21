package com.izforge.izpack.panels.userinput.field.button;

import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.panels.userinput.field.FieldConfig;

import java.util.List;

public interface ButtonFieldConfig extends FieldConfig
{
    /**
     * Get the button's name
     * @return
     */
    public String getButtonName();

    /**
     * Get success message to be sent to the user if all the button's actions suceeed.
     * @return
     */
    public String getSuccessMsg();

    /**
     * Get all the actions the button should run.
     */
    public List<ButtonAction> getButtonActions();
}
