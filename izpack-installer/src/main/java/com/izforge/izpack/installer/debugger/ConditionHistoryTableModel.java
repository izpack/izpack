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

import com.izforge.izpack.api.rules.Condition;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class ConditionHistoryTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 5966543100431588652L;

    public static final String[] columnheader = {"Id", "Value"};

    private final List<ConditionHistory> tableValues;

    public ConditionHistoryTableModel()
    {
        this.tableValues = new ArrayList<>();
    }

    /* (non-Javadoc)
    * @see javax.swing.table.TableModel#getColumnCount()
    */

    public int getColumnCount()
    {
        return columnheader.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */

    public int getRowCount()
    {
        return tableValues.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0  || rowIndex > tableValues.size() || columnIndex < 0 )
        {
            return null;
        }
        final ConditionHistory conditionHistory = tableValues.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return conditionHistory.getId();

            case 1:
                return conditionHistory;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */

    public String getColumnName(int column)
    {
        return columnheader[column];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */

    public Class getColumnClass(int columnIndex)
    {
        if (columnIndex == 1)
        {
            return ConditionHistory.class;
        }
        else
        {
            return String.class;
        }
    }

    public void clearState()
    {
        for (ConditionHistory conditionHistory : tableValues)
        {
            conditionHistory.clearState();
        }
    }

    public void setValue(Condition condition, boolean currentValue,  String comment) {
        ConditionHistory conditionHistory = tableValues.stream()
                .filter(ch -> ch.getId().equals(condition.getId()))
                .findFirst()
                .orElse(null);
        if (conditionHistory == null)
        {
            conditionHistory = new ConditionHistory(condition);
            tableValues.add(conditionHistory);
        }
        conditionHistory.addValue(currentValue, comment);
    }
}

