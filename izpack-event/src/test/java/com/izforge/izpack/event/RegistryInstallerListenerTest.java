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

package com.izforge.izpack.event;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.coi.tools.os.win.RegDataContainer;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.NativeLibException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.os.RegistryDefaultHandler;
import com.izforge.izpack.core.os.RegistryHandler;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.unpacker.IUnpacker;
import com.izforge.izpack.test.RunOn;
import com.izforge.izpack.test.junit.PlatformRunner;
import com.izforge.izpack.test.util.TestLibrarian;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.PrivilegedRunner;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.Win_RegistryHandler;

/**
 * Tests the {@link RegistryInstallerListener} class.
 *
 * @author Tim Anderson
 */
@RunWith(PlatformRunner.class)
@RunOn(Platform.Name.WINDOWS)
public class RegistryInstallerListenerTest
{

    /**
     * Temporary folder to perform installations to.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The variable replacer.
     */
    private VariableSubstitutor replacer;

    /**
     * The installation data.
     */
    private InstallData installData;

    /**
     * The resources.
     */
    private Resources resources;

    /**
     * The unpacker.
     */
    private IUnpacker unpacker;

    /**
     * The uninstallation data.
     */
    private UninstallData uninstallData;

    /**
     * The rules.
     */
    private RulesEngine rules;

    /**
     * The housekeeper.
     */
    private Housekeeper housekeeper;

    /**
     * The registry handler.
     */
    private RegistryDefaultHandler handler;

    /**
     * The registry.
     */
    private RegistryHandler registry;


    /**
     * Sets up the test case.
     *
     * @throws IOException for any I/O error
     */
    @Before
    public void setUp() throws IOException
    {
        assertFalse("This test must be run as administrator, or with Windows UAC turned off",
                    new PrivilegedRunner(Platforms.WINDOWS).isElevationNeeded());

        Properties properties = new Properties();
        Variables variables = new DefaultVariables(properties);

        replacer = new VariableSubstitutorImpl(variables);

        AutomatedInstallData data = new AutomatedInstallData(variables, Platforms.WINDOWS);
        data.setMessages(Mockito.mock(Messages.class));
        installData = data;

        File installDir = temporaryFolder.getRoot();
        installData.setInstallPath(installDir.getPath());

        resources = Mockito.mock(Resources.class);
        InputStream specStream = getClass().getResourceAsStream("/com/izforge/izpack/event/registry/RegistrySpec.xml");
        assertNotNull(specStream);
        Mockito.when(resources.getInputStream(RegistryInstallerListener.SPEC_FILE_NAME)).thenReturn(specStream);
        Mockito.when(resources.getInputStream(RegistryInstallerListener.UNINSTALLER_ICON)).thenThrow(
                new ResourceNotFoundException("Resource not found"));

        unpacker = Mockito.mock(IUnpacker.class);
        uninstallData = new UninstallData();
        rules = Mockito.mock(RulesEngine.class);
        housekeeper = Mockito.mock(Housekeeper.class);
        handler = Mockito.mock(RegistryDefaultHandler.class);
        TargetFactory factory = Mockito.mock(TargetFactory.class);
        Mockito.when(factory.getNativeLibraryExtension()).thenReturn("dll");
        Librarian librarian = new TestLibrarian(factory, housekeeper);
        registry = new Win_RegistryHandler(librarian);
        Mockito.when(handler.getInstance()).thenReturn(registry);
    }

    /**
     * Verifies that the Windows registry is updated by {@link RegistryInstallerListener#afterPacks}.
     *
     * @throws NativeLibException for any regitry error
     */
    @Test
    public void testRegistry() throws NativeLibException
    {
        String appName = "IzPackRegistryTest";
        String appVersion = "1.0";
        String uninstallName = appName + "-" + appVersion;
        String appURL = "http://www.test.com";
        String uninstallKey = RegistryHandler.UNINSTALL_ROOT + uninstallName;
        String key = "SOFTWARE\\IzForge\\IzPack\\" + uninstallName;

        // clean out any existing key
        deleteKey(uninstallKey);
        deleteKey(key + "\\ЮникодТестКлюч");  // i.e. Unicode Test Key in Serbian (thanks google)
        deleteKey(key + "\\Path");
        deleteKey(key + "\\DWORD");
        deleteKey(key + "\\BIN");
        deleteKey(key + "\\MULTI");
        deleteKey(key);

        // initialise variables to support variable expansion of registry keys and values
        installData.setVariable("APP_NAME", appName);
        installData.setVariable("APP_VER", appVersion);
        installData.setVariable("UNINSTALL_NAME", uninstallName);
        installData.setVariable("APP_URL", appURL);

        // initialise the listener
        RegistryInstallerListener listener = new RegistryInstallerListener(
                unpacker, replacer, installData, uninstallData, resources, rules, housekeeper, handler);
        listener.initialise();

        // run the listener
        ProgressListener progressListener = Mockito.mock(ProgressListener.class);
        Pack pack = new Pack("Core", null, null, null, null, true, true, false, null, true, 0);
        listener.afterPacks(Arrays.asList(pack), progressListener);

        // verify RegistrySpec.xml changes applied to the registry

        // The first changes are for the special "UninstallStuff" pack.
        assertStringEquals(uninstallKey, "DisplayName", uninstallName);
        assertStringEquals(uninstallKey, "UninstallString", "\"$JAVA_HOME\\bin\\javaw.exe\" -jar \"" +
                installData.getInstallPath() + "\\uninstaller\\uninstaller.jar\"");
        assertStringEquals(uninstallKey, "DisplayIcon", installData.getInstallPath() + "\\bin\\icons\\izpack.ico");
        assertStringEquals(uninstallKey, "HelpLink", appURL);

        // Verify the "Core" pack changes applied
        assertKeyExists(key + "\\ЮникодТестКлюч");

        assertStringEquals(key, "Path", installData.getInstallPath());
        assertLongEquals(key, "DWORD", 42);
        assertBytesEquals(key, "BIN", new byte[]{0x42, 0x49, 0x4e, 0x20, 0x54, 0x45, 0x53, 0x54,
                0x42, 0x49, 0x4e, 0x20, 0x54, 0x45, 0x53, 0x54});
        assertStringsEquals(key, "MULTI", new String[]{"A multi string with three elements", "Element two",
                "Element three"});

        // now roll back the changes
        installData.setInstallSuccess(false);
        listener.cleanUp();

        // verify the entries no longer present
        assertKeyNotExists(uninstallKey);
        assertKeyNotExists(key);
    }

    /**
     * Asserts that a registry key exists.
     *
     * @param key the key to check
     * @throws NativeLibException for any registry error
     */
    private void assertKeyExists(String key) throws NativeLibException
    {
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        assertTrue(registry.keyExist(key));
    }

    /**
     * Asserts that a registry key doesn't exist.
     *
     * @param key the key to check
     * @throws NativeLibException for any registry error
     */
    private void assertKeyNotExists(String key) throws NativeLibException
    {
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        assertFalse(registry.keyExist(key));
    }

    /**
     * Asserts that a registry REG_SZ value exists for the specified key, and matches the given value.
     *
     * @param key      the key to check
     * @param name     the name of the value to check
     * @param expected the expected value
     * @throws NativeLibException for any registry error
     */
    private void assertStringEquals(String key, String name, String expected) throws NativeLibException
    {
        RegDataContainer value = getValue(key, name, RegDataContainer.REG_SZ, "REG_SZ");
        assertEquals(expected, value.getStringData());
    }

    /**
     * Asserts that a registry REG_DWORD value exists for the specified key, and matches the given value.
     *
     * @param key      the key to check
     * @param name     the name of the value to check
     * @param expected the expected value
     * @throws NativeLibException for any registry error
     */
    private void assertLongEquals(String key, String name, long expected) throws NativeLibException
    {
        RegDataContainer value = getValue(key, name, RegDataContainer.REG_DWORD, "REG_DWORD");
        assertEquals(expected, value.getDwordData());
    }

    /**
     * Asserts that a registry REG_BINARY value exists for the specified key, and matches the given value.
     *
     * @param key      the key to check
     * @param name     the name of the value to check
     * @param expected the expected value
     * @throws NativeLibException for any registry error
     */
    private void assertBytesEquals(String key, String name, byte[] expected) throws NativeLibException
    {
        RegDataContainer value = getValue(key, name, RegDataContainer.REG_BINARY, "REG_BINARY");
        assertArrayEquals(expected, value.getBinData());
    }

    /**
     * Asserts that a registry REG_MULTI_SZ value exists for the specified key, and matches the given value.
     *
     * @param key      the key to check
     * @param name     the name of the value to check
     * @param expected the expected value
     * @throws NativeLibException for any registry error
     */
    private void assertStringsEquals(String key, String name, String[] expected) throws NativeLibException
    {
        RegDataContainer value = getValue(key, name, RegDataContainer.REG_MULTI_SZ, "REG_MULTI_SZ");
        assertArrayEquals(expected, value.getMultiStringData());
    }

    /**
     * Returns the registry value for the specified key, name and value type.
     *
     * @param key      the registry key
     * @param name     the registry value name
     * @param type     the value type
     * @param typeName the symbolic type name, for error reporting
     * @return the corresponding value container
     * @throws NativeLibException for any registry error
     */
    private RegDataContainer getValue(String key, String name, int type, String typeName) throws NativeLibException
    {
        assertKeyExists(key);
        assertTrue(registry.valueExist(key, name));
        RegDataContainer value = registry.getValue(key, name);
        assertEquals("Registry key value " + name + " is not type " + typeName, type, value.getType());
        return value;
    }

    /**
     * Helper to delete a registry key.
     *
     * @param key the registry key
     * @throws NativeLibException for any registry error
     */
    private void deleteKey(String key) throws NativeLibException
    {
        registry.setRoot(RegistryHandler.HKEY_LOCAL_MACHINE);
        if (registry.keyExist(key))
        {
            registry.deleteKey(key);
        }
    }

}