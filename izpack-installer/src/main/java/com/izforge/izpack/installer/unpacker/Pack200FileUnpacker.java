/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.os.FileQueue;
import org.apache.commons.compress.java.util.jar.Pack200;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarOutputStream;

/**
 * A file unpacker for pack200 files.
 *
 * @author Tim Anderson
 */
class Pack200FileUnpacker extends FileUnpacker
{

    /**
     * Constructs a <tt>Pack200FileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param resources   the pack resources
     * @param queue       the file queue. May be {@code null}
     */
    public Pack200FileUnpacker(Cancellable cancellable, PackResources resources, FileQueue queue)
    {
        super(cancellable, queue);
    }

    /**
     * Unpacks a pack packFile.
     *
     * @param packFile            the pack packFile meta-data
     * @param packInputStream the pack input stream
     * @param target          the target
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer exception
     */
    @Override
    public void unpack(PackFile packFile, InputStream packInputStream, File target)
            throws IOException, InstallerException
    {
        InputStream in = IOUtils.buffer(packInputStream);
        JarOutputStream jarOut = null;

        try
        {
            jarOut = new JarOutputStream(getTarget(packFile, target));
            Pack200.Unpacker unpacker = createPack200Unpacker(packFile);
            unpacker.unpack(in, jarOut);
        }
        finally
        {
            IOUtils.closeQuietly(jarOut);
            IOUtils.closeQuietly(in);
        }

        postCopy(packFile);
    }

    private Pack200.Unpacker createPack200Unpacker(PackFile packFile)
    {
        Pack200.Unpacker unpacker = Pack200.newUnpacker();
        Map<String, String> defaultUnpackerProperties = unpacker.properties();
        Map<String, String> localPackerProperties = packFile.getPack200Properties();
        if (localPackerProperties != null)
        {
            defaultUnpackerProperties.putAll(localPackerProperties);
        }
        return unpacker;
    }
}
