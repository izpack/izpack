/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
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

import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.rules.CompareCondition;
import com.izforge.izpack.api.rules.ComparisonOperator;

public class CompareNumericsCondition extends CompareCondition
{
    private static final long serialVersionUID = 3463371028276391505L;

    private static final transient Logger logger = Logger.getLogger(CompareNumericsCondition.class.getName());

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
            try
            {
                int leftValue = Integer.valueOf(arg1);
                int rightValue = Integer.valueOf(arg2);
                switch (operator)
                {
                    case EQUAL:
                        result = leftValue == rightValue;
                        break;
                    case NOTEQUAL:
                        result = leftValue != rightValue;
                        break;
                    case GREATER:
                        result = leftValue > rightValue;
                        break;
                    case GREATEREQUAL:
                        result = leftValue >= rightValue;
                        break;
                    case LESS:
                        result = leftValue < rightValue;
                        break;
                    case LESSEQUAL:
                        result = leftValue <= rightValue;
                        break;
                    default:
                        break;
                }
            }
            catch (NumberFormatException nfe)
            {
                logger.warning("One of the values to compare is not in numeric format");
            }
        }
        return result;
    }
}