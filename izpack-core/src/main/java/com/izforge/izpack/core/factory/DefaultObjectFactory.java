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

package com.izforge.izpack.core.factory;


import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.util.ClassUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


/**
 * Default implementation of {@link ObjectFactory}.
 *
 * @author Tim Anderson
 */
@ApplicationScoped
public class DefaultObjectFactory implements ObjectFactory
{
    /**
     * The container.
     */
    private final Container container;


    /**
     * Constructs a <tt>DefaultObjectFactory</tt>.
     *
     * @param container the container
     */
    @Inject
    public DefaultObjectFactory(Container container)
    {
        this.container = container;
    }

    /**
     * Creates a new instance of the specified type.
     * <p/>
     * Constructor arguments may be specified as parameters, or injected by the factory.
     * When specified as parameters, order is unimportant, but must be unambiguous.
     *
     * @param type       the object type
     * @param parameters additional constructor parameters
     * @return a new instance
     */
    @Override
    public <T> T create(Class<T> type, Object... parameters)
    {
        if (parameters.length == 0)
        {
            return container.getComponent(type);
        }
        else
        {
            throw new UnsupportedOperationException("create with arguments");
        }
        
    }

    /**
     * Creates a new instance of the specified class name.
     * <p/>
     * Constructor arguments may be specified as parameters, or injected by the factory.
     * When specified as parameters, order is unimportant, but must be unambiguous.
     *
     * @param className  the class name
     * @param superType  the super type
     * @param parameters additional constructor parameters
     * @return a new instance
     * @throws ClassCastException if <tt>className</tt> does not implement or extend <tt>superType</tt>
     * @throws com.izforge.izpack.api.exception.IzPackClassNotFoundException
     *                            if the class cannot be found
     */
    @Override
    public <T> T create(String className, Class<T> superType, Object... parameters)
    {
        Class<? extends T> type = ClassUtil.getClass(className, superType);
        return create(type, parameters);
    }
}
