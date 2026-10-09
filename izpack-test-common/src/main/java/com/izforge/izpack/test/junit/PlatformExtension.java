/*
 * IzPack - Copyright 2001-2026 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.test.junit;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;

/** Jupiter platform filtering with the same platform-family matching as RunOn. */
public class PlatformExtension implements ExecutionCondition
{
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
    {
        // Class-level rejection would prevent a method restriction from overriding it.
        if (context.getTestMethod().isEmpty())
        {
            return ConditionEvaluationResult.enabled("Platform restrictions are evaluated per method");
        }
        return matches(context.getRequiredTestMethod(), new Platforms().getCurrentPlatform())
                ? ConditionEvaluationResult.enabled("Eligible platform")
                : ConditionEvaluationResult.disabled("Excluded by @RunOn");
    }

    static boolean matches(Method method, Platform platform)
    {
        RunOn restriction = method.getAnnotation(RunOn.class);
        if (restriction == null)
        {
            restriction = method.getDeclaringClass().getAnnotation(RunOn.class);
        }
        if (restriction == null)
        {
            return true;
        }
        for (Platform.Name name : restriction.value())
        {
            if (platform.isA(name))
            {
                return true;
            }
        }
        return false;
    }
}
