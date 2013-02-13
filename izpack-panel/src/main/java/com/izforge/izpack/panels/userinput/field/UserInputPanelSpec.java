/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.userinput.field;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * The user input panel specification.
 *
 * @author Tim Anderson
 */
public class UserInputPanelSpec
{

    /**
     * The name of the XML file that specifies the panel layout.
     */
    public static final String SPEC_FILE_NAME = "userInputSpec.xml";

    /**
     * Panel element name.
     */
    public static final String PANEL = "panel";

    /**
     * Provides access to the XML configuration.
     */
    private final Config config;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The rules.
     */
    private final RulesEngine rules;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The user input language pack resource name.
     */
    private static final String LANG_FILE_NAME = "userInputLang.xml";

    /**
     * The panel identifier attribute name
     */
    private static final String PANEL_IDENTIFIER = "id";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(UserInputPanelSpec.class.getName());


    /**
     * Constructs a {@code UserInputPanelSpec}.
     *
     * @param resources   the resources
     * @param installData the installation data
     * @param factory     the factory
     * @param rules       the rules
     * @param matcher     the platform-model matcher
     */
    public UserInputPanelSpec(Resources resources, InstallData installData, ObjectFactory factory,
                              RulesEngine rules, PlatformModelMatcher matcher)
    {
        Messages messages = installData.getMessages();
        try
        {
            messages = messages.newMessages(LANG_FILE_NAME);
        }
        catch (ResourceNotFoundException exception)
        {
            logger.info(exception.getMessage());
        }

        config = new Config(SPEC_FILE_NAME, resources, installData, factory, messages);
        this.installData = installData;
        this.rules = rules;
        this.matcher = matcher;
    }

    /**
     * Updates any variables referenced in the panel specification.
     *
     * @param spec the panel specification
     * @return the variable names
     */
    public Set<String> updateVariables(IXMLElement spec)
    {
        Set<String> result = new HashSet<String>();
        List<IXMLElement> variables = spec.getChildrenNamed("variable");

        for (IXMLElement variable : variables)
        {
            String name = config.getAttribute(variable, "name");
            String value = variable.getAttribute("value");

            if (value == null && variable.hasChildren())
            {
                // try and get the value from the child
                IXMLElement element = variable.getFirstChildNamed("value");
                if (element != null)
                {
                    value = element.getContent();
                }
            }

            if (isConditionTrue(variable) && matchesCurrentPlatform(variable))
            {
                if (value != null)
                {
                    // substitute variables in value field
                    value = replaceVariables(value);

                    // try to cut out circular references
                    installData.setVariable(name, "");
                    value = replaceVariables(value);
                }
                // set variable
                installData.setVariable(name, value);
                result.add(name);
            }
        }
        return result;
    }

    /**
     * Returns the specification for the supplied panel.
     *
     * @param panel the panel
     * @return the corresponding specification
     * @throws IzPackException if there is no specification for the panel
     */
    public IXMLElement getPanelSpec(Panel panel)
    {
        String panelId = panel.getPanelId();
        List<IXMLElement> panels = config.getRoot().getChildrenNamed(PANEL);
        IXMLElement result = null;
        for (IXMLElement spec : panels)
        {
            String id = spec.getAttribute(PANEL_IDENTIFIER);

            if (id != null && panelId != null && panelId.equals(id))
            {
                // use the current element as spec
                result = spec;
                break;
            }
        }
        if (result == null)
        {
            throw new IzPackException("No user input specification with " + PANEL_IDENTIFIER + "=" + panelId);
        }
        return result;
    }

    /**
     * Creates fields for each field configuration in the panel.
     *
     * @param panel the panel configuration
     * @return the fields
     */
    public List<Field> createFields(IXMLElement panel)
    {
        List<Field> result = new ArrayList<Field>();
        List<IXMLElement> elements = panel.getChildrenNamed("field");
        FieldFactory factory = new FieldFactory(config, installData, matcher);

        for (IXMLElement element : elements)
        {
            Field field = factory.create(element);
            if (field.isConditionTrue())
            {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public Config getConfig()
    {
        return config;
    }

    /**
     * Helper to replace variables in text.
     *
     * @param text the text to perform replacement on. May be {@code null}
     * @return the text with any variables replaced with their values
     */
    private String replaceVariables(String text)
    {
        return installData.getVariables().replace(text);
    }

    private boolean isConditionTrue(IXMLElement variable)
    {
        String condition = variable.getAttribute("conditionid");
        return condition == null || rules.isConditionTrue(condition, installData);
    }

    private boolean matchesCurrentPlatform(IXMLElement variable)
    {
        // are there any OS-Constraints?
        List<OsModel> osList = OsConstraintHelper.getOsList(variable);
        return matcher.matchesCurrentPlatform(osList);
    }
}
