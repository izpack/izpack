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

package com.izforge.izpack.core.container;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackClassNotFoundException;


/**
 * A {@link Container} that delegates to another.
 *
 * @author Tim Anderson
 */
public abstract class AbstractDelegatingContainer implements Container
{

    /**
     * The container to delegate to.
     */
    private final Container container;


    /**
     * Constructs an <tt>AbstractDelegatingContainer</tt>.
     *
     * @param container the container
     */
    public AbstractDelegatingContainer(Container container)
    {
        this.container = container;
    }

    /**
     * Register a component type.
     *
     * @param componentType the component type
     * @throws ContainerException if registration fails
     */
    @Override
    public <T> void addComponent(Class<T> componentType)
    {
        container.addComponent(componentType);
    }

    /**
     * Register a component.
     *
     * @param componentKey   the component identifier. This must be unique within the container
     * @param implementation the component implementation
     * @throws ContainerException if registration fails
     */
    @Override
    public void addComponent(Object componentKey, Object implementation)
    {
        container.addComponent(componentKey, implementation);
    }

    /**
     * Retrieve a component by its component type.
     * <p/>
     * If the component type is registered but an instance does not exist, then it will be created.
     *
     * @param componentType the type of the component
     * @return the corresponding object instance, or <tt>null</tt> if it does not exist
     * @throws ContainerException if component creation fails
     */
    @Override
    public <T> T getComponent(Class<T> componentType)
    {
        return container.getComponent(componentType);
    }

    /**
     * Retrieve a component by its component key or type.
     * <p/>
     * If the component type is registered but an instance does not exist, then it will be created.
     *
     * @param componentKeyOrType the key or type of the component
     * @return the corresponding object instance, or <tt>null</tt> if it does not exist
     * @throws ContainerException if component creation fails
     */
    @Override
    public Object getComponent(Object componentKeyOrType)
    {
        return container.getComponent(componentKeyOrType);
    }

    /**
     * Creates a child container.
     * <p/>
     * A child container:
     * <ul>
     * <li>may have different objects keyed on the same identifiers as its parent.</li>
     * <li>will query its parent for dependencies if they aren't available</li>
     * <li>is disposed when its parent is disposed</li>
     * </ul>
     *
     * @return a new container
     */
    @Override
    public Container createChildContainer()
    {
        return container.createChildContainer();
    }

    /**
     * Removes a child container.
     *
     * @param child the container to remove
     * @return <tt>true</tt> if the container was removed
     */
    @Override
    public boolean removeChildContainer(Container child)
    {
        return container.removeChildContainer(child);
    }

    /**
     * Disposes of the container and all of its child containers.
     */
    @Override
    public void dispose()
    {
        container.dispose();
    }

    /**
     * Returns a class given its name.
     *
     * @param className the class name
     * @param superType the super type
     * @return the corresponding class
     * @throws ClassCastException           if <tt>className</tt> does not implement or extend <tt>superType</tt>
     * @throws IzPackClassNotFoundException if the class cannot be found
     */
    @Override
    public <T> Class<T> getClass(String className, Class<T> superType)
    {
        return container.getClass(className, superType);
    }
}
