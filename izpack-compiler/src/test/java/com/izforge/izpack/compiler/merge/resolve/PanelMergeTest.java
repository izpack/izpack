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

package com.izforge.izpack.compiler.merge.resolve;

import static com.izforge.izpack.matcher.MergeMatcher.getEntryNames;
import static com.izforge.izpack.matcher.ZipMatcher.getFileNameListFromZip;
import static com.izforge.izpack.test.MergeUtils.doDoubleMerge;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.compiler.container.TestResolveContainer;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.merge.PanelMerge;
import com.izforge.izpack.panels.hello.HelloPanel;
import com.izforge.izpack.test.Container;
import java.io.File;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for panel merge
 *
 * @author Anthonin Bonnefoy
 */
@Container(TestResolveContainer.class)
public class PanelMergeTest
{
    private PanelMerge panelMerge;

    private CompilerPathResolver pathResolver;

    public PanelMergeTest(CompilerPathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
    }

    @Test
    public void testResolvePanelNameFromFile() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("HelloPanel");
        assertThat(getEntryNames(panelMerge)).contains("com/izforge/izpack/panels/hello/HelloPanel.class");
    }

    @Test
    public void testResolvePanelWithCompleteNameFromFile() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestClass");
        assertThat(getEntryNames(panelMerge)).contains("com/izforge/izpack/panels/hello/HelloPanelTestClass.class");
    }

    @Test
    public void testResolvePanelWithDependencies() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestWithDependenciesClass");
        assertThat(getEntryNames(panelMerge)).contains("com/izforge/izpack/panels/hello/HelloPanelTestWithDependenciesClass.class",
                "com/izforge/izpack/panels/depend/DependedClass.class");
    }

    @Test
    public void testGetClassNameFromPanelMergeWithFullClassGiven() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestClass");
        assertThat(panelMerge.getPanelClass().getName()).isEqualTo("com.izforge.izpack.panels.hello.HelloPanelTestClass");
    }

    @Test
    public void testGetClassNameFromPanelMergeWithOnlyPanelName() throws Exception
    {
        panelMerge = pathResolver.getPanelMerge("HelloPanel");
        assertThat(panelMerge.getPanelClass().getName()).isEqualTo(HelloPanel.class.getName());
    }

    @Test
    public void testMergeDuplicatePanel() throws Exception
    {
        Mergeable mergeable = pathResolver.getPanelMerge("com.izforge.izpack.panels.hello.HelloPanelTestClass");
        File tempFile = doDoubleMerge(mergeable);
        ZipFile tempZipFile = new ZipFile(tempFile);
        assertThat(getFileNameListFromZip(tempZipFile)).filteredOn(entry -> entry.equals("com/izforge/izpack/panels/hello/HelloPanelTestClass.class")).hasSize(1);
    }

    /**
     * Verifies that panel dependencies in a different package to that of the panel are merged.
     */
    @Test
    public void testMergePanelWithDependenciesInAnotherPackage()
    {
        PanelMerge merge1 = pathResolver.getPanelMerge("com.izforge.izpack.panels.treepacks.TreePacksPanel");
        assertThat(getEntryNames(merge1)).contains("com/izforge/izpack/panels/treepacks/TreePacksPanel.class",
                "com/izforge/izpack/panels/packs/PacksPanelInterface.class");

        PanelMerge merge2 = pathResolver.getPanelMerge("com.izforge.izpack.panels.htmlhello.HTMLHelloPanel");
        assertThat(getEntryNames(merge2)).contains("com/izforge/izpack/panels/htmlhello/HTMLHelloPanel.class",
                "com/izforge/izpack/panels/htmlinfo/HTMLInfoPanel.class");
    }

}
