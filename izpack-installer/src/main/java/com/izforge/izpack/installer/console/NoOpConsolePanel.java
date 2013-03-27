/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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
import com.izforge.izpack.util.Console;


/**
 * A dummy console panel implementation. It returns {@code true} for all operations.
 * Subclass from this to provide a dummy console implementation of an IzPanel.
 *
 * @author Tim Anderson
 */
public abstract class NoOpConsolePanel extends AbstractConsolePanel
{

    /**
     * Constructs a {@code NoOpConsolePanel}.
     */
    public NoOpConsolePanel()
    {
        super(null);
    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return {@code true}
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
     * @return {@code true}
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        return true;
    }
}
