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

package com.izforge.izpack.panels.userinput.field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.logic.AndCondition;
import com.izforge.izpack.core.rules.logic.NotCondition;
import com.izforge.izpack.core.rules.logic.OrCondition;
import com.izforge.izpack.core.rules.process.PackSelectionCondition;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Configuration element reader.
 *
 * @author Tim Anderson
 */
public class ElementReader
{

    /**
     * The configuration.
     */
    private final Config config;

    /**
     * Constructs a {@code ElementReader}.
     *
     * @param config the configuration to read from
     */
    public ElementReader(Config config)
    {
        this.config = config;
    }

    /**
     * Returns the pack names associated with an element.
     *
     * @param element the element
     * @return the 'name' attributes from the "createForPack" elements
     */
    public List<String> getPacks(IXMLElement element)
    {
        List<IXMLElement> elements = element.getChildrenNamed("createForPack");
        return getNames(elements);
    }

    /**
     * Returns the unselected pack names associated with an element.
     *
     * @param element the element
     * @return the 'name' attributes from the "createForUnselectedPack" elements
     */
    public List<String> getUnselectedPacks(IXMLElement element)
    {
        List<IXMLElement> elements = element.getChildrenNamed("createForUnselectedPack");
        return getNames(elements);
    }

    /**
     * Returns the OS models associated with an element.
     *
     * @param element the element
     * @return the OSes
     */
    public List<OsModel> getOsModels(IXMLElement element)
    {
        return OsConstraintHelper.getOsList(element);
    }

    /**
     * Gets a global condition for viewing an UserInputPanel depending on the following optional nested tags to the panel tag:
     * <ul>
     * <li>createForPack
     * <li>createForUnselectedPack
     * <li>os
     * </ul>
     * @param spec UserInputPanel descriptor
     * @param the platform-model matcher
     * @param the installation data
     * @param the rules engine
     * @return
     */
    public Condition getComplexPanelCondition(IXMLElement spec, final PlatformModelMatcher matcher,
            InstallData installData, RulesEngine rules)
    {
        List<String> forPacks = getPacks(spec);
        List<String> forUnselectedPacks = getUnselectedPacks(spec);
        final List<OsModel> forOs = getOsModels(spec);

        Set<Condition> globalConditions = new HashSet<Condition>();

        if (!forOs.isEmpty())
        {
            Condition osMatcherCondition = new Condition() {

                @Override
                public void readFromXML(IXMLElement xmlcondition) throws Exception {}

                @Override
                public void makeXMLData(IXMLElement conditionRoot) {}

                @Override
                public boolean isTrue()
                {
                    return matcher.matchesCurrentPlatform(forOs);
                }
            };
            osMatcherCondition.setId(osMatcherCondition.toString());
            globalConditions.add(osMatcherCondition);
        }

        if (!forPacks.isEmpty())
        {
            Condition newCondition;
            if (forPacks.size() > 1)
            {
                OrCondition orCondition = new OrCondition(rules);
                orCondition.setId(orCondition.toString());
                for (String packName : forPacks)
                {
                    orCondition.addOperands(createPackSelectionCondition(installData, packName));
                }
                newCondition = orCondition;
            }
            else
            {
                newCondition = createPackSelectionCondition(installData, forPacks.iterator().next());
            }
            globalConditions.add(newCondition);
        }

        if (!forUnselectedPacks.isEmpty())
        {
            Condition newCondition;
            if (forUnselectedPacks.size() > 1)
            {
                OrCondition orCondition = new OrCondition(rules);
                orCondition.setId(orCondition.toString());
                for (String packName : forUnselectedPacks)
                {
                    orCondition.addOperands(createPackUnselectionCondition(installData, rules, packName));
                }
                newCondition = orCondition;
            }
            else
            {
                newCondition = createPackUnselectionCondition(installData, rules, forUnselectedPacks.iterator().next());
            }
            globalConditions.add(newCondition);
        }

        if (!globalConditions.isEmpty())
        {
            Condition newCondition;
            if (globalConditions.size() > 1)
            {
                AndCondition andCondition = new AndCondition(rules);
                andCondition.setId(andCondition.toString());
                for (Condition globalCondition : globalConditions)
                {
                    andCondition.addOperands(globalCondition);
                }
                newCondition = andCondition;
            }
            else {
                newCondition = globalConditions.iterator().next();
            }
            return newCondition;
        }
        return null;
    }

    private static Condition createPackSelectionCondition(InstallData installData, String packName)
    {
        PackSelectionCondition packSelectionCondition = new PackSelectionCondition();
        packSelectionCondition.setId(packSelectionCondition.toString());
        packSelectionCondition.setInstallData(installData);
        packSelectionCondition.setPack(packName);
        return packSelectionCondition;
    }

    private static Condition createPackUnselectionCondition(InstallData installData, RulesEngine rules, String packName)
    {
        PackSelectionCondition packSelectionCondition = new PackSelectionCondition();
        packSelectionCondition.setId(packSelectionCondition.toString());
        packSelectionCondition.setInstallData(installData);
        packSelectionCondition.setPack(packName);
        NotCondition packNotSelectedCondition = new NotCondition(rules);
        packNotSelectedCondition.setId(packNotSelectedCondition.toString());
        packNotSelectedCondition.setInstallData(installData);
        packNotSelectedCondition.setReferencedCondition(packSelectionCondition);
        return packNotSelectedCondition;
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    protected Config getConfig()
    {
        return config;
    }

    /**
     * Returns the names associated with the supplied elements.
     *
     * @param elements the elements
     * @return the "name" attributes
     */
    private List<String> getNames(List<IXMLElement> elements)
    {
        List<String> result = new ArrayList<String>();
        for (IXMLElement element : elements)
        {
            result.add(config.getAttribute(element, "name"));
        }
        return result;
    }

}
