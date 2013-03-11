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
import com.izforge.izpack.util.Console;

/**
 * Performs interactive console installation.
 *
 * @author Tim Anderson
 */
public class ConsoleInstallAction extends AbstractInstallAction
{

    /**
     * The console.
     */
    private final Console console;

    /**
     * Constructs a <tt>ConsoleInstallAction</tt>.
     *
     * @param installData the installation date
     * @param writer      the uninstallation data writer
     */
    public ConsoleInstallAction(Console console, InstallData installData, UninstallDataWriter writer)
    {
        super(installData, writer);
        this.console = console;
    }


    /**
     * Runs the action for the panel.
     *
     * @param panel the panel
     * @return {@code true} if the action was successful, otherwise {@code false}
     */
    @Override
    public boolean run(ConsolePanelView panel)
    {
        ConsolePanel view = panel.getView();
        return view.run(getInstallData(), console);
    }

}
