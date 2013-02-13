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

package com.izforge.izpack.panels.userinput.gui.space;

import javax.swing.JPanel;

import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.gui.GUIField;


/**
 * Spacer view.
 *
 * @author Tim Anderson
 */
public class GUISpacer extends GUIField
{

    /**
     * Constructs a {@code GUISpacerView}.
     *
     * @param field the field
     */
    public GUISpacer(Spacer field)
    {
        super(field);
        JPanel panel = new JPanel();
        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.stretch = true;

        addComponent(panel, constraints);
    }

}
