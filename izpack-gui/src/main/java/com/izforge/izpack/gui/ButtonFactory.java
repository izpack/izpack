/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2002 Jan Blok
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
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * This class makes it possible to use default buttons on macosx platform
 */
public class ButtonFactory
{

    private static boolean useHighlightButtons = false;

    private static boolean useButtonIcons = false;

    /**
     * Used to reserve unique button mnemonics for all buttons in a panel.
     */
    private static HashMap<String,String> panelButtonMnemonics = new HashMap<String, String>();

    /**
     * Used to reserve unique button mnemonics for buttons external to a given panel. Ie. navigation buttons.
     */
    private static HashMap<String,String> reservedButtonMnemonics = new HashMap<String, String>();

    /**
     * Enable icons for buttons This setting has no effect on OSX
     */
    public static void useButtonIcons()
    {
        useButtonIcons(true);
    }

    /**
     * Enable or disable icons for buttons This setting has no effect on OSX
     *
     * @param useit flag which determines the behavior
     */
    public static void useButtonIcons(boolean useit)
    {
        if (System.getProperty("mrj.version") == null)
        {
            useButtonIcons = useit;
        }
    }

    /**
     * Enable highlight buttons This setting has no effect on OSX
     */
    public static void useHighlightButtons()
    {
        useHighlightButtons(true);
    }

    /**
     * Enable or disable highlight buttons This setting has no effect on OSX
     *
     * @param useit flag which determines the behavior
     */
    public static void useHighlightButtons(boolean useit)
    {
        if (System.getProperty("mrj.version") == null)
        {
            useHighlightButtons = useit;
        }
        useButtonIcons(useit);
    }

    public static JButton createButton(Icon icon, Color color)
    {
        JButton result;
        if (useHighlightButtons)
        {
            if (useButtonIcons)
            {
                result = new HighlightJButton(icon, color);
            }
            else
            {
                result = new HighlightJButton("", color);
            }

        }
        else
        {
            if (useButtonIcons)
            {
                result = new JButton(icon);
            }
            else
            {
                result = new JButton();
            }
        }
        return addEnterKeyAction(result);
    }

    public static JButton createButton(String text, Color color)
    {
        JButton result;
        if (useHighlightButtons)
        {
            result = new HighlightJButton(text, color);
        }
        else
        {
            result = new JButton(text);
        }
        setButtonMnemonic(result, panelButtonMnemonics);
        return result;
    }

    public static JButton createButton(String text, Icon icon, Color color)
    {
        JButton result;
        if (useHighlightButtons)
        {
            if (useButtonIcons)
            {
                result = new HighlightJButton(text, icon, color);
            }
            else
            {
                result = new HighlightJButton(text, color);
            }
        }
        else
        {
            if (useButtonIcons)
            {
                result = new JButton(text, icon);
            }
            else
            {
                result = new JButton(text);
            }
        }
        setButtonMnemonic(result, panelButtonMnemonics);
        return addEnterKeyAction(result);
    }

    public static JButton createButton(Action a, Color color)
    {
        JButton result;
        if (useHighlightButtons)
        {
            result = new HighlightJButton(a, color);
        }
        else
        {
            result = new JButton(a);
        }
        return addEnterKeyAction(result);
    }

    /**
     * Registers the action lister used for VK_SPACE to be the same as that for VK_ENTER if:
     * <ul>
     * <li>there is a VK_SPACE action listener; and</li>
     * <li>there is no action listener for VK_ENTER</li>
     * </ul>
     * This is a workaround for IZPACK-296.
     *
     * @param button the button
     * @return the button
     */
    private static JButton addEnterKeyAction(JButton button)
    {
        ActionListener spacePress = button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false));
        ActionListener spaceRelease = button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true));
        ActionListener enterPress = button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        ActionListener enterRelease = button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true));
        if (spacePress != null && spaceRelease != null && enterPress == null && enterRelease == null)
        {
            button.registerKeyboardAction(spacePress, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                                          JComponent.WHEN_FOCUSED);
            button.registerKeyboardAction(spaceRelease, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                                          JComponent.WHEN_FOCUSED);
        }
        return button;
    }

    /**
     * Sets a mnemonic on the specified button that is derived from
     * the given button caption text.
     *
     * @param button the JButton.
     * @param map the HashMap to add the mnemonic to.
     */
    private static void setButtonMnemonic(JButton button, HashMap<String, String> map)
    {
        String text = button.getText();

        if (text != null)
        {
            String key = findMnemonic(text);

            if (key != null)
            {
                button.setMnemonic(key.charAt(0));
                map.put(key, text);
            }
        }
    }

    /**
     * Finds an available key mnemonic for this button and returns it.
     *
     * @param text Non-null text caption for the JButton.
     * @return the first available nmemonic, or null if none is found.
     */
    private static String findMnemonic(String text)
    {
        String caption = text.toLowerCase();
        String key = null;

        /**
         * Iterate through the characters in this button's
         * text until we either find a suitable mnemonic,
         * or else run out of characters.
         */
        while (caption != null && caption.length() > 0)
        {
            // The mnemonic, or key.
            key = String.valueOf(caption.charAt(0));

            // If key is already in use by other buttons or by installer frame:
            if (panelButtonMnemonics.containsKey(key) || reservedButtonMnemonics.containsKey(key))
            {
                caption = caption.substring(1);
                continue;
            } else
            {
                // If key doesn't exist, this mnemonic
                // is available, so set it and finish.
                return key;
            }
        }
        return null;
    }

    /**
     * Adds the given mnemonics to the reservedButtonMnemonics map, thereby
     * reserving the use of these mnemonics to only the navigator buttons.
     * @param buttons an array of buttons used by the navigator.
     */
    public static void reserveButtonMnemonics(JButton[] buttons)
    {
        for (JButton button : buttons) {
            setButtonMnemonic(button, reservedButtonMnemonics);
        }
    }

    /**
     * Clears the current map of panel button mnemonics.
     */
    public static void clearPanelButtonMnemonics()
    {
        panelButtonMnemonics.clear();
    }

    /**
     * Clears the current map of navigator button mnemonics.
     */
    public static void clearReservedButtonMnemonics()
    {
        reservedButtonMnemonics.clear();
    }

    /**
     * Clears all button mnemonics from their respective maps.
     */
    public static void clearAllMnemonics()
    {
        clearPanelButtonMnemonics();
        clearReservedButtonMnemonics();
    }

}
