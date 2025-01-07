/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
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

package com.izforge.izpack.core.variable;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.util.IoHelper;

import java.io.Serializable;
import java.util.Set;

public class EnvironmentValue extends ValueImpl implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = -658114236595736672L;

    public String variable; // mandatory

    public EnvironmentValue(String variable)
    {
        super();
        this.variable = variable;
    }

    public String getVariable()
    {
        return this.variable;
    }

    public void setVariable(String variable)
    {
        this.variable = variable;
    }

    @Override
    public void validate() throws Exception
    {
        if (this.variable == null || this.variable.length() <= 0)
        {
            throw new Exception("No or empty environment variable name");
        }
    }

    @Override
    public String resolve()
    {
        return System.getenv(variable);
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors) throws Exception
    {
        String _variable_ = variable;
        for (VariableSubstitutor substitutor : substitutors)
        {
            _variable_ = substitutor.substitute(_variable_);
        }

        return System.getenv(_variable_);
    }

    @Override
    public Set<String> getVarRefs()
    {
        return parseUnresolvedVariableNames(variable);
    }

}
