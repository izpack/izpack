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

import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;


/**
 * Registry helper.
 *
 * @author Klaus Bartz
 * @author Tim Anderson
 */
public class RegistryHelper
{

    /**
     * The registry handler, or <tt>null</tt> if the registry isn't supported on the current platform.
     */
    private final RegistryHandler handler;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(RegistryHelper.class.getName());


    /**
     * Constructs a <tt>RegistryHelper</tt>.
     *
     * @param handler the registry handler
     */
    public RegistryHelper(RegistryDefaultHandler handler)
    {
        this.handler = handler.getInstance();
    }

    /**
     * Returns whether the handled application is already registered or not. The validation will be
     * made only on systems which contains a registry (Windows).
     *
     * @return <tt>true</tt> if the application is registered
     * @throws NativeLibException for any native library error
     */
    public boolean isRegistered() throws NativeLibException
    {
        boolean result = false;
        String uninstallName = getUninstallName();
        if (uninstallName != null)
        {
            String keyName = RegistryHandler.UNINSTALL_ROOT + uninstallName;
            handler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
            result = handler.keyExist(keyName);
            if (!result)
            {
                handler.setRoot(RegistryHandler.HKEY_CURRENT_USER);
                result = handler.keyExist(keyName);
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
     *
     * @return the installation path, or <tt>null</tt> if the application hasn't been installed or the path cannot
     *         be determined
     * @throws NativeLibException    for any native library error
     * @throws IllegalStateException if the uninstallation name of the application is <tt>null</tt>
     */
    public String getInstallationPath() throws NativeLibException
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
            handler.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
            if (!handler.valueExist(keyName, "UninstallString"))
            {
                // application not installed, or registry entry missing
                log.log(Level.INFO, "Cannot determine previous installation path of " + uninstallName);
            }
            else
            {
                String uninstall = handler.getValue(keyName, "UninstallString").getStringData();
                int start = uninstall.lastIndexOf("-jar ");
                if (start != -1 && start < uninstall.length() - 5)
                {
                    String path = uninstall.substring(start + 5).trim();
                    if (path.startsWith("\""))
                    {
                        path = path.substring(1).trim();
                    }
                    int end = path.indexOf("uninstaller");
                    if (end >= 0)
                    {
                        result = path.substring(0, end - 1);
                    }
                }
                if (result == null)
                {
                    log.log(Level.WARNING, "Cannot determine installation path from: " + uninstall);
                }
            }
        }
        return result;
    }

    /**
     * Generates an unique uninstall name.
     *
     * @return the unique uninstall name, or <tt>null</tt> if the registry isn't supported on the platform
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
}
