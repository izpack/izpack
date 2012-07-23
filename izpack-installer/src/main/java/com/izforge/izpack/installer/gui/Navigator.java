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
package com.izforge.izpack.installer.gui;

import javax.swing.Icon;


/**
 * Panel navigator.
 *
 * @author Tim Anderson
 */
public interface Navigator
{

    /**
     * Determines if the next panel may be navigated to.
     *
     * @return {@code true} if the next panel may be navigated to
     */
    boolean isNextEnabled();

    /**
     * Determines if the next panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    void setNextEnabled(boolean enable);

    /**
     * Makes the 'next' button visible or invisible.
     *
     * @param visible if {@code true} makes the button visible, otherwise makes it invisible.
     */
    void setNextVisible(boolean visible);

    /**
     * Sets the text for the 'next' button.
     *
     * @param text the button text. May be {@code null}
     */
    void setNextText(String text);

    /**
     * Sets the icon for the 'next' button.
     *
     * @param icon the icon. May be {@code null}
     */
    void setNextIcon(Icon icon);

    /**
     * Determines if the previous panel may be navigated to.
     *
     * @return {@code true} if the previous panel may be navigated to
     */
    boolean isPreviousEnabled();

    /**
     * Determines if the previous panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    void setPreviousEnabled(boolean enable);

    /**
     * Makes the 'previous' button visible/invisible.
     *
     * @param visible if {@code true} makes the button visible, otherwise makes it invisible.
     */
    void setPreviousVisible(boolean visible);

    /**
     * Sets the text for the 'previous' button.
     *
     * @param text the button text. May be {@code null}
     */
    void setPreviousText(String text);

    /**
     * Sets the icon for the 'previous' button.
     *
     * @param icon the icon. May be {@code null}
     */
    void setPreviousIcon(Icon icon);

    /**
     * Determines if the 'quit' button is enabled.
     *
     * @return {@code true} if the 'quit' button is enabled
     */
    boolean isQuitEnabled();

    /**
     * Determines if the 'quit' button is enabled.
     *
     * @param enable if {@code true}, enable quit, otherwise disable it
     */
    void setQuitEnabled(boolean enable);

    /**
     * Makes the 'quit' button visible/invisible.
     *
     * @param visible if {@code true} makes the button visible, otherwise makes it invisible.
     */
    void setQuitVisible(boolean visible);

    /**
     * Sets the text for the 'quit' button.
     *
     * @param text the button text. May be {@code null}
     */
    void setQuitText(String text);

    /**
     * Sets the icon for the 'quit' button.
     *
     * @param icon the icon. May be {@code null}
     */
    void setQuitIcon(Icon icon);

    /**
     * Navigates to the next panel.
     *
     * @return {@code true} if the next panel was displayed, or {@code false} if the last panel is displayed
     */
    boolean next();

    /**
     * Navigates to the previous panel.
     *
     * @return {@code true} if the previous panel was displayed, or {@code false} if the first panel is displayed
     */
    boolean previous();

    /**
     * Quits installation, if quit is enabled, and installation is complete.
     * <p/>
     * This method does not return if the quit is accepted.
     */
    void quit();

}
