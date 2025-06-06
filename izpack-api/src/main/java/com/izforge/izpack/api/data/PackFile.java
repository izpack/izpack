/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
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
import com.izforge.izpack.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encloses information about a packed file. This class abstracts the way file data is stored to
 * package.
 *
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class PackFile implements Serializable
{
    private static final long serialVersionUID = -834377078706854909L;

    @SuppressWarnings("unused")
    private static AtomicInteger nextInstanceId = new AtomicInteger(0);
    private final int instanceId;

    /**
     * The Path of the file relative to the given (compiletime's) basedirectory.
     * Can be resolved while installing with either current working directory or directory of "installer.jar".
     */
    private String relativePath = null;

    /**
     * The full path name of the target file
     */
    private String targetPath = null;

    /**
     * The target operating system constraints of this file
     */
    private List<OsModel> osConstraints = null;

    /**
     * The packed file
     */
    private File packedFile;

    /**
     * The length of the file in bytes
     */
    private long length = 0;

    /**
     * The size of the file used to calculate the pack size.
     * This is usually the compressed size if the file is packed with a compression algorithm.
     */
    private long size = 0;

    /**
     * The last-modification time of the file.
     */
    private long mtime = -1;

    /**
     * True if file is a directory (length should be 0 or ignored)
     */
    private boolean isDirectory = false;

    /**
     * Whether or not this file is going to override any existing ones
     */
    private OverrideType override;

    /**
     * Glob mapper expression for mapping the resulting file name if overriding is allowed and the
     * file does already exist. This is similar like the Ant globmapper target expression when
     * mapping from "*".
     */
    private String overrideRenameTo;

    /**
     * Whether or not this file might be blocked by the operating system
     */
    private Blockable blockable = Blockable.BLOCKABLE_NONE;

    /**
     * Additional attributes or any else for customisation
     */
    private Map additionals = null;

    private String streamResourceName;
    private long streamOffset = -1;

    private PackFile linkedPackFile;

    /**
     * condition for this packfile
     */
    private String condition = null;

    /**
     * Constructs and initializes from a source file.
     *
     * @param baseDir  the baseDirectory of the Fileselection/compilation or null
     * @param src      file which this PackFile describes
     * @param target   the path to install the file to
     * @param osList   OS constraints
     * @param override what to do when the file already exists
     * @throws FileNotFoundException if the specified file does not exist.
     */
    public PackFile(File baseDir, File src, String target, List<OsModel> osList, OverrideType override,
                    String overrideRenameTo, Blockable blockable)
    throws IOException
    {
        this(src, FileUtil.getRelativeFileName(src, baseDir), target, osList, override, overrideRenameTo, blockable,
                null);
    }

    /**
     * Constructs and initializes from a source file.
     *
     * @param src                file which this PackFile describes
     * @param relativeSourcePath the path relative to the compiletime's basedirectory, use computeRelativePathFrom(File, File) to compute this.
     * @param target             the path to install the file to
     * @param osList             OS constraints
     * @param override           what to do when the file already exists
     * @param additionals        additional attributes
     * @throws FileNotFoundException if the specified file does not exist.
     */
    public PackFile(File src, String relativeSourcePath, String target, List<OsModel> osList, OverrideType override,
                    String overrideRenameTo, Blockable blockable, Map additionals)
            throws FileNotFoundException
    {
        instanceId = nextInstanceId.getAndIncrement();
        if (!src.exists()) // allows cleaner client co
        {
            throw new FileNotFoundException("No such file: " + src);
        }

        if ('/' != File.separatorChar)
        {
            target = target.replace(File.separatorChar, '/');
        }
        if (target.endsWith("/"))
        {
            target = target.substring(0, target.length() - 1);
        }

        this.packedFile = src;
        this.relativePath = (relativeSourcePath != null) ? relativeSourcePath.replace(File.separatorChar, '/') : null;

        this.targetPath = target.replace(File.separatorChar, '/');
        this.osConstraints = osList;
        this.override = override;
        this.overrideRenameTo = overrideRenameTo;
        this.blockable = blockable;

        this.mtime = src.lastModified();
        this.isDirectory = src.isDirectory();
        if (!this.isDirectory())
        {
            this.length = src.length();
            this.size = this.length;
        }
        this.additionals = additionals;

        // File.length is undefined for directories - we don't add any data, so don't skip
        // any please!
        if (isDirectory)
        {
            length = 0;
        }
    }

    /**
     * Constructs and initializes from a source file.
     *
     * @param baseDir     The Base directory that is used to search for the files. This is used to build the relative path's
     * @param src         file which this PackFile describes
     * @param target      the path to install the file to
     * @param osList      OS constraints
     * @param override    what to do when the file already exists
     * @param additionals additional attributes
     * @throws FileNotFoundException if the specified file does not exist.
     */
    public PackFile(File baseDir, File src, String target, List<OsModel> osList, OverrideType override,
                    String overrideRenameTo, Blockable blockable, Map additionals)
    throws IOException
    {
        this(src, FileUtil.getRelativeFileName(src, baseDir), target, osList, override, overrideRenameTo, blockable,
                additionals);
    }

    /**
     * Get the unique ID compiled into this object
     * @return the unique ID
     */
    public int getId()
    {
        return instanceId;
    }

    public PackFile getLinkedPackFile()
    {
        return linkedPackFile;
    }

    public void setLinkedPackFile(PackFile linkedPackFile)
    {
        // only accept non null value
        this.linkedPackFile = Objects.requireNonNull(linkedPackFile);
        // update packed size and offset in order for unpacking to work correctly
        size = linkedPackFile.size;
        streamOffset = linkedPackFile.streamOffset;
    }

    public String getStreamResourceName()
    {
        return streamResourceName;
    }

    public void setStreamResourceName(String streamResourceName)
    {
        this.streamResourceName = streamResourceName;
    }

    public long getStreamOffset()
    {
        return streamOffset;
    }

    public void setStreamOffset(long offset)
    {
        this.streamOffset = offset;
    }

    /**
     * The target operating system constraints of this file
     */
    public final List<OsModel> osConstraints()
    {
        return osConstraints;
    }

    /**
     * The packed file object
     */
    public final File getFile()
    {
        return packedFile;
    }

    /**
     * The length of the file in bytes
     */
    public final long length()
    {
        return length;
    }

    /**
     * The size of the file in bytes (is the same as the length if it is not a loose pack)
     */
    public final long size()
    {
        return size;
    }

    /**
     * Override the acutal packed file size (after compressing)
     * @param size the size in bytes
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    /**
     * The last-modification time of the file.
     */
    public final long lastModified()
    {
        return mtime;
    }

    /**
     * Whether or not this file is going to override any existing ones
     */
    public final OverrideType override()
    {
        return override;
    }

    /**
     * Returns globmapper expression for mapping the resulting file name if overriding is allowed
     * and the file does already exist. This is similar like the Ant globmapper target expression
     * when mapping from "*".
     *
     * @return mapper pattern
     */
    public final String overrideRenameTo()
    {
        return overrideRenameTo;
    }

    /**
     * Whether or not this file might be blocked during installation/uninstallation
     */
    public final Blockable blockable()
    {
        return blockable;
    }

    public final boolean isDirectory()
    {
        return isDirectory;
    }

    public final boolean isBackReference()
    {
        return (linkedPackFile != null);
    }

    /**
     * The full path name of the target file, using '/' as fileseparator.
     */
    public final String getTargetPath()
    {
        return targetPath;
    }

    /**
     * The Path of the file relative to the given (compiletime's) basedirectory.
     * Can be resolved while installing with either current working directory or directory of "installer.jar"
     */
    public String getRelativeSourcePath()
    {
        return relativePath;
    }

    /**
     * Returns the additionals map.
     *
     * @return additionals
     */
    public Map getAdditionals()
    {
        return additionals;
    }


    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.condition;
    }


    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public boolean hasCondition()
    {
        return this.condition != null;
    }

    public void setLoosePackInfo(boolean loose)
    {
        if (loose)
        {
            // file is part of a loose pack
            length = 0;
        }
    }

    @Override
    public String toString()
    {
        return String.format("%s (length=%s, size=%s, streamOffset=%s, backReference=%s)",
                packedFile, length, size, streamOffset, (linkedPackFile != null));
    }
}
