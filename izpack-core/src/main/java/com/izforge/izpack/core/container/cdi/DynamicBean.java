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

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;

final class DynamicBean<T> implements Bean<T>, PassivationCapable {
  private final Class<T> type;
  private final T instance;
  private final Set<Type> types;
  private final Set<Annotation> qualifiers;

  DynamicBean(Class<T> type, T instance) {
    this.type = type;
    this.instance = instance;
    qualifiers = new HashSet<>(Arrays.asList(Default.Literal.INSTANCE, Any.Literal.INSTANCE));
    types = collectTypes(type);
  }

  static Set<Type> collectTypes(Class<?> type) {
    Set<Type> result = new HashSet<>();
    collectTypes(type, result::add);
    return result;
  }

  static void collectTypes(Class<?> type, Consumer<Class<?>> action) {
    if (type.isInterface()) {
      collectInterfaceTypes(type, action);
    } else {
      action.accept(type);
      if (Object.class.equals(type)) {
        return;
      }
      for (Class<?> interfaceClass : type.getInterfaces()) {
        collectInterfaceTypes(interfaceClass, action);
      }
      collectTypes(type.getSuperclass(), action);
    }
  }

  static void collectInterfaceTypes(Class<?> type, Consumer<Class<?>> action) {
    if (type == null) {
      return;
    }
    action.accept(type);
    collectInterfaceTypes(type.getSuperclass(), action);
  }

  public DynamicBean<T> addQualifier(Annotation annotation) {
    qualifiers.add(annotation);
    return this;
  }

  @Override
  public final Set<Type> getTypes() {
    return unmodifiableSet(types);
  }

  @Override
  public final T create(CreationalContext<T> creationalContext) {
    return instance;
  }

  @Override
  public final void destroy(T instance, CreationalContext<T> creationalContext) {
    creationalContext.release();
  }

  @Override
  public final Set<Annotation> getQualifiers() {
    return unmodifiableSet(qualifiers);
  }

  @Override
  public final Class<? extends Annotation> getScope() {
    return ApplicationScoped.class;
  }

  @Override
  public final Set<Class<? extends Annotation>> getStereotypes() {
    return emptySet();
  }

  @Override
  public final Set<InjectionPoint> getInjectionPoints() {
    return emptySet();
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Class<?> getBeanClass() {
    return type;
  }

  @Override
  public boolean isAlternative() {
    return false;
  }

  @Override
  public String getId() {
    return type.getName();
  }

  @Override
  public String toString() {
    return String.format("DynamicBean(%s)", instance);
  }
}
