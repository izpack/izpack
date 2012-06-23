/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.compiler.container.provider;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.tools.zip.ZipEntry;
import org.picocontainer.injectors.Provider;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class CompressedOutputStreamProvider implements Provider
{

    public OutputStream provide(CompilerData compilerData, JarOutputStream jarOutputStream) throws CompressorException, IOException
    {
        OutputStream outputStream = jarOutputStream;
        String comprFormat = compilerData.getComprFormat();
        if (comprFormat.equals("bzip2"))
        {
            ZipEntry entry = new ZipEntry("bzip2");
            entry.setMethod(ZipEntry.STORED);
            entry.setComment("bzip2");
            // We must set the entry before we get the compressed stream
            // because some writes initialize data (e.g. bzip2).
            jarOutputStream.putNextEntry(entry);
            jarOutputStream.flush(); // flush before we start counting

            outputStream = new CompressorStreamFactory().createCompressorOutputStream("bzip2", jarOutputStream);
            jarOutputStream.closeEntry();
        }
        return outputStream;
    }
}
