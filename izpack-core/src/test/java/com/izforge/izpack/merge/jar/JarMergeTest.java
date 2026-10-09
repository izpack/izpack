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

import static com.izforge.izpack.matcher.MergeMatcher.getEntryNames;
import static com.izforge.izpack.merge.resolve.ResolveUtils.convertPathToPosixPath;
import static com.izforge.izpack.merge.resolve.ResolveUtils.processUrlToJarPath;
import static com.izforge.izpack.util.IoHelper.mergeTarget;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.ClassLoader.getSystemResource;
import static java.net.URLClassLoader.newInstance;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.core.container.TestMergeContainer;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.merge.resolve.PathResolver;
import com.izforge.izpack.test.Container;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

/**
 * Test for merge jar
 *
 * @author Anthonin Bonnefoy
 */
@Container(TestMergeContainer.class)
public class JarMergeTest
{
    @TempDir
    Path directory;
    private PathResolver pathResolver;
    private MergeableResolver mergeableResolver;

    public JarMergeTest(PathResolver pathResolver, MergeableResolver mergeableResolver)
    {
        this.pathResolver = pathResolver;
        this.mergeableResolver = mergeableResolver;
    }

    @Test
    public void testAddJarContent()
    {
        URL resource = getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(resource);
        assertThat(getEntryNames(jarMerge)).contains("jar/izforge/izpack/panels/hello/HelloPanel.class");
    }

    @Test
    public void testMergeClassFromJarFile()
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("org/apache/commons/io/ByteOrderMark.class");

        assertThat(jarMergeList).hasSize(1);

        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(getEntryNames(jarMerge)).contains("org/apache/commons/io/ByteOrderMark.class");
    }

    @Test
    public void testMergeClassFromJarFileWithDestination()
    {
        List<Mergeable> jarMergeList = pathResolver.getMergeableFromPath("org/apache/commons/io/ByteOrderMark.class",
                                                                         "foo/SomeRandomClass.class");

        assertThat(jarMergeList).hasSize(1);

        Mergeable jarMerge = jarMergeList.get(0);
        assertThat(getEntryNames(jarMerge)).contains("foo/SomeRandomClass.class");
    }

    @Test
    public void testMergeJarFoundDynamicallyLoaded() throws IOException {
        URL urlJar = getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        try (URLClassLoader loader = newInstance(new URL[]{urlJar}, getSystemClassLoader()))
        {
            Mergeable jarMerge = mergeableResolver.getMergeableFromURLWithDestination(loader.getResource("jar/izforge/"),
                    "com/dest");
            assertThat(getEntryNames(jarMerge)).contains("com/dest/izpack/panels/hello/HelloPanel.class");
        }
    }


    @Test
    public void testFindPanelInJar()
    {
        URL resource = getSystemResource("com/izforge/izpack/merge/test/izpack-panel-5.0.0-SNAPSHOT.jar");
        Mergeable jarMerge = mergeableResolver.getMergeableFromURL(resource);
        File file = jarMerge.find(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() ||
                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase("CheckedHelloPanel");
            }
        });
        assertThat(convertPathToPosixPath(file.getAbsolutePath())).contains("com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class");
    }


    @Test
    public void testFindFileInJarFoundWithURL() throws IOException {
        URL urlJar = getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        try (URLClassLoader loader = newInstance(new URL[]{urlJar}, getSystemClassLoader()))
        {
            Mergeable jarMerge = mergeableResolver.getMergeableFromURL(loader.getResource("jar/izforge"));
            File file = jarMerge.find(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().matches(".*HelloPanel\\.class") || pathname.isDirectory();
                }
            });
            assertThat(file.getName()).isEqualTo("HelloPanel.class");
        }
    }

    @Test
    public void testRegexpMatch()
    {
        String toCheckKo = "com/izforge/izpack/panels/installationgroup/";
        String toCheckOk = "com/izforge/izpack/panels/install/InstallationPanel.class";
        String regexp = "com/izforge/izpack/panels/install/+(.*)";
        assertThat(toCheckKo.matches(regexp)).isFalse();
        assertThat(toCheckOk.matches(regexp)).isTrue();

        assertThat("test//Double//".replace("//", "/")).isEqualTo("test/Double/");
    }

    /**
     * Verifies that signature files are excluded from being merged.
     */
    @Test
    public void testExcludeSignatures() throws IOException
    {
        // create a test jar, with a number of files, including dummy signatures
        Path jar = createTempFile(directory, "sigtest", ".jar");
        try (JarOutputStream stream = new JarOutputStream(newOutputStream(jar)))
        {
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
        }

        // now merge to a mocked JarOutputStream
        URL url = jar.toUri().toURL();
        String jarPath = processUrlToJarPath(url);
        JarMerge merge = new JarMerge(url, jarPath);
        JarOutputStream output = mock(JarOutputStream.class);
        merge.merge(mergeTarget(output));

        // verify that the signature files have been excluded
        ArgumentCaptor<ZipEntry> captor = forClass(ZipEntry.class);
        verify(output, times(2)).putNextEntry(captor.capture());
        List<ZipEntry> allValues = captor.getAllValues();
        assertThat(allValues).hasSize(2);
        assertThat(allValues.get(0).getName()).isEqualTo("META-INF/ok1");
        assertThat(allValues.get(1).getName()).isEqualTo("META-INF/ok2");
    }
}
