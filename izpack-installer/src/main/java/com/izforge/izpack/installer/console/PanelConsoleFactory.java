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

package com.izforge.izpack.installer.console;

import java.util.logging.Logger;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.factory.ObjectFactory;


/**
 * Factory for {@link PanelConsole} instances.
 *
 * @author Tim Anderson
 */
class PanelConsoleFactory
{
    /**
     * The factory to delegate to.
     */
    private final ObjectFactory factory;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PanelConsoleFactory.class.getName());


    /**
     * Constructs a <tt>PanelConsoleFactory</tt>.
     *
     * @param factory the factory to delegate to
     */
    public PanelConsoleFactory(ObjectFactory factory)
    {
        this.factory = factory;
    }

    /**
     * Attempts to create an {@link PanelConsole} corresponding to the specified panel.
     *
     * @param panel the panel
     * @return the corresponding {@link PanelConsole}
     * @throws InstallerException if there is no {@link PanelConsole} for the panel
     */
    public PanelConsole create(Panel panel) throws InstallerException
    {
        Class<PanelConsole> impl = getClass(panel);
        return factory.create(impl);
    }

    /**
     * Returns the PanelConsole class corresponding to the specified panel.
     *
     * @param panel the panel
     * @return the corresponding {@link PanelConsole} implementation class, or <tt>null</tt> if none is found
     */
    public Class<PanelConsole> getClass(Panel panel)
    {
        Class<PanelConsole> result = getClass(panel.getClassName() + "Console");
        if (result == null)
        {
            // use the old ConsoleHelper suffix convention
            result = getClass(panel.getClassName() + "ConsoleHelper");
        }
        return result;
    }


    /**
     * Returns the {@link PanelConsole} class for the specified class name.
     *
     * @param name the class name
     * @return the corresponding class, or <tt>null</tt> if it cannot be found or does not implement
     *         {@link PanelConsole}.
     */
    @SuppressWarnings("unchecked")
    private Class<PanelConsole> getClass(String name)
    {
        Class<PanelConsole> result = null;
        try
        {
            Class type = Class.forName(name);
            if (!PanelConsole.class.isAssignableFrom(type))
            {
                logger.warning(name + " does not implement " + PanelConsole.class.getName() + ", ignoring");
            }
            else
            {
                result = (Class<PanelConsole>) type;
            }
        }
        catch (ClassNotFoundException e)
        {
            // Ignore
            logger.fine("No PanelConsole found for class " + name + ": " + e.toString());
        }
        return result;
    }

}
