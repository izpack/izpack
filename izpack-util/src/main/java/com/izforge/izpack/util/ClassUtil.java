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

package com.izforge.izpack.util;

import com.izforge.izpack.api.exception.IzPackClassNotFoundException;

public final class ClassUtil
{
  private ClassUtil() {}

  /**
   * Returns a class given its name.
   *
   * @param className the class name
   * @param superType the super type
   * @return the corresponding class
   * @throws ClassCastException if <tt>className</tt> does not implement or extend
   *         <tt>superType</tt>
   * @throws IzPackClassNotFoundException if the class cannot be found
   */
  public static <T> Class<T> getClass(String className, Class<T> superType)
  {
    try
    {
      // Using the superclass class loader to load the child to avoid multiple copies of the
      // superclass being
      // loaded in separate class loaders. This is typically an issue during testing where
      // the same classes may be loaded twice - once by maven, and once by the installer.
      ClassLoader classLoader = superType.getClassLoader();
      if (classLoader == null)
      {
        // may be null for bootstrap class loader
        classLoader = ClassUtil.class.getClassLoader();
      }
      Class<?> type = classLoader.loadClass(className);
      if (!superType.isAssignableFrom(type))
      {
        throw new ClassCastException("Class '" + type.getName() + "' does not implement " + superType.getName());
      }
      @SuppressWarnings("unchecked")
      Class<T> returnType = (Class<T>)type; 
      return returnType;
    }
    catch (ClassNotFoundException exception)
    {
      throw new IzPackClassNotFoundException(className, exception);
    }
  }
}
