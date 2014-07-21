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

package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.panels.userinput.field.button.ButtonField;
import com.izforge.izpack.panels.userinput.field.button.ButtonFieldReader;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.field.check.CheckFieldReader;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.field.custom.CustomFieldReader;
import com.izforge.izpack.panels.userinput.field.divider.Divider;
import com.izforge.izpack.panels.userinput.field.divider.DividerReader;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.DirFieldReader;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.FileFieldReader;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileFieldReader;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupFieldReader;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.rule.RuleFieldReader;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.field.search.SearchFieldReader;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.field.statictext.StaticText;
import com.izforge.izpack.panels.userinput.field.statictext.StaticTextFieldReader;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.field.title.TitleField;
import com.izforge.izpack.panels.userinput.field.title.TitleFieldReader;
import com.izforge.izpack.util.PlatformModelMatcher;


/**
 * Factory for {@link Field}s.
 *
 * @author Tim Anderson
 */
public class FieldFactory
{

    /**
     * The field types.
     */
    enum Type
    {
        BUTTON, CHECK, COMBO, CUSTOM, DIR, DIVIDER, FILE, MULTIFILE, PASSWORD, RADIO, RULE, SPACE, SEARCH, STATICTEXT, TEXT, TITLE
    }

    /**
     * The configuration.
     */
    private final Config config;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;


    /**
     * Constructs a {@code FieldFactory}.
     *
     * @param config      the configuration
     * @param installData the installation data
     * @param matcher     the platform-model matcher
     */
    public FieldFactory(Config config, InstallData installData, PlatformModelMatcher matcher)
    {
        this.config = config;
        this.installData = installData;
        this.matcher = matcher;
    }

    /**
     * Creates a new field.
     *
     * @param element the field element
     * @return a new field
     * @throws IzPackException if the field is invalid
     */
    public Field create(IXMLElement element)
    {
        Field result;
        Type type;
        String value = config.getAttribute(element, "type");
        try
        {
            type = Type.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new IzPackException("Invalid field type: " + value + " in " + config.getContext(element));
        }
        switch (type)
        {
            case BUTTON:
                result = new ButtonField(new ButtonFieldReader(element, config, installData), installData);
                break;
            case CHECK:
                result = new CheckField(new CheckFieldReader(element, config), installData);
                break;
            case COMBO:
                result = new ComboField(new SimpleChoiceReader(element, config, installData), installData);
                break;
            case CUSTOM:
                result = new CustomField(new CustomFieldReader(element, config, matcher, installData), installData);
                break;
            case DIR:
                result = new DirField(new DirFieldReader(element, config), installData);
                break;
            case DIVIDER:
                result = new Divider(new DividerReader(element, config), installData);
                break;
            case FILE:
                result = new FileField(new FileFieldReader(element, config), installData);
                break;
            case MULTIFILE:
                result = new MultipleFileField(new MultipleFileFieldReader(element, config), installData);
                break;
            case PASSWORD:
                result = new PasswordGroupField(new PasswordGroupFieldReader(element, config), installData);
                break;
            case RADIO:
                result = new RadioField(new SimpleChoiceReader(element, config, installData), installData);
                break;
            case RULE:
                result = new RuleField(new RuleFieldReader(element, config), installData, config.getFactory());
                break;
            case SEARCH:
                result = new SearchField(new SearchFieldReader(element, config, matcher), installData);
                break;
            case SPACE:
                result = new Spacer(new SimpleFieldReader(element, config), installData);
                break;
            case STATICTEXT:
                result = new StaticText(new StaticTextFieldReader(element, config), installData);
                break;
            case TEXT:
                result = new TextField(new FieldReader(element, config), installData);
                break;
            case TITLE:
                result = new TitleField(new TitleFieldReader(element, config), installData);
                break;
            default:
                throw new IzPackException("Unsupported field type: " + value + " in " + config.getContext(element));
        }
        return result;
    }
}
