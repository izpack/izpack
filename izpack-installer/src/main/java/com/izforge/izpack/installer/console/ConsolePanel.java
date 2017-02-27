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

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.Console;
import com.izforge.izpack.api.config.Options;

import java.util.Properties;

/**
 * Defines the Interface that must be implemented for running Panels in console
 * mode.
 * <p/>
 * Implementing classes MUST NOT link against AWT/Swing classes. Thus the panels
 * cannot implement this interface directly, they should use e.g. helper classes
 * instead.
 *
 * @author Mounir El Hajj
 * @author Tim Anderon
 */
public interface ConsolePanel
{

    /**
     * Generates a properties file for each input field or variable.
     *
     * @param installData the installation data
     * @param options the options file to write to
     * @return {@code true} if the generation is successful, otherwise
     * {@code false}
     */
    boolean generateOptions(InstallData installData, Options options);

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties the properties
     * @return {@code true} if the installation is successful, otherwise
     * {@code false}
     */
    boolean run(InstallData installData, Properties properties);

    /**
     * Runs the panel in an interactive console.
     *
     * @param installData the installation data
     * @param console the console
     * @return {@code true} if the panel ran successfully, otherwise
     * {@code false}
     */
    boolean run(InstallData installData, Console console);

    /**
     * Create th auto-xml
     *
     * @param rootElement
     */
    public void createInstallationRecord(IXMLElement rootElement);

    /**
     * Do some user interaction on the console depending on the result of a
     * panel validation. This is necessary to inform the user on the console
     * about the forbidden progress and to prevent failing the installer in case
     * the panel data validator treats the data to be wrong. Instead the user
     * should be asked whether to redisplay the input fields to fix the wrong
     * values.
     *
     * @param valid whether the validation has been successful
     * @return false - let the installer fail, true - let the installer continue
     * to run
     * @see com.izforge.izpack.api.installer.DataValidator
     */
    boolean handlePanelValidationResult(boolean valid);
}
