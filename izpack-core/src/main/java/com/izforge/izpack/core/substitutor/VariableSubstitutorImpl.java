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

package com.izforge.izpack.core.substitutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Substitutes variables occurring in an input stream or a string. This implementation supports a
 * generic variable value mapping and escapes the possible special characters occurring in the
 * substituted values. The file types specifically supported are plain text files (no escaping),
 * Java properties files, and XML files. A valid variable name matches the regular expression
 * [a-zA-Z][a-zA-Z0-9_]* and names are case sensitive. Variables are referenced either by $NAME or
 * ${NAME} (the latter syntax being useful in situations like ${NAME}NOTPARTOFNAME). If a referenced
 * variable is undefined then it is not substituted but the corresponding part of the stream is
 * copied as is.
 * <p/>
 * This is a abstract base type for all kinds of variables
 */
@ApplicationScoped
public class VariableSubstitutorImpl implements VariableSubstitutor, Serializable
{
    private static final long serialVersionUID = 3907213762447685687L;

    private static final Logger logger = Logger.getLogger(VariableSubstitutorImpl.class.getName());

    /**
     * The replacement variables
     */
    @SuppressWarnings("TransientFieldNotInitialized")
    private transient Variables variables;

    /**
     * Constructs a substituter with the specified variables.
     *
     * @param variables the variables
     */
    @Inject
    public VariableSubstitutorImpl(Variables variables)
    {
        this.variables = variables;
    }

    /**
     * Whether braces are required for substitution.
     */
    private boolean bracesRequired = false;

    /**
     * Get whether this substitutor requires braces.
     */
    public boolean isBracesRequired()
    {
        return bracesRequired;
    }

    /**
     * Specify whether this substitutor requires braces.
     */
    @Override
    public void setBracesRequired(boolean braces)
    {
        bracesRequired = braces;
    }

    /**
     * Substitutes the variables found in the specified string. Escapes special characters using
     * file type specific escaping if necessary. The plain type is assumed
     *
     * @param str the string to check for variables
     * @return the string with substituted variables
     * @throws IllegalArgumentException An error occured
     */
    @Override
    public String substitute(String str)
    {
        return substitute(str, SubstitutionType.TYPE_PLAIN);
    }

    /**
     * Substitutes the variables found in the specified string. Escapes special characters using
     * file type specific escaping if necessary.
     *
     * @param str  the string to check for variables
     * @param type the escaping type or null for plain
     * @return the string with substituted variables
     * @throws IllegalArgumentException An error occured
     */
    @Override
    public String substitute(String str, SubstitutionType type)
    {
        if (str == null)
        {
            return null;
        }

        // Create reader and write for the strings

        try
        {
            return IOUtils.toString(new VariableSubstitutorReader(new StringReader(str), variables, type, bracesRequired));
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error when substituting variables", e);
            throw new IzPackException(e);
        }
    }

    /**
     * Substitutes the variables found in the specified input stream. Escapes special characters
     * using file type specific escaping if necessary.
     *
     * @param in       the input stream to read
     * @param out      the output stream to write
     * @param type     the file type or null for plain
     * @param encoding the character encoding or null for default
     * @return the number of substitutions made
     * @throws IOException an error occured
     */
    @Override
    public int substitute(InputStream in, OutputStream out, SubstitutionType type, String encoding)
            throws Exception
    {
        return IOUtils.copy(new VariableSubstitutorInputStream(in, encoding, variables, type, bracesRequired), out);
    }

    /**
     * Substitute method Variant that gets An Input Stream and returns A String
     *
     * @param in   The Input Stream, with Placeholders
     * @param type The used FormatType
     * @return the substituted result as string
     * @throws IOException an error occured
     */
    @Override
    public String substitute(InputStream in, SubstitutionType type)
            throws Exception
    {
        VariableSubstitutorInputStream inputStream = new VariableSubstitutorInputStream(in, variables, type, bracesRequired);
        return IOUtils.toString(inputStream, inputStream.getEncoding());
    }


    /**
     * Substitutes the variables found in the data read from the specified reader. Escapes special
     * characters using file type specific escaping if necessary.
     *
     * @param reader the reader to read
     * @param writer the writer used to write data out
     * @param type   the file type or null for plain
     * @return the number of substitutions made
     * @throws IOException an error occured
     */
    @Override
    public int substitute(Reader reader, Writer writer, SubstitutionType type) throws Exception
    {
        return IOUtils.copy(new VariableSubstitutorReader(reader, variables, type, bracesRequired), writer);
    }

}
