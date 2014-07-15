/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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

package com.izforge.izpack.installer.unpacker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * A {@link ParsableFile} parser.
 *
 * @author Julien Ponge
 * @author Johannes Lehtinen
 */
public class ScriptParser
{
    private static final Logger logger = Logger.getLogger(ScriptParser.class.getName());

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor replacer;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * Constructs a new parser. The parsable files specified must have pretranslated paths
     * (variables expanded and file separator characters converted if necessary).
     *
     * @param replacer the variable replacer to use
     * @param matcher  the platform-model matcher
     */
    public ScriptParser(VariableSubstitutor replacer, PlatformModelMatcher matcher)
    {
        this.replacer = replacer;
        this.matcher = matcher;
    }

    /**
     * Parses a file.
     *
     * @param parsable the file to parse
     * @throws Exception if parsing fails
     */
    public void parse(ParsableFile parsable) throws Exception
    {
        // check whether the OS matches
        if (!matcher.matchesCurrentPlatform(parsable.getOsConstraints()))
        {
            return;
        }

        // Create a temporary file for the parsed data
        // (Use the same directory so that renaming works later)
        File file = new File(parsable.getPath());

        logger.fine("Parsing and replacing variables in file " + file + "...");

        File parsedFile;
        try
        {
            parsedFile = File.createTempFile("izpp", null, file.getParentFile());
        }
        catch (IOException exception)
        {
            throw new IOException("Failed to create temporary file for " + parsable.getPath() + " in directory "
                                          + file.getParentFile(), exception);
        }

        // Parses the file
        // (Use buffering because substitutor processes byte at a time)
        FileInputStream inFile = new FileInputStream(file);
        BufferedInputStream in = new BufferedInputStream(inFile, 5120);
        FileOutputStream outFile = new FileOutputStream(parsedFile);
        BufferedOutputStream out = new BufferedOutputStream(outFile, 5120);
        replacer.substitute(in, out, parsable.getType(), parsable.getEncoding());
        in.close();
        out.close();

        // Replace the original file with the parsed one
        if (!file.delete())
        {
            throw new IOException("Failed to delete file: " + file);
        }
        if (!parsedFile.renameTo(file))
        {
            throw new IOException("Could not rename file " + parsedFile + " to " + file);
        }
    }
}
