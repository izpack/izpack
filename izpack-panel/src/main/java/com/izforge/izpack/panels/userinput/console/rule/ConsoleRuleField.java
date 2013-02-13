/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.panels.userinput.console.rule;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.ConsoleInputField;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.util.Console;


/**
 * Console presentation of {@link RuleField}.
 *
 * @author Tim Anderson
 */
public class ConsoleRuleField extends ConsoleInputField
{
    /**
     * Constructs a {@code ConsoleRuleField}.
     *
     * @param field   the field
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleRuleField(RuleField field, Console console, Prompt prompt)
    {
        super(field, console, prompt);
    }

    /**
     * Validates a value.
     *
     * @param value the value to validate
     * @return the validation status
     */
    @Override
    protected ValidationStatus validate(String value)
    {
        return ((RuleField) getField()).validateFormatted(value);
    }
}
