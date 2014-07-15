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

package com.izforge.izpack.panels.userinput.gui.search;

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.FlowLayout;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;


/**
 * Search field view.
 *
 * @author Tim Anderson
 */
public class GUISearchField extends GUIField
{

    /**
     * The component.
     */
    private final SearchInputField searchInputField;

    /**
     * Constructs a {@code GUISearchField}.
     *
     * @param field       the field
     * @param installData the installation data
     * @param frame       the frame
     */
    public GUISearchField(SearchField field, GUIInstallData installData, InstallerFrame frame)
    {
        super(field);
        String filename = field.getFilename();
        String checkFilename = field.getCheckFilename();
        JComboBox combo = new JComboBox();

        combo.setEditable(true);
        combo.setName(field.getVariable());

        for (String choice : field.getChoices())
        {
            combo.addItem(choice);
        }
        combo.setSelectedIndex(field.getSelectedIndex());

        addDescription();
        addLabel();

        Messages messages = installData.getMessages();
        StringBuilder tooltip = new StringBuilder();

        if (filename != null && filename.length() > 0)
        {
            tooltip.append(messages.get("UserInputPanel.search.location", filename));
        }

        boolean showAutodetect = (checkFilename != null) && (checkFilename.length() > 0);
        if (showAutodetect)
        {
            if (tooltip.length() != 0)
            {
                tooltip.append("\n");
            }
            tooltip.append(messages.get("UserInputPanel.search.location.checkedfile", checkFilename));
        }

        if (tooltip.length() > 0)
        {
            combo.setToolTipText(tooltip.toString());
        }

        TwoColumnConstraints east = new TwoColumnConstraints(TwoColumnConstraints.EAST);
        addComponent(combo, east);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JButton autoDetect = ButtonFactory.createButton(messages.get("UserInputPanel.search.autodetect"),
                                                        installData.buttonsHColor);
        autoDetect.setVisible(showAutodetect);
        autoDetect.setToolTipText(messages.get("UserInputPanel.search.autodetect.tooltip"));

        JButton browse = ButtonFactory.createButton(messages.get("UserInputPanel.search.browse"),
                                                    installData.buttonsHColor);

        buttonPanel.add(autoDetect);
        buttonPanel.add(browse);

        addComponent(buttonPanel, new TwoColumnConstraints(TwoColumnConstraints.EASTONLY));
        searchInputField = new SearchInputField(field, frame, combo, autoDetect, browse, installData);
        addTooltip();
    }

    /**
     * Updates the field from the view.
     *
     * @param prompt the prompt to display messages
     * @param skipValidation set to true when wanting to save field data without validating
     * @return {@code true} if the field was updated, {@code false} if the view is invalid
     */
    @Override
    public boolean updateField(Prompt prompt, boolean skipValidation)
    {
        getField().setValue(searchInputField.getResult());
        return true;
    }
}
