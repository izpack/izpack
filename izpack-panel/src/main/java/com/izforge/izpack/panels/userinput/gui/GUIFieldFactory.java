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

package com.izforge.izpack.panels.userinput.gui;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.button.ButtonField;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.field.divider.Divider;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.field.statictext.StaticText;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.field.title.TitleField;
import com.izforge.izpack.panels.userinput.gui.button.GUIButtonField;
import com.izforge.izpack.panels.userinput.gui.check.GUICheckField;
import com.izforge.izpack.panels.userinput.gui.combo.GUIComboField;
import com.izforge.izpack.panels.userinput.gui.custom.GUICustomField;
import com.izforge.izpack.panels.userinput.gui.divider.GUIDivider;
import com.izforge.izpack.panels.userinput.gui.file.GUIDirField;
import com.izforge.izpack.panels.userinput.gui.file.GUIFileField;
import com.izforge.izpack.panels.userinput.gui.file.GUIMultipleFileField;
import com.izforge.izpack.panels.userinput.gui.password.GUIPasswordGroupField;
import com.izforge.izpack.panels.userinput.gui.radio.GUIRadioField;
import com.izforge.izpack.panels.userinput.gui.rule.GUIRuleField;
import com.izforge.izpack.panels.userinput.gui.search.GUISearchField;
import com.izforge.izpack.panels.userinput.gui.space.GUISpacer;
import com.izforge.izpack.panels.userinput.gui.statictext.GUIStaticText;
import com.izforge.izpack.panels.userinput.gui.text.GUITextField;
import com.izforge.izpack.panels.userinput.gui.title.GUITitleField;

/**
 * Factory for {@link GUIField}s.
 *
 * @author Tim Anderson
 */
public class GUIFieldFactory
{

    /**
     * The installation data.
     */
    private final GUIInstallData installData;

    /**
     * The parent panel.
     */
    private final IzPanel parent;

    /**
     * The installer frame.
     */
    private final InstallerFrame frame;

    /**
     * The prompt.
     */
    private final Prompt prompt;


    /**
     * Constructs a {@code GUIFieldFactory}.
     *
     * @param installData the installation data
     * @param parent      the parent panel
     * @param frame       the installer frame
     * @param prompt      the prompt
     */
    public GUIFieldFactory(GUIInstallData installData, IzPanel parent, InstallerFrame frame, Prompt prompt)
    {
        this.installData = installData;
        this.parent = parent;
        this.frame = frame;
        this.prompt = prompt;
    }

    /**
     * Creates a view to display the supplied field.
     *
     * @param field the field
     * @return the view to display the field
     * @throws IzPackException if the view cannot be created
     */
    public GUIField create(Field field, UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        GUIField result;
        if (field instanceof RuleField)
        {
            result = new GUIRuleField((RuleField) field);
        }
        else if (field instanceof TextField)
        {
            result = new GUITextField((TextField) field);
        }
        else if (field instanceof ComboField)
        {
            result = new GUIComboField((ComboField) field);
        }
        else if (field instanceof RadioField)
        {
            result = new GUIRadioField((RadioField) field);
        }
        else if (field instanceof PasswordGroupField)
        {
            result = new GUIPasswordGroupField((PasswordGroupField) field);
        }
        else if (field instanceof Spacer)
        {
            result = new GUISpacer((Spacer) field);
        }
        else if (field instanceof Divider)
        {
            result = new GUIDivider((Divider) field);
        }
        else if (field instanceof CheckField)
        {
            result = new GUICheckField((CheckField) field);
        }
        else if (field instanceof StaticText)
        {
            result = new GUIStaticText((StaticText) field);
        }
        else if (field instanceof TitleField)
        {
            result = new GUITitleField((TitleField) field, installData, frame.getIcons());
        }
        else if (field instanceof SearchField)
        {
            result = new GUISearchField((SearchField) field, installData, frame);
        }
        else if (field instanceof FileField)
        {
            result = new GUIFileField((FileField) field, installData, parent, prompt);
        }
        else if (field instanceof DirField)
        {
            result = new GUIDirField((DirField) field, installData, parent, prompt);
        }
        else if (field instanceof MultipleFileField)
        {
            result = new GUIMultipleFileField((MultipleFileField) field, installData, frame);
        }
        else if (field instanceof ButtonField)
        {
            result = new GUIButtonField((ButtonField) field);
        }
        else if (field instanceof CustomField)
        {
            result = createCustom((CustomField) field, userInputPanelSpec, spec);
        }
        else
        {
            throw new IzPackException("Unsupported field type: " + field.getClass().getName());
        }
        return result;
    }

    /**
     * Creates a view to display the supplied field.
     * This field is a container for fields to be placed in columns.
     *
     * @param customField field of type CustomField
     * @return the view to display the field
     * @throws IzPackException if the view cannot be created
     */
    public GUIField createCustom(CustomField customField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        FieldCommand fieldCommand = new createFieldCommand(userInputPanelSpec, spec);
        return new GUICustomField(customField, fieldCommand, userInputPanelSpec, spec, installData, parent);
    }

    /**
     * Private class to wrap the create command.
     * This allows us to pass the create command for user later on.
     */
    private class createFieldCommand extends FieldCommand
    {
        private final UserInputPanelSpec userInputPanelSpec;
        private final IXMLElement spec;
        public createFieldCommand(UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
        {
            this.userInputPanelSpec = userInputPanelSpec;
            this.spec = spec;
        }
        public GUIField createGuiField(Field field)
        {
            return create(field, userInputPanelSpec, spec);
        }
    }
}

