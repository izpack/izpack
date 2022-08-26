/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

package com.izforge.izpack.installer.debugger;

import com.izforge.izpack.api.rules.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class ConditionHistory
{
    private final Condition condition;
    private final List<Object[]> values;

    private boolean newcondition;
    private boolean changedcondition;


    public ConditionHistory(Condition condition)
    {
        this.condition = condition;
        values = new ArrayList<>();
        newcondition = true;
        changedcondition = true;
    }

    public String getId() {
        return condition.getId();
    }

    public void addValue(boolean value, String comment)
    {
        if ((values.size() == 0) || value != getLastValue())
        {
            Object[] valuecomment = new Object[2];
            valuecomment[0] = value;
            valuecomment[1] = comment;
            this.values.add(valuecomment);
            if (values.size() == 1)
            {
                newcondition = true;
                changedcondition = true;
            }
            else
            {
                changedcondition = true;
            }
        }
    }

    public boolean getLastValue()
    {
        if (values.size() > 0)
        {
            return (Boolean) (values.get(values.size() - 1))[0];
        }
        else
        {
            return false;
        }
    }

    public int getValueCount()
    {
        return values.size();
    }

    public void clearState()
    {
        newcondition = false;
        changedcondition = false;
    }

    /**
     * @return the newcondition
     */
    public boolean isNewcondition()
    {
        return this.newcondition;
    }


    /**
     * @return the changedcondition
     */
    public boolean isChangedcondition()
    {
        return this.changedcondition;
    }

    public String getConditionHistoryDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append("<html><body>");
        details.append("<h3>Details of <b>");
        details.append(this.condition.getId());
        details.append("</b></h3>");
        for (int i = values.size() - 1; i >= 0; i--)
        {
            Object[] condcomment = values.get(i);
            details.append(i + 1);
            details.append(". ");
            details.append(condcomment[0].toString());
            details.append(" (");
            details.append(condcomment[1]);
            details.append(")<br>");
        }
        details.append("<h4>Dependencies</h4>");
        details.append(this.condition.getDependenciesDetails());
        details.append("</body></html>");
        return details.toString();
    }

    public String toString()
    {
        return Boolean.toString(getLastValue());
    }

    @Override
    public int hashCode() {
        return condition.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConditionHistory)
        {
            return condition.getId().equals((((ConditionHistory)obj).condition.getId()));
        }
        return false;
    }
}

