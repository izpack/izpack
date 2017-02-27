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
package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.IoHelper;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Console-based implementation of the {@link PackResources} interface.
 *
 * @author Tim Anderson
 */
public class ConsolePackResources extends AbstractPackResources
{

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ConsolePackResources.class.getName());

    /**
     * Constructs a {@code DefaultPackResources}.
     *
     * @param resources the local resources
     */
    public ConsolePackResources(Resources resources, InstallData installData)
    {
        super(resources, installData);
    }

    @Override
    protected InputStream getWebPackStream(String name, String webDirURL)
    {
        InputStream result;

        InstallData installData = getInstallData();
        String baseName = installData.getInfo().getInstallerBase();
        File installerDir = new File(baseName).getParentFile();

        if (baseName.contains("/"))
        {
            baseName = baseName.substring(baseName.lastIndexOf('/'));
        }

        String packFileName = baseName + ".pack-" + name + ".jar";

        // Look first in same directory as primary jar, then download it if not found
        File packLocalFile = new File(installerDir, packFileName);
        if (packLocalFile.exists() && packLocalFile.canRead())
        {
            LOGGER.info("Found local pack " + packLocalFile.getAbsolutePath());
        }
        else
        {
            String packURL = webDirURL + "/" + packFileName.replace(" ", "%20");
            String tempFolder = IoHelper.translatePath(installData.getInfo().getUninstallerPath()
                    + WEB_TEMP_SUB_PATH, installData.getVariables());
            File tempDir = new File(tempFolder);
            tempDir.mkdirs();

            try
            {
                LOGGER.info("Downloading remote pack " + packURL);
                packLocalFile = File.createTempFile("izpacktempfile", "jar", new File(tempFolder));
                InputStream webStream = new URL(packURL).openStream();
                write(webStream, packLocalFile);
            }
            catch (IOException exception)
            {
                throw new ResourceException("Failed to read pack", exception);
            }
        }

        try
        {
            URL url = new URL("jar:" + packLocalFile.toURI().toURL() + "!/packs/pack-" + name);
            result = url.openStream();
        }
        catch (IOException exception)
        {
            throw new ResourceException("Failed to read pack", exception);
        }
        return result;
    }

    private static void write(InputStream input, File file) throws IOException
    {
        OutputStream output = new FileOutputStream(file);
        int letter = 0;
        byte[] buffer = new byte[1024];
        while ((letter = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, letter);
        }
        output.flush();
        output.close();
    }
}
