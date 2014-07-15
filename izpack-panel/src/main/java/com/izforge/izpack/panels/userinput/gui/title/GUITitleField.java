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

package com.izforge.izpack.panels.userinput.gui.title;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.panels.userinput.field.Alignment;
import com.izforge.izpack.panels.userinput.field.title.TitleField;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Title field view.
 *
 * @author Tim Anderson
 */
public class GUITitleField extends GUIField
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(GUITitleField.class.getName());

    /**
     * Constructs a {@code GUITitleField}.
     *
     * @param field       the field
     * @param installData the installation data
     * @param icons       the icons
     */
    public GUITitleField(TitleField field, InstallData installData, IconsDatabase icons)
    {
        super(field);
        String title = field.getLabel();

        if (title != null)
        {
            JLabel label = null;
            ImageIcon icon;
            String iconName = field.getIconName(installData.getMessages());
            if (iconName != null)
            {
                try
                {
                    icon = icons.get(iconName);
                    label = LabelFactory.create(title, icon, SwingConstants.TRAILING, true);
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Icon " + iconName + " not found in icon list: " + e.getMessage(), e);
                }
            }
            if (label == null)
            {
                label = LabelFactory.create(title);
            }
            Font font = label.getFont();
            float size = font.getSize();
            int style = 0;

            if (field.isBold())
            {
                style += Font.BOLD;
            }
            if (field.isItalic())
            {
                style += Font.ITALIC;
            }

            float multiplier = field.getTitleSize();
            font = font.deriveFont(style, size * multiplier);
            label.setFont(font);
            label.setAlignmentX(0);

            int justify = getAlignment(field.getAlignment());
            TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.NORTH, justify);
            addComponent(label, constraints);
        }
        addTooltip();
    }

    /**
     * Maps an {@code Alignment} to the {@link TwoColumnConstraints} constants.
     *
     * @param alignment the alignment to map
     * @return the corresponding int value
     * @see TwoColumnConstraints
     */
    private int getAlignment(Alignment alignment)
    {
        int result = TwoColumnConstraints.RIGHT;
        if (alignment == Alignment.LEFT)
        {
            result = TwoColumnConstraints.LEFT;
        }
        else if (alignment == Alignment.CENTER)
        {
            result = TwoColumnConstraints.CENTER;
        }

        return result;
    }

}
