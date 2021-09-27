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

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.util.os.FileQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;


/**
 * An unpacker for {@link Pack#loose loose} pack files.
 *
 * @author Tim Anderson
 */
public class LooseFileUnpacker extends FileUnpacker
{

    /**
     * The prompt to warn of missing files.
     */
    private final Prompt prompt;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(LooseFileUnpacker.class.getName());

    /**
     * Constructs a <tt>LooseFileUnpacker</tt>.
     *
     * @param cancellable determines if unpacking should be cancelled
     * @param queue       the file queue. May be {@code null}
     * @param prompt      the prompt to warn of missing files
     */
    public LooseFileUnpacker(Cancellable cancellable, FileQueue queue, Prompt prompt)
    {
        super(cancellable, queue);
        this.prompt = prompt;
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
    public void unpack(PackFile file, InputStream packInputStream, File target)
            throws IOException, InstallerException
    {
        // Old way of doing the job by using the (absolute) sourcepath.
        // Since this is very likely to fail and does not conform to the documentation prefer using relative
        // path's

        File resolvedFile = file.getFile();
        if (!resolvedFile.exists())
        {
            // If provided, try finding the file in 'looseFilesDir', otherwise
            // fallback to 'user.dir' which is likely (depends on launcher type)
            // the current directory of the executable or jar-file...

            final String looseFilesDir = System.getProperty("looseFilesDir", System.getProperty("user.dir"));
            final File parent = new File(looseFilesDir);
            resolvedFile = new File(parent, file.getRelativeSourcePath());
        }
        if (resolvedFile.exists())
        {
            InputStream stream = null;
            try
            {
                stream = new FileInputStream(resolvedFile);
                // may have a different length & last modified than we had at compile time, therefore we have to
                // build a new PackFile for the copy process...
                file = new PackFile(resolvedFile.getParentFile(), resolvedFile, file.getTargetPath(),
                                    file.osConstraints(), file.override(), file.overrideRenameTo(),
                                    file.blockable(), file.getAdditionals());

                copy(file, stream, target);
            }
            catch (IOException e)
            {
                logger.warning("Error when copying file " + resolvedFile + ": " + e);
            }
            finally
            {
                try
                {
                    if (stream != null)
                        stream.close();
                }
                catch(IOException e)
                {
                    logger.warning("Error when closing stream: " + e);
                }
            }
        }
        else
        {
            // file not found. Since this file was loosely bundled, continue with the installation.
            logger.warning("Could not find loosely bundled file: " + file.getRelativeSourcePath());
            if (prompt.confirm(Prompt.Type.WARNING, "File not found", "Could not find loosely bundled file: "
                    + file.getRelativeSourcePath(), Prompt.Options.OK_CANCEL) == Prompt.Option.OK)
            {
                throw new InstallerException("Installation cancelled");
            }
        }
    }
}
