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

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * Avoids Pico allocation before Jupiter evaluates method OS conditions.
 * Jupiter remains responsible for condition diagnostics and disabled reporting.
 */
final class OsEligibility
{
    private static final String ARCHITECTURE = System.getProperty("os.arch");
    private static final String CONDITION_PACKAGE = "org.junit.jupiter.api.condition.";

    private OsEligibility()
    {
    }

    static boolean matches(ExtensionContext context)
    {
        for (ExtensionContext current = context; current != null; current = current.getParent().orElse(null))
        {
            AnnotatedElement element = current.getElement().orElse(null);
            if (!deactivated(context, "EnabledOnOsCondition"))
            {
                EnabledOnOs enabled = AnnotationSupport.findAnnotation(element, EnabledOnOs.class).orElse(null);
                if (enabled != null && !matches(enabled.value(), enabled.architectures()))
                {
                    return false;
                }
            }
            if (!deactivated(context, "DisabledOnOsCondition"))
            {
                DisabledOnOs disabled = AnnotationSupport.findAnnotation(element, DisabledOnOs.class).orElse(null);
                if (disabled != null && (disabled.value().length > 0 || disabled.architectures().length > 0)
                        && matches(disabled.value(), disabled.architectures()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean matches(OS[] systems, String[] architectures)
    {
        return (systems.length == 0 || Arrays.stream(systems).anyMatch(OS::isCurrentOs))
                && (architectures.length == 0
                || Arrays.stream(architectures).anyMatch(ARCHITECTURE::equalsIgnoreCase));
    }

    private static boolean deactivated(ExtensionContext context, String condition)
    {
        String patterns = context.getConfigurationParameter("junit.jupiter.conditions.deactivate").orElse("");
        String name = CONDITION_PACKAGE + condition;
        // Jupiter's documented class-name patterns: comma separated, '.' matches '.' or '$',
        // and '*' matches one or more characters. Do not depend on its internal filter utility.
        return Arrays.stream(patterns.split(",")).map(String::strip).filter(pattern -> !pattern.isEmpty())
                .anyMatch(pattern -> Pattern.matches(pattern.replace("$", "\\$")
                        .replace(".", "[.$]").replace("*", ".+"), name));
    }
}
