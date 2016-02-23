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

import com.izforge.izpack.installer.data.ConsoleInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.test.util.TestConsole;
import com.izforge.izpack.test.util.TestHousekeeper;

/**
 * Test implementation of the {@link ConsoleInstaller}.
 * <p/>
 * This supports running the installer against a script of input commands.
 *
 * @author Tim Anderson
 */
public class TestConsoleInstaller extends ConsoleInstaller
{

    /**
     * Constructs a <tt>TestConsoleInstaller</tt>
     *
     * @param panels       the panels
     * @param installData  the installation date
     * @param writer       the uninstallation data writer
     * @param console      the console
     * @param housekeeper  the house-keeper
     * @throws Exception for any error
     */
    public TestConsoleInstaller(ConsolePanels panels, ConsoleInstallData installData, UninstallDataWriter writer,
                                TestConsole console, TestHousekeeper housekeeper)
            throws Exception
    {
        super(panels, installData, writer, console, housekeeper);
    }

    /**
     * Returns the console.
     *
     * @return the console
     */
    @Override
    public TestConsole getConsole()
    {
        return (TestConsole) super.getConsole();
    }

}
