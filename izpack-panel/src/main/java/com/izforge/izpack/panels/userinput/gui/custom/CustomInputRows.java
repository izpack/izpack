package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.panels.userinput.FieldCommand;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.ValidationStatus;
import com.izforge.izpack.panels.userinput.field.custom.Column;
import com.izforge.izpack.panels.userinput.field.custom.CustomField;
import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPanel that contains the possible rows of fields defined by the user.
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

public class CustomInputRows extends JPanel
{
    private final UserInputPanelSpec userInputPanelSpec;

    private final IXMLElement spec;

    private final FieldCommand createField;

    private int numberOfRows = 0;

    private final int numberOfColumns;

    private Map<Integer, List<GUIField>> guiFields;

    private final CustomField customInfoField;

    private final int maxRow;

    private final int minRow;

    private final List<String> labels;

    private GUIInstallData installData;

    public CustomInputRows(CustomField customField, FieldCommand createField, UserInputPanelSpec userInputPanelSpec, IXMLElement spec, GUIInstallData installData)
    {
        super();
        this.spec = spec;
        this.userInputPanelSpec = userInputPanelSpec;
        this.createField = createField;
        this.customInfoField = customField;
        this.numberOfColumns = getNumberOfColumns(customInfoField);
        this.maxRow = customField.getMaxRow();
        this.minRow = customField.getMinRow();
        this.labels = getLabels(customInfoField);
        this.guiFields = new HashMap<Integer, List<GUIField>>();
        this.installData = installData;

        super.setLayout(new GridLayout(0, numberOfColumns));
        addInitialRows();
    }

    /**
     * Add the minimum amount of rows specified.
     * The default minimum amount of rows is one.
     */
    private void addInitialRows()
    {
        for(int count = minRow; count > 0; count--)
        {
            addRow();
        }
    }

    /**
     * Add an additional row of fields defined by the user.
     */
    public void addRow()
    {
        numberOfRows++;
        List<GUIField> fields;

        if (guiFields.size() >= numberOfRows)
        {
            fields = guiFields.get(numberOfRows);
        }
        else
        {
            fields = new ArrayList<GUIField>();
            for (Field field : createCustomField(userInputPanelSpec, spec).getFields())
            {

                GUIField guiField = createField.createGuiField(field);
                guiField.setVariable(guiField.getVariable() + "." + numberOfRows);
                fields.add(guiField);
            }
            guiFields.put(numberOfRows, fields);
        }

        for (GUIField field : fields)
        {
            //TODO: Check for the always display option to show as disabled
            //TODO: Check for condition
            //if (guiField.getField().isConditionTrue())
            field.setDisplayed(true);
            for (Component component : field.getComponents())
            {
                JComponent jComponent = component.getComponent();
                Object jConstraints = component.getConstraints();

                if (!(jComponent instanceof JLabel) && !(jComponent instanceof JTextPane))
                {
                    this.add(jComponent, jConstraints);
                }
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Remove last added row of fields defined by the user.
     */
    public void removeRow()
    {
        for (int colNumber = numberOfColumns; colNumber > 0; colNumber--)
        {
            this.remove(this.getComponentCount() - colNumber);
        }

        guiFields.remove(numberOfRows);
        numberOfRows--;

        revalidate();
        repaint();
    }

    public boolean atMax()
    {
        return (numberOfRows < maxRow);
    }

    public boolean atMin()
    {
        return (numberOfRows > minRow);
    }

    /**
     * Validate and update installData
     * @param prompt
     * @return
     */
    public boolean updateField(Prompt prompt)
    {
        installData.setVariable(customInfoField.getVariable(), numberOfRows+"");
        for (int i = 1; i <= numberOfRows; i++)
        {
            for(GUIField guiField : guiFields.get(i))
            {
                if (guiField.isDisplayed())
                {
                    if (!guiField.updateField(prompt))
                    {
                        return false;
                    }
                }
            }
        }
        List<Column> columns = customInfoField.getColumns();
        String [] columnVariables = getVariablesByColumn();
        for (int i = 0; i < columnVariables.length; i++)
        {
            ValidationStatus status = columns.get(i).validate(columnVariables[i]);
            if (!status.isValid())
            {
                prompt.warn(status.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * Retrive a list of labels
     * @param customInfoField
     * @return
     */
    public List<String> getLabels(CustomField customInfoField)
    {
        List<String> labels = new ArrayList<String>();
        for (Field field : customInfoField.getFields())
        {
            GUIField guiField = createField.createGuiField(field);
            labels.add(guiField.getSummaryKey());
        }
        return  labels;
    }
    public List<String> getLabels()
    {
        return this.labels;
    }

    /**
     * Retrive a list of variables
     * @return
     */
    public List<String> getVariables()
    {
        List<String> countedVariables = new ArrayList<String>();

        for (int i = 1; i <= numberOfRows; i++)
        {
            for(GUIField guiField : guiFields.get(i))
            {
                if (guiField.isDisplayed())
                {
                   countedVariables.add(guiField.getVariable());
                }
            }
        }
        return countedVariables;
    }

    private String[] getVariablesByColumn()
    {
        String[] columnVariables = new String[numberOfColumns];

        for(int col=0; col < numberOfColumns; col++)
        {
            columnVariables[col] = "";
            for (int row=1; row <= numberOfRows; row++)
            {
                GUIField guiField = guiFields.get(row).get(col);
                if (guiField.isDisplayed())
                {
                    columnVariables[col] += installData.getVariable(guiField.getVariable()) + ",";
                }
            }
        }
        for (int i=0; i < columnVariables.length; i++)
        {
            String v = columnVariables[i];
            columnVariables[i] = v.substring(0, v.length()-1);
        }
        return columnVariables;
    }

    /**
     * Find the number of columns required for this custom field
     *
     * @param customInfoField
     * @return
     */
    public int getNumberOfColumns(CustomField customInfoField)
    {
       return customInfoField.getFields().size();
    }

    /**
     * Generate a new custom field.
     * @return
     */
    private CustomField createCustomField(UserInputPanelSpec userInputPanelSpec, IXMLElement spec)
    {
        List<Field> fields = userInputPanelSpec.createFields(spec);
        for (Field field : fields)
        {
            if (field instanceof CustomField)
            {
                if(field.getVariable().equals(customInfoField.getVariable()))
                {
                    return (CustomField) field;
                }
            }
        }
        return  null;
    }

    public JPanel getHeader()
    {
        JPanel header = new JPanel(new GridLayout(1, numberOfColumns));

        for (Field field: customInfoField.getFields())
        {
            String heading = field.getDescription();
            if (heading == null)
            {
                heading = field.getLabel();
            }
            JLabel label = new JLabel(heading);
            header.add(label);
        }
        return header;
    }
    @Override
    public void setEnabled(boolean enabled)
    {
        for (java.awt.Component component : this.getComponents())
        {
            component.setEnabled(enabled);
        }
    }
}
