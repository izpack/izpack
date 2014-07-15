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

package com.izforge.izpack.api.data;

import java.io.Serializable;
import java.util.List;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public interface DynamicVariable extends Serializable
{
    /**
     * @return the name
     */
    String getName();

    /**
     * @param name the name to set
     */
    void setName(String name);

    /**
     * @return the value
     */
    Value getValue();

    /**
     * @param value the value to set
     */
    void setValue(Value value);

    /**
     * @return the conditionid
     */
    String getConditionid();

    /**
     * @param conditionid the conditionid to set
     */
    void setConditionid(String conditionid);

    void validate() throws Exception;

    String evaluate(VariableSubstitutor... substitutors) throws Exception;

    boolean isCheckonce();
    void setCheckonce(boolean checkonce);

    boolean isChecked();
    void setChecked();

    void setIgnoreFailure(boolean ignore);

    void addFilter(ValueFilter filter);

    List<ValueFilter> getFilters();
}

