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

import com.izforge.izpack.util.TargetFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides on windows a registry handler. All classes which needs registry access should
 * be use only one handler.
 *
 * @author Klaus Bartz
 */
public class RegistryDefaultHandler
{

    /**
     * The registry handler.
     */
    private RegistryHandler registryHandler;

    /**
     * The factory for creating {@link RegistryHandler} instances for the current platform.
     */
    private TargetFactory factory;

    /**
     * True if an attempt has been made to initialise {@link #registryHandler}.
     */
    private boolean initialized = false;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(RegistryDefaultHandler.class.getName());

    /**
     * Constructs a <tt>RegistryDefaultHandler</tt>.
     *
     * @param factory the factory for creating {@link RegistryHandler} instances for the current platform
     */
    public RegistryDefaultHandler(TargetFactory factory)
    {
        this.factory = factory;
    }

    public synchronized RegistryHandler getInstance()
    {
        if (!initialized)
        {
            try
            {
                // Load the system dependant handler.
                registryHandler = factory.makeObject(RegistryHandler.class);
            }
            catch (Throwable exception)
            {
                log.log(Level.WARNING, "Failed to create RegistryHandler: " + exception.getMessage(), exception);
            }
            initialized = true;
        }

        return (registryHandler);
    }
}
