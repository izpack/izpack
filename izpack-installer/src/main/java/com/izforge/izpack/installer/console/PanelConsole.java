/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.Console;

/**
 * Defines the Interface that must be implemented for running Panels in console mode.
 * <p/>
 * Implementing classes MUST NOT link against awt/swing classes. Thus the Panels cannot implement
 * this interface directly, they should use e.g. helper classes instead.
 *
 * @author Mounir El Hajj
 * @deprecated use {@link ConsolePanel}
 */
@Deprecated
public interface PanelConsole extends ConsolePanel
{

    /**
     * Generates a properties file for each input field or variable.
     *
     * @param installData the installation data
     * @param printWriter the properties file to write to
     * @return <tt>true</tt> if the generation is successful, otherwise <tt>false</tt>
     */
    boolean runGeneratePropertiesFile(InstallData installData, PrintWriter printWriter);

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt> if the installation is successful, otherwise <tt>false</tt>
     */
    boolean runConsoleFromProperties(InstallData installData, Properties properties);

    /**
     * Runs the panel in interactive console mode.
     *
     * @param installData the installation data
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    boolean runConsole(InstallData installData);

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    boolean runConsole(InstallData installData, Console console);

}
