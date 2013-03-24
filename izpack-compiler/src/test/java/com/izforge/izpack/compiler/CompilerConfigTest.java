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

package com.izforge.izpack.compiler;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.maven.shared.jar.JarAnalyzer;
import org.apache.maven.shared.jar.classes.JarClasses;
import org.apache.maven.shared.jar.classes.JarClassesAnalysis;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.matcher.MergeMatcher;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.merge.MergeManagerImpl;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for an Izpack compilation
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainer.class)
@InstallFile("samples/helloAndFinish.xml")
public class CompilerConfigTest
{
    private JarFile jar;
    private CompilerConfig compilerConfig;
    private CompilerPathResolver pathResolver;
    private MergeManagerImpl mergeManager;
    private AbstractContainer testContainer;

    public CompilerConfigTest(TestCompilerContainer container, CompilerConfig compilerConfig,
                              CompilerPathResolver pathResolver, MergeManagerImpl mergeManager)
    {
        this.testContainer = container;
        this.compilerConfig = compilerConfig;
        this.pathResolver = pathResolver;
        this.mergeManager = mergeManager;
    }

    @Test
    public void installerShouldContainInstallerClassResourcesAndImages() throws Exception
    {
        compilerConfig.executeCompiler();
        jar = testContainer.getComponent(JarFile.class);
        assertThat(jar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/installer/bootstrap/Installer.class",
                "com/izforge/izpack/panels/hello/HelloPanel.class",
                "resources/vars",
                "com/izforge/izpack/img/JFrameIcon.png"));
    }

    @Test
    public void mergeManagerShouldGetTheMergeableFromPanel() throws Exception
    {
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("HelloPanel"));
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("CheckedHelloPanel"));

        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles(
                "com/izforge/izpack/panels/hello/HelloConsolePanel.class",
                "com/izforge/izpack/panels/hello/HelloPanel.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloConsolePanel.class"));
    }

    @Test
    @Ignore
    public void testImportAreResolved() throws Exception
    {
        JarAnalyzer jarAnalyzer = new JarAnalyzer(new File(jar.getName()));
        JarClassesAnalysis jarClassAnalyzer = new JarClassesAnalysis();
        JarClasses jarClasses = jarClassAnalyzer.analyze(jarAnalyzer);
        List<String> imports = jarClasses.getImports();
        List<String> listFromZip = ZipMatcher.getFileNameListFromZip(jar);
        ArrayList<String> result = new ArrayList<String>();
        List<String> ignorePackage = Arrays.asList("java/", "org/w3c/", "org/xml/", "javax/", "text/html", "packs/pack",
                                                   "com/thoughtworks");
        for (String anImport : imports)
        {
            if (anImport.matches("([a-z]+\\.)+[a-zA-Z]+"))
            {
                String currentClass = anImport.replaceAll("\\.", "/") + ".class";
                if (ignorePackage.contains(currentClass))
                {
                    continue;
                }
                if (!listFromZip.contains(currentClass))
                {
                    result.add(currentClass);
                }
            }
            if (!result.isEmpty())
            {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : result)
                {
                    stringBuilder.append(s).append('\n');
                }
                Assert.fail("Missing imports : " + stringBuilder);
            }
        }
    }

    /**
     * Verifies that the panelDependencies.properties file is used to resolve panel dependencies.
     */
    @Test
    public void testResolvePanelDependencies()
    {
        mergeManager.addResourceToMerge(pathResolver.getPanelMerge("DefaultTargetPanel"));

        assertThat(mergeManager, MergeMatcher.isMergeableContainingFiles(
                "com/izforge/izpack/panels/defaulttarget/DefaultTargetPanel.class",
                "com/izforge/izpack/panels/defaulttarget/DefaultTargetConsolePanel.class",
                "com/izforge/izpack/panels/target/TargetPanelHelper.class"));
    }
}
