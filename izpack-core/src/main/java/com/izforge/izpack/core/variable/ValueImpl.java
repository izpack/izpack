/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010, 2012 René Krell
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

import java.util.Set;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Value;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.variable.utils.ValueUtils;

public abstract class ValueImpl implements Value
{
    private InstallData installData;

    @Override
    public abstract void validate() throws Exception;

    @Override
    public abstract String resolve() throws Exception;

    @Override
    public abstract String resolve(VariableSubstitutor... substitutors) throws Exception;

    @Override
    public InstallData getInstallData()
    {
        return installData;
    }

    @Override
    public void setInstallData(InstallData installData)
    {
        this.installData = installData;
    }

    @Override
    public String toString()
    {
        return this.getClass().getName();
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof ValueImpl))
        {
            return false;
        }
        return true;
    }

    protected static Set<String> parseUnresolvedVariableNames(String... strings)
    {
        return ValueUtils.parseUnresolvedVariableNames(strings);
    }
}
