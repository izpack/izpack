/*
 * IzPack - Copyright 2001-2013 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Tim Anderson
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
package com.izforge.izpack.installer.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.gui.IzPanel;


/**
 * Helper routines for panels.
 *
 * @author Tim Anderson
 */
public class PanelHelper
{

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(PanelHelper.class.getName());


    /**
     * Returns the console implementation of an {@link IzPanel}.
     * <p/>
     * Console implementations must use the naming convention:
     * <p>
     * {@code <prefix>ConsolePanel}
     * </p>
     * where <em>{@code <prefix>}</em> is the IzPanel name, minus <em>Panel</em>.
     * <br/>
     * E.g for the panel {@code HelloPanel}, the console implementation must be named {@code HelloConsolePanel}.
     * <p/>
     * For backwards-compatibility, the sufixes <em>Console</em> and <em>ConsoleHelper</em> are also supported.
     * Support for this will be removed when the {@link com.izforge.izpack.installer.console.PanelConsole} interface is
     * removed.
     *
     * @param className the IzPanel class name
     * @return the corresponding console implementation, or {@code null} if none is found
     */
    public static Class<ConsolePanel> getConsolePanel(String className)
    {
        Class<ConsolePanel> result = getClass(className.replaceAll("Panel$", "ConsolePanel"), ConsolePanel.class);
        if (result == null)
        {
            result = getPanelClass(ConsolePanel.class, className, "Console", "ConsoleHelper");
        }
        return result;
    }

    /**
     * Returns the automated implementation of an {@link IzPanel}.
     *
     * @param className the IzPanel class name
     * @return the corresponding automated implementation, or {@code null} if none is found
     */
    public static Class<PanelAutomation> getAutomatedPanel(String className)
    {
        return getPanelClass(PanelAutomation.class, className, "Automation", "AutomationHelper");
    }

    /**
     * Returns an alternate implementation of an {@link IzPanel} given its name, and possible suffixes.
     *
     * @param superType the super-type of the alternate implementation
     * @param className the IzPanel class name
     * @param suffixes  the possible suffixes
     * @return the corresponding implementation, or {@code null} if none is found
     */
    private static <T> Class<T> getPanelClass(Class<T> superType, String className, String... suffixes)
    {
        Class<T> result = null;
        for (String suffix : suffixes)
        {
            result = getClass(className + suffix, superType);
            if (result != null)
            {
                break;
            }
        }
        return result;
    }

    /**
     * Returns a class for the specified class name.
     *
     * @param name the class name
     * @return the corresponding class, or {@code null} if it cannot be found or does not implement the super-type.
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getClass(String name, Class<T> superType)
    {
        Class<T> result = null;
        try
        {
            Class type = Class.forName(name);
            if (!superType.isAssignableFrom(type))
            {
                logger.warning(name + " does not implement " + superType.getName() + ", ignoring");
            }
            else
            {
                result = (Class<T>) type;
            }
        }
        catch (Throwable exception)
        {
            // ignore
            logger.log(Level.FINE, "No " + superType.getSimpleName() + " + found for class " + name + ": "
                    + exception.toString(), exception);
        }
        return result;
    }


}
