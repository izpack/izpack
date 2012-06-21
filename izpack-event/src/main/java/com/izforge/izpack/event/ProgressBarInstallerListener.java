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

package com.izforge.izpack.event;

import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.ProgressNotifiers;

/**
 * Installer listener for reset the progress bar and initialise {@link ProgressNotifiers} to support progress bar
 * interaction. To support progress bar interaction, add this installer listener as first listener.
 *
 * @author Klaus Bartz
 */
public class ProgressBarInstallerListener extends AbstractInstallerListener
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ProgressBarInstallerListener.class.getName());

    /**
     * Constructs a <tt>ProgressBarInstallerListener</tt>.
     *
     * @param installData the installation data
     * @param notifiers   the progress notifiers
     */
    public ProgressBarInstallerListener(InstallData installData, ProgressNotifiers notifiers)
    {
        super(installData, notifiers);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.compiler.InstallerListener#afterPacks(com.izforge.izpack.installer.AutomatedInstallData,
     * com.izforge.izpack.api.handler.AbstractUIProgressHandler)
     */

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        ProgressNotifiers notifiers = getProgressNotifiers();
        int count = notifiers.getNotifiers();
        if (count > 0)
        {
            String progress = getMessage("CustomActions.progress");
            String tip = getMessage("CustomActions.tip");
            if ("CustomActions.tip".equals(tip) || "CustomActions.progress".equals(progress))
            {
                logger.fine("No messages found for custom action progress bar interactions; skipped");
                return;
            }
            notifiers.setNotifyProgress(true);
            listener.restartAction("Configure", progress, tip, count);
        }
    }

}
