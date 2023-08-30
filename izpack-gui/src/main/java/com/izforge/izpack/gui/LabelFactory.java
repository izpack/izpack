/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/**
 * <p>
 * A label factory which can handle modified look like to present icons or present it not.
 * </p>
 *
 * @author Klaus Bartz
 */
public class LabelFactory implements SwingConstants
{
    private static boolean useLabelIcons = true;
    private static float labelFontSizeVal = 1.0f;
    private static Font customLabelFontObj = null;

    /**
     * Returns whether the factory creates labels with icons or without icons.
     *
     * @return whether the factory creates labels with icons or without icons
     */
    public static boolean isUseLabelIcons()
    {
        return useLabelIcons;
    }

    /**
     * Sets the use icon state.
     *
     * @param useLabelIcons flag for the icon state
     */
    public static void setUseLabelIcons(boolean useLabelIcons)
    {
        LabelFactory.useLabelIcons = useLabelIcons;
    }

    /**
     * Returns the current label-font-size multiplier.
     *
     * @return the current label-font-size multiplier (or 1.0 if none
     *         has been entered).
     */
    public static float getLabelFontSize()
    {
        return labelFontSizeVal;
    }

    /**
     * Sets the label-font-size multiplier.  If the value is not greater
     * than zero or is greater than 5.0 then it will not be used.
     *
     * @param val label-font-size multiplier value to use.
     */
    public static void setLabelFontSize(float val)
    {
        if (val > 0.0f && val <= 5.0f && val != labelFontSizeVal)
        {
            labelFontSizeVal = val;
            final Font fontObj = (new JLabel()).getFont();
            customLabelFontObj =
                    fontObj.deriveFont(fontObj.getSize2D() * val);
        }
    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     *
     * @param image the image to be used as label icon
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image)
    {
        return (create(image, CENTER));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label, else an empty label returns.
     *
     * @param image               the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(Icon image, int horizontalAlignment)
    {
        return (create(null, image, horizontalAlignment));

    }

    /**
     * Returns a new JLabel with the horizontal alignment CENTER.
     *
     * @param text the text to be set
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text)
    {
        return (create(text, CENTER));

    }

    /**
     * Returns a new JLabel or FullLineLabel with the horizontal alignment CENTER.
     *
     * @param text       the text to be set
     * @param isFullLine determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, boolean isFullLine)
    {
        return (create(text, CENTER, isFullLine));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment.
     *
     * @param text                the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment)
    {
        return (create(text, null, horizontalAlignment));

    }

    /**
     * Returns a new JLabel or FullLineLabel with the given horizontal alignment.
     *
     * @param text                the text to be set
     * @param horizontalAlignment horizontal alignment of the label
     * @param isFullLine          determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, int horizontalAlignment, boolean isFullLine)
    {
        return (create(text, null, horizontalAlignment, isFullLine));

    }

    /**
     * Returns a new JLabel with the given horizontal alignment. If isUseLabelIcons is true, the
     * given image will be set to the label. The given text will be set always to the label. It is
     * allowed, that image and/or text are null.
     *
     * @param text                the text to be set
     * @param image               the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @return new JLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment)
    {
        return (create(text, image, horizontalAlignment, false));
    }

    /**
     * Returns a new JLabel or FullLineLabel with the given horizontal alignment. If isUseLabelIcons
     * is true, the given image will be set to the label. The given text will be set always to the
     * label. It is allowed, that image and/or text are null.
     *
     * @param text                the text to be set
     * @param image               the image to be used as label icon
     * @param horizontalAlignment horizontal alignment of the label
     * @param isFullLine          determines whether a FullLineLabel or a JLabel should be created
     * @return new JLabel or FullLineLabel with the given parameters
     */
    public static JLabel create(String text, Icon image, int horizontalAlignment, boolean isFullLine)
    {
        JLabel retval;
        if (image != null && isUseLabelIcons())
        {
            if (isFullLine)
            {
                retval = new FullLineLabel(image);
            }
            else
            {
                retval = new JLabel(image);
            }
        }
        else
        {
            if (isFullLine)
            {
                retval = new FullLineLabel();
            }
            else
            {
                retval = new JLabel();
            }
        }
        if (text != null)
        {
            retval.setText(text);
        }
        if (customLabelFontObj != null)
        {
            retval.setFont(customLabelFontObj);
        }
        retval.setHorizontalAlignment(horizontalAlignment);
        return (retval);
    }

    /**
     * The Font of Labels in many cases
     */
    public static Font getControlTextFont()
    {
        Font fontObj = (getLAF() != null) ?
                MetalLookAndFeel.getControlTextFont() : UIManager.getDefaults().getFont("Label.font");
        //if guiprefs 'labelFontSize' multiplier value
        // has been setup then apply it to the font:
        final float val;
        if ((val = LabelFactory.getLabelFontSize()) != 1.0f)
        {
            fontObj = fontObj.deriveFont(fontObj.getSize2D() * val);
        }
        return fontObj;
    }

    protected static MetalLookAndFeel getLAF()
    {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        if (lookAndFeel instanceof MetalLookAndFeel)
        {
            return ((MetalLookAndFeel) lookAndFeel);
        }
        return (null);
    }

    /**
     * Returns a new MultiLineLabel. The given text will be set always to the label.
     *
     * @param text  the text to be set
     * @return new MultiLineLabel with the given text
     */
    public static MultiLineLabel createMultiLineLabel(String text)
    {
        MultiLineLabel multiLineLabel = new MultiLineLabel(text);
        multiLineLabel.setText(text);
        if (customLabelFontObj != null)
        {
            multiLineLabel.setFont(customLabelFontObj);
        }
        else 
        {
            multiLineLabel.setFont(getControlTextFont());
        }
        return multiLineLabel;
    }

    /**
     * This class is only needed to signal a different layout handling. There is no additional
     * functionality related to a JLabel. Only the needed constructors are implemented.
     * A FullLineLabel gets from the IzPanelLayout as default a constraints for a full line.
     * Therefore, the width of this label do not determine the width of a column as a JLable
     * it do.
     *
     * @author Klaus Bartz
     */
    public static class FullLineLabel extends JLabel
    {
        /**
         * Required (serializable)
         */
        private static final long serialVersionUID = 2918265795390777147L;

        /**
         * Creates a <code>JLabel</code> instance with the specified image.
         * The label is centered vertically and horizontally
         * in its display area.
         *
         * @param image The image to be displayed by the label.
         */
        public FullLineLabel(Icon image)
        {
            super(image);
        }

        /**
         * Default constructor.
         */
        public FullLineLabel()
        {
            super();
        }
    }
}
