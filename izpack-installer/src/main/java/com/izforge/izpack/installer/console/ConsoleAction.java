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

package com.izforge.izpack.installer.console;

import com.izforge.izpack.api.data.InstallData;

/**
 * Console installer action.
 *
 * @author Tim Anderson
 */
public abstract class ConsoleAction
{
    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Constructs a <tt>ConsoleAction</tt>.
     *
     * @param installData the installation data
     */
    public ConsoleAction(InstallData installData)
    {
        this.installData = installData;
    }

    /**
     * Runs the action for the panel.
     *
     * @param panel the panel
     * @return {@code true} if the action was successful, otherwise {@code false}
     */
    public abstract boolean run(ConsolePanelView panel);

    /**
     * Invoked after the action has been successfully run for each panel.
     * <p/>
     * Performs any necessary clean up.
     *
     * @return {@code true} if the operation succeeds; {@code false} if it fails
     */
    public abstract boolean complete();

    /**
     * Determines if this is an installation action.
     * <p/>
     * An installation action is any action that performs installation. Installation actions need to be distinguished
     * from other actions as they may subsequently require a reboot.
     * <p/>
     * This default implementation always returns  <tt>true</tt>.
     *
     * @return <tt>true</tt>
     */
    public boolean isInstall()
    {
        return true;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }


}
