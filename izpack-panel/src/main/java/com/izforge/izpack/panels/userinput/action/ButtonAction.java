package com.izforge.izpack.panels.userinput.action;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.Console;

import java.util.HashMap;
import java.util.Map;

public abstract class ButtonAction
{
    /**
     * Access to installData
     */
    final protected InstallData installData;

    /**
     * Messages available for the button
     */
    protected Map<String, String> messages = new HashMap<String, String>();

    public ButtonAction(InstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Give mapping from string name to the actual string value.
     * @param messages
     */
    public void setMessages(Map<String, String> messages)
    {
        this.messages = messages;
    }

    /**
     * Actual execution logic for the action.
     * @return
     */
    public abstract boolean execute();

    /**
     * Execution method for console.
     * @return
     */
    public abstract boolean execute(Console console);

    /**
     * Execution method for GUI
     * @param prompt
     * @return
     */
    public abstract boolean execute(Prompt prompt);
}
