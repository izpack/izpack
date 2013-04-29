/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
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

package com.izforge.izpack.core.rules.logic;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.ConditionWithMultipleOperands;
import com.izforge.izpack.api.rules.RulesEngine;

/**
 * Defines a condition where both operands have to be true
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 */
public class AndCondition extends ConditionWithMultipleOperands
{
    private static final long serialVersionUID = -449457141608045073L;

    protected transient RulesEngine rules;

    public AndCondition(RulesEngine rules)
    {
        this.rules = rules;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        if (xmlcondition.getChildrenCount() <= 0)
        {
            throw new Exception("Missing nested element in condition \"" + getId() + "\"");
        }
        for (IXMLElement element : xmlcondition.getChildren())
        {
            nestedConditions.add(rules.createCondition(element));
        }
    }

    @Override
    public boolean isTrue()
    {
        boolean result = true;
        for (Condition condition : nestedConditions)
        {
            result = result && condition.isTrue();
        }
        return result;
    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on:<ul><li>");
        for (Condition condition : nestedConditions)
        {
            details.append(condition.getDependenciesDetails());
            details.append("</li> AND <li>");
        }
        details.append("</li></ul>");
        return details.toString();
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        for (Condition condition : nestedConditions)
        {
            IXMLElement left = rules.createConditionElement(condition, conditionRoot);
            condition.makeXMLData(left);
            conditionRoot.addChild(left);
        }
    }
}
