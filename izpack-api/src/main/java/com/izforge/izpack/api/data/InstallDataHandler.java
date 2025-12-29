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

package com.izforge.izpack.api.data;

import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.Platform;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * The implementer is responsible to create a {@link InstallData} instance.
 * 
 * @author Patrick Reinhart
 */
public interface InstallDataHandler {

  /**
   * Returns a new {@link InstallData} instance.
   * 
   * @param resources the resource manager to obtain optional resources
   * @param variables the supported variables
   * @param platform the actual platform
   * @param locales the locale information
   * @param availablePackPredicate the predicate to select the available {@link Pack}s
   * @return a newly created automated installation data instance
   * @throws ResourceException if resource access problems occur
   */
  InstallData create(Resources resources, Variables variables, Platform platform, Locales locales,
                     Predicate<Pack> availablePackPredicate) throws ResourceException;

}
