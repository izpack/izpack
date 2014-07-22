package com.izforge.izpack.panels.userinput.field.button;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.panels.userinput.action.ButtonAction;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.SimpleFieldReader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonFieldReader extends SimpleFieldReader implements ButtonFieldConfig
{
    private final Messages messages;
    private final InstallData installData;

    /**
     * Constructs a {@code FieldReader}.
     *
     * @param field  the field element to read
     * @param config the configuration
     */
    public ButtonFieldReader(IXMLElement field, Config config, InstallData installData)
    {
        super(field, config);
        this.installData = installData;
        this.messages = installData.getMessages();
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

    /**
     * Get success message to be sent to the user if all the button's actions suceeed.
     * @return
     */
    public String getSuccessMsg()
    {
        String successMsg = getSpec().getAttribute("successMsg");
        if(successMsg == null)
        {
            successMsg = "";
        }
        return messages.get(successMsg);
    }

    /**
     * Get all the actions the button should run.
     */
    public List<ButtonAction> getButtonActions()
    {
        List<ButtonAction> buttonActions = new ArrayList<ButtonAction>();

        for(IXMLElement runSpec : this.getSpec().getChildrenNamed("run"))
        {
            Map<String, String> buttonMessages = new HashMap<String, String>();

            String actionClass = runSpec.getAttribute("class");
            try
            {
                Class<ButtonAction> buttonActionClass = (Class<ButtonAction>) Class.forName(actionClass);
                Constructor<ButtonAction> buttonActionConstructor = buttonActionClass.getConstructor(InstallData.class);
                ButtonAction buttonAction = buttonActionConstructor.newInstance(installData);

                for (IXMLElement message : runSpec.getChildrenNamed("msg"))
                {
                    String id = message.getAttribute("id");
                    String name = message.getAttribute("name");
                    String value = messages.get(id);

                    buttonMessages.put(name, value);
                }
                buttonAction.setMessages(buttonMessages);
                buttonActions.add(buttonAction);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //Failed to create button
            }
        }

        return buttonActions;
    }
}
