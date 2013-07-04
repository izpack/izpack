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

package com.izforge.izpack.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
public class VariableSubstitutor implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 3907213762447685687L;

    /**
     * The variable value mappings
     */
    protected transient Properties variables;

    /**
     * Whether braces are required for substitution.
     */
    protected boolean bracesRequired = false;

    /**
     * A constant for file type. Plain file.
     */
    protected final static int TYPE_PLAIN = 0;

    /**
     * A constant for file type. Java properties file.
     */
    protected final static int TYPE_JAVA_PROPERTIES = 1;

    /**
     * A constant for file type. XML file.
     */
    protected final static int TYPE_XML = 2;

    /**
     * A constant for file type. Shell file.
     */
    protected final static int TYPE_SHELL = 3;

    /**
     * A constant for file type. Plain file with '@' start char.
     */
    protected final static int TYPE_AT = 4;

    /**
     * A constant for file type. Java file, where \ have to be escaped.
     */
    protected final static int TYPE_JAVA = 5;

    /**
     * A constant for file type. Plain file with ANT-like variable markers, ie @param@
     */
    protected final static int TYPE_ANT = 6;

    /**
     * A constant for file type. Plain file with X3Web variables markers, ie $$param$$
     */
    protected final static int TYPE_X3WEB = 7;

    /**
     * PLAIN = "plain"
     */
    public final static String PLAIN = "plain";

    /**
     * X3WEB = "x3web"
     */
    public final static String X3WEB = "x3web";

    /**
     * A mapping of file type names to corresponding integer constants.
     */
    protected final static Map<String, Integer> typeNameToConstantMap;

    // Initialize the file type map
    static
    {
        typeNameToConstantMap = new HashMap<String, Integer>();
        typeNameToConstantMap.put("plain", TYPE_PLAIN);
        typeNameToConstantMap.put("javaprop", TYPE_JAVA_PROPERTIES);
        typeNameToConstantMap.put("java", TYPE_JAVA);
        typeNameToConstantMap.put("xml", TYPE_XML);
        typeNameToConstantMap.put("shell", TYPE_SHELL);
        typeNameToConstantMap.put("at", TYPE_AT);
        typeNameToConstantMap.put("ant", TYPE_ANT);
        typeNameToConstantMap.put("x3web", TYPE_X3WEB);
    }

    /**
     * Constructs a new substitutor using the specified variable value mappings. The environment
     * hashtable is copied by reference. Braces are not required by default
     *
     * @param variables the map with variable value mappings
     */
    public VariableSubstitutor(Properties variables)
    {
        this.variables = variables;
    }

    /**
     * Get whether this substitutor requires braces.
     */
    public boolean areBracesRequired()
    {
        return bracesRequired;
    }

    /**
     * Specify whether this substitutor requires braces.
     */
    public void setBracesRequired(boolean braces)
    {
        bracesRequired = braces;
    }

    /**
     * Substitutes the variables found in the specified string. Escapes special characters using
     * file type specific escaping if necessary.
     *
     * @param str  the string to check for variables
     * @param type the escaping type or null for plain
     * @return the string with substituted variables
     * @throws IllegalArgumentException if unknown escaping type specified
     */
    public String substitute(String str, String type) throws IllegalArgumentException
    {
        if (str == null)
        {
            return null;
        }

        // Create reader and write for the strings
        StringReader reader = new StringReader(str);
        StringWriter writer = new StringWriter();

        // Substitute any variables
        try
        {
            substitute(reader, writer, type);
        }
        catch (IOException e)
        {
            throw new Error("Unexpected I/O exception when reading/writing memory "
                    + "buffer; nested exception is: " + e);
        }

        // Return the resulting string
        return writer.getBuffer().toString();
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
     * @throws IllegalArgumentException     if unknown file type specified
     * @throws UnsupportedEncodingException if encoding not supported
     * @throws IOException                  if an I/O error occurs
     */
    public int substitute(InputStream in, OutputStream out, String type, String encoding)
            throws IllegalArgumentException, UnsupportedEncodingException, IOException
    {
        // Check if file type specific default encoding known
        if (encoding == null)
        {
            int t = getTypeConstant(type);
            switch (t)
            {
                case TYPE_JAVA_PROPERTIES:
                    encoding = "ISO-8859-1";
                    break;
                case TYPE_XML:
                    encoding = "UTF-8";
                    break;
            }
        }

        // Create the reader and write
        InputStreamReader reader = (encoding != null ? new InputStreamReader(in, encoding)
                : new InputStreamReader(in));
        OutputStreamWriter writer = (encoding != null ? new OutputStreamWriter(out, encoding)
                : new OutputStreamWriter(out));

        int subs=0;
        
        // Copy the data and substitute variables
        if (X3WEB.equals(type)) subs = substituteforweb(reader, writer, type);
        else subs = substitute(reader, writer, type);

        // Flush the write so that everything gets written out
        writer.flush();

        return subs;
    }

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
    public String substitute(InputStream in, String type
    )
            throws IllegalArgumentException, UnsupportedEncodingException,
            IOException
    {
        // Check if file type specific default encoding known
        String encoding = PLAIN;
        {
            int t = getTypeConstant(type);

            switch (t)
            {
                case TYPE_JAVA_PROPERTIES:
                    encoding = "ISO-8859-1";

                    break;

                case TYPE_XML:
                    encoding = "UTF-8";

                    break;
            }
        }

        // Create the reader and write
        InputStreamReader reader = ((encoding != null)
                ? new InputStreamReader(in, encoding)
                : new InputStreamReader(in));
        StringWriter writer = new StringWriter();

        // Copy the data and substitute variables
        substitute(reader, writer, type);

        // Flush the write so that everything gets written out
        writer.flush();

        return writer.getBuffer().toString();
    }

    public int substituteforweb(Reader reader, Writer writer, String type)
    throws IllegalArgumentException, IOException
    {
        int subs = 0;
        
        BufferedReader strReader = new BufferedReader(reader);
        
        while (true)
        {
            String nextLine = strReader.readLine();
            
            // reached the end of stream
            if (nextLine == null) break;
            
            String[] chunks = nextLine.split("\\$\\$");
            
            for  (int i=0; i<chunks.length; i++)
            {
                if (variables.containsKey(chunks[i]))
                {
                    String varvalue = variables.getProperty(chunks[i]);
                    if (varvalue !=null )
                    {
                        writer.write(varvalue);
                        subs++;
                    }
                    else
                    {
                        //????
                        writer.write(chunks[i]);
                    }
                }
                else
                {
                    writer.write(chunks[i]);
                }
            }

            writer.write("\r\n");

        }
        
        return subs;
    }
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
    public int substitute(Reader reader, Writer writer, String type)
            throws IllegalArgumentException, IOException
    {
        // Check the file type
        int t = getTypeConstant(type);

        // determine character which starts (and ends) a variable
        char variable_start = '$';
        char variable_end = '\0';
        if (t == TYPE_SHELL)
        {
            variable_start = '%';
        }
        else if (t == TYPE_AT)
        {
            variable_start = '@';
        }
        else if (t == TYPE_ANT)
        {
            variable_start = '@';
            variable_end = '@';
        }


        int subs = 0;

        // Copy data and substitute variables
        int c = reader.read();

        while (true)
        {
            // Find the next potential variable reference or EOF
            while (c != -1 && c != variable_start)
            {
                writer.write(c);
                c = reader.read();
            }
            if (c == -1)
            {
                return subs;
            }

            // Check if braces used or start char escaped
            boolean braces = false;
            c = reader.read();
            if (c == '{')
            {
                braces = true;
                c = reader.read();
            }
            else if (bracesRequired)
            {
                writer.write(variable_start);
                continue;
            }
            else if (c == -1)
            {
                writer.write(variable_start);
                return subs;
            }

            // Read the variable name
            StringBuffer nameBuffer = new StringBuffer();
            while (c != -1 && (braces && c != '}') || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z') || (braces && ((c == '[') || (c == ']')))
                    || (((c >= '0' && c <= '9') || c == '_' || c == '.' || c == '-') && nameBuffer.length() > 0))
            {
                nameBuffer.append((char) c);
                c = reader.read();
            }
            String name = nameBuffer.toString();

            // Check if a legal and defined variable found
            String varvalue = null;

            if (((!braces || c == '}') &&
                    (!braces || variable_end == '\0' || variable_end == c)
            ) && name.length() > 0)
            {
                // check for environment variables
                if (braces && name.startsWith("ENV[")
                        && (name.lastIndexOf(']') == name.length() - 1))
                {
                    varvalue = IoHelper.getenv(name.substring(4, name.length() - 1));
                    if (varvalue == null)
                        varvalue = "";
                }
                else
                {
                    varvalue = variables.getProperty(name);
                }

                subs++;
            }

            // Substitute the variable...
            if (varvalue != null)
            {
                writer.write(escapeSpecialChars(varvalue, t));
                if (braces || variable_end != '\0')
                {
                    c = reader.read();
                }
            }
            // ...or ignore it
            else
            {
                writer.write(variable_start);
                if (braces)
                {
                    writer.write('{');
                }
                writer.write(name);
            }
        }
    }

    /**
     * Returns the internal constant for the specified file type.
     *
     * @param type the type name or null for plain
     * @return the file type constant
     */
    protected int getTypeConstant(String type)
    {
        if (type == null)
        {
            return TYPE_PLAIN;
        }
        Integer integer = typeNameToConstantMap.get(type);
        if (integer == null)
        {
            throw new IllegalArgumentException("Unknown file type " + type);
        }
        else
        {
            return integer;
        }
    }

    /**
     * Escapes the special characters in the specified string using file type specific rules.
     *
     * @param str  the string to check for special characters
     * @param type the target file type (one of TYPE_xxx)
     * @return the string with the special characters properly escaped
     */
    protected String escapeSpecialChars(String str, int type)
    {
        StringBuffer buffer;
        int len;
        int i;
        switch (type)
        {
            case TYPE_PLAIN:
            case TYPE_AT:
            case TYPE_ANT:
                return str;
            case TYPE_SHELL:
                //apple mac has major problem with \r, make sure they are gone
                return str.replace("\r","");
            case TYPE_JAVA_PROPERTIES:
            case TYPE_JAVA:
                buffer = new StringBuffer(str);
                len = str.length();
                for (i = 0; i < len; i++)
                {
                    // Check for control characters
                    char c = buffer.charAt(i);
                    if (type == TYPE_JAVA_PROPERTIES)
                    {
                        if (c == '\t' || c == '\n' || c == '\r')
                        {
                            char tag;
                            if (c == '\t')
                            {
                                tag = 't';
                            }
                            else if (c == '\n')
                            {
                                tag = 'n';
                            }
                            else
                            {
                                tag = 'r';
                            }
                            buffer.replace(i, i + 1, "\\" + tag);
                            len++;
                            i++;
                        }

                        // Check for special characters
                        if (c == '\\' || c == '"' || c == '\'' || c == ' ')
                        {
                            buffer.insert(i, '\\');
                            len++;
                            i++;
                        }
                    }
                    else
                    {
                        if (c == '\\')
                        {
                            buffer.replace(i, i + 1, "\\\\");
                            len++;
                            i++;
                        }
                    }
                }
                return buffer.toString();
            case TYPE_XML:
                buffer = new StringBuffer(str);
                len = str.length();
                for (i = 0; i < len; i++)
                {
                    String r = null;
                    char c = buffer.charAt(i);
                    switch (c)
                    {
                        case '<':
                            r = "&lt;";
                            break;
                        case '>':
                            r = "&gt;";
                            break;
                        case '&':
                            r = "&amp;";
                            break;
                        case '\'':
                            r = "&apos;";
                            break;
                        case '"':
                            r = "&quot;";
                            break;
                    }
                    if (r != null)
                    {
                        buffer.replace(i, i + 1, r);
                        len = buffer.length();
                        i += r.length() - 1;
                    }
                }
                return buffer.toString();
            default:
                throw new Error("Unknown file type constant " + type);
        }
    }
}

