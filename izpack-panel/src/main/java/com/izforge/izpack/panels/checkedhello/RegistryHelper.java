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

package com.izforge.izpack.panels.checkedhello;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.IoHelper;


/**
 * Registry helper.
 *
 * @author Klaus Bartz
 * @author Tim Anderson
 */
public class RegistryHelper
{

    /**
     * The registry handler, or {@code null} if the registry isn't supported on the current platform.
     */
    private final RegistryHandler handler;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(RegistryHelper.class.getName());

    /**
     * The registry uninstall string key.
     */
    private static final String UNINSTALL_STRING = "UninstallString";

    /**
     * Install path variable.
     */
    private static final String INSTALL_PATH = "$INSTALL_PATH";


    /**
     * Constructs a {@code RegistryHelper}.
     *
     * @param handler     the registry handler
     * @param installData the installation data
     */
    public RegistryHelper(RegistryDefaultHandler handler, InstallData installData)
    {
        this.handler = handler.getInstance();
        this.installData = installData;
    }

    /**
     * Returns whether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows).
     *
     * @return {@code true} if the application is registered
     * @throws NativeLibException for any native library error
     */
    public boolean isRegistered() throws NativeLibException
    {
        boolean result = false;
        if (handler != null)
        {
            String uninstallName = getUninstallName();
            if (uninstallName != null)
            {
                String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
                if (exists(RegistryHandler.HKEY_LOCAL_MACHINE, keyName, UNINSTALL_STRING)
                        || exists(RegistryHandler.HKEY_CURRENT_USER, keyName, UNINSTALL_STRING))
                {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Returns the uninstallation name.
     *
     * @return the uninstallation name. May be {@code null}
     */
    public String getUninstallName()
    {
        return (handler != null) ? handler.getUninstallName() : null;
    }

    /**
     * Returns the installation path of the application.
     * <p/>
     * NOTE: this uses the UninstallString registry entry
     *
     * @return the installation path, or {@code null} if the application hasn't been installed or the path cannot
     *         be determined
     * @throws NativeLibException    for any native library error
     * @throws IllegalStateException if the uninstallation name of the application is {@code null}
     */
    public String getInstallationPath() throws NativeLibException
    {
        String result = null;
        String command = getUninstallCommand();
        if (command != null)
        {
            int start = command.lastIndexOf("-jar ");
            if (start != -1 && start < command.length() - 5)
            {
                String path = command.substring(start + 5).trim();
                if (path.startsWith("\""))
                {
                    path = path.substring(1).trim();
                }
                int end = path.indexOf("uninstaller");
                if (end >= 0)
                {
                    result = path.substring(0, end - 1);
                    // now need to remove the uninstaller directory, if one exists, to determine the path.
                    // This is not 100% reliable, as the uninstaller directory may be different to that in the new
                    // installation. A better approach would be to examine the install.log in the uninstaller jar
                    String uninstallerPath = installData.getInfo().getUninstallerPath();
                    if (uninstallerPath != null && uninstallerPath.startsWith(INSTALL_PATH))
                    {
                        uninstallerPath = IoHelper.translatePath(uninstallerPath);
                        uninstallerPath = uninstallerPath.substring(INSTALL_PATH.length());
                        if (result.endsWith(uninstallerPath))
                        {
                            result = result.substring(0, result.length() - uninstallerPath.length());
                        }
                    }
                }
            }
            if (result == null)
            {
                log.log(Level.WARNING, "Cannot determine installation path from: " + command);
            }
        }
        return result;
    }

    /**
     * Returns the command to uninstall the application, if it is registered.
     *
     * @return the command, or {@code null} if the application isn't registered
     * @throws NativeLibException    for any native library error
     * @throws IllegalStateException if the uninstallation name of the application is {@code null}
     */
    public String getUninstallCommand() throws NativeLibException
    {
        String result = null;
        if (handler != null)
        {
            String uninstallName = handler.getUninstallName();
            if (uninstallName == null)
            {
                throw new IllegalStateException("Cannot determine uninstallation name");
            }

            String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
            if (!exists(RegistryHandler.HKEY_LOCAL_MACHINE, keyName, UNINSTALL_STRING)
                    && !exists(RegistryHandler.HKEY_CURRENT_USER, keyName, UNINSTALL_STRING))
            {
                log.log(Level.INFO, "Cannot determine previous installation path of " + uninstallName);
            }
            else
            {
                result = handler.getValue(keyName, UNINSTALL_STRING).getStringData();
            }
        }
        return result;
    }

    /**
     * Generates an unique uninstall name.
     *
     * @return the unique uninstall name, or {@code null} if the registry isn't supported on the platform
     * @throws NativeLibException for any native library error
     */
    public String updateUninstallName() throws NativeLibException
    {
        String result = null;
        if (handler != null)
        {
            String uninstallName = handler.getUninstallName();
            if (uninstallName == null)
            {
                throw new IllegalStateException("Cannot determine uninstallation name");
            }
            int count = 1;
            while (true)
            {
                // loop round until an unique key is generated
                String newUninstallName = uninstallName + "(" + count + ")";
                String keyName = RegistryHandler.UNINSTALL_ROOT + newUninstallName;
                handler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
                if (!handler.keyExist(keyName))
                {
                    handler.setUninstallName(newUninstallName);
                    result = newUninstallName;
                    break;
                }
                else
                {
                    ++count;
                }
            }
        }
        return result;
    }

    /**
     * Determines whether a given value under a given key exists.
     * <p/>
     * NOTE: this operation has the side effect that the registry root changes.
     *
     * @param root  the root of the registry access.
     * @param key   the key name
     * @param value the value name
     * @return {@code true} it exists, otherwise {@code false}
     * @throws NativeLibException
     */
    protected boolean exists(int root, String key, String value) throws NativeLibException
    {
        handler.setRoot(root);
        return handler.valueExist(key, value);
    }
}
