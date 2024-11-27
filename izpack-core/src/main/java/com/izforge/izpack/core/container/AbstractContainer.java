/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Anthonin Bonnefoy
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

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.name.Names;
import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackClassNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * Abstract implementation of the {@link Container} interface.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
public abstract class AbstractContainer implements Container {

    /**
     * The underlying container.
     */
    private final List<SimpleModule<?>> modules = new ArrayList<>();
    private final Injector parent;
    private Injector injector;

    /**
     * Constructs an <tt>AbstractContainer</tt>.
     * <p/>
     * The container must be initialised via {@link #initialise()} before use.
     */
    public AbstractContainer() {
        this(null, true);
    }

    public AbstractContainer(boolean fillContainer) {
        this(null, fillContainer);
    }

    /**
     * Constructs an <tt>AbstractContainer</tt>.
     * <p/>
     * If a container is provided, {@link #initialise()} will be invoked. Subclasses should only
     * provide a container if they don't require their constructor to complete before <tt>initialise</tt> is called.
     *
     * @param parent the underlying container. May be <tt>null</tt>
     * @throws ContainerException if initialisation fails
     */
    private AbstractContainer(Injector parent, boolean fillContainer) {
        this.parent = parent;
        if (fillContainer) {
            initialise();
        }
    }

    /**
     * Register a component type.
     *
     * @param componentType the component type
     * @throws ContainerException if registration fails
     */
    @Override
    public <T> void addComponent(Class<T> componentType) {
        addModule(componentType, binder -> binder.in(Scopes.SINGLETON));
    }

    @Override
    public <T> void addComponent(T component) {
        addModule((Class<T>) component.getClass(), binder -> binder.toInstance(component));
    }

    @Override
    public <T, U extends T> void addProvider(Class<T> type, Class<? extends Provider<U>> provider) {
        addModule(type, binder -> binder.toProvider(provider).in(Scopes.SINGLETON));
    }

    @Override
    public <T, U extends T> void addProvider(Class<T> type, Provider<U> provider) {
        addModule(type, binder -> binder.toProvider(provider));
    }

    /**
     * Register a component.
     *
     * @param componentKey   the component identifier. This must be unique within the container
     * @param implementation the component implementation
     * @throws ContainerException if registration fails
     */
    @Override
    public <T, U extends T> void addComponent(Class<T> componentKey, Class<U> implementation) {
        addModule(componentKey, binder -> binder.to(implementation).in(Scopes.SINGLETON));
    }

    @Override
    public <T, U extends T> void addComponent(Class<T> componentKey, U implementation) {
        addModule(componentKey, binder -> binder.toInstance(implementation));
    }

    @Override
    public <T, U extends T> void addComponent(String componentKey, Class<T> type, U implementation) {
        addModule(type, binder -> binder
                .annotatedWith(Names.named(componentKey))
                .toInstance(implementation));
    }

    @Override
    public <T, U extends T> void addComponent(String componentKey, Class<T> type, Class<U> implementation) {
        addModule(type, binder -> binder
                .annotatedWith(Names.named(componentKey))
                .to(implementation)
                .in(Scopes.SINGLETON));
    }

    private <T> void addModule(Class<T> componentKey, Consumer<AnnotatedBindingBuilder<T>> mapper) {
        if (this.injector != null) {
            throw new IllegalStateException("Cannot add " + componentKey.getSimpleName() + " after container has been initialized");
        }
        this.modules.add(new SimpleModule<T>(componentKey, mapper));
    }

    @Override
    public <T> void removeComponent(Class<T> componentType) {
        if (this.injector != null) {
            throw new IllegalStateException("Cannot remove component " + componentType.getSimpleName() + " after container has been initialized");
        }
        this.modules.removeIf(module -> module.componentKey.equals(componentType));
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
    public <T> T getComponent(Class<T> componentType) {
        return getInjector().getInstance(componentType);
    }

    /**
     * Retrieve a component by its component key or type.
     * <p/>
     * If the component type is registered but an instance does not exist, then it will be created.
     *
     * @param key the key or type of the component
     * @return the corresponding object instance, or <tt>null</tt> if it does not exist
     * @throws ContainerException if component creation fails
     */
    @Override
    public <T> T getComponent(String key, Class<T> type) {
        return getInjector().getInstance(Key.get(type, Names.named(key)));
    }

    private Injector getInjector() {
        if (injector == null) {
            if (parent != null) {
                injector = parent.createChildInjector(modules);
            } else {
                injector = Guice.createInjector(modules);
            }
        }
        return injector;
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
     * @throws ContainerException if creation fails
     */
    @Override
    public Container createChildContainer() {
        return new ChildContainer(getInjector());
    }

    /**
     * Removes a child container.
     *
     * @param child the container to remove
     * @return <tt>true</tt> if the container was removed
     */
    @Override
    public boolean removeChildContainer(Container child) {
        // Not supported by Guice
        return false;
    }


    /**
     * Disposes of the container and all of its child containers.
     */
    @Override
    public void dispose() {
        modules.clear();
        injector = null;
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
    @SuppressWarnings("unchecked")
    public <T> Class<T> getClass(String className, Class<T> superType) {
        @SuppressWarnings("rawtypes")
        Class type;
        try {
            // Using the superclass class loader to load the child to avoid multiple copies of the superclass being
            // loaded in separate class loaders. This is typically an issue during testing where
            // the same classes may be loaded twice - once by maven, and once by the installer.
            ClassLoader classLoader = superType.getClassLoader();
            if (classLoader == null) {
                // may be null for bootstrap class loader
                classLoader = getClass().getClassLoader();
            }
            type = classLoader.loadClass(className);
            if (!superType.isAssignableFrom(type)) {
                throw new ClassCastException("Class '" + type.getName() + "' does not implement "
                        + superType.getName());
            }
        } catch (ClassNotFoundException exception) {
            throw new IzPackClassNotFoundException(className, exception);
        }
        return (Class<T>) type;
    }

    /**
     * Initialises the container.
     * <p/>
     * This must only be invoked once.
     *
     * @throws ContainerException if initialisation fails, or the container has already been initialised
     */
    protected void initialise() {
        if (this.injector != null) {
            throw new ContainerException("Container already initialised");
        }
        fillContainer();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     * <p/>
     * This exposes the underlying <tt>PicoContainer</tt> to enable subclasses to perform complex initialisation.
     * <p/>
     * For convenience, implementations are permitted to throw <tt>PicoException</tt> - these
     * will be rethrown as {@link ContainerException}.
     * <p/>
     *
     * @throws ContainerException if initialisation fails
     */
    protected void fillContainer() {
    }

    /**
     * Concrete container used by {@link #createChildContainer()}.
     */
    private static class ChildContainer extends AbstractContainer {

        /**
         * Constructs a ChildContainer.
         *
         * @param parent the parent container
         */
        public ChildContainer(Injector parent) {
            super(parent, true);
        }
    }

    private static class SimpleModule<T> implements com.google.inject.Module {
        private final Class<T> componentKey;
        private final Consumer<AnnotatedBindingBuilder<T>> mapper;

        private SimpleModule(Class<T> componentKey, Consumer<AnnotatedBindingBuilder<T>> mapper) {
            this.componentKey = componentKey;
            this.mapper = mapper;
        }

        @Override
        public final void configure(Binder binder) {
            mapper.accept(binder.bind(componentKey));
        }
    }
}
