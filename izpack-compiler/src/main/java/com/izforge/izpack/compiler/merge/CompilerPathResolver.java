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

package com.izforge.izpack.compiler.merge;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.compiler.util.CompilerClassLoader;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.PanelHelper;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;


/**
 * A {@link PathResolver} for the compiler.
 *
 * @author Tim Anderson
 */
public class CompilerPathResolver extends PathResolver
{

    /**
     * The class loader.
     */
    private final CompilerClassLoader loader;

    /**
     * The panel dependencies.
     */
    private final Properties panelDependencies;


    /**
     * Constructs a <tt>CompilerPathResolver</tt>.
     *
     * @param mergeableResolver the mergeable resolver
     * @param loader            the class loader
     * @param panelDependencies panel dependency properties
     */
    public CompilerPathResolver(MergeableResolver mergeableResolver, CompilerClassLoader loader,
                                Properties panelDependencies)
    {
        super(mergeableResolver);
        this.loader = loader;
        this.panelDependencies = panelDependencies;
    }

    /**
     * Creates a new {@link PanelMerge} given the panel class name.
     *
     * @param className the panel class name
     * @return a new {@link PanelMerge}
     */
    public PanelMerge getPanelMerge(String className)
    {
        Class type = loader.loadClass(className, IzPanel.class);
        Map<String, List<Mergeable>> mergeableByPackage = new HashMap<String, List<Mergeable>>();
        List<Mergeable> mergeable = new ArrayList<Mergeable>();
        getMergeableByPackage(type, mergeableByPackage);
        Class consoleType = PanelHelper.getConsolePanel(className);
        if (consoleType != null)
        {
            getMergeableByPackage(consoleType, mergeableByPackage);
        }
        Class automatedType = PanelHelper.getAutomatedPanel(className);
        if (automatedType != null)
        {
            getMergeableByPackage(automatedType, mergeableByPackage);
        }
        for (List<Mergeable> pkg : mergeableByPackage.values())
        {
            mergeable.addAll(pkg);
        }

        return new PanelMerge(type, mergeable);
    }

    /**
     * Returns a list of {@link Mergeable} instances for all resources in the specified package.
     *
     * @param merge the package to merge
     * @return all resources in the specified package
     */
    public List<Mergeable> getMergeablePackage(Package merge)
    {
        List<Mergeable> result = new ArrayList<Mergeable>();
        String destination = merge.getName().replaceAll("\\.", "/");

        Enumeration<URL> urls;
        try
        {
            urls = getClass().getClassLoader().getResources(destination);
        }
        catch (IOException exception)
        {
            throw new CompilerException("Failed to locate resources in package: " + merge.getName(), exception);
        }
        MergeableResolver mergeableResolver = getMergeableResolver();
        while (urls.hasMoreElements())
        {
            URL obtainPackage = urls.nextElement();
            result.add(mergeableResolver.getMergeableFromURLWithDestination(obtainPackage, destination + "/"));
        }
        return result;
    }

    /**
     * Locates the {@link Mergeable} instances for a panel and its dependencies.
     *
     * @param type      the panel class or interface
     * @param mergeable the instances, keyed on package name
     */
    private void getMergeableByPackage(Class type, Map<String, List<Mergeable>> mergeable)
    {
        String pkg = type.getPackage().getName();
        if (!mergeable.containsKey(pkg))
        {
            mergeable.put(pkg, getMergeablePackage(type.getPackage()));
            if (panelDependencies.containsKey(type.getSimpleName()))
            {
                String dependPackage = (String) panelDependencies.get(type.getSimpleName());
                if (!mergeable.containsKey(dependPackage))
                {
                    mergeable.put(dependPackage, getMergeableFromPackageName(dependPackage));
                }
            }
        }
        for (Class iface : type.getInterfaces())
        {
            getMergeableByPackage(iface, mergeable);
        }
        Class superClass = type.getSuperclass();
        if (superClass != null && !superClass.equals(IzPanel.class)
                && !superClass.equals(AbstractConsolePanel.class)
                && !superClass.equals(PanelAutomationHelper.class) && !superClass.equals(Object.class))
        {
            getMergeableByPackage(superClass, mergeable);
        }
    }
}
