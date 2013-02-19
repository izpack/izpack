/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
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

package com.izforge.izpack.core.os;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.coi.tools.os.win.MSWinConstants;
import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.exception.NativeLibException;

/**
 * This class represents a registry handler in a operating system independent way. OS specific
 * subclasses are used to implement the necessary mapping from this generic API to the classes that
 * reflect the system dependent AIP.
 *
 * @author Klaus Bartz
 */
public class RegistryHandler implements MSWinConstants
{
    public static final String UNINSTALL_ROOT = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\";

    public static final Map<String, Integer> ROOT_KEY_MAP = new HashMap<String, Integer>();

    private String uninstallName = null;

    static
    {
        ROOT_KEY_MAP.put("HKCR", HKEY_CLASSES_ROOT);
        ROOT_KEY_MAP.put("HKEY_CLASSES_ROOT", HKEY_CLASSES_ROOT);
        ROOT_KEY_MAP.put("HKCU", HKEY_CURRENT_USER);
        ROOT_KEY_MAP.put("HKEY_CURRENT_USER", HKEY_CURRENT_USER);
        ROOT_KEY_MAP.put("HKLM", HKEY_LOCAL_MACHINE);
        ROOT_KEY_MAP.put("HKEY_LOCAL_MACHINE", HKEY_LOCAL_MACHINE);
        ROOT_KEY_MAP.put("HKU", HKEY_USERS);
        ROOT_KEY_MAP.put("HKEY_USERS", HKEY_USERS);
        ROOT_KEY_MAP.put("HKPD", HKEY_PERFORMANCE_DATA);
        ROOT_KEY_MAP.put("HKEY_PERFORMANCE_DATA", HKEY_PERFORMANCE_DATA);
        ROOT_KEY_MAP.put("HKCC", HKEY_CURRENT_CONFIG);
        ROOT_KEY_MAP.put("HKEY_CURRENT_CONFIG", HKEY_CURRENT_CONFIG);
        ROOT_KEY_MAP.put("HKDDS", HKEY_DYN_DATA);
        ROOT_KEY_MAP.put("HKEY_DYN_DATA", HKEY_DYN_DATA);

    }

    /**
     * Constructs a {@code RegistryHandler}.
     */
    public RegistryHandler()
    {
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    public void setValue(String key, String value, String contents) throws NativeLibException
    {
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    public void setValue(String key, String value, String[] contents) throws NativeLibException
    {
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    public void setValue(String key, String value, byte[] contents) throws NativeLibException
    {
    }

    /**
     * Sets the given contents to the given registry value. If a sub key or the registry value does
     * not exist, it will be created.
     *
     * @param key      the registry key which should be used or created
     * @param value    the registry value into which the contents should be set
     * @param contents the contents for the value
     * @throws NativeLibException for any registry error
     */
    public void setValue(String key, String value, long contents) throws NativeLibException
    {
    }

    /**
     * Returns the contents of the key/value pair if value exist, else the given default value.
     *
     * @param key        the registry key which should be used
     * @param value      the registry value from which the contents should be requested
     * @param defaultVal value to be used if no value exist in the registry
     * @return requested value if exist, else the default value
     * @throws NativeLibException for any registry error
     */
    public RegDataContainer getValue(String key, String value, RegDataContainer defaultVal) throws NativeLibException
    {
        return defaultVal;
    }

    /**
     * Returns whether a key exist or not.
     *
     * @param key key to be evaluated
     * @return whether a key exist or not
     * @throws NativeLibException for any registry error
     */
    public boolean keyExist(String key) throws NativeLibException
    {
        return false;
    }

    /**
     * Returns whether a the given value under the given key exist or not.
     *
     * @param key   key to be used as path for the value
     * @param value value name to be evaluated
     * @return whether a the given value under the given key exist or not
     * @throws NativeLibException for any registry error
     */
    public boolean valueExist(String key, String value) throws NativeLibException
    {
        return false;
    }

    /**
     * Returns all keys which are defined under the given key.
     *
     * @param key key to be used as path for the sub keys
     * @return all keys which are defined under the given key
     * @throws NativeLibException for any registry error
     */
    public String[] getSubkeys(String key) throws NativeLibException
    {
        return null;
    }

    /**
     * Returns all value names which are defined under the given key.
     *
     * @param key key to be used as path for the value names
     * @return all value names which are defined under the given key
     * @throws NativeLibException for any registry error
     */
    public String[] getValueNames(String key) throws NativeLibException
    {
        return null;
    }

    /**
     * Returns the contents of the key/value pair if value exist, else an exception is raised.
     *
     * @param key   the registry key which should be used
     * @param value the registry value from which the contents should be requested
     * @return requested value if exist, else an exception
     * @throws NativeLibException for any registry error
     */
    public RegDataContainer getValue(String key, String value) throws NativeLibException
    {
        return null;
    }

    /**
     * Creates the given key in the registry.
     *
     * @param key key to be created
     * @throws NativeLibException for any registry error
     */
    public void createKey(String key) throws NativeLibException
    {
    }

    /**
     * Deletes the given key if it exist, else throws an exception.
     *
     * @param key key to be deleted
     * @throws NativeLibException for any registry error
     */
    public void deleteKey(String key) throws NativeLibException
    {
    }

    /**
     * Deletes a key under the current root if it is empty, else do nothing.
     *
     * @param key key to be deleted
     * @throws NativeLibException for any registry error
     */
    public void deleteKeyIfEmpty(String key) throws NativeLibException
    {
    }

    /**
     * Deletes a value.
     *
     * @param key   key of the value which should be deleted
     * @param value value name to be deleted
     * @throws NativeLibException for any registry error
     */
    public void deleteValue(String key, String value) throws NativeLibException
    {
    }

    /**
     * Sets the root for the next registry access.
     * <p/>
     * TODO - this doesn't support multi-threaded access
     *
     * @param i an integer which refers to a HKEY
     * @throws NativeLibException for any registry error
     */
    public void setRoot(int i) throws NativeLibException
    {
    }

    /**
     * Return the root as integer (HKEY_xxx).
     *
     * @return the root as integer
     * @throws NativeLibException for any registry error
     */
    public int getRoot() throws NativeLibException
    {
        return (0);
    }

    /**
     * Sets up whether or not previous contents of registry values will
     * be logged by the 'setValue()' method.  When registry values are
     * overwritten by repeated installations, the desired behavior can
     * be to have the registry value removed rather than rewound to the
     * last-set contents (achieved via 'false').  If this method is not
     * called then the flag wll default to 'true'.
     *
     * @param flagVal true to have the previous contents of registry
     *                values logged by the 'setValue()' method.
     * @throws NativeLibException for any registry error
     */
    public void setLogPrevSetValueFlag(boolean flagVal) throws NativeLibException
    {
    }

    /**
     * Determines whether or not previous contents of registry values
     * will be logged by the 'setValue()' method.
     *
     * @return true if the previous contents of registry values will be
     *         logged by the 'setValue()' method.
     * @throws NativeLibException for any registry error
     */
    public boolean getLogPrevSetValueFlag() throws NativeLibException
    {
        return (true);
    }

    /**
     * Activates logging of registry changes.
     *
     * @throws NativeLibException for any registry error
     */
    public void activateLogging() throws NativeLibException
    {
    }

    /**
     * Suspends logging of registry changes.
     *
     * @throws NativeLibException for any registry error
     */
    public void suspendLogging() throws NativeLibException
    {
    }

    /**
     * Resets logging of registry changes.
     *
     * @throws NativeLibException for any registry error
     */
    public void resetLogging() throws NativeLibException
    {
    }

    /**
     * Returns a copy of the collected logging information.
     *
     * @return a copy of the collected logging information
     * @throws NativeLibException for any registry error
     */
    public List<Object> getLoggingInfo() throws NativeLibException
    {
        return null;
    }

    /**
     * Registers logging information. This replaces any existing logging information.
     *
     * @param info the logging information
     * @throws NativeLibException for any registry error
     */
    public void setLoggingInfo(List info) throws NativeLibException
    {
    }

    /**
     * Adds logging information.
     *
     * @param info the logging information
     * @throws NativeLibException for any registry error
     */
    public void addLoggingInfo(List info) throws NativeLibException
    {
    }

    /**
     * Rewinds all logged actions.
     *
     * @throws NativeLibException for any registry error
     */
    public void rewind() throws NativeLibException
    {
    }

    /**
     * Returns the uninstall name.
     *
     * @return the uninstall name. May be {@code null}
     */
    public String getUninstallName()
    {
        return uninstallName;
    }

    /**
     * Sets the uninstall name.
     *
     * @param name the uninstall name. May be {@code null}
     */
    public void setUninstallName(String name)
    {
        uninstallName = name;
    }

}
