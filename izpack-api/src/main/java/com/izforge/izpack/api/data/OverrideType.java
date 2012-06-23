/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.api.data;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Override type for pack
 *
 * @author Anthonin Bonnefoy
 */
public enum OverrideType
{
    OVERRIDE_FALSE("false"), OVERRIDE_TRUE("true"), OVERRIDE_ASK_FALSE("askfalse"), OVERRIDE_ASK_TRUE("asktrue"), OVERRIDE_UPDATE("update");

    private static Map<String, OverrideType> lookup;

    private String attribute;

    OverrideType(String attribute)
    {
        this.attribute = attribute;
    }

    static
    {
        lookup = new HashMap<String, OverrideType>();
        for (OverrideType overridetype : EnumSet.allOf(OverrideType.class))
        {
            lookup.put(overridetype.getAttribute(), overridetype);
        }
    }

    public String getAttribute()
    {
        return attribute;
    }

    public static OverrideType getOverrideTypeFromAttribute(String attribute)
    {
        if (attribute != null && lookup.containsKey(attribute))
        {
            return lookup.get(attribute);
        }
        return null;
    }
}
