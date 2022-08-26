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

import javax.swing.DefaultRowSorter;
import javax.swing.table.TableModel;

public class DebugRowSorter<M extends TableModel> extends DefaultRowSorter<M, String> {
    protected DebugRowSorter(M model) {
        setModelWrapper(new ModelWrapper<M, String>()
        {
            @Override
            public M getModel()
            {
                return model;
            }

            @Override
            public int getColumnCount()
            {
                return model.getColumnCount();
            }

            @Override
            public int getRowCount()
            {
                return model.getRowCount();
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                return model.getValueAt(row, column);
            }

            @Override
            public String getIdentifier(int row)
            {
                return (String) model.getValueAt(row, 0);
            }
        });
        toggleSortOrder(0);
    }
}
