/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.userinput.console;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.panels.userinput.console.check.ConsoleCheckField;
import com.izforge.izpack.panels.userinput.console.combo.ConsoleComboField;
import com.izforge.izpack.panels.userinput.console.divider.ConsoleDividerField;
import com.izforge.izpack.panels.userinput.console.file.ConsoleDirField;
import com.izforge.izpack.panels.userinput.console.file.ConsoleFileField;
import com.izforge.izpack.panels.userinput.console.password.ConsolePasswordGroupField;
import com.izforge.izpack.panels.userinput.console.radio.ConsoleRadioField;
import com.izforge.izpack.panels.userinput.console.rule.ConsoleRuleField;
import com.izforge.izpack.panels.userinput.console.spacer.ConsoleSpacerField;
import com.izforge.izpack.panels.userinput.console.staticText.ConsoleStaticText;
import com.izforge.izpack.panels.userinput.console.text.ConsoleTextField;
import com.izforge.izpack.panels.userinput.console.title.ConsoleTitleField;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.field.divider.Divider;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.field.statictext.StaticText;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.field.title.TitleField;
import com.izforge.izpack.util.Console;


/**
 * Factory for {@link ConsoleField} instances.
 *
 * @author Tim Anderson
 */
public class ConsoleFieldFactory
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * The prompt.
     */
    private final Prompt prompt;


    /**
     * Constructs a {@code ConsoleFieldFactory}.
     *
     * @param console the console
     * @param prompt  the prompt
     */
    public ConsoleFieldFactory(Console console, Prompt prompt)
    {
        this.console = console;
        this.prompt = prompt;
    }

    /**
     * Creates a new {@code ConsoleField} to display the supplied field.
     *
     * @param field the field to display
     * @return a new {@code ConsoleField}
     */
    public ConsoleField create(Field field)
    {
        ConsoleField result;
        if (field instanceof CheckField)
        {
            result = new ConsoleCheckField((CheckField) field, console, prompt);
        }
        else if (field instanceof ComboField)
        {
            result = new ConsoleComboField((ComboField) field, console, prompt);
        }
        else if (field instanceof Divider)
        {
            result = new ConsoleDividerField(field, console, prompt);
        }
        else if (field instanceof DirField)
        {
            result = new ConsoleDirField((DirField) field, console, prompt);
        }
        else if (field instanceof FileField)
        {
            result = new ConsoleFileField((FileField) field, console, prompt);
        }
        else if (field instanceof PasswordGroupField)
        {
            result = new ConsolePasswordGroupField((PasswordGroupField) field, console, prompt);
        }
        else if (field instanceof RadioField)
        {
            result = new ConsoleRadioField((RadioField) field, console, prompt);
        }
        else if (field instanceof RuleField)
        {
            result = new ConsoleRuleField((RuleField) field, console, prompt);
        }
        else if (field instanceof Spacer)
        {
            result = new ConsoleSpacerField(field, console, prompt);
        }
        else if (field instanceof StaticText)
        {
            result = new ConsoleStaticText(field, console, prompt);
        }
        else if (field instanceof TextField)
        {
            result = new ConsoleTextField((TextField) field, console, prompt);
        }
        else if (field instanceof TitleField)
        {
            result = new ConsoleTitleField(field, console, prompt);
        }
        else
        {
            throw new IzPackException("Unsupported field type: " + field.getClass().getName());
        }
        return result;
    }
}
