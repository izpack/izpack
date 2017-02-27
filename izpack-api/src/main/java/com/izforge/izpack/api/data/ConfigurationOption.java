/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.api.data;

import java.io.Serializable;

import com.izforge.izpack.api.rules.RulesEngine;

public class ConfigurationOption implements Serializable
{

    private static final long serialVersionUID = 2616397619106736648L;

    private final String value;

    private final String conditionId;

    private final String defaultValue;

    public ConfigurationOption(String value, String conditionId, String defaultValue)
    {
        super();
        this.value = value;
        this.conditionId = conditionId;
        this.defaultValue = defaultValue;
    }

    public ConfigurationOption(String value, String conditionId)
    {
        this(value, conditionId, null);
    }

    public ConfigurationOption(String value)
    {
        this(value, null);
    }

    /**
     * Get the option's current value according to the optional condition
     *
     * @return the current value
     */
    public String getValue(RulesEngine rules)
    {
        final String result;
        if (rules == null | conditionId == null || rules.isConditionTrue(conditionId))
        {
            result = value;
        }
        else
        {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "value='" + value + "'"
                + (conditionId == null ? "" : "conditionId+" + conditionId + "'")
                + (defaultValue == null ? "" : "defaultValue+" + defaultValue + "'");
    }
}
