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

import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class MergeManagerTest
{
    private MergeManagerImpl mergeManager;

    public MergeManagerTest(MergeManagerImpl mergeManager)
    {
        this.mergeManager = mergeManager;
    }

    @Test
    public void testProcessJarPath() throws Exception
    {
        URL resource = new URL("file:/home/test/unjar.jar!com/package/in/jar");
        String jarPath = ResolveUtils.processUrlToJarPath(resource);
        System.out.println(jarPath);
        assertThat(jarPath, Is.is("/home/test/unjar.jar"));
    }

    @Test
    public void testAddDirectoryWithFile() throws Exception
    {
        mergeManager.addResourceToMerge("com/izforge/izpack/core/rules/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles(
                "com/izforge/izpack/core/rules/builtin_conditions.xml",
                "com/izforge/izpack/core/rules/conditions.xml"));
    }

    @Test
    public void testAddResourceToMerge() throws Exception
    {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");
        assertThat(mergeManager,
                   MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/merge/MergeManager.class"));
    }

    @Test
    public void testAddResourceToMergeWithDestination() throws Exception
    {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/", "com/dest/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/MergeManager.class"));
    }

    @Test
    public void testAddSingleClassToMergeWithDestinationFromAJar() throws Exception
    {
        mergeManager.addResourceToMerge("org/junit/", "com/dest/");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/Assert.class"));
    }

    @Test
    public void testAddPackageToMergeWithDestinationFromAJar() throws Exception
    {
        mergeManager.addResourceToMerge("org/junit", "com/dest");
        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles("com/dest/Assert.class"));
    }


}
