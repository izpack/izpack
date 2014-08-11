package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.panels.userinput.processor.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SimpleChoiceReader extends FieldReader implements ChoiceFieldConfig
{

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The fallback value of a radiobutton marked checked.
     */
    private String defaultValue;


    /**
     * Constructs a {@code SimpleChoiceReader}.
     *
     * @param field  the field element to read
     * @param config the configuration
     */
    public SimpleChoiceReader(IXMLElement field, Config config, InstallData installData)
    {
        super(field, config);
        this.installData = installData;

        for (IXMLElement choice : getSpec().getChildrenNamed("choice"))
        {
            if (getConfig().getBoolean(choice, "set", false))
            {
                defaultValue = config.getAttribute(choice, "value");
                break;
            }
        }
    }

    /**
     * Returns the choices.
     *
     * @return the choices
     */
    public List<Choice> getChoices()
    {
        List<Choice> result = new ArrayList<Choice>();
        Config config = getConfig();
        RulesEngine rules = installData.getRules();
        for (IXMLElement choice : getSpec().getChildrenNamed("choice"))
        {
            String processorClass = choice.getAttribute("processor");
            String conditionId = config.getString(choice, "conditionid", null);

            if (processorClass != null && !"".equals(processorClass))
            {
                String values;
                try
                {
                    Processor processor = config.getFactory().create(processorClass, Processor.class);
                    values = processor.process(null);
                }
                catch (Throwable exception)
                {
                    throw new IzPackException("Failed to get choices from processor=" + processorClass + " in "
                            + config.getContext(choice), exception);
                }
                StringTokenizer tokenizer = new StringTokenizer(values, ":");

                while (tokenizer.hasMoreTokens())
                {
                    String token = tokenizer.nextToken();
                    if (isDisplayed(rules, conditionId))
                    {
                        result.add(new Choice(token, token));
                    }
                }
            }
            else
            {
                String value = config.getAttribute(choice, "value");
                if (isDisplayed(rules, conditionId))
                {
                    result.add(new Choice(value, getText(choice)));
                }
            }
        }
        return result;
    }

    /**
     * Determines if a choice is selected.
     * <p/>
     * A choice is selected if:
     * <ul>
     * <li>the variable value is the same as the choice "value" attribute; or</li>
     * <li>the "set" attribute is 'true'</li>
     * </ul>
     *
     * @param value         the choice value
     * @param choice        the choice element
     * @param variableValue the variable value. May be {@code null}
     * @return {@code true} if the choice is selected
     */
    protected boolean isSelected(String value, IXMLElement choice, String variableValue)
    {
        boolean result = false;
        if (variableValue != null)
        {
            if (variableValue.equals(value))
            {
                result = true;
            }
        }
        else
        {
            result = (defaultValue != null && defaultValue.equals(variableValue));
        }
        return result;
    }

    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Returns the index of the selected choice.
     * <p/>
     * A choice is selected if:
     * <ul>
     * <li>the variable value is the same as the choice "value" attribute; or</li>
     * <li>the "set" attribute is 'true'</li>
     * </ul>
     * <p/>
     * This is only valid after {@link #()} is invoked.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    public int getSelectedIndex(String variable)
    {
        int selected = 0;
        Config config = getConfig();
        RulesEngine rules = installData.getRules();
        String variableValue = installData.getVariable(variable);
        for (IXMLElement choice : getSpec().getChildrenNamed("choice"))
        {
            String value = config.getAttribute(choice, "value");
            String conditionId = config.getString(choice, "conditionid", null);
            if(variableValue == null)
            {
               return  selected;
            }
            else
            {
                if (isDisplayed(rules, conditionId) && isSelected(value, choice, variableValue))
                {
                    return  selected;
                }
            }
            selected ++;
        }
        return 0;
    }

    private boolean isDisplayed(RulesEngine rules, String conditionId)
    {
        return (rules == null || conditionId == null || rules.isConditionTrue(conditionId));
    }
}
