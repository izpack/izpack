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

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.file.FileMerge;
import com.izforge.izpack.merge.jar.JarMerge;

/**
 * Helper methods for mergeable
 *
 * @author Anthonin Bonnefoy
 */
public class MergeableResolver
{
    private Map<OutputStream, List<String>> mergeContent = new HashMap<OutputStream, List<String>>();

    public MergeableResolver()
    {
    }

    public Mergeable getMergeableFromURL(URL url)
    {
        if (!ResolveUtils.isJar(url))
        {
            return new FileMerge(url, mergeContent);
        }
        return new JarMerge(url, ResolveUtils.processUrlToJarPath(url), mergeContent);
    }

    public Mergeable getMergeableFromURL(URL url, String resourcePath)
    {
        if (ResolveUtils.isJar(url))
        {            
            return new JarMerge(url, ResolveUtils.processUrlToJarPath(url), mergeContent);
        }
        else
        {
            return new FileMerge(url, resourcePath, mergeContent);
        }
    }

    public Mergeable getMergeableFromURLWithDestination(URL url, String destination)
    {
        if (ResolveUtils.isJar(url))
        {
            if (ResolveUtils.isFileInJar(url))
            {
                return new JarMerge(ResolveUtils.processUrlToJarPath(url), ResolveUtils.processUrlToInsidePath(url), destination, mergeContent);
            }
            return new JarMerge(ResolveUtils.processUrlToJarPath(url), ResolveUtils.processUrlToJarPackage(url), destination, mergeContent);
        }
        else
        {
            return new FileMerge(url, destination, mergeContent);
        }
    }
}
