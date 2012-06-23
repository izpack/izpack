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

package com.izforge.izpack.uninstaller.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.unix.ShellScript;


/**
 * Scripts to execute after uninstallation is complete.
 * <p/>
 * Scripts are only run if the current platform is a unix platform.
 * <p/>
 *
 * @author Tim Anderson
 */
public class RootScripts
{

    /**
     * The scripts.
     */
    private final List<String> scripts;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(RootScripts.class.getName());


    /**
     * Constructs a <tt>RootScripts</tt>.
     *
     * @param resources used to locate the root scripts
     * @param platform  the current platform
     * @throws IzPackException if the root scripts cannot be read
     */
    public RootScripts(Resources resources, Platform platform)
    {
        if (platform.isA(Platform.Name.UNIX))
        {
            scripts = getRootScripts(resources);
        }
        else
        {
            scripts = null;
        }
    }

    /**
     * Runs the root scripts.
     * <p/>
     * NOTE: there is no facility for error detection or reporting. TODO?
     */
    public void run()
    {
        if (scripts != null)
        {
            for (String script : scripts)
            {
                run(script);
            }
        }
    }

    /**
     * Returns the root scripts.
     *
     * @return the root scripts
     * @throws IzPackException if a resource cannot be read
     */
    private List<String> getRootScripts(Resources resources)
    {
        List<String> result = new ArrayList<String>();
        for (int index = 0; ; ++index)
        {
            try
            {
                String name = UninstallData.ROOTSCRIPT + Integer.toString(index);
                ObjectInputStream in = null;
                try
                {
                    InputStream inputStream = resources.getInputStream(name);
                    in = new ObjectInputStream(inputStream);
                    result.add(in.readUTF());
                }
                catch (IOException exception)
                {
                    throw new IzPackException("Failed to read resource: " + name, exception);
                }
                finally
                {
                    FileUtils.close(in);
                }
            }
            catch (ResourceNotFoundException ignore)
            {
                break;
            }
        }
        return result;
    }

    /**
     * Removes the given files as root for the given Users
     *
     * @param script The Script to exec at uninstall time by root.
     */
    protected void run(String script)
    {
        try
        {
            boolean enabled = logger.isLoggable(Level.FINE);
            if (enabled)
            {
                logger.fine("Executing script: " + script);
            }

            File file = File.createTempFile("izpackrootscript", ".sh");

            String result = ShellScript.execAndDelete(new StringBuffer(script), file.getPath());
            if (enabled)
            {
                logger.fine("Result: " + result);
            }
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to execute script: " + exception.getMessage(), exception);
        }
    }

}
