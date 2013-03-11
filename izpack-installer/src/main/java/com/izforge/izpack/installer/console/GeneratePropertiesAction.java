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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.file.FileUtils;

/**
 * Action to generate properties for each panel.
 *
 * @author Tim Anderson
 */
class GeneratePropertiesAction extends ConsoleAction
{
    /**
     * The writer to write properties to.
     */
    private final PrintWriter writer;

    /**
     * Constructs a <tt>GeneratePropertiesAction</tt>.
     *
     * @param installData the installation data
     * @param path        the path to write properties to
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist
     *                               but cannot be created, or cannot be opened for any other reason
     */
    public GeneratePropertiesAction(InstallData installData, String path) throws FileNotFoundException
    {
        super(installData);
        writer = new PrintWriter(new FileOutputStream(path), true);
    }

    /**
     * Determines if this is an installation action.
     *
     * @return <tt>false</tt>
     */
    @Override
    public boolean isInstall()
    {
        return false;
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
        return panel.getView().generateProperties(getInstallData(), writer);
    }

    /**
     * Invoked after the action has been successfully run for each panel.
     *
     * @return {@code true} if the operation succeeds; {@code false} if it fails
     */
    @Override
    public boolean complete()
    {
        FileUtils.close(writer);
        return true;
    }
}
