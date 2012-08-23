/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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

package com.izforge.izpack.util.config;

import java.io.IOException;
import java.util.logging.Logger;

import com.izforge.izpack.util.config.base.Reg;

public class RegistryTask extends SingleConfigurableTask
{
    private static final Logger logger = Logger.getLogger(RegistryTask.class.getName());

    /*
     * Instance variables.
     */

    protected String key;
    protected String fromKey;

    /**
     * Location of the configuration file to be edited; required.
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * Location of the configuration file to be patched from; optional.
     */
    public void setFromKey(String key)
    {
        this.fromKey = key;
    }

    @Override
    protected void readSourceConfigurable() throws Exception
    {
        // deal with a registry key to patch from
        if (this.fromKey != null)
        {
            try
            {
                logger.fine("Loading from registry: " + this.fromKey);
                fromConfigurable = new Reg(this.fromKey);
            }
            catch (IOException ioe)
            {
                throw new Exception(ioe.toString());
            }
        }
    }

    @Override
    protected void readConfigurable() throws Exception
    {
        if (this.key != null)
        {
            try
            {
                logger.fine("Loading from registry: " + this.key);
                configurable = new Reg(this.key);
            }
            catch (IOException ioe)
            {
                throw new Exception(ioe.toString());
            }
        }
    }

    @Override
    protected void writeConfigurable() throws Exception
    {

        if (configurable == null)
        {
            logger.warning("Registry key " + this.key
                    + " did not exist and is not allowed to be created");
            return;
        }

        try
        {
            Reg r = (Reg) configurable;
            r.store();
        }
        catch (IOException ioe)
        {
            throw new Exception(ioe);
        }
    }

    @Override
    protected void checkAttributes() throws Exception
    {
        if (this.key == null) { throw new Exception("Key attribute must be set"); }
    }
}
