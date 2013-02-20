/*
 * $Id: Compiler.java 1918 2007-11-29 14:02:17Z dreil $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil, 2010 René Krell
 *
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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.DynamicVariable;
import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.variable.PlainValue;

public class DynamicVariableImpl implements DynamicVariable
{
    private static final long serialVersionUID = -7985397187206803090L;

    private static final transient Logger logger = Logger.getLogger(DynamicVariableImpl.class.getName());

    private String name;

    private Value value;

    private String conditionid;

    private List<ValueFilter> filters;

    private boolean checkonce = false;

    private boolean ignorefailure = true;

    private transient String currentValue;

    public DynamicVariableImpl() {}

    public DynamicVariableImpl(String name, String value) {
        setName(name);
        setValue(new PlainValue(value));
    }

    @Override
    public void addFilter(ValueFilter filter)
    {
        if (filters == null)
        {
            filters = new LinkedList<ValueFilter>();
        }
        filters.add(filter);
    }

    @Override
    public List<ValueFilter> getFilters()
    {
        return filters;
    }

    @Override
    public void validate() throws Exception
    {
        if (name == null)
        {
            throw new Exception("No dynamic variable name defined");
        }

        if (value == null)
        {
            throw new Exception("No dynamic variable value defined for variable " + name);
        }

        value.validate();

        if (filters != null)
        {
            for (ValueFilter filter : filters)
            {
                filter.validate();
            }
        }
    }

    private String filterValue(String value, VariableSubstitutor... substitutors) throws Exception
    {
        String newValue = value;

        if (value != null && filters != null)
        {
            logger.fine("Dynamic variable before filtering: " + name + "=" + newValue);
            for (ValueFilter filter : filters)
            {
                newValue = filter.filter(newValue, substitutors);
                logger.fine("Dynamic variable after applying filter "
                                    + filter.getClass().getSimpleName() + ": " + name + "=" + newValue);
            }
        }

        return newValue;
    }

    @Override
    public String evaluate(VariableSubstitutor... substitutors) throws Exception
    {
        String newValue = currentValue;

        if (value == null)
        {
            return null;
        }

        if (checkonce && currentValue != null)
        {
            return filterValue(currentValue, substitutors);
        }

        try
        {
            newValue = value.resolve(substitutors);

            if (checkonce)
            {
                currentValue = newValue;
            }

            newValue = filterValue(newValue, substitutors);
        }
        catch (Exception e)
        {
            if (!ignorefailure)
            {
                throw e;
            }
            logger.log(Level.WARNING,
                       "Error evaluating dynamic variable '" + getName() + "': " + e,
                       e);
        }

        return newValue;
    }

    /**
     * @return the name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    @Override
    public void setName(String name)
    {
        if (name != null)
        {
            this.name = name;
        }
    }

    /**
     * @return the value
     */
    @Override
    public Value getValue()
    {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    @Override
    public void setValue(Value value)
    {
        if (value != null)
        {
            this.value = value;
        }
    }

    /**
     * @return the conditionid
     */
    @Override
    public String getConditionid()
    {
        return this.conditionid;
    }

    /**
     * @param conditionid the conditionid to set
     */
    @Override
    public void setConditionid(String conditionid)
    {
        if (conditionid != null)
        {
            this.conditionid = conditionid;
        }
    }

    public boolean isCheckonce()
    {
        return checkonce;
    }

    @Override
    public void setCheckonce(boolean checkonce)
    {
        this.checkonce = checkonce;
    }

    public boolean isIgnoreFailure()
    {
        return ignorefailure;
    }

    @Override
    public void setIgnoreFailure(boolean ignore)
    {
        this.ignorefailure = ignore;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof DynamicVariable))
        {
            return false;
        }
        DynamicVariable compareObj = (DynamicVariable) obj;
        return (name.equals(compareObj.getName())
                && (conditionid == null || conditionid.equals(compareObj.getConditionid())));
    }

    @Override
    public int hashCode()
    {
        // TODO: check if this always correct
        return name.hashCode() ^ conditionid.hashCode();
    }

}
