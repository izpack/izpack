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

package com.izforge.izpack.api.rules;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Comparison operators that can be used for in conditions
 *
 * @author Rene Krell
 */
public enum ComparisonOperator
{
    EQUAL("eq"), NOTEQUAL("ne"),
    LESSEQUAL("leq"), LESS("lt"),
    GREATEREQUAL("geq"), GREATER("gt");

    private static Map<String, ComparisonOperator> lookup;

    private String attribute;

    ComparisonOperator(String attribute)
    {
        this.attribute = attribute;
    }

    static
    {
        lookup = new HashMap<String, ComparisonOperator>();
        for (ComparisonOperator op : EnumSet.allOf(ComparisonOperator.class))
        {
            lookup.put(op.getAttribute(), op);
        }
    }

    public String getAttribute()
    {
        return attribute;
    }

    public static ComparisonOperator getComparisonOperatorFromAttribute(String attribute)
    {
        if (attribute != null && lookup.containsKey(attribute))
        {
            return lookup.get(attribute);
        }
        return null;
    }
}
