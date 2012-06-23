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

package com.izforge.izpack.merge;

import com.izforge.izpack.api.merge.Mergeable;

/**
 * Manager for all the merging logic.
 *
 * @author Anthonin Bonnefoy
 */
public interface MergeManager extends Mergeable
{

    /**
     * Add the given resource to merge in the produced installer. <br />
     * You can provide a resource, a className or a package to merge, it will be resolved by searching inside the classpath.<br />
     * By default, the destination path is the same as the source. If you give a package com/my/package, it will be added in com/my/package in the installer jar.
     *
     * @param resourcePath Resource path to merge in the installer. It should be of the form "com/izforge/izpack/"
     */
    void addResourceToMerge(String resourcePath);

    /**
     * Add the given resource to merge in the produced installer. <br />
     * You can provide a resource, a className or a package to merge, it will be resolved by searching inside the classpath.<br />
     * You can define the destination of the resource inside the produced installer jar.
     *
     * @param resourcePath Resource path to merge in the installer. It should be of the form "com/izforge/izpack/"
     * @param destination  Destination of the resource inside the jar.
     */
    void addResourceToMerge(String resourcePath, String destination);

    void addResourceToMerge(Mergeable mergeable);
}
