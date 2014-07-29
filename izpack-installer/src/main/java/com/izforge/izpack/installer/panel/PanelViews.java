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

import com.izforge.izpack.installer.data.UninstallData;

import java.io.File;
import java.util.List;


/**
 * Manages navigation between panels, providing access to the view of each panel.
 *
 * @author Tim Anderson
 */
public interface PanelViews<T extends AbstractPanelView<V>, V> extends Panels
{

    /**
     * Returns the panel views.
     *
     * @return the panel views
     */
    List<T> getPanelViews();

    /**
     * Returns the current view.
     *
     * @return the current view, or {@code null} if there is none
     */
    V getView();

    /**
     * Returns the current panel view.
     *
     * @return the current panel view, or {@code null} if there is none
     */
    T getPanelView();

    /**
     * Returns the index of a visible panel, relative to other visible panels.
     *
     * @param panel the panel
     * @return the panel's visible index, or {@code -1} if the panel is not visible
     */
    int getVisibleIndex(T panel);


    /**
     * Writes an XML tree to a file.
     *
     * @param file file of where the auto-install will be placed
     * @param uninstallData used to ensure that the file will be removed by the uninstaller
     * @throws Exception
     */
    void writeInstallationRecord(File file, UninstallData uninstallData) throws Exception;

}
