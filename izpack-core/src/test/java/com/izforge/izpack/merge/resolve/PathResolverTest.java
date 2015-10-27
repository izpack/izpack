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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.hamcrest.number.IsGreaterThan;
import org.hamcrest.text.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.jar.JarMerge;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.FileUtil;

/**
 * Test of path resolver
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class PathResolverTest
{
    private PathResolver pathResolver;

    public PathResolverTest(PathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }

    @Test
    public void testGetMergeableFromJar() throws Exception
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("junit/framework");
        assertThat(jarMergeList.size(), Is.is(1));
        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(jarMerge, IsInstanceOf.instanceOf(JarMerge.class));
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("junit/framework/Assert.class",
                "junit/framework/AssertionFailedError.class"
        ));
    }

    @Test
    public void testResolvePathOfJar()
    {
        Set<URL> urlList = pathResolver.resolvePath("com/izforge");
        assertThat(urlList.size(), new IsGreaterThan<Integer>(1));
    }

    @Test
    public void testResolvePathOfFileAndJar() throws Exception
    {
        Set<URL> urlList = pathResolver.resolvePath("META-INF/MANIFEST.MF");
        assertThat(getListPathFromListURL(urlList), IsCollectionContaining.hasItems(
                StringContains.containsString("jar!"),
                IsNot.not(StringContains.containsString("jar!"))
        ));
    }

    @Test
    public void testResolvePathOfDirectory() throws Exception
    {
        Collection<URL> urlList = pathResolver.resolvePath("com/izforge/izpack/merge/");
        assertThat(getListPathFromListURL(urlList), IsCollectionContaining.hasItems(
                IsNot.not(StringContains.containsString("jar!"))
        ));
    }

    @Test
    public void ftestGetMergeableFromFile() throws Exception
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/file/FileMerge.class");
        Mergeable mergeable = mergeables.get(0);
        assertThat(mergeable, MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/merge/file/FileMerge.class")
        );
    }

    @Test
    public void testGetMergeableFromFileWithDestination() throws Exception
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/file/FileMerge.class", "a/dest/FileMerge.class");
        Mergeable mergeable = mergeables.get(0);
        assertThat(mergeable, MergeMatcher.isMergeableContainingFiles("a/dest/FileMerge.class")
        );
    }

    @Test
    public void testGetMergeableFromDirectory() throws Exception
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/");
        assertThat(mergeables, IsCollectionContaining.hasItem(
                MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/merge/resolve/PathResolver.class")));
    }

    @Test
    public void testGetMergeableFromDirectoryWithDestination() throws Exception
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/", "a/dest/");
        assertThat(mergeables,
                IsCollectionContaining.hasItem(
                        MergeMatcher.isMergeableContainingFiles("a/dest/resolve/PathResolver.class")));
    }

    @Test
    public void testGetMergeableFromPackage() throws Exception
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPackageName("com.izforge.izpack.merge");
        assertThat(mergeables, IsCollectionContaining.hasItem(
                MergeMatcher.isMergeableContainingFiles("com/izforge/izpack/merge/resolve/PathResolver.class")));
    }

    private Collection<String> getListPathFromListURL(Collection<URL> urlList)
    {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (URL url : urlList)
        {
            arrayList.add(url.getPath());
        }
        return arrayList;
    }

    @Test
    public void testIsJarWithURL() throws Exception
    {
        URL fileResource = ClassLoader.getSystemResource("com/izforge/izpack/merge/AbstractMerge.class");
        URL jarResource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        assertThat(ResolveUtils.isJar(
                fileResource),
                Is.is(false));
        assertThat(ResolveUtils.isJar(
                jarResource),
                Is.is(true));
    }

    @Test
    public void testIsJarWithFile() throws Exception
    {
        File fileResource = FileUtil.convertUrlToFile(ClassLoader.getSystemResource("com/izforge/izpack/merge/AbstractMerge.class"));
        File jarResource = FileUtil.convertUrlToFile(ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar"));
        assertThat(ResolveUtils.isJar(
                fileResource),
                Is.is(false));
        assertThat(ResolveUtils.isJar(
                jarResource),
                Is.is(true));
    }


    @Test
    public void pathResolverShouldTransformClassNameToPackagePath() throws Exception
    {
        String pathFromClassName = ResolveUtils.getPanelsPackagePathFromClassName("com.test.sora.UneClasse");
        assertThat(pathFromClassName, Is.is("com/test/sora/"));
    }

    @Test
    public void pathResolverShouldReturnDefaultPackagePath() throws Exception
    {
        String pathFromClassName = ResolveUtils.getPanelsPackagePathFromClassName("UneClasse");
        assertThat(pathFromClassName, Is.is("com/izforge/izpack/panels/"));
    }

}
