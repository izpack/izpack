/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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
package com.izforge.izpack.installer.util;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.util.Platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Target panel helper methods.
 *
 * @author Tim Anderson
 */
public class InstallPathHelper
{
    /**
     * Target panel directory variable name.
     */
    public static final String TARGET_PANEL_DIR = "TargetPanel.dir";

    /**
     * Target panel directory variable prefix.
     */
    private static final String PREFIX = TARGET_PANEL_DIR + ".";

    /**
     * Returns the installation path for the current platform.
     * <p/>
     * This looks for a variable in the following order:
     * <ol>
     * <li>{@code TargetPanel.dir.<platform symbolic name>}</li>
     * <li>{@code TargetPanel.dir.<platform name>}. This searches any parent platforms if none is found</li>
     * <li>{@code TargetPanel.dir}</li>
     * <li>{@code DEFAULT_INSTALL_PATH}</li>
     * <li>{@code SYSTEM_user_dir}</li>
     * </ol>
     *
     * @param installData the installation data
     * @return the default platform path, or {@code null} if none is found
     */
    public static String getPath(InstallData installData)
    {
        String currentPath = installData.getInstallPath();
        if (currentPath != null)
        {
            return currentPath;
        }

        String defaultPath = installData.getDefaultInstallPath();
        if (defaultPath == null)
        {
            // Make the default path point to the current location
            defaultPath = System.getProperty("user.dir");
        }

        String path = getTargetPanelDir(installData);
        if (path != null)
        {
            path = installData.getVariables().replace(path);
        }
        if (path == null && defaultPath != null)
        {
            path = installData.getVariables().replace(defaultPath);
        }
        return path;
    }

    /**
     * Returns the installation path for the current platform.
     * <p/>
     * This looks for a variable prefixed with {@code TargetPanel.dir} in the following order:
     * <ol>
     * <li>{@code TargetPanel.dir.<platform symbolic name>}</li>
     * <li>{@code TargetPanel.dir.<platform name>}. This searches any parent platforms if none is found</li>
     * <li>{@code TargetPanel.dir}</li>
     * </ol>
     *
     * @param installData the installation data
     * @return the default platform path, or {@code null} if none is found
     */
    private static String getTargetPanelDir(InstallData installData)
    {
        Platform platform = installData.getPlatform();
        String path = null;
        if (platform.getSymbolicName() != null)
        {
            path = installData.getVariable(PREFIX + platform.getSymbolicName().toLowerCase());
        }
        if (path == null)
        {
            path = getTargetPanelDir(installData, platform.getName());
        }
        if (path == null)
        {
            path = installData.getVariable(TARGET_PANEL_DIR);
        }
        return path;
    }

    /**
     * Returns the installation path for the specified platform name.
     * <p/>
     * This looks for a variable named {@code TargetPanel.dir.<platform name>}. If none is found, it searches the
     * parent platforms, in a breadth-first manner.
     *
     * @param installData the installation data
     * @param name        the platform name
     * @return the default path, or {@code null} if none is found
     */
    private static String getTargetPanelDir(InstallData installData, Platform.Name name)
    {
        String path = null;
        List<Platform.Name> queue = new ArrayList<>();
        queue.add(name);
        while (!queue.isEmpty())
        {
            name = queue.remove(0);
            path = installData.getVariable(PREFIX + name.toString().toLowerCase());
            if (path != null)
            {
                break;
            }
            Collections.addAll(queue, name.getParents());
        }
        return path;
    }
}
