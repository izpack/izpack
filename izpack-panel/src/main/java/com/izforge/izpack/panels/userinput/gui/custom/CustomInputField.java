package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class CustomInputField extends JPanel implements ActionListener
{
    private GUIInstallData installData;

    private IzPanel parent;

    private List<Field> fields;

    private final static String addCommand = "addComponent";

    private final static String removeCommand = "removeCompoent";

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
        gridBagLayout.columnWidths = new int[] { 190 };
        gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
        gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        
        this.setLayout(gridBagLayout);
        this.addComponents(rows, controlPanel);
        updateControlPanel();
    }

    /**
     * Add components to panel
     *
     * @param rows the groups of components that can be dynamically added and removed
     * @param controlPanel buttons that control when components are added or removed
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
        controlPanelConstraints.anchor = GridBagConstraints.EAST;
        controlPanelConstraints.gridx = 0;
        controlPanelConstraints.gridy = 2;

        add(controlPanel, controlPanelConstraints);
    }

    /**
     * Initialize the control panel
     * The control panel is the row that contains the buttons to add and remove a row.
     * @return
     */
    private JPanel initializeControlPanel()
    {
        JPanel controlPanel = new JPanel(new GridLayout(1, 2));

        JButton addButton = new JButton("add");
        addButton.setActionCommand(addCommand);
        addButton.addActionListener(this);

        JButton removeButton = new JButton("remove");
        removeButton.setEnabled(false);
        removeButton.setActionCommand(removeCommand);
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

        if (actionCommand.equals(addCommand))
        {
            rows.addRow();
        }
        else if (actionCommand.equals(removeCommand))
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
        controlPanel.getComponent(0).setEnabled(rows.atMax());
        controlPanel.getComponent(1).setEnabled(rows.atMin());
        revalidate();
        repaint();
    }

    public boolean updateField(Prompt prompt)
    {
        return rows.updateField(prompt);
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
        for( Component component : header.getComponents())
        {
            component.setEnabled(enabled);
        }
        for( Component component : controlPanel.getComponents())
        {
            if(enabled)
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

