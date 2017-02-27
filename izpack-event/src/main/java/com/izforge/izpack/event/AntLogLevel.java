package com.izforge.izpack.event;

/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
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
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Project;

public enum AntLogLevel
{
    ERROR("error", Project.MSG_ERR),
    WARNING("warning", Project.MSG_WARN),
    INFO("info", Project.MSG_INFO),
    VERBOSE("verbose", Project.MSG_VERBOSE),
    DEBUG("debug", Project.MSG_DEBUG);

    private final String name;
    private final int level;

    AntLogLevel(String name, int level)
    {
        this.name = name;
        this.level = level;
    }

    public String getName()
    {
        return name;
    }

    public int getLevel()
    {
        return level;
    }

    private final static Map<String, AntLogLevel> REVERSED;

    static
    {
        REVERSED = new HashMap<String, AntLogLevel>();
        for (AntLogLevel l : values())
        {
            REVERSED.put(l.getName(), l);
        }
    }

    public static AntLogLevel fromName(String name)
    {
        return fromName(name, null);
    }

    public static AntLogLevel fromName(String name, AntLogLevel defaultLevel)
    {
        AntLogLevel level = REVERSED.get(name);
        if (level == null)
        {
            return defaultLevel;
        }
        return level;
    }
}
