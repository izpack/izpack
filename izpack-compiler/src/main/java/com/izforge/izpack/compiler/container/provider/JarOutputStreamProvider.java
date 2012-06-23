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

import java.io.File;
import java.io.IOException;
import java.util.zip.Deflater;

import org.picocontainer.injectors.Provider;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.stream.JarOutputStream;

/**
 * OutputStream Provider.
 *
 * @author Anthonin Bonnefoy
 */
public class JarOutputStreamProvider implements Provider
{

    public JarOutputStream provide(CompilerData compilerData) throws IOException
    {
        JarOutputStream jarOutputStream;
        File file = new File(compilerData.getOutput());
        if (file.exists())
        {
            file.delete();
        }
        if (compilerData.isMkdirs())
        {
            file.getParentFile().mkdirs();
        }
        jarOutputStream = new JarOutputStream(file);
        int level = compilerData.getComprLevel();
        if (level >= 0 && level < 10)
        {
            jarOutputStream.setLevel(level);
        }
        else
        {
            jarOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        }
        jarOutputStream.setPreventClose(true); // Needed at using FilterOutputStreams which calls close
        return jarOutputStream;
    }
}
