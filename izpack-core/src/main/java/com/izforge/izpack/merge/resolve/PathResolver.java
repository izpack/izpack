/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.merge.resolve;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.merge.Mergeable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Try to resolve paths by searching inside the classpath or files with the corresponding name
 *
 * @author Anthonin Bonnefoy
 */
@ApplicationScoped
public class PathResolver
{
    /**
     * The mergeable resolver.
     */
    private final MergeableResolver mergeableResolver;


    /**
     * Constrcuts a <tt>PathResolver</tt>.
     *
     * @param mergeableResolver the mergeable resolver
     */
    @Inject
    public PathResolver(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
    }

    /**
     * Search for the sourcePath in classpath (inside jar or directory) or as a normal path and then return the type or File.
     * Ignore all path containing test-classes.
     *
     * @param sourcePath Source path to search
     * @return url list
     */
    public Set<URL> resolvePath(String sourcePath)
    {
        Set<URL> result = findResources(sourcePath);
        if (result.isEmpty())
        {
            throw new IzPackException(
                    "The path '" + sourcePath + "' is not present inside the classpath.\n"
                            + "The current classpath is :" + ResolveUtils.getCurrentClasspath());
        }
        return result;
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public List<Mergeable> getMergeableFromPath(String resourcePath)
    {
        Set<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<>();
        for (URL url : urlList)
        {
            result.add(mergeableResolver.getMergeableFromURL(url, resourcePath));
        }
        return result;
    }

    public List<Mergeable> getMergeableFromPackageName(String dependPackage)
    {
        return getMergeableFromPath(dependPackage.replaceAll("\\.", "/") + "/");
    }

    public List<Mergeable> getMergeableJarFromPackageName(String packageName)
    {
        Set<URL> urlSet = ResolveUtils.getJarUrlForPackage(packageName);
        ArrayList<Mergeable> list = new ArrayList<>();
        for (URL url : urlSet)
        {
            list.add(mergeableResolver.getMergeableFromURL(url));
        }
        return list;
    }

    /**
     * Return the mergeable from the given path.
     *
     * @param resourcePath Resource path to search
     * @param destination  The destination of resources when merging will ocure.
     * @return Mergeable list of mergeable. Empty if nothing found.
     */
    public List<Mergeable> getMergeableFromPath(String resourcePath, String destination)
    {
        Set<URL> urlList = resolvePath(resourcePath);
        List<Mergeable> result = new ArrayList<>();
        for (URL url : urlList)
        {
            result.add(mergeableResolver.getMergeableFromURLWithDestination(url, destination));
        }
        return result;
    }

    /**
     * Find all resources for the specified resource path.
     *
     * @param resourcePath the resource path
     * @return urls matching the resource path
     */
    protected Set<URL> findResources(String resourcePath)
    {
        Set<URL> result = new HashSet<>();
        URL path = ResolveUtils.getFileFromPath(resourcePath);
        if (path != null)
        {
            result.add(path);
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            Enumeration<URL> iterator = loader.getResources(resourcePath);
            while (iterator.hasMoreElements())
            {
                URL url = iterator.nextElement();
                result.add(url);
            }
        }
        catch (IOException e)
        {
            throw new IzPackException(e);
        }
        return result;
    }

    /**
     * Returns the mergeable resolver.
     *
     * @return the mergeable resolver
     */
    protected MergeableResolver getMergeableResolver()
    {
        return mergeableResolver;
    }


}
