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

package com.izforge.izpack.panels.finish;

import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.util.Console;

/**
 * Console implementation of the {@link FinishPanel}.
 *
 * @author Mounir el hajj
 */
public class FinishConsolePanel extends AbstractConsolePanel
{

    /**
     * Constructs an {@code FinishConsolePanel}.
     *
     * @param panel the parent panel/view. May be {@code null}
     */
    public FinishConsolePanel(PanelView<Console> panel)
    {
        super(panel);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt>
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        if (installData.isInstallSuccess())
        {
            console.println("Installation was successful");
            console.println("application installed on " + installData.getInstallPath());
        }
        else
        {
            console.println("Install Failed!!!");
        }
        return true;
    }

}
