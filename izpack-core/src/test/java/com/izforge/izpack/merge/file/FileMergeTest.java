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

package com.izforge.izpack.merge.file;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileFilter;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.izforge.izpack.matcher.MergeMatcher;

/**
 * Test for fileMerge
 *
 * @author Anthonin Bonnefoy
 */
public class FileMergeTest
{
    private Map<OutputStream, List<String>> mergeContent = new HashMap<OutputStream, List<String>>();

    @Test
    public void testMergeSingleFile() throws Exception
    {
        FileMerge fileMerge = new FileMerge(getClass().getResource("FileMergeTest.class"), mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("FileMergeTest.class"));
    }

    @Test
    public void testMergeDirectory() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/test");
        FileMerge fileMerge = new FileMerge(url, mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("test/.placeholder"));
    }

    @Test
    public void testMergeDirectoryWithDestination() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/test");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/", mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("my/dest/path/.placeholder"));
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("my/dest/path/izpack-panel-5.0.0-SNAPSHOT.jar"));
    }

    @Test
    public void testMergeFileWithDestination() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/file/FileMergeTest.class");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/NewFile.ga", mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("my/dest/path/NewFile.ga"));
    }

    @Test
    public void testMergeFileWithRootDestination() throws Exception
    {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/file/FileMergeTest.class");
        FileMerge fileMerge = new FileMerge(url, "NewFile.ga", mergeContent);
        assertThat(fileMerge, MergeMatcher.isMergeableContainingFile("NewFile.ga"));
    }


    @Test
    public void findFileInDirectory() throws Exception
    {
        FileMerge fileMerge = new FileMerge(ClassLoader.getSystemResource("com/izforge/izpack/merge/test"), mergeContent);
        File file = fileMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.getName().equals(".placeholder") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is(".placeholder"));
    }

}
