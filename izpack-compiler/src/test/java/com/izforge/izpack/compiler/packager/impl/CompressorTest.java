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

package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.data.PackCompression;
import com.izforge.izpack.compiler.data.CompilerData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Test compressor stream
 *
 * @author Anthonin Bonnefoy
 */
public class CompressorTest
{
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testBzip2Compression() throws IOException//, CompressorException
    {
        final File root = temporaryFolder.getRoot();
        String baseDir = root.toString();
        CompilerData data = new CompilerData(
                "",
                baseDir,
                baseDir + "/target/output.jar",
                false);
        data.setComprFormat(PackCompression.BZIP2.toName());
        final Path setupJar = root.toPath().resolve("setup.jar");
        JarOutputStream jarOutputStream = PackagerBase.getJarOutputStream(setupJar, data);
        ZipEntry zipEntry = new ZipEntry("test");
        zipEntry.setComment("bzip2");
        jarOutputStream.putNextEntry(zipEntry);
    }
}
