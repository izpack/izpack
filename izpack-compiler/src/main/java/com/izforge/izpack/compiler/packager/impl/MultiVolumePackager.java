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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipEntry;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.XPackFile;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.resolve.CompilerPathResolver;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.core.io.FileSpanningOutputStream;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.IoHelper;


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
     * @param compressor        the pack compressor
     * @param compilerData      the compiler data
     */
    public MultiVolumePackager(Properties properties, PackagerListener listener, JarOutputStream installerJar,
                               MergeManager mergeManager, CompilerPathResolver pathResolver,
                               MergeableResolver mergeableResolver, PackCompressor compressor,
                               CompilerData compilerData)
    {
        super(properties, listener, installerJar, mergeManager, pathResolver, mergeableResolver, compressor,
              compilerData);
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
    protected void writePacks() throws IOException
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
        int volumes = writePacks(packs, volume);

        // write metadata for reading in volumes
        logger.fine("Written " + volumes + " volumes");

        JarOutputStream installerJar = getInstallerJar();
        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        out.writeInt(volumes);
        out.writeUTF(volume.getName());
        out.flush();
        installerJar.closeEntry();

        // Now that we know sizes, write pack metadata to primary jar.
        installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "packs.info"));
        out = new ObjectOutputStream(installerJar);
        out.writeInt(count);

        for (PackInfo pack : packs)
        {
            out.writeObject(pack.getPack());
        }
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
    private int writePacks(List<PackInfo> packs, File volume) throws IOException
    {
        FileSpanningOutputStream volumes = new FileSpanningOutputStream(volume, maxFirstVolumeSize, maxVolumeSize);
        File targetDir = volume.getParentFile();
        if (targetDir == null)
        {
            throw new IOException("Cannot determine parent directory of " + volume);
        }
        for (PackInfo packInfo : packs)
        {
            writePack(packInfo, volumes, targetDir);
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
    private void writePack(PackInfo packInfo, FileSpanningOutputStream volumes, File targetDir) throws IOException
    {
        Pack pack = packInfo.getPack();
        pack.setSize(0);

        String name = pack.getName();
        sendMsg("Writing Pack: " + name, PackagerListener.MSG_VERBOSE);
        logger.fine("Writing Pack: " + name);
        ZipEntry entry = new ZipEntry(RESOURCES_PATH + "packs/pack-" + name);

        JarOutputStream installerJar = getInstallerJar();
        installerJar.putNextEntry(entry);
        ObjectOutputStream packStream = new ObjectOutputStream(installerJar);

        writePackFiles(packInfo, volumes, pack, packStream, targetDir);

        // Write out information about parsable files
        packStream.writeInt(packInfo.getParsables().size());
        for (ParsableFile file : packInfo.getParsables())
        {
            packStream.writeObject(file);
        }

        // Write out information about executable files
        packStream.writeInt(packInfo.getExecutables().size());
        for (ExecutableFile file : packInfo.getExecutables())
        {
            packStream.writeObject(file);
        }

        // Write out information about update check files
        packStream.writeInt(packInfo.getUpdateChecks().size());
        for (UpdateCheck check : packInfo.getUpdateChecks())
        {
            packStream.writeObject(check);
        }

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
        // write the file meta-data
        Set<PackFile> files = packInfo.getPackFiles();
        packStream.writeInt(files.size());

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

            // write pack file meta-data
            packStream.writeObject(pf);
            packStream.flush(); // make sure it is written
            // even if not written, it counts towards pack size
            pack.addSize(pf.length());
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

        FileInputStream in = new FileInputStream(file);
        long bytesWritten = IoHelper.copyStream(in, volumes);
        long afterPosition = volumes.getFilePointer();
        logger.fine("File (" + packFile.sourcePath + ") " + beforePosition + " <-> " + afterPosition);

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
        in.close();
    }

}