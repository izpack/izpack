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

package com.izforge.izpack.compiler.container.provider;

import org.apache.commons.cli.ParseException;
import org.picocontainer.injectors.Provider;

import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;

/**
 * Provide CompileData coming from CliAnalyzer
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerDataProvider implements Provider
{
    private String[] args;

    public CompilerDataProvider(String[] args)
    {
        this.args = args;
    }

    public CompilerData provide(CliAnalyzer cliAnalyzer, CompilerContainer compilerContainer) throws ParseException
    {
        CompilerData compilerData = cliAnalyzer.printAndParseArgs(args);
        compilerContainer.addConfig("installFile", compilerData.getInstallFile());
        // REFACTOR : find a way to test with a fake home
        // compilerData.resolveIzpackHome();
        return compilerData;
    }

}
