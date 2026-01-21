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
package com.izforge.izpack.installer.container.provider;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.resource.DefaultLocales;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.api.data.PackInfo;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.Platforms;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AutomatedInstallDataProvider} class.
 *
 * @author Tim Anderson
 */
public class AutomatedInstallDataProviderTest
{

    /**
     * Verifies that a custom lang pack may be specified in a resource file, and that the custom messages override
     * those in the default lang pack.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCustomLangPack() throws Exception
    {
        ClassLoader loader = Mockito.mock(ClassLoader.class);
        ResourceManager resources = new ResourceManager(loader)
        {
            @Override
            public Object getObject(String name)
            {
                if (name.equals("langpacks.info"))
                {
                    return Arrays.asList("eng", "fra");
                }

                return super.getObject(name);
            }
        };

        // set up mock resources
        mock(loader, "resources/info", new Info());
        mock(loader, "resources/panelsOrder", new ArrayList<Panel>());
        mock(loader, "resources/packs.info", createPacksInfo());
        mock(loader, "resources/vars", new Properties());
        mock(loader, "resources/dynvariables", new ArrayList<DynamicVariable>());
        mock(loader, "resources/installerrequirements", new ArrayList<InstallerRequirement>());
        mock(loader, "resources/dynconditions", new ArrayList<DynamicInstallerRequirementValidator>());

        // now set up the default lang pack, and the custom lang pack
        InputStream defaultPack = createLangPack("str id='standard.message' txt='This is a standard message'",
                                                 "str id='overridden.message' txt='This should be replaced'",
                                                 "str id='overridden.message2' txt='This should also be replaced'");
        InputStream customPack = createLangPack("str id='custom.message' txt='This is a custom message'",
                                                "str id='overridden.message' txt='Message overridden'");
        InputStream userInputPack = createLangPack("str id='user.input.message' txt='This is a user input panel message'",
                                                   "str id='overridden.message2' txt='Message2 overridden'");

        mock(loader, "resources/langpacks/eng.xml", defaultPack);
        mock(loader, "resources/" + Resources.CUSTOM_TRANSLATIONS_RESOURCE_NAME + "_eng", customPack);
        mock(loader, "resources/" + Resources.USER_INPUT_TRANSLATIONS_RESOURCE_NAME + "_eng", userInputPack);

        // set up the locale to english because the mock resources contain only a langpack for english
        Locales locales = new DefaultLocales(resources, Locale.ENGLISH);
        resources.setLocales(locales);

        AutomatedInstallDataProvider provider = new AutomatedInstallDataProvider();

        DefaultVariables variables = new DefaultVariables();
        Housekeeper housekeeper = Mockito.mock(Housekeeper.class);
        PlatformModelMatcher matcher = Mockito.mock(PlatformModelMatcher.class);
        when(matcher.getCurrentPlatform()).thenReturn(Platforms.MANDRAKE_LINUX);

        // populate the installation data
        AutomatedInstallData installData = provider.provide(resources, locales, variables, housekeeper, matcher);

        // verify the expected messages are returned
        Messages messages = installData.getMessages();
        assertNotNull(messages);

        assertEquals("This is a standard message", messages.get("standard.message"));
        assertEquals("This is a custom message", messages.get("custom.message"));
        assertEquals("Message overridden", messages.get("overridden.message"));
        assertEquals("This is a user input panel message", messages.get("user.input.message"));
        assertEquals("Message2 overridden", messages.get("overridden.message2"));
    }

    /**
     * Helper to create a lang pack and return a stream to it.
     *
     * @param messages the lang pack messages, of the form {@code "str id='key' txt='value'"}
     * @return a stream of the messages
     * @throws IOException for any I/O error
     */
    private InputStream createLangPack(String... messages) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        writer.write("<izpack:langpack");
        writer.write(" version=\"5.0\"");
        writer.write(" xmlns:izpack=\"http://izpack.org/schema/langpack\"");
        writer.write(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        writer.write(" xsi:schemaLocation=\"http://izpack.org/schema/langpack https://izpack.org/schema/5.0/izpack-langpack-5.0.xsd\">\n");
        for (String message : messages)
        {
            writer.write("<" + message + "/>\n");
        }
        writer.write("</izpack:langpack>");
        writer.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

    /**
     * Helper to return a stream for the given resource path when {@link ClassLoader#getResourceAsStream(String)} is
     * invoked.
     *
     * @param loader the mocked class loader
     * @param path   the resource path
     * @param stream the stream to return for the specified resource path
     * @throws IOException for any I/O error
     */
    private void mock(ClassLoader loader, String path, InputStream stream) throws IOException
    {
        when(loader.getResourceAsStream(path)).thenReturn(stream);

        //  set up a dummy URL to indicate that the resource exists
        when(loader.getResource(path)).thenReturn(new URL("file://" + path));
    }

    /**
     * Helper to return a stream for the given resource when {@link ClassLoader#getResourceAsStream(String)} is
     * invoked.
     *
     * @param loader   the mocked class loader
     * @param path     the resource path
     * @param resource the resource to serialize and return
     * @throws IOException for any I/O error
     */
    private void mock(ClassLoader loader, String path, Object resource) throws IOException
    {
        mock(loader, path, serialize(resource));
    }

    /**
     * Helper to serialize and return a stream to an object.
     *
     * @param object the object to serialize
     * @return a stream of the serialized object
     * @throws IOException for any I/O error
     */
    private InputStream serialize(Object object) throws IOException
    {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
        objectOutput.writeObject(object);
        objectOutput.close();
        return new ByteArrayInputStream(byteOutput.toByteArray());
    }

    /**
     * Helper to create a dummy "packs.info" resource.
     *
     * @return a stream of the "packs.info" resource
     * @throws IOException for any I/O error
     */
    private InputStream createPacksInfo() throws IOException
    {
        List<PackInfo> packsInfo = new ArrayList<PackInfo>();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(stream);
        objStream.writeObject(packsInfo);
        objStream.close();
        return new ByteArrayInputStream(stream.toByteArray());
    }

}
