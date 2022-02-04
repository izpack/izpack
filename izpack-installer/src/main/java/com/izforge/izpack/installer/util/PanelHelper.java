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

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.gui.IzPanel;

import java.util.function.Predicate;
import java.util.logging.Logger;


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
     * Returns the IzPanel implementation of an {@link ConsolePanel}.
     * <p/>
     * Console implementations must use the naming convention:
     * <p>
     * {@code <prefix>ConsolePanel}
     * </p>
     * where <em>{@code <prefix>}</em> is the IzPanel name, minus <em>Panel</em>.
     * <br/>
     * E.g for the panel {@code HelloPanel}, the console implementation must be named {@code HelloConsolePanel}.
     *
     * @param className the ConsolePanel class name
     * @return the corresponding IzPanel implementation, or {@code null} if none is found
     */
    public static Class<IzPanel> getIzPanel(String className)
    {
        return getIzPanel(className, PanelHelper.class.getClassLoader());
    }

    /**
     * Returns the IzPanel implementation of an {@link ConsolePanel}.
     * <p/>
     * Console implementations must use the naming convention:
     * <p>
     * {@code <prefix>ConsolePanel}
     * </p>
     * where <em>{@code <prefix>}</em> is the IzPanel name, minus <em>Panel</em>.
     * <br/>
     * E.g for the panel {@code HelloPanel}, the console implementation must be named {@code HelloConsolePanel}.
     *
     * @param className the ConsolePanel class name
     * @param loader    the class loader to use
     * @return the corresponding IzPanel implementation, or {@code null} if none is found
     */
    public static Class<IzPanel> getIzPanel(String className, ClassLoader loader)
    {
        Class<IzPanel> result = getClass(className.replaceAll("ConsolePanel$", "Panel"), IzPanel.class, loader);
        if (result == null)
        {
            result = getClass(className.replaceAll("ConsoleHelper$", ""), IzPanel.class, loader);
            if (result == null)
            {
                result = getClass(className.replaceFirst("Console", ""), IzPanel.class, loader);
            }
        }
        return result;
    }

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
     *
     * @param className the IzPanel class name
     * @return the corresponding console implementation, or {@code null} if none is found
     */
    public static Class<ConsolePanel> getConsolePanel(String className)
    {
        return getConsolePanel(className, PanelHelper.class.getClassLoader());
    }

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
     *
     * @param className the IzPanel class name
     * @param loader    the class loader to use
     * @return the corresponding console implementation, or {@code null} if none is found
     */
    public static Class<ConsolePanel> getConsolePanel(String className, ClassLoader loader)
    {
        Class<ConsolePanel> result = getClass(className.replaceAll("Panel$", "ConsolePanel"), ConsolePanel.class,
                                              loader);
        if (result == null)
        {
            result = getPanelClass(ConsolePanel.class, className, loader, "Console", "ConsoleHelper");
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
        return getAutomatedPanel(className, PanelAutomation.class.getClassLoader());
    }

    /**
     * Returns the automated implementation of an {@link IzPanel}.
     *
     * @param className the IzPanel class name
     * @param loader    the class loader to use
     * @return the corresponding automated implementation, or {@code null} if none is found
     */
    public static Class<PanelAutomation> getAutomatedPanel(String className, ClassLoader loader)
    {
        return getPanelClass(PanelAutomation.class, className, loader, "Automation", "AutomationHelper");
    }

    /**
     * Returns an alternate implementation of an {@link IzPanel} given its name, and possible suffixes.
     *
     * @param superType the super-type of the alternate implementation
     * @param className the IzPanel class name
     * @param loader    the class loader
     * @param suffixes  the possible suffixes
     * @return the corresponding implementation, or {@code null} if none is found
     */
    private static <T> Class<T> getPanelClass(Class<T> superType, String className, ClassLoader loader,
                                              String... suffixes)
    {
        Class<T> result = null;
        for (String suffix : suffixes)
        {
            result = getClass(className + suffix, superType, loader);
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
     * @param name   the class name
     * @param loader the class loader to use to load the class
     * @return the corresponding class, or {@code null} if it cannot be found or does not implement the super-type.
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getClass(String name, Class<T> superType, ClassLoader loader)
    {
        Class<T> result = null;
        try
        {
            Class type = loader.loadClass(name);
            if (!superType.isAssignableFrom(type))
            {
                logger.fine(name + " does not implement " + superType.getName() + ", ignoring");
            }
            else
            {
                result = (Class<T>) type;
            }
        }
        catch (ClassNotFoundException ignored) {}
        return result;
    }

    public static String getPanelTitleMessageKey(Panel panel, String defaultSuffix, InstallData installData)
    {
        return getPanelSpecificKey(panel, defaultSuffix,
                key -> installData.getMessages().getMessages().containsKey(key));
    }

    public static String getPanelResourceName(Panel panel, String defaultSuffix, Resources resources)
    {
        return getPanelSpecificKey(panel, defaultSuffix,
                key -> resources.getString(key, null) != null);
    }

    private static String getPanelSpecificKey(Panel panel, String defaultSuffix, Predicate<String> predicate)
    {
        String className = panel.getClassName();
        String panelName = className.substring(className.lastIndexOf('.') + 1);
        panelName = panelName.replaceAll("Console", "");
        String panelId = panel.getPanelId();
        if (panelId != null)
        {
            String key = panelName + "." + panelId;
            if (predicate.test(key))
            {
                return key;
            }
        }
        return panelName + "." + defaultSuffix;
    }
}
