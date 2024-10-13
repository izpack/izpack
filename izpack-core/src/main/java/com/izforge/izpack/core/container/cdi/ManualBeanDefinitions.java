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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.izforge.izpack.core.provider.LocalesProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedMethod;

final class ManualBeanDefinitions implements Extension {
  private static final Logger LOGGER = Logger.getLogger(CdiInitializationContextImpl.class.getName());

  private final List<DynamicBean<?>> additionalBeans;
  private final Set<Class<?>> vetoedBeans;

  ManualBeanDefinitions() {
    additionalBeans = new ArrayList<>();
    vetoedBeans = new HashSet<>();
  }

  void registerBean(DynamicBean<?> implementationBean) {
    additionalBeans.add(implementationBean);
  }

  void addVeto(Class<?> vetoedType) {
    vetoedBeans.add(vetoedType);
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
    additionalBeans.forEach(event::addBean);
  }


  public void annotatedType(@Observes @WithAnnotations({ApplicationScoped.class, Dependent.class}) ProcessAnnotatedType<?> type) {
    Class<?> javaClass = type.getAnnotatedType().getJavaClass();
    if (vetoedBeans.contains(javaClass) || additionalBeans.stream().anyMatch(ab -> ab.getTypes().stream().anyMatch(p -> containsType(javaClass, p)))) {
      LOGGER.info(() -> "***** Vetoing "  + type);
      type.veto();
    }
  }

  boolean containsType(Class<?> javaClass, Type type) {
    if (!javaClass.isInterface() &&  type instanceof Class) {
      Class<?> typeClass = (Class<?>)type;
      return javaClass.equals(typeClass);
    }
    return false;
  }
}
