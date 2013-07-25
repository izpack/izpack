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

package com.izforge.izpack.util.config.base.spi;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.izforge.izpack.util.config.base.Registry;
import com.izforge.izpack.util.config.base.Registry.Type;

public class RegEscapeTool extends EscapeTool
{
    private static final RegEscapeTool INSTANCE = ServiceFinder.findService(RegEscapeTool.class);
    private static final Charset HEX_CHARSET = Charset.forName("UTF-16LE");
    private static final int LOWER_DIGIT = 0x0f;
    private static final int UPPER_DIGIT = 0xf0;
    private static final int DIGIT_SIZE = 4;

    public static final RegEscapeTool getInstance()
    {
        return INSTANCE;
    }

    public TypeValuesPair decode(String raw)
    {
        Type type = type(raw);
        String value = (type == Type.REG_SZ) ? unquote(raw) : raw.substring(type.toString().length() + 1);
        String[] values;

        switch (type)
        {

            case REG_EXPAND_SZ:
            case REG_MULTI_SZ:
                value = bytes2string(binary(value));
                break;

            case REG_DWORD:
            case REG_DWORD_4:
                value = String.valueOf(Long.parseLong(value, HEX_RADIX));
                break;

            case REG_SZ:
                break;

            default:
                break;
        }

        if (type == Type.REG_MULTI_SZ)
        {
            values = splitMulti(value);
        }
        else
        {
            values = new String[] { value };
        }

        return new TypeValuesPair(type, values);
    }

    public String encode(TypeValuesPair data)
    {
        String ret = null;

        if (data.getType() == Type.REG_SZ)
        {
            ret = quote(data.getValues()[0]);
        }
        else if (data.getValues()[0] != null)
        {
            ret = encode(data.getType(), data.getValues());
        }

        return ret;
    }

    byte[] binary(String value)
    {
        byte[] bytes = new byte[value.length()];
        int idx = 0;
        int shift = DIGIT_SIZE;

        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);

            if (Character.isWhitespace(c))
            {
                continue;
            }

            if (c == ',')
            {
                idx++;
                shift = DIGIT_SIZE;
            }
            else
            {
                int digit = Character.digit(c, HEX_RADIX);

                if (digit >= 0)
                {
                    bytes[idx] |= digit << shift;
                    shift = 0;
                }
            }
        }

        return Arrays.copyOfRange(bytes, 0, idx + 1);
    }

    String encode(Type type, String[] values)
    {
        StringBuilder buff = new StringBuilder();

        buff.append(type.toString());
        buff.append(Type.SEPARATOR_CHAR);
        switch (type)
        {

            case REG_EXPAND_SZ:
                buff.append(hexadecimal(values[0]));
                break;

            case REG_DWORD:
                buff.append(String.format("%08x", Long.parseLong(values[0])));
                break;

            case REG_MULTI_SZ:
                int n = values.length;

                for (int i = 0; i < n; i++)
                {
                    buff.append(hexadecimal(values[i]));
                    buff.append(',');
                }

                buff.append("00,00");
                break;

            default:
                buff.append(values[0]);
                break;
        }

        return buff.toString();
    }

    String hexadecimal(String value)
    {
        StringBuilder buff = new StringBuilder();

        if ((value != null) && (value.length() != 0))
        {
            byte[] bytes = string2bytes(value);

            for (byte aByte : bytes)
            {
                buff.append(Character.forDigit((aByte & UPPER_DIGIT) >> DIGIT_SIZE, HEX_RADIX));
                buff.append(Character.forDigit(aByte & LOWER_DIGIT, HEX_RADIX));
                buff.append(',');
            }

            buff.append("00,00");
        }

        return buff.toString();
    }

    Registry.Type type(String raw)
    {
        Registry.Type type;

        if (raw.charAt(0) == DOUBLE_QUOTE)
        {
            type = Registry.Type.REG_SZ;
        }
        else
        {
            int idx = raw.indexOf(Registry.TYPE_SEPARATOR);

            type = (idx < 0) ? Registry.Type.REG_SZ : Registry.Type.fromString(raw.substring(0, idx));
        }

        return type;
    }

    // XXX Java 1.4 compatibility hack
    private String bytes2string(byte[] bytes)
    {
        String str;

        try
        {
            str = new String(bytes, 0, bytes.length - 2, HEX_CHARSET);
        }
        catch (NoSuchMethodError x)
        {
            try
            {
                str = new String(bytes, 0, bytes.length, HEX_CHARSET.name());
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new IllegalStateException(ex);
            }
        }

        return str;
    }

    private String[] splitMulti(String value)
    {
        int len = value.length();
        int start;
        int end;
        int n = 0;

        start = 0;
        for (end = value.indexOf(0, start); end >= 0; end = value.indexOf(0, start))
        {
            n++;
            start = end + 1;
            if (start >= len)
            {
                break;
            }
        }

        String[] values = new String[n];

        start = 0;
        for (int i = 0; i < n; i++)
        {
            end = value.indexOf(0, start);
            values[i] = value.substring(start, end);
            start = end + 1;
        }

        return values;
    }

    // XXX Java 1.4 compatibility hack
    private byte[] string2bytes(String value)
    {
        byte[] bytes;

        try
        {
            bytes = value.getBytes(HEX_CHARSET);
        }
        catch (NoSuchMethodError x)
        {
            try
            {
                bytes = value.getBytes(HEX_CHARSET.name());
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new IllegalStateException(ex);
            }
        }

        return bytes;
    }
}
