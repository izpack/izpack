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

package com.izforge.izpack.core.rules.process;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.ConditionReference;
import com.izforge.izpack.api.rules.RulesEngine;

/**
 * References an already defined condition
 */
public class RefCondition extends ConditionReference
{
    private static final long serialVersionUID = -2298283511093626640L;

    protected transient RulesEngine rules;

    private String referencedConditionId;

    public RefCondition(RulesEngine rules)
    {
        this.rules = rules;
    }

    public String getReferencedConditionId()
    {
        return referencedConditionId;
    }

    @Override
    public void readFromXML(IXMLElement xmlcondition) throws Exception
    {
        this.referencedConditionId = xmlcondition.getAttribute("refid");
        if (this.referencedConditionId == null)
        {
            throw new Exception("Missing attribute \"refid\" in condition \"" + getId() + "\"");
        }
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
        if (condition == null)
        {
            return false;
        }
        return condition.isTrue();
    }

    @Override
    public String getDependenciesDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append(this.getId());
        details.append(" depends on:<ul><li>");
        details.append(getReferencedCondition().getDependenciesDetails());
        details.append("</li></ul>");
        return details.toString();
    }

    @Override
    public void makeXMLData(IXMLElement conditionRoot)
    {
        conditionRoot.setAttribute("refid", this.referencedConditionId);
    }
}
