/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.core.container;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.InstallDataHandler;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.container.cdi.CdiInitializationContextImpl;
import com.izforge.izpack.core.factory.InstallDataFactory;
import com.izforge.izpack.util.Platform;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Qualifier;


/**
 * Abstract implementation of the {@link Container} interface.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 */
public abstract class AbstractContainer implements Container {
  private final Map<String, Object> panels;
  private final AtomicReference<State> init = new AtomicReference<>(State.NEW);

  /**
   * The underlying container.
   */
  private CdiInitializationContext container;


  /**
   * Constructs an <tt>AbstractContainer</tt>.
   * <p/>
   * The container must be initialised via {@link #initialise()} before use.
   */
  public AbstractContainer() {
    this(null);
  }

  /**
   * Constructs an <tt>AbstractContainer</tt>.
   * <p/>
   * If a container is provided, {@link #initialise(CdiInitializationContext, Consumer)} will be invoked.
   * Subclasses should only provide a container if they don't require their constructor to complete
   * before <tt>initialise</tt> is called.
   *
   * @param container the underlying container. May be <tt>null</tt>
   * @throws ContainerException if initialisation fails
   */
  public AbstractContainer(CdiInitializationContext container) {
    panels = new HashMap<>();
    if (container != null) {
      initialise(container, this::fillContainer);
    }
  }

  /**
   * Register a component.
   *
   * @param componentType the component type
   * @param implementation the component implementation
   * @param annotations optional qualifier annotations
   * @throws ContainerException if registration fails
   */
  @Deprecated
  public <T, I extends T> void addComponent(Class<T> componentType, I implementation,
      Annotation... annotations) {
    container.addComponent(componentType, implementation, annotations);
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
          Annotation[] qualifiers = Stream.of(componentType.getAnnotations())
              .filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class))
              .toArray(Annotation[]::new);
          return CDI.current().select(componentType, qualifiers).get();

  }

  /**
     * Disposes of the container and all of its child containers.
     */
    @Override
    public final void dispose()
    {
        container.close();
    }

  @Override
  public final void addPanel(String id, Object panel) {
    Object existingPanel = panels.putIfAbsent(id, panel);
    if (existingPanel != null) {
      throw new IllegalArgumentException(
          "Panel with id [" + id + " already exist: " + existingPanel);
    }
  }

  @Override
  public final Object getPanel(String id) {
    return panels.get(id);
  }

  @Override
  public InstallData create(Resources resources, Variables variables, Platform platform,
                            Locales locales, Predicate<Pack> availablePackPredicate) throws ResourceException {
    return InstallDataFactory.create(resources, variables, platform, locales, availablePackPredicate, AutomatedInstallData::new);
  }

  /**
   * Initialises the container.
   * <p/>
   * This must only be invoked once.
   *
   * @throws ContainerException if initialisation fails, or the container has already been
   *         initialised
   */
  protected final void initialise() {
    initialise(null);
  }

  /**
   * Initialises the container.
   * <p/>
   * This must only be invoked once.
   *
   * @param action the consumer action registering additional components
   * @throws ContainerException if initialisation fails, or the container has already been
   *         initialised
   */
  protected final void initialise(Consumer<CdiInitializationContext> action) {
    initialise(createContainer(), action);
  }

  /**
   * Initialises the container.
   * <p/>
   * This must only be invoked once.
   *
   * @param context the CDI initialization context
   * @param action the consumer action registering additional components
   * @throws ContainerException if initialisation fails, or the container has already been
   *         initialised
   */
  protected final void initialise(CdiInitializationContext context, Consumer<CdiInitializationContext> action) {
    Objects.requireNonNull(context, "context must not be null");
    if (!init.compareAndSet(State.NEW, State.INITIALIZING)) {
      throw new ContainerException("Container already initialised");
    }
    this.container = context;
    try {
      // default components
      context.addComponent(Container.class, this);
      context.addComponent(InstallDataHandler.class, this);
      // components of subclasses
      fillContainer(context);
      // optional components if needed
      if (action != null) {
        action.accept(context);
      }
      context.start();
    } catch (ContainerException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new ContainerException(exception);
    }
    init.set(State.INITIALIZED);
  }

  /**
   * Invoked by {@link #initialise} to fill the container.
   * <p/>
   * This exposes the underlying <tt>PicoContainer</tt> to enable subclasses to perform complex
   * initialisation.
   * <p/>
   *
   * @param context the CDI initialization context
   * @throws ContainerException if initialisation fails
   */
  protected void fillContainer(CdiInitializationContext context) {
  }

  /**
   * Creates a new container.
   *
   * @return a new container
   */
  protected final CdiInitializationContext createContainer() {
    return new CdiInitializationContextImpl();
  }

  private enum State {
    NEW, INITIALIZING, INITIALIZED
  }
}
