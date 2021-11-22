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

import com.izforge.izpack.api.data.Variables;
import com.izforge.izpack.api.substitutor.SubstitutionType;
import com.izforge.izpack.util.IoHelper;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * An input reader which resolves IzPack variables on the fly
 */
public class VariableSubstitutorReader extends Reader
{
    private PushbackReader pushbackReader = null;
    /**
     * The replacement variables
     */
    private final Variables variables;
    private SubstitutionType type;

    /**
     * Whether braces are required for substitution.
     */
    private boolean bracesRequired = false;

    private char variable_start = '$';
    private char variable_end = '\0';
    private boolean inBraces = false;

    private final StringBuilder varNameBuffer = new StringBuilder();
    private String varValue = null;
    private int varValueIndex = 0;


    public VariableSubstitutorReader(Reader source, Variables variables, SubstitutionType type, boolean bracesRequired)
    {
        this(source, variables, type);
        this.bracesRequired = bracesRequired;
    }

    public VariableSubstitutorReader(Reader source, Variables variables, SubstitutionType type)
    {
        this.pushbackReader = new PushbackReader(source);
        this.variables = variables;
        this.type = type;
        if (type == null)
        {
            type = SubstitutionType.getDefault();
        }

        // determine character which starts (and ends) a variable
        switch (type)
        {
            case TYPE_SHELL:
                variable_start = '%';
                break;

            case TYPE_AT:
                variable_start = '@';
                break;

            case TYPE_ANT:
                variable_start = '@';
                variable_end = '@';
                break;

            default:
                break;
        }
    }

    /**
     * Get whether this substitution requires braces.
     */
    public boolean isBracesRequired()
    {
        return bracesRequired;
    }

    /**
     * Specify whether this substitution requires braces.
     */
    public void setBracesRequired(boolean braces)
    {
        bracesRequired = braces;
    }


    @Override
    public int read(CharBuffer target) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public int read() throws IOException
    {
        if(varValue != null){
            if(varValueIndex < varValue.length()){
                return varValue.charAt(varValueIndex++);
            }
            if(varValueIndex == varValue.length()){
                varValue = null;
                varValueIndex = 0;
            }
        }

        int data = pushbackReader.read();
        if(data != variable_start) return data;

        data = pushbackReader.read();
        if (data == '{')
        {
            inBraces = true;
        }
        else if (bracesRequired)
        {
            pushbackReader.unread(data);
            return variable_start;
        }

        varNameBuffer.delete(0, varNameBuffer.length());

        if (!inBraces && data != -1)
        {
            varNameBuffer.append((char) data);
        }

        data = pushbackReader.read();
        while (
                data >= ' ' && (inBraces && data != '}')
                || (inBraces && ((data == '[') || (data == ']')))
                || isAllowedCharInVariableName(data)
        )
        {
            varNameBuffer.append((char) data);
            data = pushbackReader.read();
        }

        boolean variable = wasItPlausibleVariableName(data);
        String name = varNameBuffer.toString();
        if (variable && name.length() > 0)
        {
            // check for environment variables
            if (inBraces && name.startsWith("ENV[")
                    && (name.lastIndexOf(']') == name.length() - 1))
            {
                varValue = IoHelper.getenv(name.substring(4, name.length() - 1));
                if (varValue == null)
                {
                    varValue = "";
                }
            }
            else if (inBraces && name.startsWith("SYSTEM[")
                    && (name.lastIndexOf(']') == name.length() - 1))
            {
                varValue = System.getProperty(name.substring(7, name.length() - 1));
            }
            // TODO: Compatibility mode - to be removed in future
            else if (inBraces && name.startsWith("SYSTEM_") && name.length() > 7)
            {
                varValue = System.getProperty(name.substring(7).replace('_', '.'));
            }
            else
            {
                varValue = variables.get(name);
            }
        }

        boolean unclosedBraces = false;
        if (data <= ' ')
        {
            if (data != -1)
            {
                pushbackReader.unread(data);
            }
            unclosedBraces = true;
        } else if (
                (data == variable_start && variable_start != variable_end)
                || (!isAllowedCharInVariableName(data) && data != '}' && data != variable_end)
                )
        {
            pushbackReader.unread(data);
        }

        if(varValue == null)
        {
            varValue = variable_start
                    + (inBraces ? "{" : "")
                    + varNameBuffer.toString()
                    + (inBraces && !unclosedBraces ? "}" : "")
                    + (variable_end != '\0' && variable ? variable_end : "");
        }
        else
        {
            varValue = escapeSpecialChars(varValue);
        }

        inBraces = false;

        if(varValue.length() == 0){
            return read();
        }

        return varValue.charAt(varValueIndex++);
    }

    private boolean wasItPlausibleVariableName(int data) throws IOException
    {
        if (variable_end == '\0')
        {
            return !inBraces || data == '}';
        }
        if (inBraces)
        {
            if (data != '}')
            {
                return false;
            }
            int nextData = pushbackReader.read();
            if (nextData == -1)
            {
                return false;
            }
            if (variable_end == nextData)
            {
                return true;
            }
            pushbackReader.unread(nextData);
            return false;
        }
        return variable_end == data;
    }

    @Override
    public int read(char cbuf[]) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        int charsRead = 0;
        for(int i=0; i<len; i++){
            int nextChar = read();
            if(nextChar == -1) {
                if(charsRead == 0){
                    charsRead = -1;
                }
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char) nextChar;
        }
        return charsRead;
    }

    @Override
    public void close() throws IOException {
        this.pushbackReader.close();
    }

    @Override
    public long skip(long n) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public boolean ready() throws IOException {
        return this.pushbackReader.ready();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }

    @Override
    public void reset() throws IOException {
        throw new RuntimeException("Operation Not Supported");
    }


    private static boolean isAllowedCharInVariableName(int c)
    {
        return (
                (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (((c >= '0' && c <= '9')
                || c == '_' || c == '.' || c == '-'))
        );
    }

    /**
     * Escapes the special characters in the specified string using file type specific rules.
     *
     * @param str  the string to check for special characters
     * @return the string with the special characters properly escaped
     */
    private String escapeSpecialChars(String str)
    {
        StringBuffer buffer;
        int len;
        int i;

        if (type == null)
        {
            type = SubstitutionType.getDefault();
        }

        switch (type)
        {
            case TYPE_PLAIN:
            case TYPE_AT:
            case TYPE_ANT:
                return str;
            case TYPE_SHELL:
                // apple mac has major problem with \r, make sure they are gone
                return str.replace("\r", "");
            case TYPE_JAVA_PROPERTIES:
            case TYPE_JAVA:
                buffer = new StringBuffer(str);
                len = str.length();
                boolean leading = true;
                for (i = 0; i < len; i++)
                {
                    // Check for control characters
                    char c = buffer.charAt(i);
                    if (type.equals(SubstitutionType.TYPE_JAVA_PROPERTIES))
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
                        // According to the spec:
                        // 'For the element, leading space characters, but not embedded or trailing
                        // space characters,
                        // are written with a preceding \ character'
                        else if (c == ' ')
                        {
                            if (leading)
                            {
                                buffer.insert(i, '\\');
                                len++;
                                i++;
                            }
                        }
                        else if (c == '\\' || c == '"' || c == '\'')
                        {
                            leading = false;
                            buffer.insert(i, '\\');
                            len++;
                            i++;
                        }
                        else
                        {
                            leading = false;
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
