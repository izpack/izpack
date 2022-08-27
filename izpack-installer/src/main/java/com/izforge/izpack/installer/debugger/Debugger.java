/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

package com.izforge.izpack.installer.debugger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;

/**
 * Class for debugging variables and conditions.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class Debugger extends WindowAdapter
{
    private final Dimension DEFAULT_PANEL_SIZE = new Dimension(500, 500);

    private final InstallData idata;
    private final IconsDatabase icons;
    private final RulesEngine rules;
    private final Color buttonsHColor;
    private final VariableHistoryTableModel variablesmodel;
    private final ConditionHistoryTableModel conditionhistorymodel;
    private final Preferences preferences;

    private Properties lasttimevariables;

    public Debugger(InstallData installdata, IconsDatabase icons, RulesEngine rules, Color buttonsHColor)
    {
        idata = installdata;
        this.rules = rules;
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        this.icons = icons;
        this.buttonsHColor = buttonsHColor;
        this.variablesmodel = new VariableHistoryTableModel();
        this.conditionhistorymodel = new ConditionHistoryTableModel();
        preferences = Preferences.userNodeForPackage(Debugger.class);
        this.init();
    }


    private void init()
    {
        for (String variableName : lasttimevariables.stringPropertyNames())
        {
            variablesmodel.setValue(variableName, lasttimevariables.getProperty(variableName), "initial value");
        }
        updateConditionsHistory("initial value");
    }

    private void debugVariables(Panel nextpanelmetadata, Panel lastpanelmetadata)
    {
        getChangedVariables(nextpanelmetadata, lastpanelmetadata);
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
    }

    private void debugConditions(Panel nextpanelmetadata, Panel lastpanelmetadata)
    {
        conditionhistorymodel.clearState();
        updateConditionsHistory(
                "changed after panel switch" +
                (lastpanelmetadata == null ? "" : " from " + lastpanelmetadata.getPanelId()) +
                " to " + nextpanelmetadata.getPanelId());
    }

    private void updateConditionsHistory(String comment)
    {
        for (String conditionid : rules.getKnownConditionIds())
        {
            Condition condition = rules.getCondition(conditionid);
            conditionhistorymodel.setValue(condition, rules.isConditionTrue(condition), comment);
        }
        conditionhistorymodel.fireTableDataChanged();
    }

    private Properties getChangedVariables(Panel nextpanelmetadata, Panel lastpanelmetadata)
    {
        Properties currentvariables = (Properties) idata.getVariables().getProperties().clone();
        Properties changedvariables = new Properties();

        variablesmodel.clearState();
        // check for changed and new variables
        Enumeration currentvariableskeys = currentvariables.keys();
        boolean changes = false;
        while (currentvariableskeys.hasMoreElements())
        {
            String key = (String) currentvariableskeys.nextElement();
            String currentvalue = currentvariables.getProperty(key);
            String oldvalue = lasttimevariables.getProperty(key);

            if ((oldvalue == null))
            {
                variablesmodel.setValue(key, currentvalue, lastpanelmetadata != null ?
                                "new after panel " + lastpanelmetadata.getPanelId() :
                                "new on first panel ");
                changes = true;
                changedvariables.put(key, currentvalue);
            }
            else
            {
                if (!currentvalue.equals(oldvalue))
                {
                    variablesmodel.setValue(key, currentvalue, lastpanelmetadata != null ?
                            "changed value after panel " + lastpanelmetadata.getPanelId() :
                            "changed value on first panel ");
                    changes = true;
                    changedvariables.put(key, currentvalue);
                }
            }
        }
        if (changes)
        {
            variablesmodel.fireTableDataChanged();
        }
        return changedvariables;
    }

    private void modifyVariableManually(String name, String value)
    {
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        variablesmodel.setValue(name, value, "modified manually");
        variablesmodel.fireTableDataChanged();
        updateConditionsHistory("after manual modification of variable " + name);
    }

    private void removeVariableManually(String name)
    {
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        variablesmodel.removeValue(name, "removed manually");
        variablesmodel.fireTableDataChanged();
        updateConditionsHistory("after manual modification of variable " + name);
    }

    private static boolean isSet(String value)
    {
        return  value != null && !value.isEmpty();
    }

    public JPanel getDebugPanel()
    {
        final JPanel debugpanel = new JPanel();
        debugpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        debugpanel.setLayout(new BorderLayout());

        JTable variablestable = new JTable(variablesmodel);
        variablestable.setDefaultRenderer(VariableHistory.class, new VariableHistoryTableCellRenderer());
        variablestable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variablestable.setRowSelectionAllowed(true);
        variablestable.setRowSorter(new DebugRowSorter<>(variablesmodel));

        JScrollPane scrollpane = new JScrollPane(variablestable);

        debugpanel.add(scrollpane, BorderLayout.CENTER);

        JPanel varchangepanel = new JPanel(new BorderLayout());
        varchangepanel.setLayout(new BoxLayout(varchangepanel, BoxLayout.LINE_AXIS));
        varchangepanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        final JTextField varname = new JTextField();
        varchangepanel.add(varname);
        JLabel label = new JLabel("=");
        varchangepanel.add(label);
        final JTextField varvalue = new JTextField();
        varchangepanel.add(varvalue);

        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.LINE_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        JButton changevarbtn = ButtonFactory.createButton(idata.getMessages().get("debug.changevariable"),
                icons.get("debug.changevariable"), buttonsHColor);
        changevarbtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String varnametxt = varname.getText();
                String varvaluetxt = varvalue.getText();
                if (isSet(varnametxt) && varvaluetxt != null)
                {
                    idata.setVariable(varnametxt, varvaluetxt);
                    modifyVariableManually(varnametxt, varvaluetxt);
                    varname.setText("");
                    varvalue.setText("");
                }
            }
        });
        JButton deletebtn = ButtonFactory.createButton(idata.getMessages().get("debug.deletevariable"),
                icons.get("debug.deletevariable"), buttonsHColor);
        deletebtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String varnametxt = varname.getText();
                if (isSet(varnametxt))
                {
                    idata.setVariable(varnametxt, null);
                    removeVariableManually(varnametxt);
                    varname.setText("");
                    varvalue.setText("");
                }
            }
        });
        variablestable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int selectedrow = variablestable.getRowSorter().convertRowIndexToModel(variablestable.getSelectedRow());
                VariableHistory variableHistory = (VariableHistory) variablesmodel.getValueAt(selectedrow,1);
                String selectedVariableName = variableHistory.getName();

                if (e.getClickCount() == 1)
                {
                    varname.setText(selectedVariableName);
                    varvalue.setText(variableHistory.getLastValue());
                }
                else
                {

                    JFrame variabledetails = new JFrame("Details");

                    JTextPane detailspane = new JTextPane();
                    detailspane.setContentType("text/html");
                    detailspane.setText(variableHistory.getValueHistoryDetails());
                    detailspane.setEditable(false);
                    JScrollPane scroller = new JScrollPane(detailspane);

                    Container container = variabledetails.getContentPane();
                    container.setLayout(new BorderLayout());
                    container.add(scroller, BorderLayout.CENTER);

                    variabledetails.pack();
                    variabledetails.setLocationRelativeTo(debugpanel);
                    variabledetails.setVisible(true);
                }
            }
        });
        buttonpanel.add(changevarbtn);
        buttonpanel.add(deletebtn);

        JPanel editactionspanel = new JPanel();
        editactionspanel.setLayout(new BoxLayout(editactionspanel, BoxLayout.PAGE_AXIS));
        editactionspanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        editactionspanel.add(varchangepanel);
        editactionspanel.add(buttonpanel);

        debugpanel.add(editactionspanel, BorderLayout.SOUTH);

        JPanel conditionpanel = new JPanel();
        conditionpanel.setLayout(new BorderLayout());
        conditionpanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        final JTable conditiontable = new JTable(conditionhistorymodel);
        conditiontable.setDefaultRenderer(ConditionHistory.class, new ConditionHistoryTableCellRenderer());
        conditiontable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conditiontable.setRowSelectionAllowed(true);
        conditiontable.setRowSorter(new DebugRowSorter<>(conditionhistorymodel));
        conditiontable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int selectedrow = conditiontable.getRowSorter().convertRowIndexToModel(conditiontable.getSelectedRow());
                ConditionHistory aConditionHistory = (ConditionHistory) conditiontable.getModel().getValueAt(selectedrow, 1);

                if (e.getClickCount() == 2)
                {
                    JFrame variabledetails = new JFrame("Details");

                    JTextPane detailspane = new JTextPane();
                    detailspane.setContentType("text/html");
                    detailspane.setText(aConditionHistory.getConditionHistoryDetails());
                    detailspane.setEditable(false);
                    JScrollPane scroller = new JScrollPane(detailspane);

                    Container container = variabledetails.getContentPane();
                    container.setLayout(new BorderLayout());
                    container.add(scroller, BorderLayout.CENTER);

                    variabledetails.pack();
                    variabledetails.setLocationRelativeTo(debugpanel);
                    variabledetails.setVisible(true);
                }
            }
        });

        JScrollPane conditionscroller = new JScrollPane(conditiontable);
        conditionpanel.add(conditionscroller, BorderLayout.CENTER);

        JTabbedPane tabpane = new JTabbedPane(JTabbedPane.TOP);
        tabpane.insertTab("Variable settings", null, debugpanel, "", 0);
        tabpane.insertTab("Condition settings", null, conditionpanel, "", 1);
        JPanel mainpanel = new JPanel();
        mainpanel.setLayout(new BorderLayout());
        mainpanel.add(tabpane, BorderLayout.CENTER);
        return mainpanel;
    }

    /**
     * Debug state changes after panel switch.
     *
     * @param nextpanelmetadata
     * @param lastpanelmetadata
     */
    public void switchPanel(Panel nextpanelmetadata, Panel lastpanelmetadata)
    {
        this.debugVariables(nextpanelmetadata, lastpanelmetadata);
        this.debugConditions(nextpanelmetadata, lastpanelmetadata);
    }

    public void packSelectionChanged(String comment)
    {
        this.updateConditionsHistory(comment);
    }

    public JFrame initialize(JFrame targetFrame) {
        final Dimension panelSize = getPanelSize();
        int defaultX = 0;
        int defaultY = 0;
        targetFrame.setLocation(preferences.getInt("x", defaultX), preferences.getInt("y", defaultY));
        targetFrame.setContentPane(getDebugPanel());
        targetFrame.addWindowListener(this);
        targetFrame.setSize(panelSize);
        targetFrame.setPreferredSize(panelSize);
        return targetFrame;
    }

    public Dimension getDefaultPanelSize()
    {
        return DEFAULT_PANEL_SIZE;
    }
    public Dimension getPanelSize()
    {
        return new Dimension(preferences.getInt("width", DEFAULT_PANEL_SIZE.width), preferences.getInt("height", DEFAULT_PANEL_SIZE.height));
    }

    public void storePositionAndSize(Component component)
    {
        preferences.putInt("x", component.getX());
        preferences.putInt("y", component.getY());
        preferences.putInt("width", component.getWidth());
        preferences.putInt("height", component.getHeight());
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        storePositionAndSize(e.getComponent());
    }
}

