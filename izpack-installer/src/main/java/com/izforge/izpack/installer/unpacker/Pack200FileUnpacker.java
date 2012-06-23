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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.FileQueue;


/**
 * A file unpacker for pack200 files.
 *
 * @author Tim Anderson
 */
class Pack200FileUnpacker extends FileUnpacker
{
    /**
     * The resources.
     */
    private final PackResources resources;

    /**
     * The unpacker.
     */
    private final Pack200.Unpacker unpacker;

    /**
     * Constructs a <tt>Pack200FileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param resources   the pack resources
     * @param unpacker    the unpacker
     * @param queue       the file queue. May be {@code null}
     */
    public Pack200FileUnpacker(Cancellable cancellable, PackResources resources, Pack200.Unpacker unpacker,
                               FileQueue queue)
    {
        super(cancellable, queue);
        this.resources = resources;
        this.unpacker = unpacker;
    }

    /**
     * Unpacks a pack file.
     *
     * @param file            the pack file meta-data
     * @param packInputStream the pack input stream
     * @param target          the target
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer exception
     */
    @Override
    public void unpack(PackFile file, ObjectInputStream packInputStream, File target)
            throws IOException, InstallerException
    {
        int key = packInputStream.readInt();
        InputStream in = null;
        OutputStream out = null;
        JarOutputStream jarOut = null;

        try
        {
            in = resources.getInputStream("packs/pack200-" + key);
            out = getTarget(file, target);
            jarOut = new JarOutputStream(out);
            unpacker.unpack(in, jarOut);
            jarOut.close();
        }
        finally
        {
            FileUtils.close(in);
            FileUtils.close(out);
            FileUtils.close(jarOut);
        }

        postCopy(file);
    }

}
