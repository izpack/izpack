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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class ConditionHistoryTableCellRenderer extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = 6779914244548965230L;

    /* (non-Javadoc)
    * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
    */

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column)
    {
        JComponent comp = null;

        ConditionHistory conditionHistory = (ConditionHistory) value;

        JLabel label = new JLabel();
        label.setAutoscrolls(true);
        comp = label;

        label.setText(conditionHistory.toString());

        comp.setOpaque(true);
        if (conditionHistory.isNewcondition())
        {
            comp.setBackground(Color.green);
        }
        else if (conditionHistory.isChangedcondition())
        {
            comp.setBackground(Color.yellow);
        }
        return comp;
    }
}
