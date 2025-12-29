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

package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.exception.HelpRequestedException;
import com.izforge.izpack.compiler.exception.NoArgumentException;

import java.util.Date;
import java.util.logging.ConsoleHandler;

/**
 * CompilerLauncher class initialing bindings and launching the compiler
 *
 * @author Anthonin Bonnefoy
 */
public class CompilerLauncher
{

    /**
     * The main method if the compiler is invoked by a command-line call.
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        // exit code 1 means: error
        int exitCode = 1;
        try
        {
            CompilerData compilerData = CliAnalyzer.parseArguments(args);
            CompilerContainer compilerContainer = new CompilerContainer(new ConsoleHandler(), compilerData, compilerData::getInstallFile);

            CompilerConfig compiler = compilerContainer.getComponent(CompilerConfig.class);
            compiler.executeCompiler();

            if (compiler.wasSuccessful())
            {
                exitCode = 0;
            }

            System.out.println("Build time: " + new Date());
        }
        catch (NoArgumentException ignored)
        {
        }
        catch (HelpRequestedException ignored)
        {
        }
        catch (Exception err)
        {
            // Something bad has happened
            System.err.println("-> Fatal error :");
            System.err.println("   " + err.getMessage());
            err.printStackTrace();
            System.err.println();
            System.err.println("(tip : use -? to get the command line parameters)");
        }

        // Closes the JVM
        System.exit(exitCode);
    }

}
