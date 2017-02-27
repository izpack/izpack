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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;

/*---------------------------------------------------------------------------*/
/**
 * This class is the system independent base class for helpers which are system
 * dependent in its subclasses.
 *
 * @author Klaus Bartz
 * @deprecated no replacement
 */
/*---------------------------------------------------------------------------*/
@Deprecated
public class OSClassHelper
{

    private static final Logger LOGGER = Logger.getLogger(OSClassHelper.class.getName());

    protected InstallData installdata;

    protected Class workerClass = null;

    protected Object worker = null;

    /**
     * Default constructor
     */
    public OSClassHelper()
    {
        super();
    }

    /**
     * Creates an object which contains as worker an object of the given class
     * name if possible. If not possible, only the stack trace will be printed,
     * no exception will be raised. To determine the state, there is the method
     * good.
     *
     * @param className full qualified class name of the needed worker
     */
    public OSClassHelper(String className)
    {
        super();

        try
        {
            workerClass = Class.forName(className);
            worker = workerClass.newInstance();
        }
        catch (InstantiationException e)
        {
            LOGGER.log(Level.WARNING, "", e);
        }
        catch (IllegalAccessException e)
        {
            LOGGER.log(Level.WARNING, "", e);
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.log(Level.WARNING, "", e);
            // Do nothing, class not bound.
        }
        catch (Exception e)
        {   // If the native lib is not found an unqualified Exception will be raised.
            LOGGER.warning("Ctor OSClassHelper for " + className + ": worker not available (" + e.getMessage() + ")");
            return;
        }
        LOGGER.fine("Ctor OSClassHelper for " + className + " is good: " + good());

    }

    /**
     * Return whether the helper can do the work or not.
     *
     * @return whether the helper can do the work or not
     */
    public boolean good()
    {
        return (worker != null);
    }

    /**
     * Verifies the helper.
     *
     * @param idata current install data
     * @return whether the helper is good or not
     * @throws Exception
     */
    public boolean verify(InstallData idata) throws Exception
    {
        installdata = idata;
        return (false);
    }

}
