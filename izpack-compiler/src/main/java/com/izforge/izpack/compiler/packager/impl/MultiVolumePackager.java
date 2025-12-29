/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.XPackFile;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.core.io.FileSpanningOutputStream;
import com.izforge.izpack.api.data.PackInfo;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;


/**
 * An {@link com.izforge.izpack.compiler.packager.IPackager} that packs everything into multiple volumes.
 * <p/>
 * <p/>
 * Here's an example how to specify an installer which will create multiple volumes. In this example the volumes shall
 * be CDs with 650 megabytes. There will be an additional free space of 150 megabytes on the first volume.
 * <br/>
 * This will result in the creation of an installer.jar and multiple installer.pak* files.
 * The installer.jar plus installer.pak plus the additional resources have to be copied on the first volume,
 * each installer.pak.&lt;number&gt; on several volumes.
 * <pre>
 * {@code
 * <packaging>
 *       <packager class="com.izforge.izpack.compiler.packager.impl.MultiVolumePackager">
 *           <!-- 650 MB volumes, 150 MB space on the first volume -->
 *           <options volumesize="681574400" firstvolumefreespace="157286400"/>
 *       </packager>
 *       <unpacker class="com.izforge.izpack.installer.multiunpacker.MultiVolumeUnpacker" />
 * </packaging>
 * }
 * </pre>
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @author Tim Anderson
 */
public class MultiVolumePackager extends PackagerBase
{

    /**
     * The maximum size of the first volume, in bytes.
     */
    private long maxFirstVolumeSize = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;

    /**
     * The maximum volume size for subsequent volumes, in bytes.
     */
    private long maxVolumeSize = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;

    /**
     * The configuration attribute to specify the volume size.
     */
    private static final String VOLUME_SIZE = "volumesize";

    /**
     * The configuration attribute to specify the first volume free space size.
     */
    private static final String FIRST_VOLUME_FREE_SPACE = "firstvolumefreespace";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumePackager.class.getName());


    /**
     * Constructs a <tt>MultiVolumePackager</tt>.
     *
     * @param properties        the properties
     * @param listener          the packager listener
     * @param mergeManager      the merge manager
     * @param pathResolver      the path resolver
     * @param mergeableResolver the mergeable resolver
     * @param compilerData      the compiler data
     */
    public MultiVolumePackager(Properties properties, PackagerListener listener, MergeManager mergeManager,
                               CompilerPathResolver pathResolver, MergeableResolver mergeableResolver,
                               CompilerData compilerData, RulesEngine rulesEngine)
    {
        super(properties, listener, mergeManager, pathResolver, mergeableResolver, compilerData, rulesEngine);
    }

    /**
     * Sets the maximum size of the first volume.
     * <p/>
     * Defaults to {@link FileSpanningOutputStream#DEFAULT_VOLUME_SIZE}.
     *
     * @param size the maximum size of the first volume, in bytes
     */
    public void setMaxFirstVolumeSize(long size)
    {
        maxFirstVolumeSize = size;
    }

    /**
     * Sets the maximum volume size for all volumes bar the first.
     * <p/>
     * Defaults to {@link FileSpanningOutputStream#DEFAULT_VOLUME_SIZE}.
     *
     * @param size the maximum volume size, in bytes
     */
    public void setMaxVolumeSize(long size)
    {
        maxVolumeSize = size;
    }

    /**
     * Parses configuration information.
     * <p/>
     * This determines the {@link #setMaxFirstVolumeSize(long) maximum size of the first volume}, and
     * {@link #setMaxVolumeSize(long) maximum size of subsequent volumes} from the <em>firstvolumefreespace</em>
     * and <em>volumesize</em> attributes.
     *
     * @param data the xml-element packaging from the install.xml
     */
    public void addConfigurationInformation(IXMLElement data)
    {
        if (data != null)
        {
            long freeSpace = Long.valueOf(data.getAttribute(FIRST_VOLUME_FREE_SPACE, "0"));
            long size = Long.valueOf(data.getAttribute(VOLUME_SIZE, Long.toString(maxVolumeSize)));

            setMaxFirstVolumeSize(size - freeSpace);
            setMaxVolumeSize(size);
        }
    }

    /**
     * Writes packs to one or more <em>.pak</em> volumes.
     * <p/>
     * Pack meta-data is written to the installer jar.
     *
     * @throws IOException for any I/O error
     */
    @Override
    protected void writePacks(JarOutputStream installerJar) throws IOException
    {
        String classname = getClass().getSimpleName();

        // propagate the configuration to the variables, for debugging purposes
        getVariables().setProperty(classname + "." + FIRST_VOLUME_FREE_SPACE, Long.toString(maxFirstVolumeSize));
        getVariables().setProperty(classname + "." + VOLUME_SIZE, Long.toString(maxVolumeSize));

        List<PackInfo> packs = getPacksList();
        final int count = packs.size();
        sendMsg("Writing " + count + " Pack" + (count > 1 ? "s" : "") + " into installer");
        logger.fine("Writing " + count + " Pack" + (count > 1 ? "s" : "") + " into installer");
        logger.fine("First volume size: " + maxFirstVolumeSize);
        logger.fine("Subsequent volume size: " + maxVolumeSize);

        File volume = new File(getInfo().getInstallerBase() + ".pak").getAbsoluteFile();
        int volumes = writePacks(installerJar, packs, volume);

        // write metadata for reading in volumes
        logger.fine("Written " + volumes + " volumes");

        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        out.writeInt(volumes);
        out.writeUTF(volume.getName());
        out.flush();
        installerJar.closeEntry();

        // Now that we know sizes, write pack metadata to primary jar.
        installerJar.putNextEntry(new ZipEntry(PACKSINFO_RESOURCE_PATH));
        out = new ObjectOutputStream(installerJar);
        out.writeObject(packs);
        out.flush();
        installerJar.closeEntry();
    }

    /**
     * Writes packs to one or more <em>.pak</em> volumes.
     *
     * @param packs  the packs to write
     * @param volume the first volume
     * @return the no. of volumes written
     */
    private int writePacks(JarOutputStream installerJar, List<PackInfo> packs, File volume) throws IOException
    {
        FileSpanningOutputStream volumes = new FileSpanningOutputStream(volume, maxFirstVolumeSize, maxVolumeSize);
        File targetDir = volume.getParentFile();
        if (targetDir == null)
        {
            throw new IOException("Cannot determine parent directory of " + volume);
        }

        for (PackInfo packInfo : packs)
        {
            writePack(installerJar, packInfo, volumes, targetDir);
        }

        volumes.flush();
        volumes.close();
        return volumes.getVolumes();
    }

    /**
     * Writes a pack.
     * <p/>
     * Pack information is written to the installer jar, while the actual files are written to the volumes.
     *
     * @param packInfo  the pack information
     * @param volumes   the volumes
     * @param targetDir the target directory for loosefiles
     * @throws IOException for any I/O error
     */
    private void writePack(JarOutputStream installerJar, PackInfo packInfo, FileSpanningOutputStream volumes, File targetDir) throws IOException
    {
        Pack pack = packInfo.getPack();
        pack.setFileSize(0);

        String name = pack.getName();
        sendMsg("Writing Pack: " + name, PackagerListener.MSG_VERBOSE);
        logger.fine("Writing Pack: " + name);
        ZipEntry entry = new ZipEntry(RESOURCES_PATH + "packs/pack-" + name);

        installerJar.putNextEntry(entry);
        ObjectOutputStream packStream = new ObjectOutputStream(installerJar);

        writePackFiles(packInfo, volumes, pack, packStream, targetDir);

        // Cleanup
        packStream.flush();
    }

    /**
     * Writes the pack files.
     * <p/>
     * The file data is written to <tt>volumes</tt>, whilst the meta-data is written to <tt>packStream</tt>.
     *
     * @param packInfo   the pack information
     * @param volumes    the volumes to write to
     * @param pack       the pack
     * @param packStream the stream to write the pack meta-data to
     * @param targetDir  the target directory for loose files
     * @throws IOException for any I/O error
     */
    private void writePackFiles(PackInfo packInfo, FileSpanningOutputStream volumes, Pack pack,
                                ObjectOutputStream packStream, File targetDir) throws IOException
    {
        Set<PackFile> files = packInfo.getPackFiles();
        Map<PackFile, File> xFiles = new LinkedHashMap<PackFile, File>();

        for (PackFile packfile : files)
        {
            XPackFile pf = new XPackFile(packfile);
            File file = packInfo.getFile(packfile);
            logger.fine("Next file: " + file.getAbsolutePath());

            if (!pf.isDirectory())
            {
                if (!pack.isLoose())
                {
                    writePackFile(file, volumes, pf);
                }
                else
                {
                    // just copy the file to the target directory
                    FileUtils.copyFile(file, new File(targetDir, pf.getRelativeSourcePath()));
                }
            }

            xFiles.put(pf, file);

            // even if not written, it counts towards pack size
            pack.addFileSize(pf.length());
        }

        // Replace the PackFile objects by the corresponding XPackFile objects to be written to the packs.info resource
        Map<PackFile, File> packFilesMap = packInfo.getPackFilesMap();
        packFilesMap.clear();
        packFilesMap.putAll(xFiles);

        if (pack.getFileSize() > pack.getSize())
        {
            pack.setSize(pack.getFileSize());
        }
    }

    /**
     * Writes a pack file to the volumes.
     *
     * @param file     the file to write
     * @param volumes  the volumes
     * @param packFile the pack file
     * @throws IOException for any I/O error
     */
    private void writePackFile(File file, FileSpanningOutputStream volumes, XPackFile packFile) throws IOException
    {
        long beforePosition = volumes.getFilePointer();
        packFile.setArchiveFilePosition(beforePosition);

        // write the file to the volumes
        int volumeCount = volumes.getVolumes();

        FileInputStream in = FileUtils.openInputStream(file);
        try
        {
            long bytesWritten = IOUtils.copyLarge(in, volumes);
            long afterPosition = volumes.getFilePointer();
            logger.fine("File (" + packFile.getFile() + ") " + beforePosition + " <-> " + afterPosition);

            if (volumes.getFilePointer() != (beforePosition + bytesWritten))
            {
                logger.fine("file: " + file.getName());
                logger.fine("(Filepos/BytesWritten/ExpectedNewFilePos/NewFilePointer) ("
                        + beforePosition + "/" + bytesWritten + "/" + (beforePosition + bytesWritten)
                        + "/" + volumes.getFilePointer() + ")");
                logger.fine("Volumes (before/after) (" + volumeCount + "/" + volumes.getVolumes() + ")");
                throw new IOException("Error new file pointer is illegal");
            }

            if (bytesWritten != packFile.length())
            {
                throw new IOException("File size mismatch when reading " + file);
            }
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

}