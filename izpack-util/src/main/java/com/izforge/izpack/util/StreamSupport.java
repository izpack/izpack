/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.util;

import com.izforge.izpack.api.data.PackCompression;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;

public final class StreamSupport
{
    public static OutputStream compressedOutput(PackCompression comprFormat, OutputStream outputStream) throws IOException
    {
        switch (comprFormat)
        {
            case DEFAULT:
                return outputStream;
            case DEFLATE:
                DeflateParameters deflateParameters = new DeflateParameters();
                deflateParameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
                return new DeflateCompressorOutputStream(outputStream, deflateParameters);
            default:
                try
                {
                    return new CompressorStreamFactory().createCompressorOutputStream(comprFormat.toName(), outputStream);
                }
                catch (CompressorException e)
                {
                    throw new IOException(e);
                }
        }
    }

    public static InputStream compressedInput(PackCompression comprFormat, InputStream inputStream) throws IOException
    {
        switch (comprFormat)
        {
            case DEFAULT:
                return inputStream;
            case DEFLATE:
                DeflateParameters deflateParameters = new DeflateParameters();
                deflateParameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
                return new DeflateCompressorInputStream(inputStream, deflateParameters);
            default:
                try
                {
                    return new CompressorStreamFactory().createCompressorInputStream(comprFormat.toName(), inputStream);
                }
                catch (CompressorException e)
                {
                    throw new IOException(e);
                }
        }
    }
}
