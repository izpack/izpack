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

import static com.izforge.izpack.matcher.MergeMatcher.getEntryNames;
import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * Test for fileMerge
 *
 * @author Anthonin Bonnefoy
 */
public class FileMergeTest
{
    @Test
    public void testMergeSingleFile()
    {
        FileMerge fileMerge = new FileMerge(getClass().getResource("FileMergeTest.class"));
        assertThat(getEntryNames(fileMerge)).contains("FileMergeTest.class");
    }

    @Test
    public void testMergeDirectory()
    {
        URL url = getSystemResource("com/izforge/izpack/merge/test");
        FileMerge fileMerge = new FileMerge(url);
        assertThat(getEntryNames(fileMerge)).contains("test/.placeholder");
    }

    @Test
    public void testMergeDirectoryWithDestination()
    {
        URL url = getSystemResource("com/izforge/izpack/merge/test");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/");
        assertThat(getEntryNames(fileMerge)).contains("my/dest/path/.placeholder");
        assertThat(getEntryNames(fileMerge)).contains("my/dest/path/izpack-panel-5.0.0-SNAPSHOT.jar");
    }

    @Test
    public void testMergeFileWithDestination()
    {
        URL url = getSystemResource("com/izforge/izpack/merge/file/FileMergeTest.class");
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/NewFile.ga");
        assertThat(getEntryNames(fileMerge)).contains("my/dest/path/NewFile.ga");
    }

    @Test
    public void testMergeFileWithRootDestination()
    {
        URL url = getSystemResource("com/izforge/izpack/merge/file/FileMergeTest.class");
        FileMerge fileMerge = new FileMerge(url, "NewFile.ga");
        assertThat(getEntryNames(fileMerge)).contains("NewFile.ga");
    }


    @Test
    public void findFileInDirectory()
    {
        FileMerge fileMerge = new FileMerge(getSystemResource("com/izforge/izpack/merge/test"));
        File file = fileMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.getName().equals(".placeholder") || pathname.isDirectory();
            }
        });
        assertThat(file.getName()).isEqualTo(".placeholder");
    }

}
