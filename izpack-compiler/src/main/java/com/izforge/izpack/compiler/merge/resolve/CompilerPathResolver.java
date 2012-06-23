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

package com.izforge.izpack.compiler.merge.resolve;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.compiler.merge.panel.PanelMerge;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;


/**
 * A {@link PathResolver} for the compiler.
 * <br/>
 * This uses a {@link ClassPathCrawler} to resolve paths if they cannot be found.
 *
 * @author Tim Anderson
 */
public class CompilerPathResolver extends PathResolver
{

    /**
     * The class path crawler.
     */
    private final ClassPathCrawler classPathCrawler;

    /**
     * The panel dependencies.
     */
    private final Properties panelDependencies;


    /**
     * Constructs a <tt>CompilerPathResolver</tt>.
     *
     * @param mergeableResolver the mergeable resolver
     * @param classPathCrawler  the classpath crawler
     * @param panelDependencies panel dependency properties
     */
    public CompilerPathResolver(MergeableResolver mergeableResolver, ClassPathCrawler classPathCrawler,
                                Properties panelDependencies)
    {
        super(mergeableResolver);
        this.classPathCrawler = classPathCrawler;
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
        Class type = classPathCrawler.findClass(className);
        List<Mergeable> mergeable = getMergeablePackage(type.getPackage());
        if (panelDependencies.containsKey(type.getSimpleName()))
        {
            String dependPackage = (String) panelDependencies.get(type.getSimpleName());
            mergeable.addAll(getMergeableFromPackageName(dependPackage));
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
        Set<URL> obtainPackages = classPathCrawler.searchPackageInClassPath(merge.getName());
        MergeableResolver mergeableResolver = getMergeableResolver();
        for (URL obtainPackage : obtainPackages)
        {
            result.add(mergeableResolver.getMergeableFromURLWithDestination(obtainPackage, destination + "/"));
        }
        return result;
    }


    /**
     * Find all resources for the specified resource path.
     *
     * @param resourcePath the resource path
     * @return urls matching the resource path
     */
    @Override
    protected Set<URL> findResources(String resourcePath)
    {
        Set<URL> result = super.findResources(resourcePath);
        if (result.isEmpty())
        {
            result = classPathCrawler.searchPackageInClassPath(resourcePath);
        }
        return result;
    }

}
