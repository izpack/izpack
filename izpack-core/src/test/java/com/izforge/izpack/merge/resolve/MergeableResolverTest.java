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

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.FileUtil;

/**
 * Test for mergeableResolver
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class MergeableResolverTest
{
    private MergeableResolver mergeableResolver;
    private URL resource;

    public MergeableResolverTest(MergeableResolver mergeableResolver)
    {
        this.mergeableResolver = mergeableResolver;
    }

    @Before
    public void before() throws MalformedURLException
    {
        resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        resource = new File(FileUtil.convertUrlToFilePath(resource) + "!jar/izforge").toURI().toURL();
    }

    @Test
    public void testGetMergeableFromURL()
    {
        Mergeable mergeable = mergeableResolver.getMergeableFromURL(resource);
        assertThat(mergeable, MergeMatcher.isMergeableContainingFile("jar/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void testGetMergeableWithSpaces() throws Exception
    {
        resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/test space/vim-panel-1.0-SNAPSHOT.jar");
        resource = new File(FileUtil.convertUrlToFilePath(resource) + "!com").toURI().toURL();
        Mergeable mergeable = mergeableResolver.getMergeableFromURL(resource);
        assertThat(mergeable, MergeMatcher.isMergeableContainingFile("com/sora/panel/VimPanel.class"));
    }

    @Test
    public void testGetMergeableWithPlusSign() throws Exception
    {
        resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/vim-panel-1.0-SNAPSHOT+201804141646.jar");
        resource = new File(FileUtil.convertUrlToFilePath(resource) + "!com").toURI().toURL();
        Mergeable mergeable = mergeableResolver.getMergeableFromURL(resource);
        assertThat(mergeable, MergeMatcher.isMergeableContainingFile("com/sora/panel/VimPanel.class"));
    }

    @Test           
    public void testGetMergeableFromURLWithDestination()
    {
        Mergeable jarMerge = mergeableResolver.getMergeableFromURLWithDestination(resource, "ga");
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("ga/izpack/panels/hello/HelloPanel.class"));
    }

}
