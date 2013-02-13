/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.userinput.field.combo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.ChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.Config;
import com.izforge.izpack.panels.userinput.field.FieldReader;
import com.izforge.izpack.panels.userinput.processor.Processor;


/**
 * A reader for 'combo' fields.
 *
 * @author Tim Anderson
 */
public class ComboFieldReader extends FieldReader implements ChoiceFieldConfig<Choice>
{

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The initial selected index.
     */
    private int selected = -1;

    /**
     * Constructs a {@code ComboFieldReader}.
     *
     * @param field       the field element
     * @param config      the configuration
     * @param installData the installation data
     */
    public ComboFieldReader(IXMLElement field, Config config, InstallData installData)
    {
        super(field, config);
        this.installData = installData;
    }

    /**
     * Returns the choices.
     *
     * @return the choices
     */
    @Override
    public List<Choice> getChoices()
    {
        selected = -1;
        List<Choice> result = new ArrayList<Choice>();
        Config config = getConfig();
        String variableValue = installData.getVariable(getVariable());
        for (IXMLElement choice : getSpec().getChildrenNamed("choice"))
        {
            String processorClass = choice.getAttribute("processor");
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
                String set = config.getString(choice, "set", null);
                StringTokenizer tokenizer = new StringTokenizer(values, ":");

                while (tokenizer.hasMoreTokens())
                {
                    String token = tokenizer.nextToken();
                    if (token.equals(set))
                    {
                        selected = result.size();
                    }
                    result.add(new Choice(token, token));
                }
            }
            else
            {
                String value = config.getAttribute(choice, "value");
                if (isSelected(value, choice, variableValue))
                {
                    selected = result.size();
                }
                result.add(new Choice(value, getText(choice)));
            }
        }
        return result;
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
     * This is only valid after {@link #getChoices()} is invoked.
     *
     * @return the selected index or {@code -1} if no choice is selected
     */
    public int getSelectedIndex()
    {
        return selected;
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
    private boolean isSelected(String value, IXMLElement choice, String variableValue)
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
            result = getConfig().getBoolean(choice, "set", false);
        }
        return result;
    }

}
