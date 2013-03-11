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

import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;


/**
 * Performs installation from properties.
 *
 * @author Tim Anderson
 */
class PropertyInstallAction extends AbstractInstallAction
{
    /**
     * The properties to use for installation.
     */
    private final Properties properties;

    /**
     * Constructs a <tt>PropertyInstallAction</tt>.
     *
     * @param installData the installation data
     * @param writer      the uninstallation data writer
     * @param properties  the installation properties
     */
    public PropertyInstallAction(InstallData installData, UninstallDataWriter writer, Properties properties)
    {
        super(installData, writer);
        this.properties = properties;
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
        return panel.getView().run(getInstallData(), properties);
    }

}
