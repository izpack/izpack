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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.rules.Condition;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;

/**
 * Class for debugging variables and conditions.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class Debugger
{
    private RulesEngine rules;
    private GUIInstallData idata;

    private Properties lasttimevariables;

    private JTextPane debugtxt;
    private IconsDatabase icons;
    private Map<String, VariableHistory> variableshistory;
    private Map<String, ConditionHistory> conditionhistory;

    private JTable variablestable;
    private VariableHistoryTableModel variablesmodel;
    private VariableHistoryTableCellRenderer variablesrenderer;
    private ConditionHistoryTableModel conditionhistorymodel;
    private ConditionHistoryTableCellRenderer conditionhistoryrenderer;

    public Debugger(GUIInstallData installdata, IconsDatabase icons, RulesEngine rules)
    {
        idata = installdata;
        this.rules = rules;
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        this.icons = icons;
        this.variableshistory = new HashMap<String, VariableHistory>();
        this.conditionhistory = new HashMap<String, ConditionHistory>();
        this.init();
    }


    private void init()
    {
        String[] variablekeys = lasttimevariables.keySet().toArray(new String[lasttimevariables.size()]);
        for (String variablename : variablekeys)
        {
            VariableHistory variableHistory = new VariableHistory(variablename);
            variableHistory.addValue(lasttimevariables.getProperty(variablename), "initial value");
            variableshistory.put(variablename, variableHistory);
        }
        Set<String> conditionids = this.rules.getKnownConditionIds();
        for (String conditionid : conditionids)
        {
            Condition currentcondition = rules.getCondition(conditionid);
            boolean result = this.rules.isConditionTrue(currentcondition);

            ConditionHistory ch = null;
            ch = new ConditionHistory(currentcondition);

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

        variablesrenderer.clearState();
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
                VariableHistory variableHistory = new VariableHistory(key);
                variableHistory.addValue(currentvalue, "new after panel " + lastpanelmetadata.getPanelId());
                variableshistory.put(key, variableHistory);
                changes = true;
                changedvariables.put(key, currentvalue);
            }
            else
            {
                if (!currentvalue.equals(oldvalue))
                {
                    VariableHistory variableHistory = variableshistory.get(key);
                    variableHistory.addValue(currentvalue,
                                             "changed value after panel " + lastpanelmetadata.getPanelId());
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

    private void modifyVariableManually(String varnametxt, String varvaluetxt)
    {
        lasttimevariables = (Properties) idata.getVariables().getProperties().clone();
        VariableHistory variableHistory = variableshistory.get(varnametxt);
        if (variableHistory != null)
        {
            variableHistory.addValue(varvaluetxt, "modified manually");
        }
        variablesmodel.fireTableDataChanged();
        updateChangedConditions("after manual modification of variable " + varnametxt);
    }

    public JPanel getDebugPanel()
    {
        JPanel debugpanel = new JPanel();
        debugpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        debugpanel.setLayout(new BorderLayout());

        variablesmodel = new VariableHistoryTableModel(variableshistory);
        variablesrenderer = new VariableHistoryTableCellRenderer(variableshistory);
        variablestable = new JTable(variablesmodel);
        variablestable.setDefaultRenderer(VariableHistory.class, variablesrenderer);
        variablestable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        variablestable.setRowSelectionAllowed(true);

        JScrollPane scrollpane = new JScrollPane(variablestable);

        debugpanel.add(scrollpane, BorderLayout.CENTER);

        JPanel varchangepanel = new JPanel();
        varchangepanel.setLayout(new BoxLayout(varchangepanel, BoxLayout.LINE_AXIS));

        final JTextField varname = new JTextField();
        varchangepanel.add(varname);
        JLabel label = new JLabel("=");
        varchangepanel.add(label);
        final JTextField varvalue = new JTextField();
        varchangepanel.add(varvalue);
        JButton changevarbtn = ButtonFactory.createButton(idata.getMessages().get("debug.changevariable"),
                                                          icons.get("debug.changevariable"), idata.buttonsHColor);
        changevarbtn.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                String varnametxt = varname.getText();
                String varvaluetxt = varvalue.getText();
                if ((varnametxt != null) && (varnametxt.length() > 0))
                {
                    if ((varvaluetxt != null) && (varvaluetxt.length() > 0))
                    {
                        idata.setVariable(varnametxt, varvaluetxt);
                        modifyVariableManually(varnametxt, varvaluetxt);
                    }
                }
            }
        });
        variablestable.addMouseListener(new MouseListener()
        {

            public void mouseClicked(MouseEvent e)
            {
                int selectedrow = variablestable.getSelectedRow();
                String selectedvariable = (String) variablesmodel.getValueAt(selectedrow, 0);

                if (e.getClickCount() == 1)
                {
                    varname.setText(selectedvariable);
                }
                else
                {
                    VariableHistory variableHistory = variableshistory.get(selectedvariable);

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

            public void mouseEntered(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void mouseExited(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void mousePressed(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

        });
        varchangepanel.add(changevarbtn);
        debugpanel.add(varchangepanel, BorderLayout.SOUTH);

        JPanel conditionpanel = new JPanel();
        conditionpanel.setLayout(new BorderLayout());

        conditionhistorymodel = new ConditionHistoryTableModel(conditionhistory);
        final JTable conditiontable = new JTable(conditionhistorymodel);
        conditionhistoryrenderer = new ConditionHistoryTableCellRenderer(conditionhistory);
        conditiontable.setDefaultRenderer(ConditionHistory.class, conditionhistoryrenderer);
        conditiontable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conditiontable.setRowSelectionAllowed(true);
        conditiontable.addMouseListener(new MouseListener()
        {

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

            public void mouseEntered(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void mouseExited(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void mousePressed(MouseEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void mouseReleased(MouseEvent e)
            {
                // TODO Auto-generated method stub

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

