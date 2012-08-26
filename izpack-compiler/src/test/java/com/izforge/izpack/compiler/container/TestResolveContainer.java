/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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

package com.izforge.izpack.compiler.container;

import java.util.Properties;

import org.picocontainer.PicoException;

import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.compiler.util.CompilerClassLoader;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.util.DefaultClassNameMapper;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.merge.resolve.MergeableResolver;

/**
 * Container for com.izforge.izpack.resolve package tests.
 *
 * @author Tim Anderson
 */
public class TestResolveContainer extends AbstractContainer
{

    /**
     * Constructs a <tt>TestResolveContainer</tt>.
     *
     * @throws ContainerException if initialisation fails
     */
    public TestResolveContainer()
    {
        initialise();
    }

    /**
     * Invoked by {@link #initialise} to fill the container.
     *
     * @throws ContainerException if initialisation fails
     * @throws PicoException      for any PicoContainer error
     */
    @Override
    protected void fillContainer()
    {
        addComponent(Properties.class);
        addComponent(CompilerPathResolver.class);
        addComponent(CompilerClassLoader.class);
        addComponent(DefaultClassNameMapper.class);
        addComponent(MergeableResolver.class);

        Properties properties = getComponent(Properties.class);
        properties.put("HelloPanelTestWithDependenciesClass", "com.izforge.izpack.panels.depend");
    }

}
