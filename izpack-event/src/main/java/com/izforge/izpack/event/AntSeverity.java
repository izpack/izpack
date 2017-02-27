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

import com.izforge.izpack.api.handler.Prompt;

public enum AntSeverity
{
    ERROR("error", Prompt.Type.ERROR),
    WARNING("warning", Prompt.Type.WARNING),
    INFO("info", Prompt.Type.INFORMATION);

    private final String name;
    private final Prompt.Type level;

    AntSeverity(String name, Prompt.Type level)
    {
        this.name = name;
        this.level = level;
    }

    public String getName()
    {
        return name;
    }

    public Prompt.Type getLevel()
    {
        return level;
    }

    private final static Map<String, AntSeverity> REVERSED;

    static
    {
        REVERSED = new HashMap<String, AntSeverity>();
        for (AntSeverity l : values())
        {
            REVERSED.put(l.getName(), l);
        }
    }

    public static AntSeverity fromName(String name)
    {
        return fromName(name, null);
    }

    public static AntSeverity fromName(String name, AntSeverity defaultLevel)
    {
        AntSeverity level = REVERSED.get(name);
        if (level == null)
        {
            return defaultLevel;
        }
        return level;
    }
}
