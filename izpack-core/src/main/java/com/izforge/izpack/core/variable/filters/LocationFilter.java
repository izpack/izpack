/*
 * IzPack - Copyright 2026 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.core.variable.filters;

import org.apache.commons.io.FilenameUtils;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import java.util.Objects;

public class LocationFilter implements ValueFilter
{

    private static final long serialVersionUID = 5557014780732715339L;

    public String baseDir;

    public LocationFilter(String baseDir)
    {
        this.baseDir = baseDir;
    }

    public String getBaseDir()
    {
        return this.baseDir;
    }

    public void setBaseDir(String baseDir)
    {
        this.baseDir = baseDir;
    }

    @Override
    public void validate() throws Exception
    {
        // Nothing to be checked
    }

    @Override
    public String filter(String value, VariableSubstitutor... substitutors) throws Exception
    {
        String _baseDir_ = baseDir;
        for (VariableSubstitutor substitutor : substitutors)
        {
            _baseDir_ = substitutor.substitute(_baseDir_);
        }

        return FilenameUtils.concat(_baseDir_, value);
    }

    @Override
    public String toString()
    {
        return "(location: " + baseDir + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof LocationFilter))
        { return false; }
        return baseDir.equals(((LocationFilter) obj).getBaseDir());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(baseDir);
    }
}
