package com.izforge.izpack.api.container;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.IzPackClassNotFoundException;

public interface ContainerConfigurer {

    /**
     * Register a component type.
     *
     * @param componentType the component type
     * @throws ContainerException if registration fails
     */
    <T> void addComponent(Class<T> componentType);

    <T> void addComponent(T component);

    <T, U extends T> void addProvider(Class<T> type, Class<? extends Provider<U>> provider);

    <T, U extends T> void addProvider(Class<T> type, Provider<U> provider);

    /**
     * Register a component.
     *
     * @param componentKey   the component identifier. This must be unique within the container
     * @param implementation the component implementation
     * @throws ContainerException if registration fails
     */
    <T, U extends T> void addComponent(Class<T> componentKey, Class<U> implementation);

    <T, U extends T> void addComponent(Class<T> componentKey, U implementation);

    <T, U extends T> void addComponent(String componentKey, Class<T> type, U implementation);

    <T, U extends T> void addComponent(String componentKey, Class<T> type, Class<U> implementation);

    <T, U extends T> void addComponent(TypeLiteral<T> componentKey, Class<U> implementation);

    <T, U extends T> void addComponent(TypeLiteral<T> componentKey, U implementation);

    default void addConfig(String componentKey, String value) {
        addComponent(componentKey, String.class, value);
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
    <T> Class<T> getClass(String className, Class<T> superType);

}
