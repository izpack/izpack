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

package com.izforge.izpack.compiler.helper;

import java.io.File;
import java.util.logging.Logger;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.exception.CompilerException;
import jakarta.enterprise.inject.Vetoed;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class AssertionHelper
{
    private static final Logger logger = Logger.getLogger(AssertionHelper.class.getName());

    private String parsedFile;

    public AssertionHelper(String parsedFile)
    {
        this.parsedFile = parsedFile;
    }

    /**
     * Create parse error with consistent messages. Includes file name. For use When parent is
     * unknown.
     *
     * @param message Brief message explaining error
     */
    public void parseError(String message) throws CompilerException
    {
        throw new CompilerException(this.parsedFile + ":" + message);
    }

    /**
     * Create parse error with consistent messages. Includes file name and line # of parent. It is
     * an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    public void parseError(IXMLElement parent, String message) throws CompilerException
    {
        throw new CompilerException(this.parsedFile + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Create a chained parse error with consistent messages. Includes file name and line # of
     * parent. It is an error for 'parent' to be null.
     *
     * @param parent  The element in which the error occured
     * @param message Brief message explaining error
     */
    public void parseError(IXMLElement parent, String message, Throwable cause)
            throws CompilerException
    {
        throw new CompilerException(this.parsedFile + ":" + parent.getLineNr() + ": " + message, cause);
    }

    /**
     * Create a parse warning with consistent messages. Includes file name and line # of parent. It
     * is an error for 'parent' to be null.
     *
     * @param parent  The element in which the warning occured
     * @param message Warning message
     */
    public void parseWarn(IXMLElement parent, String message)
    {
        logger.warning(this.parsedFile + ":" + parent.getLineNr() + ": " + message);
    }

    /**
     * Checks whether a File instance is a regular file, exists and is readable. Throws appropriate
     * CompilerException to report violations of these conditions.
     *
     * @throws com.izforge.izpack.api.exception.CompilerException
     *          if the file is either not existing, not a regular file or not
     *          readable.
     */
    public void assertIsNormalReadableFile(File fileToCheck, String fileDescription)
            throws CompilerException
    {
        if (fileToCheck != null)
        {
            if (!fileToCheck.exists())
            {
                throw new CompilerException(fileDescription
                        + " does not exist: " + fileToCheck);
            }
            if (!fileToCheck.isFile())
            {
                throw new CompilerException(fileDescription
                        + " is not a regular file: " + fileToCheck);
            }
            if (!fileToCheck.canRead())
            {
                throw new CompilerException(fileDescription
                        + " is not readable by application: " + fileToCheck);
            }
        }
    }
}
