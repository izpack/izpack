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

import java.io.File;
import java.net.URL;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.DuplicateMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.MergeUtils;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for merge duplication
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class MergeDuplicationTest
{
    private PathResolver pathResolver;
    private MergeableResolver mergeableResolver;

    public MergeDuplicationTest(PathResolver pathResolver, MergeableResolver mergeableResolver)
    {
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
    }

    @Test
    public void testAddJarDuplicated() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(resource);
        File tempFile = MergeUtils.doDoubleMerge(jarMerge);
        ZipFile tempZipFile = new ZipFile(tempFile);
        assertThat(tempZipFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("jar/izforge/izpack/panels/hello/HelloPanelConsoleHelper.class")
        ));
    }

    @Test
    public void testMergeDuplicateFile() throws Exception
    {
        Mergeable mergeable = mergeableResolver.getMergeableFromURL(getClass().getResource("MergeDuplicationTest.class"), "destFile");
        File tempFile = MergeUtils.doDoubleMerge(mergeable);
        ZipFile tempZipFile = new ZipFile(tempFile);
        assertThat(tempZipFile, ZipMatcher.isZipMatching(
                DuplicateMatcher.isEntryUnique("destFile")
        ));
    }

}
