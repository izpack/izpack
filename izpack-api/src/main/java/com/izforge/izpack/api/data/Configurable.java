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

import com.izforge.izpack.api.rules.RulesEngine;

import java.io.Serializable;
import java.util.Set;

public interface Configurable extends Serializable
{

    /**
     * Add an optional configuration option to the implementing instance
     *
     * @param name Configuration option name
     * @param option Configuration option
     */
    void addConfigurationOption(String name, ConfigurationOption option);

    /**
     * Get an optional configuration value to the implementing instance from the
     * implementing instance
     *
     * @param name Configuration option name
     * @param rules Current RulesEngine instance
     * @return the effective value or {@code null}
     */
    String getConfigurationOptionValue(String name, RulesEngine rules);

    /**
     * Get an optional configuration value to the implementing instance from the
     * implementing instance.<br>
     * The {@code defaultValue} is not used if a configured option exists, but
     * the option condition is not true.
     *
     * @param name Configuration option name
     * @param rules Current RulesEngine instance
     * @param defaultValue default value if value is not configured.
     * @return the effective value or {@code null}
     */
    String getConfigurationOptionValue(String name, RulesEngine rules, String defaultValue);

    /**
     * Get an optional configuration value to the implementing instance from the
     * implementing instance
     *
     * @param name Configuration option name
     * @return the configuration option instance or {@code null}
     */
    ConfigurationOption getConfigurationOption(String name);

    /**
     * Get all configuration option names
     *
     * @return a list of all configuration option names, or {@code null} if
     * nothing has been configured
     */
    Set<String> getNames();
}
