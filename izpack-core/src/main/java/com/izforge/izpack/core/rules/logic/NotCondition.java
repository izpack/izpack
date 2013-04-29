/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil <izpack@reil-online.de>
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
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.ConditionReference;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.process.RefCondition;

/**
 * Negation of a referenced condition
 */
public class NotCondition extends ConditionReference
{
    private static final long serialVersionUID = 2886367480913278231L;

    private transient RulesEngine rules;

    private String referencedConditionId;

    public NotCondition(RulesEngine rules)
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
        else if (xmlcondition.getChildrenCount() != 1)
        {
            throw new Exception("Condition \"" + getId() + "\" needs exactly one condition as operand");
        }

        RefCondition refCondition = new RefCondition(rules);
        refCondition.readFromXML(xmlcondition.getChildAtIndex(0));
        this.referencedConditionId = refCondition.getReferencedConditionId();
    }

    @Override
    public void resolveReference()
    {
        Condition condition = null;
        if (referencedConditionId != null)
        {
            condition = rules.getCondition(referencedConditionId);
        }
        if (condition == null)
        {
            throw new IzPackException("Referenced condition \"" +  referencedConditionId + "\" not found");
        }
        setReferencedCondition(condition);
    }

    @Override
    public boolean isTrue()
    {
        Condition condition = getReferencedCondition();
        return condition != null && !condition.isTrue();
    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuilder details = new StringBuilder();
        details.append(this.getId());
        details.append(" depends on:<ul><li>NOT ");
        details.append(getReferencedCondition().getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }


    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        IXMLElement conditionElement = rules.createConditionElement(getReferencedCondition(), conditionRoot);
        getReferencedCondition().makeXMLData(conditionElement);
        conditionRoot.addChild(conditionElement);
    }

    public static Condition createFromCondition(Condition referencedCondition,
            RulesEngine rules)
    {
        NotCondition notCondition = null;
        if (referencedCondition != null)
        {
            notCondition = new NotCondition(rules);
            notCondition.setReferencedCondition(referencedCondition);
            notCondition.setInstallData(referencedCondition.getInstallData());
        }
        return notCondition;
    }
}
