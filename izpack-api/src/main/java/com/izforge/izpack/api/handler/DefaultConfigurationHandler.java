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
package com.izforge.izpack.api.handler;

import com.izforge.izpack.api.data.ConfigurationOption;
import com.izforge.izpack.api.data.Configurable;
import com.izforge.izpack.api.rules.RulesEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class DefaultConfigurationHandler implements Configurable
{

    private static final long serialVersionUID = -671908088764713053L;

    /**
     * Contains configuration values for a panel.
     */
    private Map<String, ConfigurationOption> configuration = null;

    @Override
    public void addConfigurationOption(String name, ConfigurationOption option)
    {
        if (this.configuration == null)
        {
            this.configuration = new HashMap<String, ConfigurationOption>();
        }
        this.configuration.put(name, option);
    }

    @Override
    public ConfigurationOption getConfigurationOption(String name)
    {
        ConfigurationOption option = null;
        if (this.configuration != null)
        {
            option = this.configuration.get(name);
        }
        return option;
    }

    @Override
    public String getConfigurationOptionValue(String name, RulesEngine rules, String defaultValue)
    {
        String result = null;
        ConfigurationOption option = getConfigurationOption(name);
        if (option != null)
        {
            result = option.getValue(rules);
        }
        else
        {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public String getConfigurationOptionValue(String name, RulesEngine rules)
    {
        return getConfigurationOptionValue(name, rules, null);
    }

    @Override
    public Set<String> getNames()
    {
        return configuration != null ? configuration.keySet() : null;
    }
}
