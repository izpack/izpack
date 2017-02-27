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

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class VariableHistoryTableModel extends AbstractTableModel
{

    private static final long serialVersionUID = 5966543100431588652L;

    public static final String[] COLUMN_HEADER =
    {
        "Name", "Value"
    };
    private Map<String, VariableHistory> variablevalues;

    public VariableHistoryTableModel(Map<String, VariableHistory> values)
    {
        this.variablevalues = values;
    }

    /* (non-Javadoc)
    * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return COLUMN_HEADER.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return this.variablevalues == null ? 0 : this.variablevalues.keySet().size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch (columnIndex)
        {
            case 0:
                String[] keys = this.variablevalues.keySet().toArray(new String[this.variablevalues.keySet().size()]);
                Arrays.sort(keys);
                return keys[rowIndex];

            case 1:
                String variablename = (String) getValueAt(rowIndex, 0);
                VariableHistory variableHistory = variablevalues.get(variablename);
                return variableHistory;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int column)
    {
        return COLUMN_HEADER[column];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
        /*
        if (columnIndex == 0) {
            return false;
        }
        else {
            return true;
        }
         */
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class getColumnClass(int columnIndex)
    {
        if (columnIndex == 1)
        {
            return VariableHistory.class;
        }
        else
        {
            return String.class;
        }
    }
}
