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

import java.io.InputStream;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Resources;


/**
 * Console-based implementation of the {@link PackResources} interface.
 * <br/>
 * This implementation does not support web-based pack resources. TODO
 *
 * @author Tim Anderson
 */
public class ConsolePackResources extends AbstractPackResources
{

    /**
     * Constructs a {@code DefaultPackResources}.
     *
     * @param resources the local resources
     */
    public ConsolePackResources(Resources resources, InstallData installData)
    {
        super(resources, installData);
    }

    /**
     * Returns the stream to a web-based pack resource.
     *
     * @param name      the resource name
     * @param webDirURL the web URL to load the resource from
     * @return a stream to the resource
     * @throws ResourceException if invoked
     */
    @Override
    protected InputStream getWebPackStream(String name, String webDirURL)
    {
        throw new ResourceException("Cannot retrieve web-based resource: " + name + " from URL: " + webDirURL
                                            + ". This operation is not supported.");
    }
}
