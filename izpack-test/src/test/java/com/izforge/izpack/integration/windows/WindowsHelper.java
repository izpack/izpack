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

package com.izforge.izpack.integration.windows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.swing.SwingUtilities;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.os.ShellLink;

/**
 * Helper for Windows tests.
 *
 * @author Tim Anderson
 */
public class WindowsHelper
{
    /**
     * Verifies a shortcut matches that expected.
     *
     * @param linkType    the type of link, one of the following values: <br>
     *                    <ul>
     *                    <li><code>ShellLink.DESKTOP</code>
     *                    <li><code>ShellLink.PROGRAM_MENU</code>
     *                    <li><code>ShellLink.START_MENU</code>
     *                    <li><code>ShellLink.STARTUP</code>
     *                    </ul>
     * @param userType    the type of user for the link path
     * @param group       the program group (directory) of this link. If the link is not part of a program
     *                    group, pass an empty string or null for this parameter. (...\\Desktop\\group).
     * @param name        the file name of this link. Do not include a file extension.
     * @param target      the expected target of the link
     * @param description the expected shortcut description
     * @param librarian   the librarian
     * @return the shortcut
     */
    public static File checkShortcut(final int linkType, final int userType, final String group, final String name,
                                     final File target, final String description, final Librarian librarian)
            throws Exception
    {
        final File[] shortcut = new File[1];
        // TODO - need to create ShellLink in the same thread each time, or it fails with a COM error.
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                ShellLink link;
                try
                {
                    link = new ShellLink(linkType, userType, group, name, librarian);
                }
                catch (Exception exception)
                {
                    throw new IzPackException(exception);
                }
                assertEquals(linkType, link.getLinkType());
                assertEquals(userType, link.getUserType());
                assertEquals(target, new File(link.getTargetPath()));
                assertEquals(description, link.getDescription());

                // verify the shortcut file exists
                shortcut[0] = new File(link.getFileName());
                assertTrue(shortcut[0].exists());

            }
        });
        return shortcut[0];
    }

    /**
     * Determines if a key exists in the registry.
     *
     * @param handler the registry handler
     * @param key      the key to check
     * @return <tt>true</tt> if the key exists, otherwise <tt>false</tt>
     * @throws NativeLibException for any registry error
     */
    public static boolean registryKeyExists(RegistryDefaultHandler handler, String key) throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        assertNotNull(registry);
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        return registry.keyExist(key);
    }

    /**
     * Asserts that a registry key REG_SZ value exists, and exactly matches the given value.
     *
     * @param handler the registry handler
     * @param key      the key to check
     * @param name     the name of the value to check
     * @param expected the value to match
     * @throws NativeLibException for any registry error
     */
    public static void registryValueStringEquals(RegistryDefaultHandler handler, String key, String name,
                                                 String expected) throws NativeLibException
    {
        //Registry key exists
    	RegistryHandler registry = handler.getInstance();
        assertNotNull(registry);
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        assertTrue(registry.keyExist(key));
        //Value exists as a REG_SZ
        assertTrue(registry.valueExist(key, name));
        RegDataContainer value = registry.getValue(key, name);
        assertEquals("Registry key value " + name + " is not type REG_SZ", RegDataContainer.REG_SZ, value.getType());
        //Value matches expected string
        assertEquals(expected, registry.getValue(key, name).getStringData());
    }

    /**
     * Deletes an uninstallation registry key.
     *
     * @param handler the registry handler
     * @param key      the key to delete
     * @throws NativeLibException for any registry error
     */
    public static void registryDeleteUninstallKey(RegistryDefaultHandler handler, String key) throws NativeLibException
    {
        RegistryHandler registry = handler.getInstance();
        assertNotNull(registry);
        if (!key.matches(".*\\\\Uninstall\\\\.+"))
        {
            // don't want to delete too much
            throw new IllegalArgumentException("Invalid key for deletion: " + key);
        }
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        if (registry.keyExist(key))
        {
            registry.deleteKey(key);
        }
    }
}