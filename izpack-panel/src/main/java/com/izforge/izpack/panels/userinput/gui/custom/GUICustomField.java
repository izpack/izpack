package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * JPanel that contains the possible rows of fields defined by the user,
 * along with control buttons to add and remove rows.
 *
 * GUICustomField
 * ===============================|
 * |CustomInputRows               |
 * |------------------------------|
 * |          Row 1               |
 * |          Row 2               |
 * |------------------------------|
 * |ControlButtons                |
 * |------------------------------|
 * |            |  Add  | Remove  |
 * |==============================|
 */
public class GUICustomField extends GUIField
{
    private final CustomInputField customInputField;
    public GUICustomField(CustomField customField, FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec,  GUIInstallData installData, IzPanel parent)
    {
        super(customField);
        customInputField = new CustomInputField(customField, createField, userInputPanelSpec, spec, parent, installData);
        addComponent(customInputField, new TwoColumnConstraints(TwoColumnConstraints.BOTH));
    }

    public List<String> getLabels()
    {
        return customInputField.getLabels();
    }

    public List<String> getVariables()
    {
        return customInputField.getVariables();
    }

    @Override
    public boolean updateField(Prompt prompt)
    {
        return customInputField.updateField(prompt);
    }
}
