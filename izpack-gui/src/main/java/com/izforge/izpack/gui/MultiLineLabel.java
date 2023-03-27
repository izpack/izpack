/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 1997,2002 Elmar Grom
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

import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;

/**
 * <BR>
 * <code>MultiLineLabel</code> may be used in place of javax.swing.JLabel. <BR>
 * <BR>
 * This class implements a component that is capable of displaying multiple lines of text. Line
 * breaks are inserted automatically whenever a line of text extends beyond the predefined maximum
 * line length. Line breaks will only be inserted between words, except where a single word is
 * longer than the maximum line length. Line breaks may be forced at any location in the text by
 * inserting a newline (\n). White space that is not valuable (i.e. is placed at the beginning of a
 * new line or at the very beginning or end of the text) is removed. <br>
 * <br>
 * <b>Note:</b> you can set the maximum width of the label either through one of the constructors
 * or you can call <code>setMaxWidth()</code> explicitly. If this is not set,
 * <code>MultiLineLabel</code> will derive its width from the parent component.
 *
 * @author Elmar Grom
 * @version 1.0 / 04-13-02
 */
/*---------------------------------------------------------------------------*
 * Reviving some old code here that was written before there was swing.
 * The original was written to work with awt. I had to do some massaging to
 * make it a JComponent and I hope it behaves like a reasonably good mannered
 * swing component.
 *
 * Note: Extending JComponent needed paint method implementation with
 *       drawString. Which somehow shown the text with condensed fonts. So,
 *       changed extending JTextArea.
 *---------------------------------------------------------------------------*/
public class MultiLineLabel extends JTextArea
{
    private static final long serialVersionUID = 4051045255031894837L;

    public static final int LEAST_ALLOWED = 200; // default setting for maxAllowed

    private static final int FOUND = 0; // constants for string search.

    private static final int NOT_FOUND = 1;

    private static final int NOT_DONE = 0;

    private static final int DONE = 1;

    private static final char[] WHITE_SPACE = {' ', '\n', '\t'};

    private static final char[] SPACES = {' ', '\t'};

    private static final char NEW_LINE = '\n';

    protected java.util.List<String> line = new java.util.ArrayList<>(); // text lines to display

    protected String labelText; // text lines to display

    protected int numLines; // the number of lines

    protected int lineHeight; // total height of the font

    protected int lineAscent; // font height above the baseline

    protected int lineDescent; // font height below the baseline

    protected int[] lineWidth; // width of each line

    protected int maxWidth; // width of the widest line

    private int maxAllowed = LEAST_ALLOWED; // max width allowed to use

    private boolean maxAllowedSet = false; // signals if the max allowed width

    /**
     * Constructor using default max-width.
     *
     * @param label the text to be displayed
     */
    public MultiLineLabel(String label)
    {
        this.labelText = label;
    }

    /**
     * This method searches the target string for occurrences of any of the characters in the source
     * string. The return value is the position of the first hit. Based on the mode parameter the
     * hit position is either the position where any of the source characters first was found or the
     * first position where none of the source characters where found.
     *
     * @param target the text to be searched
     * @param start  the start position for the search
     * @param source the list of characters to be searched for
     * @param mode   the search mode FOUND = reports first found NOT_FOUND = reports first not found
     * @return position of the first occurrence
     */
    int getPosition(String target, int start, char[] source, int mode)
    {
        int status;
        int position;
        int scan;
        int targetEnd;
        int sourceLength;
        char temp;

        targetEnd = (target.length() - 1);
        sourceLength = source.length;
        position = start;

        if (mode == FOUND)
        {
            status = NOT_DONE;
            while (status != DONE)
            {
                position++;
                if (!(position < targetEnd)) // end of string reached, the next
                { // statement would cause a runtime error
                    return (targetEnd);
                }
                temp = target.charAt(position);
                for (scan = 0; scan < sourceLength; scan++) // walk through the source
                { // string and compare each char
                    if (source[scan] == temp)
                    {
                        status = DONE;
                    }
                }
            }
            return (position);
        }
        else if (mode == NOT_FOUND)
        {
            status = NOT_DONE;
            while (status != DONE)
            {
                position++;
                if (!(position < targetEnd)) // end of string reached, the next
                { // statement would cause a runtime error
                    return (targetEnd);
                }
                temp = target.charAt(position);
                status = DONE;
                for (scan = 0; scan < sourceLength; scan++) // walk through the source
                { // string and compare each char
                    if (source[scan] == temp)
                    {
                        status = NOT_DONE;
                    }
                }
            }
            return (position);
        }
        return (0);
    }

    /**
     * This method scans the input string until the max allowed width is reached. The return value
     * indicates the position just before this happens.
     *
     * @param word word to break
     * @return position character position just before the string is too long
     */
    private int breakWord(String word, FontMetrics fm)
    {
        int width = 0;
        int currentPos = 0;
        int endPos = word.length() - 1;

        // make sure we don't end up with a negative position
        if (endPos <= 0)
        {
            return (currentPos);
        }
        // seek the position where the word first is longer than allowed
        while ((width < maxAllowed) && (currentPos < endPos))
        {
            currentPos++;
            width = fm.stringWidth(labelText.substring(0, currentPos));
        }
        // adjust to get the character just before (this should make it a bit
        // shorter than allowed!)
        if (currentPos != endPos)
        {
            currentPos--;
        }
        return (currentPos);
    }

    /**
     * This method breaks the label text up into multiple lines of text. Line breaks are established
     * based on the maximum available space. A new line is started whenever a line break is
     * encountered, even if the permissible length is not yet reached. Words are broken only if a
     * single word happens to be longer than one line.
     */
    private void divideLabel()
    {
        line.clear();
        FontMetrics fontMetrics = this.getFontMetrics(this.getFont());

        int startPos = 0;
        int currentPos = startPos;
        int lastPos = currentPos;
        int endPos = (labelText.length() - 1);

        int width;
        while (currentPos < endPos)
        {
            width = 0;
            // ----------------------------------------------------------------
            // find the first substring that occupies more than the granted
            // space. Break at the end of the string or a line break.
            // ----------------------------------------------------------------
            while ((width < maxAllowed) && (currentPos < endPos)
                    && (labelText.charAt(currentPos) != NEW_LINE))
            {
                lastPos = currentPos;
                currentPos = getPosition(labelText, currentPos, WHITE_SPACE, FOUND);
                width = fontMetrics.stringWidth(labelText.substring(startPos, currentPos));
            }
            // ----------------------------------------------------------------
            // if we have a line break we want to copy everything up to
            // currentPos
            // ----------------------------------------------------------------
            if (labelText.charAt(currentPos) == NEW_LINE)
            {
                lastPos = currentPos;
            }
            // ----------------------------------------------------------------
            // if we are at the end of the string we want to copy everything up
            // to the last character. Since there seems to be a problem to get
            // the last character if the substring definition ends at the very
            // last character we have to call a different substring function
            // than normal.
            // ----------------------------------------------------------------
            if (currentPos == endPos && width <= maxAllowed)
            {
                lastPos = currentPos;
                String s = labelText.substring(startPos);
                line.add(s);
            }
            // ----------------------------------------------------------------
            // in all other cases copy the substring that we have found to fit
            // and add it as a new line of text to the line vector.
            // ----------------------------------------------------------------
            else
            {
                // ------------------------------------------------------------
                // make sure it's not a single word. If so we must break it at
                // the proper location.
                // ------------------------------------------------------------
                if (lastPos == startPos)
                {
                    lastPos = startPos + breakWord(labelText.substring(startPos, currentPos), fontMetrics);
                }
                String s = labelText.substring(startPos, lastPos);
                line.add(s);
            }

            // ----------------------------------------------------------------
            // seek for the end of the white space to cut out any unnecessary
            // spaces and tabs and set the new start condition.
            // ----------------------------------------------------------------
            startPos = getPosition(labelText, lastPos, SPACES, NOT_FOUND);
            currentPos = startPos;
        }

        numLines = line.size();
        lineWidth = new int[numLines];
    }

    /**
     * This method finds the font size, each line width and the widest line.
     */
    protected void measure()
    {
        if (!maxAllowedSet)
        {
            maxAllowed = getParent().getSize().width;
        }

        // return if width is too small
        if (maxAllowed < (20))
        {
            return;
        }

        FontMetrics fontMetrics = this.getFontMetrics(this.getFont());

        // return if no font metrics available
        if (fontMetrics == null)
        {
            return;
        }

        divideLabel();

        this.lineHeight = fontMetrics.getHeight();
        this.lineDescent = fontMetrics.getDescent();
        this.maxWidth = 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numLines; i++)
        {
            if (i != 0) {
                sb.append('\n');
            }
            sb.append(line.get(i));
            this.lineWidth[i] = fontMetrics.stringWidth(this.line.get(i));
            if (this.lineWidth[i] > this.maxWidth)
            {
                this.maxWidth = this.lineWidth[i];
            }
        }
        super.setText(sb.toString());
        setEditable(false);
        setOpaque(false);
    }

    /**
     * This method may be used to set the label text
     *
     * @param labelText the text to be displayed
     */
    public void setText(String labelText)
    {
        this.labelText = labelText;
        repaint();
    }

    /**
     * This method may be used to set the max allowed line width
     *
     * @param width the max allowed line width in pixels
     */
    public void setMaxWidth(int width)
    {
        this.maxAllowed = width;
        this.maxAllowedSet = true;
        repaint();
    }

    /**
     * Moves and resizes this component. The new location of the top-left corner is specified by
     * <code>x</code> and <code>y</code>, and the new size is specified by <code>width</code>
     * and <code>height</code>.
     *
     * @param x      The new x-coordinate of this component.
     * @param y      The new y-coordinate of this component.
     * @param width  The new width of this component.
     * @param height The new height of this component.
     */
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        this.maxAllowed = width;
        this.maxAllowedSet = true;
    }

    /**
     * This method is typically used by the layout manager, it reports the necessary space to
     * display the label comfortably.
     */
    public Dimension getPreferredSize()
    {
        measure();
        Insets margin = getMargin();
        return (new Dimension(maxAllowed, (numLines * (lineHeight + lineAscent + lineDescent))
                + margin.top + margin.bottom));
    }

    /**
     * This method is typically used by the layout manager, it reports the absolute minimum space
     * required to display the entire label.
     */
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }
}
