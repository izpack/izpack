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

import java.io.FileInputStream;
import java.io.Serializable;

public class PlainConfigFileValue extends ConfigFileValue implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 7838077964844413198L;

    public String location; // mandatory

    public PlainConfigFileValue(String location, int type, String section, String key, boolean escape)
    {
        super(type, section, key, escape);
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    @Override
    public void validate() throws Exception
    {
        super.validate();
        if (this.location == null || this.location.length() <= 0)
        {
            throw new Exception("No or empty plain configuration file path");
        }
    }

    @Override
    public String resolve() throws Exception
    {
        return resolve(new FileInputStream(location));
    }

    @Override
    public String resolve(VariableSubstitutor... substitutors)
            throws Exception
    {
        String _location_ = location;
        for (VariableSubstitutor substitutor : substitutors)
        {
            _location_ = substitutor.substitute(_location_);
        }
        return resolve(new FileInputStream(_location_), substitutors);
    }
}
