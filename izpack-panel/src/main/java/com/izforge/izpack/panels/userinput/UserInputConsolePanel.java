/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
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

package com.izforge.izpack.panels.userinput;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.console.ConsoleFieldFactory;
import com.izforge.izpack.panels.userinput.field.ElementReader;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldHelper;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.api.config.Options;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The user input panel console implementation.
 *
 * @author Mounir El Hajj
 */
public class UserInputConsolePanel extends AbstractConsolePanel
{
    private static final String DISPLAY_HIDDEN = "displayHidden";
    private static final String DISPLAY_HIDDEN_CONDITION = "displayHiddenCondition";
    private static final String READONLY = "readonly";
    private static final String READONLY_CONDITION = "readonlyCondition";

    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The factory for creating field validators.
     */
    private final ObjectFactory factory;

    /**
     * The rules.
     */
    private final RulesEngine rules;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The console.
     */
    private final Console console;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    private final Panel panel;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(UserInputConsolePanel.class.getName());

    /**
     * The fields.
     */
    private List<ConsoleField> fields = new ArrayList<ConsoleField>();

    private final InstallData installData;

    /**
     * Constructs an {@code UserInputConsolePanel}.
     *
     * @param resources the resources
     * @param factory   the object factory
     * @param rules     the rules
     * @param matcher   the platform-model matcher
     * @param console   the console
     * @param prompt    the prompt
     * @param panelView     the parent panel/view
     * @param installData   the install data
     */
    public UserInputConsolePanel(Resources resources, ObjectFactory factory,
                                 RulesEngine rules, PlatformModelMatcher matcher, Console console, Prompt prompt,
                                 PanelView<ConsolePanel> panelView, InstallData installData)
    {
        super(panelView);
        this.installData = installData;
        this.resources = resources;
        this.factory = factory;
        this.rules = rules;
        this.matcher = matcher;
        this.console = console;
        this.prompt = prompt;

        UserInputPanelSpec model = new UserInputPanelSpec(resources, installData, factory, matcher);
        this.panel = getPanel();
        IXMLElement spec = model.getPanelSpec(panel);

        boolean isDisplayingHidden = false;
        try
        {
            isDisplayingHidden = Boolean.parseBoolean(spec.getAttribute(DISPLAY_HIDDEN));
        }
        catch (Exception ignore)
        {
            isDisplayingHidden = false;
        }
        panel.setDisplayHidden(isDisplayingHidden);

        String condition = spec.getAttribute(DISPLAY_HIDDEN_CONDITION);
        if (condition != null && !condition.isEmpty())
        {
            panel.setDisplayHiddenCondition(condition);
        }

        // Prevent activating on certain global conditions
        ElementReader reader = new ElementReader(model.getConfig());
        Condition globalConstraint = reader.getComplexPanelCondition(spec, matcher, installData, rules);
        if (globalConstraint != null)
        {
            rules.addPanelCondition(panel, globalConstraint);
        }

        boolean readonly = false;
        try
        {
            readonly = Boolean.parseBoolean(spec.getAttribute(READONLY));
        }
        catch (Exception ignore)
        {
            readonly = false;
        }
        panel.setReadonly(readonly);

        condition = spec.getAttribute(READONLY_CONDITION);
        if (condition != null && !condition.isEmpty())
        {
            panel.setReadonlyCondition(condition);
        }

        collectInputs(installData);
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        for (ConsoleField field : fields)
        {
            String name = field.getVariable();
            if (name != null)
            {
                String value = properties.getProperty(name);
                if (value != null)
                {
                    installData.setVariable(name, value);
                }
            }
        }
        return true;
    }

    @Override
    public boolean generateOptions(InstallData installData, Options options)
    {
        boolean commented = false;
        for (ConsoleField field : fields)
        {
            String name = field.getVariable();
            if (name != null)
            {
                options.put(name, installData.getVariable(name));
                if (!commented)
                {
                    options.addEmptyLine(name);
                    options.putComment(name, Arrays.asList(panel.getPanelId()));
                    commented = true;
                }
            }
        }
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return {@code true} if the panel ran successfully, otherwise {@code false}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Unblocked variables on panel '" + panel.getPanelId() +"': " + createListAsString(installData.getVariables().getBlockedVariableNames(panel)));
        }
        installData.getVariables().unregisterBlockedVariableNames(installData.getVariables().getBlockedVariableNames(panel), panel);
        printHeadLine(installData, console);

        boolean result = true;
        if (fields != null && !fields.isEmpty())
        {
            boolean rerun = false;
            Set<String> variables = new HashSet<String>();
            for (ConsoleField field : fields)
            {
                Field fieldDefinition = field.getField();
                boolean readonly = false;
                boolean addToPanel = false;
                boolean required = FieldHelper.isRequired(fieldDefinition, installData, matcher);

                if (required && fieldDefinition.isConditionTrue())
                {
                    readonly = fieldDefinition.isEffectiveReadonly(
                            panel.isReadonly()
                            || (panel.getReadonlyCondition() != null && rules.isConditionTrue(panel.getReadonlyCondition())),
                            rules);
                    addToPanel = true;
                    field.setDisplayed(true);
                    fieldDefinition.setSaving(true);
                }
                else if (required
                        && (
                                fieldDefinition.isEffectiveDisplayHidden(
                                        panel.isDisplayHidden()
                                        || (panel.getDisplayHiddenCondition() != null && rules.isConditionTrue(panel.getDisplayHiddenCondition())),
                                        rules)
                           )
                        )
                {
                    readonly = true;
                    addToPanel = true;
                    field.setDisplayed(false);
                }
                else
                {
                    readonly = true;
                    addToPanel = false;
                }

                if (addToPanel)
                {
                    field.setReadonly(readonly);
                    if (!field.display())
                    {
                        // field is invalid
                        rerun = true;
                        break;
                    }
                    String var = fieldDefinition.getVariable();
                    if (var != null)
                    {
                        variables.add(var);
                    }
                }
            }
            panel.setAffectedVariableNames(variables);
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine("Blocked variables on panel '" + panel.getPanelId() +"': " + createListAsString(variables));
            }
            installData.getVariables().registerBlockedVariableNames(variables, panel);

            if (rerun)
            {
                // prompt to rerun the panel or quit
                result = promptRerunPanel(installData, console);
            }
            else
            {
                result = promptEndPanel(installData, console);
            }
        }
        return result;
    }

    private void collectInputs(InstallData installData)
    {
        UserInputPanelSpec model = new UserInputPanelSpec(resources, installData, factory, matcher);
        Panel panel = getPanel();
        IXMLElement spec = model.getPanelSpec(panel);

        fields.clear();

        ConsoleFieldFactory factory = new ConsoleFieldFactory(console, prompt);
        for (Field fieldDefinition : model.createFields(spec))
        {
            ConsoleField consoleField = factory.create(fieldDefinition, model, spec);
            fields.add(consoleField);
        }
    }

    /**
     * Creates an installation record for unattended installations on {@link UserInputPanel},
     * created during GUI installations.
     */
    @Override
    public void createInstallationRecord(IXMLElement rootElement)
    {
        new UserInputPanelAutomationHelper(fields).createInstallationRecord(installData, rootElement);
    }

    @Override
    public boolean handlePanelValidationResult(boolean valid)
    {
        if (!valid)
        {
            return promptRerunPanel(installData, console);
        }
        return true;
    }

    private String createListAsString(Set<String> list)
    {
        StringBuffer msg = new StringBuffer("{");
        if (list != null)
        {
            Iterator<String> it = list.iterator();
            while (it.hasNext())
            {
                if( logger.isLoggable(Level.FINE))
                {
                    msg.append(it.next());
                    if (it.hasNext())
                    {
                        msg.append(", ");
                    }
                }
            }
        }
        msg.append("}");
        return msg.toString();
    }
}
