/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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
package com.izforge.izpack.gui;

import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * Draws an etched line border.
 *
 * @author Julien Ponge
 */
public class EtchedLineBorder extends EtchedBorder
{

    private static final long serialVersionUID = 3256999956257649201L;

    /**
     * Paints the etched line.
     *
     * @param component The component to draw the border on.
     * @param graphics The graphics object.
     * @param x The top-left x.
     * @param y The top-left y.
     * @param width The border width.
     * @param height The border height.
     */
    public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height)
    {
        graphics.translate(x, y);

        graphics.setColor(etchType == LOWERED ? getShadowColor(component) : getHighlightColor(component));
        graphics.drawLine(10, 0, width - 2, 0);

        graphics.setColor(etchType == LOWERED ? getHighlightColor(component) : getShadowColor(component));
        graphics.drawLine(10, 1, width - 2, 1);

        graphics.translate(0 - x, 0 - y);
    }
}
