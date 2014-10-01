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

import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.compiler.container.TestCompilerContainer;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for an Izpack compilation
 */
@RunWith(PicoRunner.class)
@Container(TestCompilerContainer.class)
public class CompilerConfigSamplesTest
{
    private JarFile jar;
    private CompilerConfig compilerConfig;
    private AbstractContainer testContainer;

    public CompilerConfigSamplesTest(TestCompilerContainer container, CompilerConfig compilerConfig)
    {
        this.testContainer = container;
        this.compilerConfig = compilerConfig;
    }

    @Test
    @InstallFile("samples/izpack.xml")
    public void installerShouldContainInstallerClassResourcesAndImages() throws Exception
    {
        compilerConfig.executeCompiler();
        jar = testContainer.getComponent(JarFile.class);
        assertThat((ZipFile)jar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class",
                "resources/vars",
                "com/izforge/izpack/img/JFrameIcon.png"));
    }

    @Test
    @InstallFile("samples/silverpeas/silverpeas.xml")
    public void installerShouldMergeProcessPanelCorrectly() throws Exception
    {
        compilerConfig.executeCompiler();
        jar = testContainer.getComponent(JarFile.class);
        assertThat((ZipFile)jar, ZipMatcher.isZipMatching(IsNot.not(IsCollectionContaining.hasItems(
                "com/izforge/izpack/panels/process/VariableCondition.class",
                "com/sora/panel/VimPanel.class",
                "resource/32/help-browser.png"))));
    }


    @Test
    @InstallFile("samples/silverpeas/silverpeas.xml")
    public void installerShouldConfigureSplashScreenCorrectly() throws Exception
    {
        compilerConfig.executeCompiler();
        jar = testContainer.getComponent(JarFile.class);
        assertThat((ZipFile)jar, ZipMatcher.isZipContainingFiles(
                "resources/Splash.image"));
    }


}