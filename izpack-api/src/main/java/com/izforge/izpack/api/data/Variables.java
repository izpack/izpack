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

package com.izforge.izpack.api.data;


import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.rules.RulesEngine;

import java.util.Properties;
import java.util.Set;

/**
 * A collection of variables.
 *
 * @author Tim Anderson
 */
public interface Variables
{
    /**
     * Sets a variable.
     *
     * @param name  the variable name
     * @param value the variable value. May be {@code null}
     */
    void set(String name, String value);

    /**
     * Returns the value of the specified variable.
     *
     * @param name the variable name
     * @return the value. May be {@code null}
     */
    String get(String name);

    /**
     * Returns the value of the specified variable.
     *
     * @param name         the variable name
     * @param defaultValue the default value if the variable doesn't exist, or is {@code null}
     * @return the value, or {@code defaultValue} if the variable doesn't exist or is {@code null}
     */
    String get(String name, String defaultValue);

    /**
     * Returns the boolean value of the specified variable.
     *
     * @param name the variable name
     * @return the boolean value, or {@code false} if the variable doesn't exist or is not a boolean
     */
    boolean getBoolean(String name);

    /**
     * Returns the boolean value of the specified variable.
     *
     * @param name         the variable name
     * @param defaultValue the default value if the variable doesn't exist, or is {@code null}
     * @return the boolean value, or {@code defaultValue} if the variable doesn't exist or is not a boolean
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Returns the integer value of the specified variable.
     *
     * @param name the variable name
     * @return the integer value, or {@code -1} if the variable doesn't exist or is not an integer
     */
    int getInt(String name);

    /**
     * Returns the integer value of the specified variable.
     *
     * @param name the variable name
     * @return the integer value, or {@code defaultValue} if the variable doesn't exist or is not an integer
     */
    int getInt(String name, int defaultValue);

    /**
     * Returns the long value of the specified variable.
     *
     * @param name the variable name
     * @return the long value, or {@code -1} if the variable doesn't exist or is not a long
     */
    long getLong(String name);

    /**
     * Returns the long value of the specified variable.
     *
     * @param name the variable name
     * @return the long value, or {@code defaultValue} if the variable doesn't exist or is not a long
     */
    long getLong(String name, long defaultValue);

    /**
     * Replaces any variables in the supplied value.
     *
     * @param value the value. May be {@code null}
     * @return the value with variables replaced, or {@code value} if there were no variables to replace, or
     *         replacement failed
     */
    String replace(String value);

    /**
     * Adds a dynamic variable.
     *
     * @param variable the variable to add
     */
    void add(DynamicVariable variable);

    /**
     * Refreshes dynamic variables.
     *
     * @throws InstallerException if variables cannot be refreshed
     */
    void refresh() throws InstallerException;

    /**
     * Exposes the variables as properties.
     *
     * @return the variables
     */
    Properties getProperties();

    /**
     * Register a set of variable names for blocking from further changes.
     *
     * @param names Variable names
     * @param blocker Blocking object
     */
    void registerBlockedVariableNames(Set<String> names, Object blocker);

    /**
     * Get a set of variable names that are blocked by blocking object.
     *
     * @param blocker Blocking object
     * @return variables
     */
    Set<String> getBlockedVariableNames(Object blocker);

    /**
     * Unregister a set of variable names from blocking from further changes.
     *
     * @param names Variable names
     * @param blocker Blocking object
     */
    void unregisterBlockedVariableNames(Set<String> names, Object blocker);

    /**
     * Whether a variable is currently blocked against being refreshed.
     *
     * @param name the variable name
     */
    boolean isBlockedVariableName(String name);

    /**
     * Whether the override exists of the given name.
     *
     * @return true if the override exists
     */
    boolean containsOverride(String name);

    /**
     * Exposes the variables as properties.
     *
     * @return the variables
     */
    void setOverrides(Overrides overrides);

    /**
     * Return all variables overrides.
     *
     * @return the variables overrides
     */
    Overrides getOverrides();

    /**
     * Sets the current rules engine
     *
     * @param rulesEngine the new rules engine
     */
    void setRules(RulesEngine rulesEngine);
}