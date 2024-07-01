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

package com.izforge.izpack.core.resource;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import jakarta.enterprise.inject.Vetoed;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;


/**
 * Abstract implementation of {@link Resources}.
 *
 * @author Tim Anderson
 */
@Vetoed
public class AbstractResources implements Resources
{

    /**
     * The class loader.
     */
    private final ClassLoader loader;


    /**
     * Constructs an {@code AbstractResources} using the default class loader.
     */
    public AbstractResources()
    {
        this(AbstractResources.class.getClassLoader());
    }

    /**
     * Constructs an {@code AbstractResources} with the specified class loader.
     *
     * @param loader the loader to load resources
     */
    public AbstractResources(ClassLoader loader)
    {
        this.loader = loader;
    }

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public InputStream getInputStream(final String name)
    {
        final String resolvedName = resolveName(name);
        final InputStream result = loader.getResourceAsStream(resolvedName);
        if (result == null)
        {
            throw new ResourceNotFoundException("Failed to locate resource: " + resolvedName);
        }
        return result;
    }

    /**
     * Returns the URL to a resource.
     *
     * @param name the resource name
     * @return the URL to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public URL getURL(final String name)
    {
        final URL resource = getResource(name);
        if (resource == null)
        {
            throw new ResourceNotFoundException("Failed to locate resource: " + name);
        }
        return resource;
    }

    /**
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name the resource name
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         if the resource cannot be retrieved
     */
    @Override
    public String getString(final String name)
    {
        try
        {
            return readString(name, "UTF-8");
        }
        catch (IOException exception)
        {
            throw new ResourceException("Failed to read string resource: " + name, exception);
        }
    }

    /**
     * Returns a UTF-8 encoded resource as a string.
     *
     * @param name         the resource name
     * @param defaultValue the default value, if the resource cannot be found or retrieved
     * @return the resource as a string, or {@code defaultValue} if the resource cannot be found or retrieved
     */
    @Override
    public String getString(final String name, final String defaultValue)
    {
        return getString(name, "UTF-8", defaultValue);
    }

    /**
     * Returns a resource as a string.
     *
     * @param name         the resource name
     * @param encoding     the resource encoding. May be {@code null}
     * @param defaultValue the default value, if the resource cannot be found or retrieved
     * @return the resource as a string, or {@code defaultValue} if the resource cannot be found or retrieved
     */
    @Override
    public String getString(final String name, final String encoding, final String defaultValue)
    {
        String result;
        try
        {
            result = readString(name, encoding);
        }
        catch (Exception exception)
        {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns an object resource.
     *
     * @param name the resource name
     * @return the object resource
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         if the resource cannot be retrieved
     */
    @Override
    public Object getObject(final String name) throws ResourceException, ResourceNotFoundException
    {
        try (InputStream in = getInputStream(name); ObjectInputStream objectIn = new ObjectInputStream(in))
        {
            return objectIn.readObject();
        }
        catch (Exception exception)
        {
            throw new ResourceException("Failed to read resource: " + name, exception);
        }
    }

    /**
     * Returns an {@code ImageIcon} resource.
     *
     * @param name         the resource name
     * @param alternatives alternative resource names, if {@code name} is not found
     * @return the corresponding {@code ImageIcon}
     * @throws ResourceNotFoundException if the resource cannot be found
     */
    @Override
    public ImageIcon getImageIcon(final String name, final String... alternatives)
    {
        URL resource = getResource(name);
        if (resource == null)
        {
            for (String fallback : alternatives)
            {
                resource = getResource(fallback);
                if (resource != null)
                {
                    break;
                }
            }
        }
        if (resource == null)
        {
            final StringBuilder message = new StringBuilder("Image icon resource not found in ");
            message.append(name);
            if (alternatives.length != 0)
            {
                message.append(" or ");
                message.append(Arrays.toString(alternatives));
            }
            throw new ResourceNotFoundException(message.toString());
        }

        // must use ImageIO to support BMP files
        try {
            final Image image = ImageIO.read(resource);
            return new ImageIcon(image);
        }
        catch (IOException ex) {
            throw new ResourceNotFoundException("Image icon resource not available from url: " + resource);
        }
    }

    /**
     * Returns a resource URL.
     *
     * @param name the resource name
     * @return the corresponding URL, or {@code null} if the resource cannot be found
     */
    protected URL getResource(final String name)
    {
        final String resolvedName = resolveName(name);
        return loader.getResource(resolvedName);
    }

    /**
     * Resolves relative resource names.
     * <p/>
     * This implementation assumes that all names are absolute.
     *
     * @param name the resource name
     * @return the absolute resource name, minus any leading '/'
     */
    protected String resolveName(final String name)
    {
        if (name.charAt(0) == '/')
        {
            return name.substring(1);
        }
        return name;
    }

    /**
     * Reads a string resource.
     *
     * @param name     the resource name
     * @param encoding the resource encoding. May be {@code null}
     * @return the resource as a string
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws java.io.IOException       for any I/O error
     */
    protected String readString(final String name, final String encoding) throws IOException
    {
        try (InputStream in = getInputStream(name))
        {
            return IOUtils.toString(in, Charsets.toCharset(encoding));
        }
    }

}
