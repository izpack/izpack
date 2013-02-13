/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * Copyright 2009 Dennis Reil
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.panels.userinput.gui.file;

import javax.swing.JFileChooser;

import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.file.DirFieldView;

public class DirInputField extends FileInputField
{
    private static final long serialVersionUID = 8494549823214831160L;

    public DirInputField(DirFieldView view, IzPanel parent, GUIInstallData installDataGUI)
    {
        super(view, parent, installDataGUI);
    }

    @Override
    protected void prepareFileChooser(JFileChooser filechooser)
    {
        filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

}
