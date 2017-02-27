/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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
package com.izforge.izpack.api.config.spi;

public class EscapeTool
{

    private static final String ESCAPE_LETTERS = "\\tnfbr";
    private static final String ESCAPEABLE_CHARS = "\\\t\n\f\b\r";
    private static final char ESCAPE_CHAR = '\\';
    static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final EscapeTool INSTANCE = ServiceFinder.findService(EscapeTool.class);
    private static final char ASCII_MIN = 0x20;
    private static final char ASCII_MAX = 0x7e;
    static final int HEX_DIGIT_MASK = 0x0f;
    static final int HEX_DIGIT_3_OFFSET = 4;
    static final int HEX_DIGIT_2_OFFSET = 8;
    static final int HEX_DIGIT_1_OFFSET = 12;
    static final int HEX_RADIX = 16;
    private static final int UNICODE_HEX_DIGITS = 4;
    static final char DOUBLE_QUOTE = '"';

    public static EscapeTool getInstance()
    {
        return INSTANCE;
    }

    public String escape(String line)
    {
        int len = line.length();
        StringBuilder buffer = new StringBuilder(len * 2);

        for (int i = 0; i < len; i++)
        {
            char c = line.charAt(i);
            int idx = ESCAPEABLE_CHARS.indexOf(c);

            if (idx >= 0)
            {
                buffer.append(ESCAPE_CHAR);
                buffer.append(ESCAPE_LETTERS.charAt(idx));
            }
            else if ((c < ASCII_MIN) || (c > ASCII_MAX))
            {
                escapeBinary(buffer, c);
            }
            else
            {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    public String quote(String value)
    {
        String ret = value;

        if ((value != null) && (value.length() != 0))
        {
            StringBuilder buff = new StringBuilder();

            buff.append(DOUBLE_QUOTE);
            for (int i = 0; i < value.length(); i++)
            {
                char c = value.charAt(i);

                if ((c == ESCAPE_CHAR) || (c == DOUBLE_QUOTE))
                {
                    buff.append(ESCAPE_CHAR);
                }

                buff.append(c);
            }

            buff.append(DOUBLE_QUOTE);
            ret = buff.toString();
        }

        return ret;
    }

    public String unescape(String line)
    {
        int n = line.length();
        StringBuilder buffer = new StringBuilder(n);
        int i = 0;

        while (i < n)
        {
            char c = line.charAt(i++);

            if (c == ESCAPE_CHAR)
            {
                c = line.charAt(i++);
                int next = unescapeBinary(buffer, c, line, i);

                if (next == i)
                {
                    int idx = ESCAPE_LETTERS.indexOf(c);

                    if (idx >= 0)
                    {
                        c = ESCAPEABLE_CHARS.charAt(idx);
                    }

                    buffer.append(c);
                }
                else
                {
                    i = next;
                }
            }
            else
            {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    public String unquote(String value)
    {
        StringBuilder buff = new StringBuilder();
        boolean escape = false;

        for (int i = 1; i < (value.length() - 1); i++)
        {
            char c = value.charAt(i);

            if (c == ESCAPE_CHAR)
            {
                if (!escape)
                {
                    escape = true;

                    continue;
                }

                escape = false;
            }

            buff.append(c);
        }

        return buff.toString();
    }

    void escapeBinary(StringBuilder buff, char c)
    {
        buff.append("\\u");
        buff.append(HEX[(c >>> HEX_DIGIT_1_OFFSET) & HEX_DIGIT_MASK]);
        buff.append(HEX[(c >>> HEX_DIGIT_2_OFFSET) & HEX_DIGIT_MASK]);
        buff.append(HEX[(c >>> HEX_DIGIT_3_OFFSET) & HEX_DIGIT_MASK]);
        buff.append(HEX[c & HEX_DIGIT_MASK]);
    }

    int unescapeBinary(StringBuilder buff, char escapeType, String line, int index)
    {
        int ret = index;

        if (escapeType == 'u')
        {
            try
            {
                buff.append((char) Integer.parseInt(line.substring(index, index + UNICODE_HEX_DIGITS), HEX_RADIX));
                ret = index + UNICODE_HEX_DIGITS;
            }
            catch (Exception x)
            {
                throw new IllegalArgumentException("Malformed \\uxxxx encoding.", x);
            }
        }

        return ret;
    }
}
