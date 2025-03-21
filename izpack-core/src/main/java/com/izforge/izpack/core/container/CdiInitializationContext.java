/*
 * IzPack - Copyright 2001-20212 Julien Ponge, All Rights Reserved.
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

import java.lang.annotation.Annotation;

import com.izforge.izpack.api.exception.IzPackException;

/**
 * Defines collects all CDI relevant bean informations for the izPack environment, where we do
 * not enable automatic bean discovery.
 *
 * @author Patrick Reinhart
 */
public interface CdiInitializationContext {

  <T> void addComponent(Class<T> componentType);

  <T, I extends T> void addComponent(Class<T> componentType, I implementation, Annotation...annotations);

  void addConfig(String name, Object value) throws IzPackException;

  <T> void removeComponent(Class<T> componentType);

  void start();

  void close();
}
