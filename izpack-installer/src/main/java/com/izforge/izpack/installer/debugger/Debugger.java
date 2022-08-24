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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
 * @version $Id: $
 */
public class Debugger
{
    private final InstallData idata;
    private final IconsDatabase icons;
    private final RulesEngine rules;
    private final Color buttonsHColor;
    private final VariableHistoryTableModel variablesmodel;
    private final Map<String, ConditionHistory> conditionhistory;

    private Properties lasttimevariables;
    private JTable variablestable;
    private ConditionHistoryTableModel conditionhistorymodel;
    private ConditionHistoryTableCellRenderer conditionhistoryrenderer;

    public Debugger(InstallData installdata, IconsDatabase icons, RulesEngine rules, Color buttonsHColor)
    {
        idata = installdata;
        this.rules = rules;
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        this.icons = icons;
        this.buttonsHColor = buttonsHColor;
        this.variablesmodel = new VariableHistoryTableModel();
        this.conditionhistory = new HashMap<>();
        this.init();
    }


    private void init()
    {
        for (String variableName : lasttimevariables.stringPropertyNames())
        {
            variablesmodel.setValue(variableName, lasttimevariables.getProperty(variableName), "initial value");
        }
        for (String conditionid : rules.getKnownConditionIds())
        {
            Condition currentcondition = rules.getCondition(conditionid);
            boolean result = this.rules.isConditionTrue(currentcondition);

            ConditionHistory ch = new ConditionHistory(currentcondition);

            ch.addValue(result, "initial value");
            conditionhistory.put(conditionid, ch);
        }
    }

    private void debugVariables(Panel nextpanelmetadata, Panel lastpanelmetadata)
    {
        getChangedVariables(nextpanelmetadata, lastpanelmetadata);
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
    }

    private void debugConditions(Panel nextpanelmetadata, Panel lastpanelmetadata)
    {
        conditionhistoryrenderer.clearState();
        updateChangedConditions(
                "changed after panel switch" +
                (lastpanelmetadata == null ? "" : " from " + lastpanelmetadata.getPanelId()) +
                " to " + nextpanelmetadata.getPanelId());
    }

    private void updateChangedConditions(String comment)
    {
        Set<String> conditionids = this.rules.getKnownConditionIds();
        for (String conditionid : conditionids)
        {
            Condition currentcondition = rules.getCondition(conditionid);
            ConditionHistory aConditionHistory = null;
            if (!conditionhistory.containsKey(conditionid))
            {
                // new condition
                aConditionHistory = new ConditionHistory(currentcondition);
                conditionhistory.put(conditionid, aConditionHistory);
            }
            else
            {
                aConditionHistory = conditionhistory.get(conditionid);
            }
            aConditionHistory.addValue(this.rules.isConditionTrue(currentcondition), comment);
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
        updateChangedConditions("after manual modification of variable " + name);
    }

    private void removeVariableManually(String name)
    {
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        variablesmodel.removeValue(name, "removed manually");
        variablesmodel.fireTableDataChanged();
        updateChangedConditions("after manual modification of variable " + name);
    }

    private static boolean isSet(String value)
    {
        return  value != null && !value.isEmpty();
    }

    public JPanel getDebugPanel()
    {
        JPanel debugpanel = new JPanel();
        debugpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        debugpanel.setLayout(new BorderLayout());

        variablestable = new JTable(variablesmodel);
        variablestable.setDefaultRenderer(VariableHistory.class, new VariableHistoryTableCellRenderer());
        variablestable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variablestable.setRowSelectionAllowed(true);

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
                }
            }
        });

        variablestable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int selectedrow = variablestable.getSelectedRow();
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

        conditionhistorymodel = new ConditionHistoryTableModel(conditionhistory);
        final JTable conditiontable = new JTable(conditionhistorymodel);
        conditionhistoryrenderer = new ConditionHistoryTableCellRenderer(conditionhistory);
        conditiontable.setDefaultRenderer(ConditionHistory.class, conditionhistoryrenderer);
        conditiontable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conditiontable.setRowSelectionAllowed(true);
        conditiontable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int selectedrow = conditiontable.getSelectedRow();

                String selectedcondition = (String) conditiontable.getModel().getValueAt(selectedrow, 0);

                if (e.getClickCount() == 2)
                {

                    ConditionHistory aConditionHistory = conditionhistory.get(selectedcondition);

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
        this.updateChangedConditions(comment);
    }
}

