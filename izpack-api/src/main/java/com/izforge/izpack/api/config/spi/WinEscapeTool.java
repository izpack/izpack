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

public class WinEscapeTool extends EscapeTool
{

    private static final int ANSI_HEX_DIGITS = 2;
    private static final int ANSI_OCTAL_DIGITS = 3;
    private static final int OCTAL_RADIX = 8;
    private static final WinEscapeTool INSTANCE = new WinEscapeTool();

    public static WinEscapeTool getInstance()
    {
        return INSTANCE;
    }

    @Override
    void escapeBinary(StringBuilder buff, char c)
    {
        buff.append("\\x");
        buff.append(HEX[(c >>> HEX_DIGIT_3_OFFSET) & HEX_DIGIT_MASK]);
        buff.append(HEX[c & HEX_DIGIT_MASK]);
    }

    @Override
    int unescapeBinary(StringBuilder buff, char escapeType, String line, int index)
    {
        int ret = index;

        if (escapeType == 'x')
        {
            try
            {
                buff.append((char) Integer.parseInt(line.substring(index, index + ANSI_HEX_DIGITS), HEX_RADIX));
                ret = index + ANSI_HEX_DIGITS;
            }
            catch (Exception x)
            {
                throw new IllegalArgumentException("Malformed \\xHH encoding.", x);
            }
        }
        else if (escapeType == 'o')
        {
            try
            {
                buff.append((char) Integer.parseInt(line.substring(index, index + ANSI_OCTAL_DIGITS), OCTAL_RADIX));
                ret = index + ANSI_OCTAL_DIGITS;
            }
            catch (Exception x)
            {
                throw new IllegalArgumentException("Malformed \\oOO encoding.", x);
            }
        }

        return ret;
    }
}
