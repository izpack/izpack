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

package com.izforge.izpack.api.container;

import com.izforge.izpack.api.data.InstallDataHandler;
import com.izforge.izpack.api.exception.ContainerException;


/**
 * Component container.
 *
 * @author Anthonin Bonnefoy
 * @author Tim Anderson
 * @author Patrick Reinhart
 */
public interface Container extends InstallDataHandler
{
    /**
     * Retrieve a component by its component type.
     * <p/>
     * If the component type is registered but an instance does not exist, then it will be created.
     *
     * @param componentType the type of the component
     * @return the corresponding object instance, or <tt>null</tt> if it does not exist
     * @throws ContainerException if component creation fails
     * 
     * @deprecated use {@code CDI.current()} if needed
     */
    @Deprecated
    <T> T getComponent(Class<T> componentType);

    /**
     * Disposes of the container and all of its child containers.
     */
    void dispose();

    void addPanel(String id, Object panel);

    Object getPanel(String id);
}
