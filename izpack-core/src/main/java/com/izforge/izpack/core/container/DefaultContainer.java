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

package com.izforge.izpack.core.container;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.ContainerException;
import com.izforge.izpack.api.exception.ResourceException;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.core.factory.InstallDataFactory;
import com.izforge.izpack.util.Platform;

import jakarta.enterprise.inject.Vetoed;

import java.util.function.Predicate;

/**
 * Default implementation of the {@link Container} interface.
 *
 * @author Tim Anderson
 */
@Vetoed
public class DefaultContainer extends AbstractContainer
{
    private final Class<?> classUnderTest;

    /**
     * Constructs a <tt>DefaultContainer</tt>.
     *
     * @throws ContainerException if initialization fails
     */
    public DefaultContainer()
    {
        this(null);
    }

    public DefaultContainer(Class<?> classUnderTest)
    {
        this.classUnderTest = classUnderTest;
        initialise();
    }

    @Override
    protected void fillContainer(CdiInitializationContext context)
    {
        super.fillContainer(context);
        if (classUnderTest != null) {
            context.addComponent(classUnderTest);
        }
    }

    @Override
    public InstallData create(Resources resources, Variables variables, Platform platform, Locales locales,
                              Predicate<Pack> availablePackPredicate) throws ResourceException {
        return InstallDataFactory.create(resources, variables, platform, locales, availablePackPredicate, AutomatedInstallData::new);
    }
}
