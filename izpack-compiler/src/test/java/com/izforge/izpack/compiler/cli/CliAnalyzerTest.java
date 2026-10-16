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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.izforge.izpack.compiler.data.CompilerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test cli analyzer
 *
 * @author Anthonin Bonnefoy
 */
public class CliAnalyzerTest
{
    private CliAnalyzer analyzer;

    @BeforeEach
    public void initAnalyzer()
    {
        analyzer = new CliAnalyzer();
    }

    @Test
    public void voidArgumentShouldThrowRuntimeException() throws Exception
    {
        assertThatThrownBy(() -> {
            analyzer.parseArgs(new String[]{});
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void fileNameShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml"});
        assertThat(data.getInstallFile()).isEqualTo("myInstall.xml");
    }

    @Test
    public void homeDirShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-h/mon/che min/"});
        assertThat(data.getInstallFile()).isEqualTo("myInstall.xml");
        assertThat(CompilerData.IZPACK_HOME).isEqualTo("/mon/che min/");
    }

    @Test
    public void baseDirShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-b/mon/che min/"});
        assertThat(data.getInstallFile()).isEqualTo("myInstall.xml");
        assertThat(data.getBasedir()).isEqualTo("/mon/che min/");
    }

    @Test
    public void multipleOptionShouldBeParsed() throws Exception
    {
        CompilerData data = analyzer.parseArgs(new String[]{"myInstall.xml", "-b/mon/che min/", "-k web", "-o graou.jar"});
        assertThat(data.getInstallFile()).isEqualTo("myInstall.xml");
        assertThat(data.getBasedir()).isEqualTo("/mon/che min/");
        assertThat(data.getKind()).isEqualTo("web");
        assertThat(data.getOutput()).isEqualTo("graou.jar");
    }

}
