/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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

package com.izforge.izpack.panels.userinput.field.rule;

import com.izforge.izpack.panels.userinput.field.TestFieldConfig;


/**
 * Implementation of {@link RuleFieldConfig} for testing purposes.
 *
 * @author Tim Anderson
 */
public class TestRuleFieldConfig extends TestFieldConfig implements RuleFieldConfig
{

    /**
     * The field layout.
     */
    private final String layout;

    /**
     * The field separator.
     */
    private final String separator;

    /**
     * The rule format.
     */
    private final RuleFormat format;

    /**
     * Constructs a {@code TestRuleFieldConfig}.
     *
     * @param variable  the variable
     * @param layout    the field layout
     * @param separator the field separator
     * @param format    the rule format
     */
    public TestRuleFieldConfig(String variable, String layout, String separator, RuleFormat format)
    {
        super(variable);
        this.layout = layout;
        this.separator = separator;
        this.format = format;
    }

    /**
     * Returns the field layout.
     *
     * @return the field layout
     */
    @Override
    public String getLayout()
    {
        return layout;
    }

    /**
     * Returns the field separator.
     * <p/>
     * This is a string used for separating the contents of individual fields.
     *
     * @return the field separator. May be {@code null}
     */
    @Override
    public String getSeparator()
    {
        return separator;
    }

    /**
     * Returns the field format.
     *
     * @return the field format
     */
    @Override
    public RuleFormat getFormat()
    {
        return format;
    }
}
