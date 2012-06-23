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

package com.izforge.izpack.api.substitutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * Substitutes variables occurring in an input stream or a string. This implementation supports a
 * generic variable value mapping and escapes the possible special characters occurring in the
 * substituted values. The file types specifically supported are plain text files (no escaping),
 * Java properties files, and XML files. A valid variable name matches the regular expression
 * [a-zA-Z][a-zA-Z0-9_]* and names are case sensitive. Variables are referenced either by $NAME or
 * ${NAME} (the latter syntax being useful in situations like ${NAME}NOTPARTOFNAME). If a referenced
 * variable is undefined then it is not substituted but the corresponding part of the stream is
 * copied as is.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public interface VariableSubstitutor extends Serializable
{

    /**
     * Specify whether this substitutor requires braces.
     */
    void setBracesRequired(boolean braces);

    /**
     * Substitutes the variables found in the specified string. Escapes special characters using
     * file type specific escaping if necessary.<br />
     * The escaped type will be plain.
     *
     * @param str the string to check for variables
     * @return the string with substituted variables
     * @throws IllegalArgumentException if unknown escaping type specified
     */
    String substitute(String str);

    /**
     * Substitutes the variables found in the specified string. Escapes special characters using
     * file type specific escaping if necessary.
     *
     * @param str  the string to check for variables
     * @param type the escaping type
     * @return the string with substituted variables
     * @throws IllegalArgumentException if unknown escaping type specified
     */
    String substitute(String str, SubstitutionType type);

    /**
     * Substitutes the variables found in the specified input stream. Escapes special characters
     * using file type specific escaping if necessary.
     *
     * @param in       the input stream to read
     * @param out      the output stream to write
     * @param type     the file type or null for plain
     * @param encoding the character encoding or null for default
     * @return the number of substitutions made
     * @throws IllegalArgumentException     if unknown file type specified
     * @throws UnsupportedEncodingException if encoding not supported
     * @throws IOException                  if an I/O error occurs
     * @throws
     */
    int substitute(InputStream in, OutputStream out, SubstitutionType type, String encoding)
            throws Exception;

    /**
     * Substitute method Variant that gets An Input Stream and returns A String
     *
     * @param in   The Input Stream, with Placeholders
     * @param type The used FormatType
     * @return the substituted result as string
     * @throws IllegalArgumentException     If a wrong input was given.
     * @throws UnsupportedEncodingException If the file comes with a wrong Encoding
     * @throws IOException                  If an I/O Error occurs.
     */
    String substitute(InputStream in, SubstitutionType type) throws Exception;

    /**
     * Substitutes the variables found in the data read from the specified reader. Escapes special
     * characters using file type specific escaping if necessary.
     *
     * @param reader the reader to read
     * @param writer the writer used to write data out
     * @param type   the file type or null for plain
     * @return the number of substitutions made
     * @throws IllegalArgumentException if unknown file type specified
     * @throws IOException              if an I/O error occurs
     */
    int substitute(Reader reader, Writer writer, SubstitutionType type) throws Exception;
}
