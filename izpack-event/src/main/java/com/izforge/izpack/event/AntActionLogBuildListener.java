/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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
package com.izforge.izpack.event;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DefaultLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

class AntActionLogBuildListener extends DefaultLogger
{

    private static final Logger LOGGER = Logger.getLogger(AntActionLogBuildListener.class.getName());

    public AntActionLogBuildListener(File logFile, boolean append, int level)
    {
        this.setMessageOutputLevel(level);
        if (logFile != null)
        {
            PrintStream printStream;
            try
            {
                final File canonicalLogFile = logFile.getCanonicalFile();
                FileUtils.forceMkdir(canonicalLogFile.getParentFile());
                FileUtils.touch(canonicalLogFile);
                printStream = new PrintStream(new FileOutputStream(canonicalLogFile, append));
                this.setOutputPrintStream(printStream);
                this.setErrorPrintStream(printStream);
            }
            catch (IOException e)
            {
                LOGGER.warning("Cannot log to file '" + logFile + "': " + e.getMessage());
                this.setOutputPrintStream(System.out);
                this.setErrorPrintStream(System.err);
            }
        }
        else
        {
            this.setOutputPrintStream(System.out);
            this.setErrorPrintStream(System.err);
        }
    }
}
