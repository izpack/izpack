/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005 Klaus Bartz
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

package com.izforge.izpack.util.os;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Target(TYPE)
@Retention(RUNTIME)
public @interface OsName {
  String value();

  /**
   * Supports inline instantiation of the {@link OsName} annotation.
   */
  public final static class Literal extends AnnotationLiteral<OsName> implements OsName {
      public static final Literal WINDOWS = new Literal("windows");
      public static final Literal UNIX = new Literal("unix");

      private static final long serialVersionUID = 1L;

      private final String osName;

      private Literal(String osName) {
        this.osName = osName;
      }

      @Override
      public String value() {
         return osName;
      }
  }
}
