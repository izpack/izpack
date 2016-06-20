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

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.core.variable.ValueImpl;
import com.izforge.izpack.core.variable.utils.ValueUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


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
     * Maps variable names to their stack of blocker objects.
     */
    private transient Map<String, Deque<Object>> blockedVariableNameStacks = new HashMap<String, Deque<Object>>();


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
     * @throws InstallerException if variables cannot be refreshed
     */
    @Override
    public synchronized void refresh() throws InstallerException
    {
        logger.fine("Refreshing dynamic variables");
        Set<DynamicVariable> checkedVariables = new HashSet<DynamicVariable>();
        Set<String> unsetVariables = new HashSet<String>();
        Set<String> setVariables = new HashSet<String>();
        // for dependent dynamic variables a size of dynamicVariables.size()+1 would be enough
        // in case of conditions, which depend on dynamic variables also, we need more iterations
        // to be on the safe side, we take 10*dynamicVariables.size()+1
        int maxCount = 10*dynamicVariables.size()+1;
        int count=maxCount;
        boolean changed = true;
        while (changed) {
            Hashtable<String,String> originalValues = (Hashtable<String,String>) properties.clone();
            changed = false;
            count--;        // decrement number of remaining loops
            if (count<0) {
                throw new InstallerException(
                        String.format("Refresh of dynamic variables seem to produce a loop. "
                                +"Stopped after %1s iterations. "
                                +"(Maybe a cyclic dependency of variables?)", maxCount));

            }
            for (DynamicVariable variable : dynamicVariables)
            {
                String name = variable.getName();
                if (!isBlockedVariableName(name))
                {
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
                                throw new IzPackException("Failed to refresh dynamic variable (" + name + ")", exception);
                            }
                            if (newValue == null)
                            {
                                if (variable.isAutoUnset())
                                {
                                    // Mark unset if dynamic variable cannot be evaluated and failOnError set
                                    unsetVariables.add(name);
                                }
                            }
                            else
                            {
                                set(name, newValue); // Set here for properly set conditions
                                setVariables.add(name);
                            }
                            // FIXME: Possible problem if regular value contains dollar sign (for example password)
                            if (!(newValue == null || ValueUtils.isUnresolved(newValue)))
                            {
                                variable.setChecked();
                            } else {
                                checkedVariables.add(variable);
                            }
                        }
                        else
                        {
                            String previousValue = properties.getProperty(name);
                            if (previousValue != null)
                            {
                                set(name, previousValue); // Set here for properly set conditions
                                setVariables.add(name);
                            }
                        }
                    }
                    else
                    {
                        if (variable.isAutoUnset())
                        {
                            // Mark unset if condition is not true
                            unsetVariables.add(name);
                        }
                    }
                }
                else {
                    logger.fine("Dynamic variable '" + name + "' blocked from changing due to user input");
                }
            }

            for (String key : unsetVariables)
            {
                // Don't unset dynamic variable from one definition, which
                // are set to a value from another one during this refresh
                if (!setVariables.contains(key))
                {
                    if (get(key)!=null)
                    {
                        changed = true;
                        set(key, null);
                    }
                }
            }

            Iterator<String> setIterator = setVariables.iterator();
            while(setIterator.hasNext())
            {
                String key = setIterator.next();
                String newValue = get(key);
                String oldValue = originalValues.get(key);
                if (oldValue==null || !oldValue.equals(newValue))
                {
                    changed = true;
                }
            }
        } // while changed

        for (DynamicVariable variable : checkedVariables)
        {
            variable.setChecked();
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

    @Override
    public void registerBlockedVariableNames(Set<String> names, Object blocker)
    {
        if (names != null)
        {
            for (String name : names)
            {
                Deque<Object> blockerStack = blockedVariableNameStacks.get(name);
                if (blockerStack == null)
                {
                    blockerStack = new ArrayDeque<Object>();
                }
                blockerStack.push(blocker);
                blockedVariableNameStacks.put(name, blockerStack);
            }
        }
    }

    @Override
    public void unregisterBlockedVariableNames(Set<String> names, Object blocker)
    {
        if (names != null)
        {
            for (String name : names)
            {
                Deque<Object> blockerStack = blockedVariableNameStacks.get(name);
                if (blockerStack != null)
                {
                    blockerStack.remove(blocker);
                }
            }
        }
    }

    @Override
    public boolean isBlockedVariableName(String name)
    {
        Deque<Object> blockerStack = blockedVariableNameStacks.get(name);
        return (blockerStack != null && !blockerStack.isEmpty());
    }
}
