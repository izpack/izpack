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

package com.izforge.izpack.merge.jar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for merge jar
 *
 * @author Anthonin Bonnefoy
 */
@RunWith(PicoRunner.class)
@Container(TestMergeContainer.class)
public class JarMergeTest
{
    private PathResolver pathResolver;
    private MergeableResolver mergeableResolver;

    public JarMergeTest(PathResolver pathResolver, MergeableResolver mergeableResolver)
    {
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
    }

    @Test
    public void testAddJarContent() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(resource);
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("jar/izforge/izpack/panels/hello/HelloPanel.class")
        );
    }

    @Test
    public void testMergeClassFromJarFile() throws Exception
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("org/fest/assertions/Assert.class");

        assertThat(jarMergeList.size(), Is.is(1));

        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("org/fest/assertions/Assert.class"));
    }

    @Test
    public void testMergeClassFromJarFileWithDestination() throws Exception
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("org/fest/assertions/Assert.class",
                                                                         "foo/SomeRandomClass.class");

        assertThat(jarMergeList.size(), Is.is(1));

        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("foo/SomeRandomClass.class"));
    }

    @Test
    public void testMergeJarFoundDynamicallyLoaded() throws Exception
    {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        Mergeable jarMerge = mergeableResolver.getMergeableFromURLWithDestination(loader.getResource("jar/izforge/"),
                                                                                  "com/dest");

        assertThat(jarMerge, MergeMatcher.isMergeableContainingFiles("com/dest/izpack/panels/hello/HelloPanel.class"));
    }


    @Test
    public void testFindPanelInJar() throws Exception
    {
        URL resource = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/izpack-panel-5.0.0-SNAPSHOT.jar");
        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(resource);
        File file = jarMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() ||
                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase("CheckedHelloPanel");
            }
        });
        assertThat(ResolveUtils.convertPathToPosixPath(file.getAbsolutePath()),
                   StringContains.containsString("com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
    }


    @Test
    public void testFindFileInJarFoundWithURL() throws Exception
    {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(loader.getResource("jar/izforge"));
        File file = jarMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.getName().matches(".*HelloPanel\\.class") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is("HelloPanel.class"));
    }

    @Test
    public void testRegexpMatch() throws Exception
    {
        String toCheckKo = "com/izforge/izpack/panels/installationgroup/";
        String toCheckOk = "com/izforge/izpack/panels/install/InstallationPanel.class";
        String regexp = "com/izforge/izpack/panels/install/+(.*)";
        assertThat(toCheckKo.matches(regexp), Is.is(false));
        assertThat(toCheckOk.matches(regexp), Is.is(true));

        assertThat("test//Double//".replaceAll("//", "/"), Is.is("test/Double/"));
    }

    /**
     * Verifies that signature files are excluded from being merged.
     */
    @Test
    public void testExcludeSignatures() throws IOException
    {
        // create a test jar, with a number of files, including dummy signatures
        File jar = File.createTempFile("sigtest", ".jar");
        FileOutputStream file = new FileOutputStream(jar);
        JarOutputStream stream = new JarOutputStream(file);
        stream.putNextEntry(new ZipEntry("/META-INF/ok1"));     // should merge
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("/META-INF/FOO.SF"));  // should be excluded
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("/META-INF/FOO.DSA")); // should be excluded
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("/META-INF/FOO.RSA")); // should be excluded
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("/META-INF/SIG-FOO")); // should be excluded
        stream.closeEntry();
        stream.putNextEntry(new ZipEntry("/META-INF/ok2"));     // should merge
        stream.closeEntry();
        stream.close();

        // now merge to a mocked JarOutputStream
        URL url = jar.toURI().toURL();
        String jarPath = ResolveUtils.processUrlToJarPath(url);
        JarMerge merge = new JarMerge(url, jarPath, new HashMap<OutputStream, List<String>>());
        JarOutputStream output = Mockito.mock(JarOutputStream.class);
        merge.merge(output);

        // verify that the signature files have been excluded
        ArgumentCaptor<ZipEntry> captor = ArgumentCaptor.forClass(ZipEntry.class);
        Mockito.verify(output, Mockito.times(2)).putNextEntry(captor.capture());
        List<ZipEntry> allValues = captor.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals("META-INF/ok1", allValues.get(0).getName());
        assertEquals("META-INF/ok2", allValues.get(1).getName());
    }

}
