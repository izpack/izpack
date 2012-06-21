/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2007 Markus Schlegel
 * Copyright 2007 Julien Ponge
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

package com.izforge.izpack.event;

import java.util.List;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.panels.IShortcutPanelLogic;

/**
 * Creates the Shortcuts after files have been installed. Use this listener, if you place the
 * ShortcutPanel before the Installation of the files.
 *
 * @author Marcus Schlegel, Pulinco
 */
public class LateShortcutInstallListener extends AbstractInstallerListener
{

    /**
     * The shortcut panel logic.
     */
    private IShortcutPanelLogic shortcutPanelLogic;

    /**
     * Constructs a <tt>LateShortcutInstallListener</tt>.
     *
     * @param logic       the shortcut panel behaviour
     * @param installData the installation data
     */
    public LateShortcutInstallListener(IShortcutPanelLogic logic, InstallData installData)
    {
        super(installData);
        this.shortcutPanelLogic = logic;
        logic.setCreateShortcutsImmediately(false);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        try
        {
            shortcutPanelLogic.createAndRegisterShortcuts();
        }
        catch (Exception exception)
        {
            throw new IzPackException("Failed to create shortcuts", exception);

        }
    }
}
