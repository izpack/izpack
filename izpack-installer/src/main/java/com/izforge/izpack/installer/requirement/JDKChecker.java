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

package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;
import com.izforge.izpack.util.FileExecutor;


/**
 * Verifies that the correct JDK version is available for installation to proceed.
 *
 * @author Tim Anderson
 */
public class JDKChecker implements RequirementChecker
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs a <tt>JDKChecker</tt>.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
    public JDKChecker(InstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Determines if the JDK is required, and if so, if it exists.
     *
     * @return <tt>true</tt> if JDK requirements are met, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
        boolean result;
        result = !installData.getInfo().isJdkRequired() || exists() || notFound();
        return result;
    }

    /**
     * Determines if the JDK is installed, by executing attempting to execute javac.
     *
     * @return <tt>true</tt> if javac was successfully executed, otherwise <tt>false</tt>
     */
    protected boolean exists()
    {
        FileExecutor exec = new FileExecutor();
        String[] output = new String[2];
        String[] params = {"javac", "-help"};
        return (exec.executeCommand(params, output) == 0);
    }

    /**
     * Invoked when the JDK is not found.
     * <p/>
     * This prompts the user to proceed with the installation or cancel it
     *
     * @return <tt>true</tt> if the user proceeds with the installation, <tt>false</tt> if they cancel it
     */
    protected boolean notFound()
    {
        String message = "It looks like your system does not have a Java Development Kit (JDK) available.\n"
                + "The software that you plan to install requires a JDK for both its installation and execution.\n\n"
                + "Do you still want to proceed with the installation process?";
        Prompt.Option selected = prompt.confirm(Prompt.Type.WARNING, message, Prompt.Options.YES_NO);
        return selected == Prompt.Option.YES;
    }
}