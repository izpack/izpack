/*
 * IzPack - Copyright 2001-2011 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2011 Tim Anderson
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
package com.izforge.izpack.util;

/**
 * Factory for constructing platform specific implementation implementations of
 * interfaces or classes.
 *
 * @author Tim Anderson
 * @see Platforms
 * @see Platform
 */
public interface TargetPlatformFactory
{

    /**
     * Creates a platform specific implementation of a class, for the current
     * platform.
     *
     * @param clazz the class to create a platform specific instance of
     * @return the instance for the specified platform
     * @throws Exception for any error
     */
    <T> T create(Class<T> clazz) throws Exception;

    /**
     * Creates a platform specific implementation of a class, for the specified
     * platform.
     *
     * @param clazz the class to create a platform specific instance of
     * @param platform the platform
     * @return the instance for the specified platform
     * @throws Exception for any error
     */
    <T> T create(Class<T> clazz, Platform platform) throws Exception;
}
