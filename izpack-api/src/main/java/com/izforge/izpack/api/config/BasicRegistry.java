/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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
package com.izforge.izpack.api.config;

import com.izforge.izpack.api.config.spi.IniHandler;
import com.izforge.izpack.api.config.spi.RegEscapeTool;
import com.izforge.izpack.api.config.spi.TypeValuesPair;

public class BasicRegistry extends BasicProfile implements Registry
{

    private static final long serialVersionUID = -6432826330714504802L;
    private String version;

    public BasicRegistry()
    {
        this.version = VERSION;
    }

    @Override
    public String getVersion()
    {
        return this.version;
    }

    @Override
    public void setVersion(String value)
    {
        this.version = value;
    }

    @Override
    public Key add(String name)
    {
        return (Key) super.add(name);
    }

    @Override
    public Key get(Object key)
    {
        return (Key) super.get(key);
    }

    @Override
    public Key get(Object key, int index)
    {
        return (Key) super.get(key, index);
    }

    @Override
    public Key put(String key, Section value)
    {
        return (Key) super.put(key, value);
    }

    @Override
    public Key put(String key, Section value, int index)
    {
        return (Key) super.put(key, value, index);
    }

    @Override
    public Key remove(Section section)
    {
        return (Key) super.remove(section);
    }

    @Override
    public Key remove(Object key)
    {
        return (Key) super.remove(key);
    }

    @Override
    public Key remove(Object key, int index)
    {
        return (Key) super.remove(key, index);
    }

    @Override
    Key newSection(String name)
    {
        return new BasicRegistryKey(this, name);
    }

    @Override
    void store(IniHandler formatter, Section section, String option)
    {
        store(formatter, section.getComment(option));
        Type type = ((Key) section).getType(option, Type.REG_SZ);
        String rawName = option.equals(Key.DEFAULT_NAME) ? option : RegEscapeTool.getInstance().quote(option);
        String[] values = new String[section.length(option)];

        for (int i = 0; i < values.length; i++)
        {
            values[i] = section.get(option, i);
        }

        String rawValue = RegEscapeTool.getInstance().encode(new TypeValuesPair(type, values));

        formatter.handleOption(rawName, rawValue);
    }
}
