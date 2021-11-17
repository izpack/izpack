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
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * An input stream which resolves IzPack variables on the fly
 */
public class VariableSubstitutorInputStream extends InputStream
{
    private String encoding;
    private Reader substitutorReader;
    private boolean lastSegment;
    private int index;
    private byte[] buffer = new byte[0];

    public VariableSubstitutorInputStream(InputStream inputStream, Variables variables, SubstitutionType type, boolean bracesRequired) throws UnsupportedEncodingException
    {
        this(inputStream, null, variables, type, bracesRequired);
    }

    public VariableSubstitutorInputStream(InputStream inputStream, String encoding, Variables variables, SubstitutionType type, boolean bracesRequired) throws UnsupportedEncodingException
    {
        // Check if file type specific default encoding known
        if (encoding == null)
        {
            if (type == null)
            {
                type = SubstitutionType.getDefault();
            }

            switch (type)
            {
                case TYPE_JAVA_PROPERTIES:
                    encoding = "ISO-8859-1";
                    break;
                case TYPE_XML:
                    encoding = "UTF-8";
                    break;
            }
        }

        this.encoding = encoding;

        // Create the reader and write
        InputStreamReader inputStreamReader = (encoding != null ? new InputStreamReader(inputStream, encoding)
                : new InputStreamReader(inputStream));

        substitutorReader = new VariableSubstitutorReader(inputStreamReader, variables, type, bracesRequired);
    }

    @Override
    public int read() throws IOException
    {
        if (index == buffer.length) {
            index = 0;
            buffer = new byte[0];
            if (lastSegment)
            {
                return -1;
            }
            char[] buff = new char[1024];
            int count = 0;
            while (count < buff.length)
            {
                int curChar = substitutorReader.read();
                if (curChar == -1)
                {
                    lastSegment = true;
                    if (count == 0) {
                        return -1;
                    }
                    break;
                }
                buff[count++] = (char) curChar;
            }
            buffer = String.valueOf(buff, 0, count).getBytes(encoding);
        }
        return buffer[index++] & 0xff;
    }

    @Override
    public void close() throws IOException
    {
        IOUtils.closeQuietly(substitutorReader);
        super.close();
    }

    public String getEncoding()
    {
        return encoding;
    }
}
