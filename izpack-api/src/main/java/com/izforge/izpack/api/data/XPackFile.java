/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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

package com.izforge.izpack.api.data;


import com.izforge.izpack.api.data.binding.OsModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A {@link PackFile} that includes the file position in the installation media.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class XPackFile extends PackFile implements Comparable<XPackFile>
{
    private static final long serialVersionUID = 5875050264763504283L;

    /**
     * The absolute offset of the file in the archive.
     */
    private long position;

    /**
     * Constructs an <tt>XPackFile</tt>.
     *
     * @param baseDir  the base directory of the file
     * @param src      file which this PackFile describes
     * @param target   the path to install the file to
     * @param osList   OS constraints
     * @param override what to do when the file already exists
     * @throws IOException if the specified file does not exist.
     */
    public XPackFile(File baseDir, File src, String target, List<OsModel> osList, OverrideType override,
                     String overrideRenameTo, Blockable blockable)
            throws IOException
    {
        super(baseDir, src, target, osList, override, overrideRenameTo, blockable, null);
        this.position = 0;
    }

    /**
     * Constructs an <tt>XPackFile</tt> from an {@link PackFile}.
     *
     * @param file the pack file
     * @throws IOException if an I/O error occurred
     */
    public XPackFile(PackFile file) throws IOException
    {
        super(file.getFile(), file.getRelativeSourcePath(), file.getTargetPath(), file.osConstraints(),
              file.override(), file.overrideRenameTo(), file.blockable(), file.getAdditionals());
        this.position = 0;
        this.setCondition(file.getCondition());
    }

    /**
     * Returns the position of the file in the archive.
     *
     * @return the position
     */
    public long getArchiveFilePosition()
    {
        return position;
    }

    /**
     * Sets the position of the file in the archive.
     *
     * @param position the position
     */
    public void setArchiveFilePosition(long position)
    {
        this.position = position;
    }

    public int compareTo(XPackFile arg0)
    {
        return this.getTargetPath().compareTo(arg0.getTargetPath());
    }
}