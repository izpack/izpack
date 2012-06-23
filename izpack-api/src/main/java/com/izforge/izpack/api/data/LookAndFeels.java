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
 * @author Anthonin Bonnefoy
 */
public enum LookAndFeels
{
    LIQUID("liquid"), LOOKS("looks"), SUBSTANCE("substance"), NIMBUS("nimbus"), KUNSTSTOFF("kunststoff");

    private String name;

    private static Map<String, LookAndFeels> lookup;

    static
    {
        lookup = new HashMap<String, LookAndFeels>();
        EnumSet<LookAndFeels> enumSet = EnumSet.allOf(LookAndFeels.class);
        for (LookAndFeels elem : enumSet)
        {
            lookup.put(elem.name, elem);
        }
    }

    LookAndFeels(String name)
    {
        this.name = name;
    }

    public static LookAndFeels lookup(String laf)
    {
        if (lookup.containsKey(laf))
        {
            return lookup.get(laf);
        }
        return null;
    }

}
