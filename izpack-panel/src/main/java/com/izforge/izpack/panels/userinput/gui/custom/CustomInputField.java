package com.izforge.izpack.panels.userinput.gui.custom;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;

/**
 * JPanel that contains the possible rows of fields defined by the user, along
 * with control buttons to add and remove rows.
 *
 * GUICustomField ===============================| |CustomInputRows |
 * |------------------------------| | Row 1 | | Row 2 |
 * |------------------------------| |ControlButtons |
 * |------------------------------| | | Add | Remove |
 * |==============================|
 */
public class CustomInputField extends JPanel implements ActionListener
{

    /**
     *
     */
    private static final long serialVersionUID = -5954748826095621101L;

    private GUIInstallData installData;

    private IzPanel parent;

    private List<Field> fields;

    private final static String ADD_COMMAND = "addComponent";

    private final static String REMOVE_COMMAND = "removeCompoent";

    private JPanel controlPanel;

    private JPanel header;

    private CustomInputRows rows;

    public CustomInputField(CustomField customField, FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec, IzPanel parent, GUIInstallData installData)
    {
        this.parent = parent;
        this.installData = installData;

        this.rows = new CustomInputRows(customField, createField, userInputPanelSpec, spec, installData);
        this.header = rows.getHeader();
        this.controlPanel = initializeControlPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();

        this.setLayout(gridBagLayout);

        this.addComponents(rows, controlPanel);
        updateControlPanel();
    }

    /**
     * Add components to panel
     *
     * @param rows the groups of components that can be dynamically added and
     * removed
     * @param controlPanel buttons that control when components are added or
     * removed
     */
    public void addComponents(JPanel rows, JPanel controlPanel)
    {
        GridBagConstraints headerConstraints = new GridBagConstraints();
        headerConstraints.fill = GridBagConstraints.BOTH;
        headerConstraints.anchor = GridBagConstraints.CENTER;
        headerConstraints.gridx = 0;
        headerConstraints.gridy = 0;

        add(header, headerConstraints);

        GridBagConstraints rowConstraints = new GridBagConstraints();
        rowConstraints.fill = GridBagConstraints.BOTH;
        rowConstraints.anchor = GridBagConstraints.CENTER;
        rowConstraints.gridx = 0;
        rowConstraints.gridy = 1;

        add(rows, rowConstraints);

        GridBagConstraints controlPanelConstraints = new GridBagConstraints();
        controlPanelConstraints.fill = GridBagConstraints.NONE;
        controlPanelConstraints.anchor = GridBagConstraints.SOUTHEAST;
        controlPanelConstraints.gridx = 0;
        controlPanelConstraints.gridy = 2;
        controlPanelConstraints.weighty = 1.0; //request any extra vertical space
        controlPanelConstraints.insets = new Insets(5, 0, 0, 0); //top padding

        add(controlPanel, controlPanelConstraints);
    }

    /**
     * Initialize the control panel The control panel is the row that contains
     * the buttons to add and remove a row.
     *
     * @return
     */
    private JPanel initializeControlPanel()
    {
        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        Messages messages = installData.getMessages();

        JButton addButton = ButtonFactory.createButton(messages.get("UserInputPanel.custom.swing.buttonlabel.add"),
                installData.buttonsHColor);
        addButton.setActionCommand(ADD_COMMAND);
        addButton.addActionListener(this);

        JButton removeButton = ButtonFactory.createButton(messages.get("UserInputPanel.custom.swing.buttonlabel.remove"),
                installData.buttonsHColor);
        removeButton.setEnabled(false);
        removeButton.setActionCommand(REMOVE_COMMAND);
        removeButton.addActionListener(this);

        controlPanel.add(addButton);
        controlPanel.add(removeButton);
        return controlPanel;
    }

    /**
     * Action events for the add and remove buttons.
     *
     * @param actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();

        if (actionCommand.equals(ADD_COMMAND))
        {
            rows.addRow();
        }
        else if (actionCommand.equals(REMOVE_COMMAND))
        {
            rows.removeRow();
        }

        updateControlPanel();
    }

    /**
     * Update the panel view.
     */
    private void updateControlPanel()
    {
        if (rows.hideRowControls())
        {
            controlPanel.getComponent(0).setVisible(false);
            controlPanel.getComponent(1).setVisible(false);
        }
        else
        {
            controlPanel.getComponent(0).setEnabled(rows.atMax());
            controlPanel.getComponent(1).setEnabled(rows.atMin());
        }
        revalidate();
        repaint();
    }

    public boolean updateField(Prompt prompt, boolean skipValidation)
    {
        return rows.updateField(prompt, skipValidation);
    }

    public List<String> getLabels()
    {
        return rows.getLabels();
    }

    public List<String> getVariables()
    {
        return rows.getVariables();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        for (Component component : header.getComponents())
        {
            component.setEnabled(enabled);
        }
        for (Component component : controlPanel.getComponents())
        {
            if (enabled)
            {
                updateControlPanel();
            }
            else
            {
                component.setEnabled(enabled);
            }

        }
        for (Component component : rows.getComponents())
        {
            component.setEnabled(enabled);
        }
        rows.setEnabled(enabled);
    }

}
