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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.panels.userinput.console.ConsoleField;
import com.izforge.izpack.panels.userinput.console.ConsoleFieldFactory;
import com.izforge.izpack.panels.userinput.field.ElementReader;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldHelper;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * The user input panel console implementation.
 *
 * @author Mounir El Hajj
 */
public class UserInputConsolePanel extends AbstractConsolePanel
{
    /**
     * The panel meta-data.
     */
    private Panel panel;

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

    /**
     * The fields.
     */
    private List<ConsoleField> fields = new ArrayList<ConsoleField>();

    /**
     * Constructs an {@code UserInputConsolePanel}.
     *
     * @param panel     the panel meta-data
     * @param resources the resources
     * @param factory   the object factory
     * @param rules     the rules
     * @param matcher   the platform-model matcher
     * @param console   the console
     * @param prompt    the prompt
     */
    public UserInputConsolePanel(Panel panel, Resources resources, ObjectFactory factory,
                                 RulesEngine rules, PlatformModelMatcher matcher, Console console, Prompt prompt)
    {
        this.panel = panel;
        this.resources = resources;
        this.factory = factory;
        this.rules = rules;
        this.matcher = matcher;
        this.console = console;
        this.prompt = prompt;
    }

    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        collectInputs(installData);
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
    public boolean generateProperties(InstallData installData, PrintWriter printWriter)
    {
        collectInputs(installData);
        for (ConsoleField field : fields)
        {
            String name = field.getVariable();
            if (name != null)
            {
                printWriter.println(name + "=");
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
        boolean processpanel = collectInputs(installData);
        if (!processpanel)
        {
            return true;
        }
        for (ConsoleField field : fields)
        {
            if (!field.display())
            {
                break;
            }
        }

        return promptEndPanel(installData, console);
    }

    private boolean collectInputs(InstallData installData)
    {
        UserInputPanelSpec model = new UserInputPanelSpec(resources, installData, factory, rules, matcher);
        IXMLElement spec = model.getPanelSpec(panel);

        model.updateVariables(spec);

        ElementReader reader = new ElementReader(model.getConfig());
        List<String> forPacks = reader.getPacks(spec);
        List<String> forUnselectedPacks = reader.getUnselectedPacks(spec);
        List<OsModel> forOs = reader.getOsModels(spec);

        if (!FieldHelper.isRequiredForPacks(forPacks, installData.getSelectedPacks())
                || !FieldHelper.isRequiredForUnselectedPacks(forUnselectedPacks, installData.getSelectedPacks())
                || !matcher.matchesCurrentPlatform(forOs))
        {
            return false;
        }

        ConsoleFieldFactory factory = new ConsoleFieldFactory(console, prompt);
        for (Field field : model.createFields(spec))
        {
            fields.add(factory.create(field));
        }
        return true;
    }


}
