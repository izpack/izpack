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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.izforge.izpack.util.os.WinConsole;


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
     * A constant for file type. Plain file with Dos Batch variables markers
     */
    protected final static int TYPE_BATCH = 8;

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
    
    public final static Map<Integer, String> dosCodePageIdToNameMap;

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
        typeNameToConstantMap.put("batch", TYPE_BATCH);
        
        dosCodePageIdToNameMap = new HashMap<Integer, String>();
        dosCodePageIdToNameMap.put(037, "IBM037");
        dosCodePageIdToNameMap.put(437, "IBM437");
        dosCodePageIdToNameMap.put(500, "IBM500");
        dosCodePageIdToNameMap.put(708, "ASMO-708");
        dosCodePageIdToNameMap.put(709, "Arabic");
        dosCodePageIdToNameMap.put(710, "Arabic");
        dosCodePageIdToNameMap.put(720, "DOS-720");
        dosCodePageIdToNameMap.put(737, "ibm737");
        dosCodePageIdToNameMap.put(775, "ibm775");
        dosCodePageIdToNameMap.put(850, "ibm850");
        dosCodePageIdToNameMap.put(852, "ibm852");
        dosCodePageIdToNameMap.put(855, "IBM855");
        dosCodePageIdToNameMap.put(857, "ibm857");
        dosCodePageIdToNameMap.put(858, "IBM00858");
        dosCodePageIdToNameMap.put(860, "IBM860");
        dosCodePageIdToNameMap.put(861, "ibm861");
        dosCodePageIdToNameMap.put(862, "DOS-862");
        dosCodePageIdToNameMap.put(863, "IBM863");
        dosCodePageIdToNameMap.put(864, "IBM864");
        dosCodePageIdToNameMap.put(865, "IBM865");
        dosCodePageIdToNameMap.put(866, "cp866");
        dosCodePageIdToNameMap.put(869, "ibm869");
        dosCodePageIdToNameMap.put(870, "IBM870");
        dosCodePageIdToNameMap.put(874, "windows-874");
        dosCodePageIdToNameMap.put(875, "cp875");
        dosCodePageIdToNameMap.put(932, "shift_jis");
        dosCodePageIdToNameMap.put(936, "gb2312");
        dosCodePageIdToNameMap.put(949, "ks_c_5601-1987");
        dosCodePageIdToNameMap.put(950, "big5");
        dosCodePageIdToNameMap.put(1026, "IBM1026");
        dosCodePageIdToNameMap.put(1047, "IBM01047");
        dosCodePageIdToNameMap.put(1140, "IBM01140");
        dosCodePageIdToNameMap.put(1141, "IBM01141");
        dosCodePageIdToNameMap.put(1142, "IBM01142");
        dosCodePageIdToNameMap.put(1143, "IBM01143");
        dosCodePageIdToNameMap.put(1144, "IBM01144");
        dosCodePageIdToNameMap.put(1145, "IBM01145");
        dosCodePageIdToNameMap.put(1146, "IBM01146");
        dosCodePageIdToNameMap.put(1147, "IBM01147");
        dosCodePageIdToNameMap.put(1148, "IBM01148");
        dosCodePageIdToNameMap.put(1149, "IBM01149");
        dosCodePageIdToNameMap.put(1200, "utf-16");
        dosCodePageIdToNameMap.put(1201, "unicodeFFFE");
        dosCodePageIdToNameMap.put(1250, "windows-1250");
        dosCodePageIdToNameMap.put(1251, "windows-1251");
        dosCodePageIdToNameMap.put(1252, "windows-1252");
        dosCodePageIdToNameMap.put(1253, "windows-1253");
        dosCodePageIdToNameMap.put(1254, "windows-1254");
        dosCodePageIdToNameMap.put(1255, "windows-1255");
        dosCodePageIdToNameMap.put(1256, "windows-1256");
        dosCodePageIdToNameMap.put(1257, "windows-1257");
        dosCodePageIdToNameMap.put(1258, "windows-1258");
        dosCodePageIdToNameMap.put(1361, "Johab");
        dosCodePageIdToNameMap.put(10000, "macintosh");
        dosCodePageIdToNameMap.put(10001, "x-mac-japanese");
        dosCodePageIdToNameMap.put(10002, "x-mac-chinesetrad");
        dosCodePageIdToNameMap.put(10003, "x-mac-korean");
        dosCodePageIdToNameMap.put(10004, "x-mac-arabic");
        dosCodePageIdToNameMap.put(10005, "x-mac-hebrew");
        dosCodePageIdToNameMap.put(10006, "x-mac-greek");
        dosCodePageIdToNameMap.put(10007, "x-mac-cyrillic");
        dosCodePageIdToNameMap.put(10008, "x-mac-chinesesimp");
        dosCodePageIdToNameMap.put(10010, "x-mac-romanian");
        dosCodePageIdToNameMap.put(10017, "x-mac-ukrainian");
        dosCodePageIdToNameMap.put(10021, "x-mac-thai");
        dosCodePageIdToNameMap.put(10029, "x-mac-ce");
        dosCodePageIdToNameMap.put(10079, "x-mac-icelandic");
        dosCodePageIdToNameMap.put(10081, "x-mac-turkish");
        dosCodePageIdToNameMap.put(10082, "x-mac-croatian");
        dosCodePageIdToNameMap.put(12000, "utf-32");
        dosCodePageIdToNameMap.put(12001, "utf-32BE");
        dosCodePageIdToNameMap.put(20000, "x-Chinese_CNS");
        dosCodePageIdToNameMap.put(20001, "x-cp20001");
        dosCodePageIdToNameMap.put(20002, "x_Chinese-Eten");
        dosCodePageIdToNameMap.put(20003, "x-cp20003");
        dosCodePageIdToNameMap.put(20004, "x-cp20004");
        dosCodePageIdToNameMap.put(20005, "x-cp20005");
        dosCodePageIdToNameMap.put(20105, "x-IA5");
        dosCodePageIdToNameMap.put(20106, "x-IA5-German");
        dosCodePageIdToNameMap.put(20107, "x-IA5-Swedish");
        dosCodePageIdToNameMap.put(20108, "x-IA5-Norwegian");
        dosCodePageIdToNameMap.put(20127, "us-ascii");
        dosCodePageIdToNameMap.put(20261, "x-cp20261");
        dosCodePageIdToNameMap.put(20269, "x-cp20269");
        dosCodePageIdToNameMap.put(20273, "IBM273");
        dosCodePageIdToNameMap.put(20277, "IBM277");
        dosCodePageIdToNameMap.put(20278, "IBM278");
        dosCodePageIdToNameMap.put(20280, "IBM280");
        dosCodePageIdToNameMap.put(20284, "IBM284");
        dosCodePageIdToNameMap.put(20285, "IBM285");
        dosCodePageIdToNameMap.put(20290, "IBM290");
        dosCodePageIdToNameMap.put(20297, "IBM297");
        dosCodePageIdToNameMap.put(20420, "IBM420");
        dosCodePageIdToNameMap.put(20423, "IBM423");
        dosCodePageIdToNameMap.put(20424, "IBM424");
        dosCodePageIdToNameMap.put(20833, "x-EBCDIC-KoreanExtended");
        dosCodePageIdToNameMap.put(20838, "IBM-Thai");
        dosCodePageIdToNameMap.put(20866, "koi8-r");
        dosCodePageIdToNameMap.put(20871, "IBM871");
        dosCodePageIdToNameMap.put(20880, "IBM880");
        dosCodePageIdToNameMap.put(20905, "IBM905");
        dosCodePageIdToNameMap.put(20924, "IBM00924");
        dosCodePageIdToNameMap.put(20932, "EUC-JP");
        dosCodePageIdToNameMap.put(20936, "x-cp20936");
        dosCodePageIdToNameMap.put(20949, "x-cp20949");
        dosCodePageIdToNameMap.put(21025, "cp1025");
        dosCodePageIdToNameMap.put(21027, "(deprecated)");
        dosCodePageIdToNameMap.put(21866, "koi8-u");
        dosCodePageIdToNameMap.put(28591, "iso-8859-1");
        dosCodePageIdToNameMap.put(28592, "iso-8859-2");
        dosCodePageIdToNameMap.put(28593, "iso-8859-3");
        dosCodePageIdToNameMap.put(28594, "iso-8859-4");
        dosCodePageIdToNameMap.put(28595, "iso-8859-5");
        dosCodePageIdToNameMap.put(28596, "iso-8859-6");
        dosCodePageIdToNameMap.put(28597, "iso-8859-7");
        dosCodePageIdToNameMap.put(28598, "iso-8859-8");
        dosCodePageIdToNameMap.put(28599, "iso-8859-9");
        dosCodePageIdToNameMap.put(28603, "iso-8859-13");
        dosCodePageIdToNameMap.put(28605, "iso-8859-15");
        dosCodePageIdToNameMap.put(29001, "x-Europa");
        dosCodePageIdToNameMap.put(38598, "iso-8859-8-i");
        dosCodePageIdToNameMap.put(50220, "iso-2022-jp");
        dosCodePageIdToNameMap.put(50221, "csISO2022JP");
        dosCodePageIdToNameMap.put(50222, "iso-2022-jp");
        dosCodePageIdToNameMap.put(50225, "iso-2022-kr");
        dosCodePageIdToNameMap.put(50227, "x-cp50227");
        dosCodePageIdToNameMap.put(50229, "ISO");
        dosCodePageIdToNameMap.put(50930, "EBCDIC");
        dosCodePageIdToNameMap.put(50931, "EBCDIC");
        dosCodePageIdToNameMap.put(50933, "EBCDIC");
        dosCodePageIdToNameMap.put(50935, "EBCDIC");
        dosCodePageIdToNameMap.put(50936, "EBCDIC");
        dosCodePageIdToNameMap.put(50937, "EBCDIC");
        dosCodePageIdToNameMap.put(50939, "EBCDIC");
        dosCodePageIdToNameMap.put(51932, "euc-jp");
        dosCodePageIdToNameMap.put(51936, "EUC-CN");
        dosCodePageIdToNameMap.put(51949, "euc-kr");
        dosCodePageIdToNameMap.put(51950, "EUC");
        dosCodePageIdToNameMap.put(52936, "hz-gb-2312");
        dosCodePageIdToNameMap.put(54936, "GB18030");
        dosCodePageIdToNameMap.put(57002, "x-iscii-de");
        dosCodePageIdToNameMap.put(57003, "x-iscii-be");
        dosCodePageIdToNameMap.put(57004, "x-iscii-ta");
        dosCodePageIdToNameMap.put(57005, "x-iscii-te");
        dosCodePageIdToNameMap.put(57006, "x-iscii-as");
        dosCodePageIdToNameMap.put(57007, "x-iscii-or");
        dosCodePageIdToNameMap.put(57008, "x-iscii-ka");
        dosCodePageIdToNameMap.put(57009, "x-iscii-ma");
        dosCodePageIdToNameMap.put(57010, "x-iscii-gu");
        dosCodePageIdToNameMap.put(57011, "x-iscii-pa");
        dosCodePageIdToNameMap.put(65000, "utf-7");
        dosCodePageIdToNameMap.put(65001, "utf-8");
        
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
                case TYPE_BATCH:
                    encoding = dosCodePageIdToNameMap.get(WinConsole.INSTANCE.GetConsoleOutputCP());
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
            
            if (OsVersion.IS_WINDOWS)
            {
                writer.write("\r\n");
            }
            else
            {
                writer.write("\n");
            }

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
            case TYPE_BATCH:
                //return str.replace("^", "^^").replace("<", "^<").replace(">", "^>").replace("|", "^|").replace("&", "^&").replace("%", "%%");
                return str.replace("%", "%%").replace("\\\"", "\\\\\"");
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

