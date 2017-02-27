/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
 * Copyright 2002 Paul Wilkinson
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
package com.izforge.izpack.core.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Stream which countes the bytes written through it. Be sure to flush before
 * checking size.
 */
public class ByteCountingOutputStream extends OutputStream
{

    private long count;

    private OutputStream os;

    public ByteCountingOutputStream(OutputStream os)
    {
        setOutputStream(os);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        os.write(b, off, len);
        count += len;
    }

    public void write(byte[] b) throws IOException
    {
        os.write(b);
        count += b.length;
    }

    public void write(int b) throws IOException
    {
        os.write(b);
        count++;
    }

    public void close() throws IOException
    {
        os.close();
    }

    public void flush() throws IOException
    {
        os.flush();
    }

    public long getByteCount()
    {
        return count;
    }

    protected void setOutputStream(OutputStream stream)
    {
        this.os = stream;
        count = 0;
    }
}
