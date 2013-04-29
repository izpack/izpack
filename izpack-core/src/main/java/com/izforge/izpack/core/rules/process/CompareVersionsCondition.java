/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.core.rules.process;

import java.util.Comparator;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.CompareCondition;
import com.izforge.izpack.api.rules.ComparisonOperator;

public class CompareVersionsCondition extends CompareCondition
{
    private static final long serialVersionUID = 5605592864539142416L;

    @Override
    public boolean isTrue()
    {
        boolean result = false;
        InstallData installData = getInstallData();
        if (installData != null && operand1 != null && operand2 != null)
        {
            Variables variables = installData.getVariables();
            String arg1 = variables.replace(operand1);
            String arg2 = variables.replace(operand2);
            if (operator == null)
            {
                operator = ComparisonOperator.EQUAL;
            }
            int res = new VersionStringComparator().compare(arg1, arg2);

            switch (operator)
            {
                case EQUAL:
                    result = (res == 0);
                    break;
                case NOTEQUAL:
                    result = (res != 0);
                    break;
                case GREATER:
                    result = (res > 0);
                    break;
                case GREATEREQUAL:
                    result = (res >= 0);
                    break;
                case LESS:
                    result = (res < 0);
                    break;
                case LESSEQUAL:
                    result = (res <= 0);
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    private static class VersionStringComparator implements Comparator<String>
    {
        @Override
        public int compare(String s1, String s2)
        {
            if (s1 == null && s2 == null)
            {
                return 0;
            }
            else if (s1 == null)
            {
                return -1;
            }
            else if (s2 == null)
            {
                return 1;
            }

            String[]
                    arr1 = s1.split("[^a-zA-Z0-9_]+"),
                    arr2 = s2.split("[^a-zA-Z0-9_]+");

            int i1, i2, i3;

            for (int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++)
            {
                if (ii == arr1.length)
                {
                    return ii == arr2.length ? 0 : -1;
                }
                else if (ii == arr2.length)
                {
                    return 1;
                }

                try
                {
                    i1 = Integer.parseInt(arr1[ii]);
                }
                catch (Exception x)
                {
                    i1 = Integer.MAX_VALUE;
                }

                try
                {
                    i2 = Integer.parseInt(arr2[ii]);
                }
                catch (Exception x)
                {
                    i2 = Integer.MAX_VALUE;
                }

                if (i1 != i2)
                {
                    return i1 - i2;
                }

                i3 = arr1[ii].compareTo(arr2[ii]);

                if (i3 != 0)
                {
                    return i3;
                }
            }

            return 0;
        }
    }
}