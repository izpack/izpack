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

import com.izforge.izpack.compiler.data.CompilerData;
import org.picocontainer.injectors.Provider;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

/**
 * Provides the Jar output stream  for the final installer jar
 *
 * @author Anthonin Bonnefoy
 */
public class JarOutputStreamProvider implements Provider
{
    public JarOutputStream provide(CompilerData compilerData)
    {
        try
        {
            final Path file = Paths.get(compilerData.getOutput());
            if (compilerData.isMkdirs())
            {
                Files.createDirectories(file.getParent());
            }
            final Set<String> entries = new HashSet<>();
            JarOutputStream jarOutputStream = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(file))) {
                @Override
                public void putNextEntry(ZipEntry ze) throws IOException {
                    if (entries.add(ze.getName())) {
                        super.putNextEntry(ze);
                        return;
                    }
                    throw new IOException("Duplicate entry: " + ze.getName());
                }
            };

            int level = compilerData.getComprLevel();
            if (level >= 0 && level < 10)
            {
                jarOutputStream.setLevel(level);
            }
            else
            {
                jarOutputStream.setLevel(Deflater.BEST_COMPRESSION);
            }
            return jarOutputStream;
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
