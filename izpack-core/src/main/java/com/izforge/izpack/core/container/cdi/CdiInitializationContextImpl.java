/*
 * IzPack - Copyright 2001-20212 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2010 Anthonin Bonnefoy Copyright 2012 Tim Anderson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.core.container.cdi;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.core.container.CdiInitializationContext;

import jakarta.enterprise.inject.Decorated;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

/**
 * Collects all bean definitions an use those for later creation of a bean
 * 
 * @author Patrick Reinhart
 */
public final class CdiInitializationContextImpl implements CdiInitializationContext {
  private static final Logger logger = Logger.getLogger(CdiInitializationContextImpl.class.getName());

  private final AtomicReference<State> init = new AtomicReference<>(State.NEW);
  private final Map<Class<?>, BeanImplementation> components;
  private final Set<Class<?>> vetoed;
  private final Map<String, Object> configurations;

  private SeContainer seContainer;

  public CdiInitializationContextImpl() {
    components = new HashMap<>();
    configurations = new HashMap<>();
    vetoed = new HashSet<>();
  }

  private void checkNew() {
    if (!State.NEW.equals(init.get())) {
      throw new IllegalStateException("Unable to modify components if not in new state");
    }
  }


  @Override
  public <T> void addComponent(Class<T> type) {
    addComponent(type, null);
  }

  @Override
  public <T, I extends T> void addComponent(Class<T> type, I implementation,
      Annotation... annotations) {
    checkNew();
    BeanImplementation beanImplementation = new BeanImplementation(type, implementation, annotations);
    DynamicBean.collectTypes(type, key -> registerImplementation(key, beanImplementation));
    logger.info(() -> "registered " + beanImplementation);
  }

  private void registerImplementation(Class<?> type, BeanImplementation beanImplementation) {
    if (Object.class.equals(type)) {
      return;
    }
    BeanImplementation existing = components.putIfAbsent(type, beanImplementation);
    if (existing != null && !existing.equals(beanImplementation)) {
      throw new IllegalStateException("Unable to register " + beanImplementation + " for type [" + type +
              "]. Already registered: " + existing);
    }
  }

  @Override
  public <T> void removeComponent(Class<T> type) {
    checkNew();
    if (components.remove(type) == null) {
      vetoed.add(type);
    }
  }

  @Override
  public void addConfig(String name, Object value) throws IzPackException {
    checkNew();
    configurations.put(name, value);
  }

  @Override
  public void start() {
    if (init.compareAndSet(State.NEW, State.INITIALIZING)) {
      SeContainerInitializer initializer = SeContainerInitializer.newInstance();
      ManualBeanDefinitions beanDefinitions = new ManualBeanDefinitions();
      initializer.addExtensions(beanDefinitions);
      components.values().stream().distinct()
          .forEach(implementation -> implementation.register(initializer, beanDefinitions::registerBean));
      vetoed.forEach(beanDefinitions::addVeto);
      seContainer = initializer.initialize();
      init.set(State.RUNNING);
    }
  }

  @Override
  public void close() {
    if (init.compareAndSet(State.RUNNING, State.STOPPING)) {
      seContainer.close();
      seContainer = null;
      components.clear();
      configurations.clear();
      vetoed.clear();
      init.set(State.STOPPED);
    }
  }

  private enum State {
    NEW, INITIALIZING, RUNNING, STOPPING, STOPPED;
  }

  private static final class BeanImplementation {
    private final Class<?> type;
    private final Object implementation;
    private final Annotation[] annotations;

    BeanImplementation(Class<?> type, Object implementation, Annotation[] annotations) {
      this.type = type;
      this.implementation = implementation;
      this.annotations = annotations;
    }

    void register(SeContainerInitializer initializer, 
        Consumer<DynamicBean<?>> implementationConsumer) {
      if (type.isAnnotationPresent(Decorated.class)) {
        initializer.enableDecorators(type);
      }
      if (implementation == null) {
        initializer.addBeanClasses(type);
      } else {
        implementationConsumer.accept(dynamicBean(type, implementation, annotations));
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(type.getName());
      if (implementation != null) {
        sb.append('[').append(implementation.getClass().getName()).append(']');
      }
      return sb.toString();
    }

    static <T> DynamicBean<T> dynamicBean(Class<T> type, Object instance,
        Annotation[] annotations) {
      DynamicBean<T> bean = new DynamicBean<>(type, type.cast(instance));
      for (Annotation annotation : annotations) {
        bean.addQualifier(annotation);
      }
      return bean;
    }
  }
}
