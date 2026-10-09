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

import static com.izforge.izpack.matcher.MergeMatcher.getEntryNames;
import static com.izforge.izpack.merge.resolve.ResolveUtils.getPanelsPackagePathFromClassName;
import static com.izforge.izpack.merge.resolve.ResolveUtils.isJar;
import static com.izforge.izpack.util.FileUtil.convertUrlToFile;
import static java.lang.ClassLoader.getSystemResource;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.merge.jar.JarMerge;
import com.izforge.izpack.test.Container;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Test of path resolver
 *
 * @author Anthonin Bonnefoy
 */
@Container(TestMergeContainer.class)
public class PathResolverTest
{
    private PathResolver pathResolver;

    public PathResolverTest(PathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }

    @Test
    public void testGetMergeableFromJar()
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("org/junit/jupiter/api");
        assertThat(jarMergeList).hasSize(1);
        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(jarMerge).isInstanceOf(JarMerge.class);
        assertThat(getEntryNames(jarMerge)).contains("org/junit/jupiter/api/Assertions.class",
                "org/junit/jupiter/api/Test.class");
    }

    @Test
    public void testResolvePathOfJar()
    {
        Set<URL> urlList = pathResolver.resolvePath("com/izforge");
        assertThat(urlList.size()).isGreaterThan(1);
    }

    @Test
    public void testResolvePathOfFileAndJar()
    {
        Set<URL> urlList = pathResolver.resolvePath("META-INF/MANIFEST.MF");
        assertThat(getListPathFromListURL(urlList)).anySatisfy(path ->
                assertThat(path).contains("jar!"));
        assertThat(getListPathFromListURL(urlList)).anySatisfy(path ->
                assertThat(path).doesNotContain("jar!"));
    }

    @Test
    public void testResolvePathOfDirectory()
    {
        Collection<URL> urlList = pathResolver.resolvePath("com/izforge/izpack/merge/");
        assertThat(getListPathFromListURL(urlList)).anySatisfy(path ->
                assertThat(path).doesNotContain("jar!"));
    }

    @Test
    public void testGetMergeableFromFile()
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/file/FileMerge.class");
        Mergeable mergeable = mergeables.get(0);
        assertThat(getEntryNames(mergeable)).contains("com/izforge/izpack/merge/file/FileMerge.class");
    }

    @Test
    public void testGetMergeableFromFileWithDestination()
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/file/FileMerge.class", "a/dest/FileMerge.class");
        Mergeable mergeable = mergeables.get(0);
        assertThat(getEntryNames(mergeable)).contains("a/dest/FileMerge.class");
    }

    @Test
    public void testGetMergeableFromDirectory()
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/");
        assertThat(mergeables).anySatisfy(mergeable ->
                assertThat(getEntryNames(mergeable)).contains("com/izforge/izpack/merge/resolve/PathResolver.class"));
    }

    @Test
    public void testGetMergeableFromDirectoryWithDestination()
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPath("com/izforge/izpack/merge/", "a/dest/");
        assertThat(mergeables).anySatisfy(mergeable ->
                assertThat(getEntryNames(mergeable)).contains("a/dest/resolve/PathResolver.class"));
    }

    @Test
    public void testGetMergeableFromPackage()
    {
        List<Mergeable> mergeables = pathResolver.getMergeableFromPackageName("com.izforge.izpack.merge");
        assertThat(mergeables).anySatisfy(mergeable ->
                assertThat(getEntryNames(mergeable)).contains("com/izforge/izpack/merge/resolve/PathResolver.class"));
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
    public void testIsJarWithURL()
    {
        URL fileResource = getSystemResource("com/izforge/izpack/merge/file/FileMerge.class");
        URL jarResource = getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        assertThat(isJar(
                fileResource)).isFalse();
        assertThat(isJar(
                jarResource)).isTrue();
    }

    @Test
    public void testIsJarWithFile()
    {
        File fileResource = convertUrlToFile(getSystemResource("com/izforge/izpack/merge/jar/JarMerge.class"));
        File jarResource = convertUrlToFile(getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar"));
        assertThat(isJar(
                fileResource)).isFalse();
        assertThat(isJar(
                jarResource)).isTrue();
    }


    @Test
    public void pathResolverShouldTransformClassNameToPackagePath()
    {
        String pathFromClassName = getPanelsPackagePathFromClassName("com.test.sora.UneClasse");
        assertThat(pathFromClassName).isEqualTo("com/test/sora/");
    }

    @Test
    public void pathResolverShouldReturnDefaultPackagePath()
    {
        String pathFromClassName = getPanelsPackagePathFromClassName("UneClasse");
        assertThat(pathFromClassName).isEqualTo("com/izforge/izpack/panels/");
    }

    @Test
    public void findResourcesWithMultiReleaseJar()
    {
        Set<URL> resources = pathResolver.findResources("org/jsoup");
        assertThat(resources).extracting(URL::getPath).anySatisfy(path ->
                assertThat(path).endsWith("!/META-INF/versions/11/org/jsoup/"));
        assertThat(resources).extracting(URL::getPath).anySatisfy(path ->
                assertThat(path).endsWith("!/org/jsoup/"));
    }
}
