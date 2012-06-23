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

import org.picocontainer.MutablePicoContainer;

import com.izforge.izpack.installer.container.impl.GUIInstallerContainer;
import com.izforge.izpack.test.util.TestHousekeeper;
import com.izforge.izpack.util.Housekeeper;


/**
 * Test installer container for GUI based installers.
 * <p/>
 * This returns a {@link TestHousekeeper} instead of an {@link Housekeeper}.
 *
 * @author Tim Anderson
 */
public class TestGUIInstallerContainer extends GUIInstallerContainer
{

    /**
     * Default constructor.
     */
    public TestGUIInstallerContainer()
    {
        super();
    }

    /**
     * Constructs a <tt>TestGUIInstallerContainer</tt>.
     *
     * @param container the container to use
     */
    public TestGUIInstallerContainer(MutablePicoContainer container)
    {
        super(container);
    }

    /**
     * Registers components with the container.
     *
     * @param pico the container
     */
    @Override
    protected void registerComponents(MutablePicoContainer pico)
    {
        super.registerComponents(pico);
        super.getContainer().removeComponent(Housekeeper.class);
        addComponent(TestHousekeeper.class);
    }
}
