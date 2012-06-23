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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.exception.ResourceInterruptedException;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.resource.Resources;


/**
 * Abstract implementation of the {@link PackResources} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPackResources implements PackResources
{
    /**
     * The resources.
     */
    private final Resources resources;

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * Constructs an {@code AbstractPackResources}.
     *
     * @param resources   the resources
     * @param installData the installation data
     */
    public AbstractPackResources(Resources resources, InstallData installData)
    {
        this.installData = installData;
        this.resources = resources;
    }

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     * @throws ResourceException            for any other resource error
     */
    @Override
    public InputStream getPackStream(String name)
    {
        InputStream result;
        String webDirURL = installData.getInfo().getWebDirURL();

        if (webDirURL == null)
        {
            result = getLocalPackStream(name);
        }
        else
        {
            result = getWebPackStream(name, webDirURL);
        }
        String className = installData.getInfo().getPackDecoderClassName();
        if (className != null)
        {
            result = getDecodingInputStream(result, className);
        }

        return result;
    }

    /**
     * Returns the stream to a resource.
     *
     * @param name the resource name
     * @return a stream to the resource
     * @throws ResourceNotFoundException if the resource cannot be found
     * @throws ResourceException         for any other resource error
     */
    @Override
    public InputStream getInputStream(String name)
    {
        // TODO - this is invoked to get multi-volume info, so should check on web dir.
        return resources.getInputStream(name);
    }

    /**
     * Returns a stream that decodes the supplied stream.
     *
     * @param in        the stream to decode
     * @param className the decoding input stream class name.
     * @return the decoding stream
     * @throws ResourceException for any error
     */
    protected InputStream getDecodingInputStream(InputStream in, String className)
    {
        Object result;
        try
        {
            Class decoder = Class.forName(className);
            Class[] paramsClasses = {java.io.InputStream.class};
            Constructor constructor = decoder.getDeclaredConstructor(paramsClasses);
            // Our first used decoder input stream (bzip2) reads byte for byte from
            // the source. Therefore we put a buffering stream between it and the source.
            InputStream buffer = new BufferedInputStream(in);
            Object[] params = {buffer};
            result = constructor.newInstance(params);
        }
        catch (Exception exception)
        {
            throw new ResourceException("Failed to create stream to decode resource", exception);
        }

        if (!InputStream.class.isInstance(result))
        {
            throw new ResourceException("Cannot decode resource: '" + className + "' must be derived from "
                                                + InputStream.class.getName());
        }
        return (InputStream) result;
    }

    /**
     * Returns a stream to a local pack.
     *
     * @param name the pack name
     * @return the pack stream
     */
    protected InputStream getLocalPackStream(String name)
    {
        return resources.getInputStream("packs/pack-" + name);
    }

    /**
     * Returns the stream to a web-based pack resource.
     *
     * @param name      the resource name
     * @param webDirURL the web URL to load the resource from
     * @return a stream to the resource
     * @throws ResourceNotFoundException    if the resource cannot be found
     * @throws ResourceInterruptedException if resource retrieval is interrupted
     */
    protected abstract InputStream getWebPackStream(String name, String webDirURL);

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

}
