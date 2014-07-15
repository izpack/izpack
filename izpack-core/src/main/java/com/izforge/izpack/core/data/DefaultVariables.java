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

package com.izforge.izpack.core.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;


/**
 * Default implementation of the {@link Variables} interface.
 *
 * @author Tim Anderson
 */
public class DefaultVariables implements Variables
{

    /**
     * The variables.
     */
    private final Properties properties;

    /**
     * The dynamic variables.
     */
    private List<DynamicVariable> dynamicVariables = new ArrayList<DynamicVariable>();

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor replacer;

    /**
     * The rules for evaluating dynamic variable conditions.
     */
    private RulesEngine rules;


    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(DefaultVariables.class.getName());

    /**
     * Constructs a <tt>DefaultVariables</tt>, with an empty set of variables.
     */
    public DefaultVariables()
    {
        this(new Properties());
    }

    /**
     * Constructs a <tt>DefaultVariables</tt>, from properties.
     *
     * @param properties the properties
     */
    public DefaultVariables(Properties properties)
    {
        this.properties = properties;
        replacer = new VariableSubstitutorImpl(properties);
    }

    /**
     * Sets the rules, used for dynamic variable evaluation.
     *
     * @param rules the rules
     */
    public void setRules(RulesEngine rules)
    {
        this.rules = rules;
    }

    /**
     * Sets a variable.
     *
     * @param name  the variable name
     * @param value the variable value. May be {@code null}
     */
    @Override
    public void set(String name, String value)
    {
        if (value != null)
        {
            properties.setProperty(name, value);
            logger.fine("Dynamic variable '" + name + "' set to '" + value + "'");
        }
        else
        {
            properties.remove(name);
            logger.fine("Dynamic variable '" + name + "' unset");
        }
    }

    /**
     * Returns the value of the specified variable.
     *
     * @param name the variable name
     * @return the value. May be {@code null}
     */
    @Override
    public String get(String name)
    {
        return properties.getProperty(name);
    }

    /**
     * Returns the value of the specified variable.
     *
     * @param name         the variable name
     * @param defaultValue the default value if the variable doesn't exist, or is {@code null}
     */
    @Override
    public String get(String name, String defaultValue)
    {
        return properties.getProperty(name, defaultValue);
    }

    /**
     * Returns the boolean value of the specified variable.
     *
     * @param name the variable name
     * @return the boolean value, or {@code false} if the variable doesn't exist or is not a boolean
     */
    @Override
    public boolean getBoolean(String name)
    {
        return getBoolean(name, false);
    }

    /**
     * Returns the boolean value of the specified variable.
     *
     * @param name         the variable name
     * @param defaultValue the default value if the variable doesn't exist, or is {@code null}
     * @return the boolean value, or {@code defaultValue} if the variable doesn't exist or is not a boolean
     */
    @Override
    public boolean getBoolean(String name, boolean defaultValue)
    {
        String value = get(name);
        if (value == null)
        {
            return defaultValue;
        }
        else if (value.equalsIgnoreCase("true"))
        {
            return true;
        }
        else if (value.equalsIgnoreCase("false"))
        {
            return false;
        }
        return defaultValue;
    }

    /**
     * Returns the integer value of the specified variable.
     *
     * @param name the variable name
     * @return the integer value, or {@code -1} if the variable doesn't exist or is not an integer
     */
    @Override
    public int getInt(String name)
    {
        return getInt(name, -1);
    }

    /**
     * Returns the integer value of the specified variable.
     *
     * @param name the variable name
     * @return the integer value, or {@code defaultValue} if the variable doesn't exist or is not an integer
     */
    @Override
    public int getInt(String name, int defaultValue)
    {
        int result = defaultValue;
        String value = get(name);
        if (value != null)
        {
            try
            {
                result = Integer.valueOf(value);
            }
            catch (NumberFormatException ignore)
            {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Returns the long value of the specified variable.
     *
     * @param name the variable name
     * @return the long value, or {@code -1} if the variable doesn't exist or is not a long
     */
    @Override
    public long getLong(String name)
    {
        return getLong(name, -1);
    }

    /**
     * Returns the long value of the specified variable.
     *
     * @param name the variable name
     * @return the long value, or {@code defaultValue} if the variable doesn't exist or is not a long
     */
    @Override
    public long getLong(String name, long defaultValue)
    {
        long result = defaultValue;
        String value = get(name);
        if (value != null)
        {
            try
            {
                result = Long.valueOf(value);
            }
            catch (NumberFormatException ignore)
            {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Replaces any variables in the supplied value.
     *
     * @param value the value. May be {@code null}
     * @return the value with variables replaced, or {@code value} if there were no variables to replace, or
     *         replacement failed
     */
    @Override
    public String replace(String value)
    {
        if (value != null)
        {
            try
            {
                value = replacer.substitute(value);
            }
            catch (Exception exception)
            {
                logger.log(Level.WARNING, exception.getMessage(), exception);
            }
        }
        return value;
    }

    /**
     * Adds a dynamic variable.
     *
     * @param variable the variable to add
     */
    @Override
    public synchronized void add(DynamicVariable variable)
    {
        dynamicVariables.add(variable);
    }

    /**
     * Refreshes dynamic variables.
     *
     * @throws IzPackException if variables cannot be refreshed
     */
    @Override
    public synchronized void refresh()
    {
        logger.fine("Refreshing dynamic variables");

        Properties setVariables = new Properties();
        Set<String> unsetVariables = new HashSet<String>();

        for (DynamicVariable variable : dynamicVariables)
        {
            String name = variable.getName();
            String conditionId = variable.getConditionid();
            if (conditionId == null || rules.isConditionTrue(conditionId))
            {
                if (!(variable.isCheckonce() && variable.isChecked()))
                {
                    String newValue;
                    try
                    {
                        newValue = variable.evaluate(replacer);
                    }
                    catch (IzPackException exception)
                    {
                        throw exception;
                    }
                    catch (Exception exception)
                    {
                        throw new IzPackException("Failed to refresh dynamic variables (" + name + ")", exception);
                    }
                    if (newValue == null)
                    {
                        // Mark unset if dynamic variable cannot be evaluated and failOnError set
                        unsetVariables.add(name);
                    }
                    else
                    {
                        setVariables.put(name, newValue);
                    }
                    variable.setChecked();
                }
                else
                {
                    String oldvalue = properties.getProperty(name);
                    if (oldvalue != null)
                    {
                        setVariables.put(name, oldvalue);
                    }
                }
            }
            else
            {
                // Mark unset if condition is not true
                unsetVariables.add(name);
            }
        }

        for (String key : unsetVariables)
        {
            // Don't unset dynamic variable from one definition, which
            // are set to a value from another one during this refresh
            if (!setVariables.containsKey(key))
            {
                set(key, null);
            }
        }

        for (String key : setVariables.stringPropertyNames())
        {
            set(key, setVariables.getProperty(key));
        }
    }

    /**
     * Exposes the variables as properties.
     *
     * @return the variables
     */
    @Override
    public Properties getProperties()
    {
        return properties;
    }

}
