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

import com.izforge.izpack.api.config.Config;
import com.izforge.izpack.api.config.Options;
import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.InstallData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Action to generate properties for each panel.
 *
 * @author Tim Anderson
 */
class GeneratePropertiesAction extends ConsoleAction
{

    private static final Logger LOGGER = Logger.getLogger(GeneratePropertiesAction.class.getName());

    /**
     * The options files to write properties to.
     */
    private final Options options;
    private final String path;

    /**
     * Constructs a <tt>GeneratePropertiesAction</tt>.
     *
     * @param installData the installation data
     * @param path the path to write properties to
     * @throws FileNotFoundException if the file exists but is a directory
     * rather than a regular file, does not exist but cannot be created, or
     * cannot be opened for any other reason
     */
    public GeneratePropertiesAction(InstallData installData, String path) throws FileNotFoundException
    {
        super(installData);

        Info info = installData.getInfo();
        this.options = new Options();
        Config config = this.options.getConfig();
        config.setEmptyLines(true);
        config.setHeaderComment(true);
        config.setFileEncoding(Charset.forName("ISO-8859-1"));
        this.options.setHeaderComment(Arrays.asList(info.getAppName() + " " + info.getAppVersion()));
        this.path = path;
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
     * @return {@code true} if the action was successful, otherwise
     * {@code false}
     */
    @Override
    public boolean run(ConsolePanelView panel)
    {
        return panel.getView().generateOptions(getInstallData(), options);
    }

    /**
     * Invoked after the action has been successfully run for each panel.
     *
     * @return {@code true} if the operation succeeds; {@code false} if it fails
     */
    @Override
    public boolean complete()
    {
        try
        {
            options.store(new File(path));
        }
        catch (IOException e)
        {
            LOGGER.severe("Error saving the option file.");
            return false;
        }

        return true;
    }
}
