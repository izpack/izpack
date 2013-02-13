/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.DirFieldView;


/**
 * Directory field view.
 *
 * @author Tim Anderson
 */
public class GUIDirField extends AbstractGUIFileField
{

    /**
     * Constructs a {@code GUIDirFieldView}.
     *
     * @param field       the field
     * @param installData the installation data
     * @param parent      the parent panel
     * @param prompt      the prompt
     */
    public GUIDirField(DirField field, GUIInstallData installData, IzPanel parent, Prompt prompt)
    {
        super(field);
        DirFieldView view = new DirFieldView(field, prompt);
        init(new DirInputField(view, parent, installData));
    }

}
