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
import com.izforge.izpack.installer.data.UninstallDataWriter;

/**
 * A {@link ConsoleAction} for performing installations.
 * <p/>
 * This writes uninstallation information if required, at the end of a successful installation.
 *
 * @author Tim Anderson
 */
public abstract class AbstractInstallAction extends ConsoleAction
{
    /**
     * The uninstallation data writer.
     */
    private final UninstallDataWriter writer;


    /**
     * Constructs an <tt>AbstractConsoleInstallAction</tt>.
     *
     * @param installData the installation data
     * @param writer      the uninstallation data writer
     */
    public AbstractInstallAction(InstallData installData, UninstallDataWriter writer)
    {
        super(installData);
        this.writer = writer;
    }

    /**
     * Invoked after the action has been successfully run for each panel.
     * <p/>
     * This writes uninstallation information, if required.
     *
     * @return {@code true} if the operation succeeds; {@code false} if it fails
     */
    @Override
    public boolean complete()
    {
        boolean result = true;
        if (writer.isUninstallRequired())
        {
            result = writer.write();
        }
        return result;
    }
}
