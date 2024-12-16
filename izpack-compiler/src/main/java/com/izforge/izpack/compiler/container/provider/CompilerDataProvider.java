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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.cli.ParseException;

import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;

/**
 * Provide CompileData coming from CliAnalyzer
 *
 * @author Anthonin Bonnefoy
 */
@Singleton
public class CompilerDataProvider implements Provider<CompilerData>
{
    public static final String ARGS = "CompilerDataArgs";

    private final String[] args;
    private final CliAnalyzer cliAnalyzer;
    private final CompilerContainer compilerContainer;

    @Inject
    public CompilerDataProvider(@Named(ARGS) String[] args,
                                CliAnalyzer cliAnalyzer,
                                CompilerContainer compilerContainer)
    {
        this.args = args;
        this.cliAnalyzer = cliAnalyzer;
        this.compilerContainer = compilerContainer;
    }

    @Override
    public CompilerData get()
    {
        try {
            CompilerData compilerData = cliAnalyzer.printAndParseArgs(args);
            compilerContainer.addConfig("installFile", compilerData.getInstallFile());
            // REFACTOR : find a way to test with a fake home
            // compilerData.resolveIzpackHome();
            return compilerData;

        } catch (ParseException e) {
            throw new IllegalStateException("Error while parsing the command line", e);
        }
    }
}
