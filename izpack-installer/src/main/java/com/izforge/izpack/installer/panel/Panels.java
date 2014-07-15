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

package com.izforge.izpack.installer.panel;

import java.util.List;

import com.izforge.izpack.api.data.Panel;


/**
 * Manages navigation between panels.
 *
 * @author Tim Anderson
 */
public interface Panels
{

    /**
     * Returns the panels.
     *
     * @return the panels
     */
    List<Panel> getPanels();

    /**
     * Returns the current panel.
     *
     * @return the current panel, or {@code null} if there is no current panel
     */
    Panel getPanel();

    /**
     * Determines if the current panel is valid.
     *
     * @return {@code true} if the current panel is valid
     */
    boolean isValid();

    /**
     * Returns the current panel index.
     *
     * @return the current panel index, or {@code -1} if there is no current panel
     */
    int getIndex();

    /**
     * Determines if there is another panel after the current panel.
     *
     * @return {@code true} if there is another panel
     */
    boolean hasNext();

    /**
     * Navigates to the next panel.
     * <br/>
     * Navigation can only occur if the current panel is valid.
     *
     * @return {@code true} if the next panel was navigated to
     */
    boolean next();

    /**
     * Navigates to the next panel.
     *
     * @param validate if {@code true}, only move to the next panel if validation succeeds
     * @return {@code true} if the next panel was navigated to
     */
    boolean next(boolean validate);

    /**
     * Determines if the next panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    void setNextEnabled(boolean enable);

    /**
     * Determines if navigation to the next panel has been enabled.
     * <p/>
     * return {@code true} if navigation is enabled
     */
    boolean isNextEnabled();

    /**
     * Determines if there is panel prior to the current panel.
     *
     * @return {@code true} if there is a panel prior to the current panel
     */
    boolean hasPrevious();

    /**
     * Navigates to the previous panel.
     *
     * @return {@code true} if the previous panel was navigated to
     */
    boolean previous();

    /**
     * Determines if the previous panel may be navigated to.
     *
     * @param enable if {@code true}, enable navigation, otherwise disable it
     */
    void setPreviousEnabled(boolean enable);

    /**
     * Determines if navigation to the previous panel has been enabled.
     * <p/>
     * return {@code true} if navigation is enabled
     */
    boolean isPreviousEnabled();

    /**
     * Navigates to the panel before the specified index.
     * <br/>
     * The target panel must be before the current panel.
     *
     * @return {@code true} if the previous panel was navigated to
     */
    boolean previous(int index);

    /**
     * Determines if there is another panel after the specified index.
     *
     * @param index       the panel index
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return the next panel index, or {@code -1} if there are no more panels
     */
    int getNext(int index, boolean visibleOnly);

    /**
     * Determines if there is another panel after the current index.
     *
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return {@code true} if there is another panel
     */
    int getNext(boolean visibleOnly);

    /**
     * Determines if there is another panel prior to the specified index.
     *
     * @param index       the panel index
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return the previous panel index, or {@code -1} if there are no more panels
     */
    int getPrevious(int index, boolean visibleOnly);

    /**
     * Determines if there is another panel prior to the current index.
     *
     * @param visibleOnly if {@code true}, only examine visible panels
     * @return the previous panel index, or {@code -1} if there are no more panels
     */
    int getPrevious(boolean visibleOnly);

    /**
     * Returns the number of visible panels.
     *
     * @return the number of visible panels
     */
    int getVisible();
}
