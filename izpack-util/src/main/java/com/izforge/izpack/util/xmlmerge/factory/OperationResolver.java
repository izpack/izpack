/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2009 Laurent Bovet, Alex Mathey
 * Copyright 2010, 2012 René Krell
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

package com.izforge.izpack.util.xmlmerge.factory;

import java.lang.reflect.Field;

import com.izforge.izpack.util.xmlmerge.ConfigurationException;
import com.izforge.izpack.util.xmlmerge.Operation;

/**
 * Creates an operation instance given a short name (alias) or a class name.
 *
 * @author Laurent Bovet (LBO)
 * @author Alex Mathey (AMA)
 */
public class OperationResolver
{

    /**
     * Class representing an operation.
     */
    Class m_constantClass;

    /**
     * Creates an OperationResolver given the class representing an operation.
     *
     * @param class1 The class of an operation
     */
    public OperationResolver(Class class1)
    {
        m_constantClass = class1;
    }

    /**
     * Resolves an alias or an operation class name to an operation.
     *
     * @param aliasOrClassName an alias or class name representing an operation
     * @return The resolved operation
     * @throws ConfigurationException If an error occurred during the resolving process
     */
    public Operation resolve(String aliasOrClassName) throws ConfigurationException
    {
        Field field = null;
        try
        {
            field = m_constantClass.getField(aliasOrClassName.toUpperCase());
        }
        catch (NoSuchFieldException e)
        {

            try
            {
                return (Operation) Class.forName(aliasOrClassName).newInstance();
            }
            catch (InstantiationException e1)
            {
                throw new ConfigurationException("Cannot instanciate object from class "
                        + aliasOrClassName);
            }
            catch (IllegalAccessException e1)
            {
                throw new ConfigurationException("Cannot access constructor or class "
                        + aliasOrClassName);
            }
            catch (ClassNotFoundException e1)
            {
                throw new ConfigurationException("Verb not found or class not found:"
                        + aliasOrClassName);
            }
            catch (ClassCastException e1)
            {
                throw new ConfigurationException("Class does not implement Operation :"
                        + aliasOrClassName);
            }

        }
        try
        {
            return (Operation) field.get(null);
        }
        catch (IllegalAccessException e)
        {
            // should not happen
            throw new ConfigurationException(e);
        }
    }

}
