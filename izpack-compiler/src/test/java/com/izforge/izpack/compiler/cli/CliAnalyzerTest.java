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

package com.izforge.izpack.compiler.cli;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import com.izforge.izpack.compiler.data.CompilerData;

/**
 * Test cli analyzer
 *
 * @author Anthonin Bonnefoy
 */
public class CliAnalyzerTest
{
    private CliAnalyzer analyzer;

    @Before
    public void initAnalyzer()
    {
        analyzer = new CliAnalyzer();
    }

    @Test(expected = RuntimeException.class)
    public void voidArgumentShouldThrowRuntimeException() throws Exception
    {
        analyzer.parseArgs(new String[]{});
    }

    @Test
    public void fileNameShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
    }

    @Test
    public void homeDirShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-h/mon/che min/"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
        assertThat(CompilerData.IZPACK_HOME, Is.is("/mon/che min/"));
    }

    @Test
    public void baseDirShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-b/mon/che min/"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
        assertThat(data.getBasedir(), Is.is("/mon/che min/"));
    }

    @Test
    public void multipleOptionShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-b/mon/che min/", "-k web", "-o graou.jar"});
        assertThat(data.getInstallFile(), Is.is("myInstall.xml"));
        assertThat(data.getBasedir(), Is.is("/mon/che min/"));
        assertThat(data.getKind(), Is.is("web"));
        assertThat(data.getOutput(), Is.is("graou.jar"));
    }

}
