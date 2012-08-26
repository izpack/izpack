/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Pack200;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.compiler.compressor.PackCompressor;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.compiler.stream.JarOutputStream;
import com.izforge.izpack.core.io.ByteCountingOutputStream;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.PackInfo;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import com.izforge.izpack.util.IoHelper;

/**
 * The packager class. The packager is used by the compiler to put files into an installer, and
 * create the actual installer files.
 *
 * @author Julien Ponge
 * @author Chadwick McHenry
 */
public class Packager extends PackagerBase
{

    /**
     * Decoration of the installer jar stream.
     * May be compressed or not depending on the compiler data.
     */
    private final OutputStream outputStream;


    /**
     * Constructs a <tt>Packager</tt>.
     *
     * @param properties        the properties
     * @param listener          the packager listener
     * @param jarOutputStream   the installer jar output stream
     * @param compressor        the pack compressor
     * @param outputStream      decoration of the installer jar stream. May be compressed or not depending on the
     *                          compiler data.
     * @param mergeManager      the merge manager
     * @param pathResolver      the path resolver
     * @param mergeableResolver the mergeable resolver
     * @param compilerData      the compiler data
     */
    public Packager(Properties properties, PackagerListener listener, JarOutputStream jarOutputStream,
                    PackCompressor compressor, OutputStream outputStream, MergeManager mergeManager,
                    CompilerPathResolver pathResolver, MergeableResolver mergeableResolver, CompilerData compilerData)
    {
        super(properties, listener, jarOutputStream, mergeManager, pathResolver, mergeableResolver, compressor,
              compilerData);
        this.outputStream = outputStream;
    }

    /**
     * Write packs to the installer jar, or each to a separate jar.
     *
     * @throws IOException for any I/O error
     */
    @Override
    protected void writePacks() throws IOException
    {
        List<PackInfo> packs = getPacksList();
        final int num = packs.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");

        // Map to remember pack number and bytes offsets of back references
        Map<File, Object[]> storedFiles = new HashMap<File, Object[]>();

        // Pack200 files map
        Map<Integer, File> pack200Map = new HashMap<Integer, File>();
        int pack200Counter = 0;

        // Force UTF-8 encoding in order to have proper ZipEntry names.
        JarOutputStream installerJar = getInstallerJar();
        installerJar.setEncoding("utf-8");

        // First write the serialized files and file metadata data for each pack
        // while counting bytes.

        int packNumber = 0;
        IXMLElement root = new XMLElementImpl("packs");

        for (PackInfo packInfo : packs)
        {
            Pack pack = packInfo.getPack();
            pack.setFileSize(0);

            // create a pack specific jar if required
            // REFACTOR : Repare web installer
            // REFACTOR : Use a mergeManager for each packages that will be added to the main merger

//            if (packJarsSeparate) {
            // See installer.Unpacker#getPackAsStream for the counterpart
//                String name = baseFile.getName() + ".pack-" + pack.id + ".jar";
//                packStream = IoHelper.getJarOutputStream(name, baseFile.getParentFile());
//            }

            sendMsg("Writing Pack " + packNumber + ": " + pack.getName(), PackagerListener.MSG_VERBOSE);

            // Retrieve the correct output stream
            org.apache.tools.zip.ZipEntry entry = new org.apache.tools.zip.ZipEntry(
                    RESOURCES_PATH + "packs/pack-" + pack.getName());
            installerJar.putNextEntry(entry);
            installerJar.flush(); // flush before we start counting

            ByteCountingOutputStream dos = new ByteCountingOutputStream(outputStream);
            ObjectOutputStream objOut = new ObjectOutputStream(dos);

            // We write the actual pack files
            objOut.writeInt(packInfo.getPackFiles().size());

            for (PackFile packFile : packInfo.getPackFiles())
            {
                boolean addFile = !pack.isLoose();
                boolean pack200 = false;
                File file = packInfo.getFile(packFile);

                if (file.getName().toLowerCase().endsWith(".jar") && getInfo().isPack200Compression()
                        && isNotSignedJar(file))
                {
                    packFile.setPack200Jar(true);
                    pack200 = true;
                }

                // use a back reference if file was in previous pack, and in
                // same jar
                Object[] info = storedFiles.get(file);
                if (info != null && !packSeparateJars())
                {
                    packFile.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                    addFile = false;
                }

                objOut.writeObject(packFile); // base info

                if (addFile && !packFile.isDirectory())
                {
                    long pos = dos.getByteCount(); // get the position

                    if (pack200)
                    {
                        /*
                         * Warning!
                         *
                         * Pack200 archives must be stored in separated streams, as the Pack200 unpacker
                         * reads the entire stream...
                         *
                         * See http://java.sun.com/javase/6/docs/api/java/util/jar/Pack200.Unpacker.html
                         */
                        pack200Map.put(pack200Counter, file);
                        objOut.writeInt(pack200Counter);
                        pack200Counter = pack200Counter + 1;
                    }
                    else
                    {
                        FileInputStream inStream = new FileInputStream(file);
                        long bytesWritten = IoHelper.copyStream(inStream, objOut);
                        inStream.close();
                        if (bytesWritten != packFile.length())
                        {
                            throw new IOException("File size mismatch when reading " + file);
                        }
                    }

                    storedFiles.put(file, new Object[]{pack.getName(), pos}); // TODO - see IZPACK-799
                }

                // even if not written, it counts towards pack size
                pack.addFileSize(packFile.size());
            }

            if (pack.getFileSize() > pack.getSize())
            {
                pack.setSize(pack.getFileSize());
            }

            // Write out information about parsable files
            objOut.writeInt(packInfo.getParsables().size());

            for (ParsableFile parsableFile : packInfo.getParsables())
            {
                objOut.writeObject(parsableFile);
            }

            // Write out information about executable files
            objOut.writeInt(packInfo.getExecutables().size());
            for (ExecutableFile executableFile : packInfo.getExecutables())
            {
                objOut.writeObject(executableFile);
            }

            // Write out information about updatecheck files
            objOut.writeInt(packInfo.getUpdateChecks().size());
            for (UpdateCheck updateCheck : packInfo.getUpdateChecks())
            {
                objOut.writeObject(updateCheck);
            }

            // Cleanup
            objOut.flush();
            if (!getCompressor().useStandardCompression())
            {
                outputStream.close();
            }

            installerJar.closeEntry();

            // close pack specific jar if required
            if (packSeparateJars())
            {
                installerJar.closeAlways();
            }

            IXMLElement child = new XMLElementImpl("pack", root);
            child.setAttribute("name", pack.getName());
            child.setAttribute("size", Long.toString(pack.getSize()));
            child.setAttribute("fileSize", Long.toString(pack.getFileSize()));
            if (pack.getLangPackId() != null)
            {
                child.setAttribute("id", pack.getLangPackId());
            }
            root.addChild(child);

            packNumber++;
        }

        // Now that we know sizes, write pack metadata to primary jar.
        installerJar.putNextEntry(new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + "packs.info"));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        out.writeInt(packs.size());

        for (PackInfo packInfo : packs)
        {
            out.writeObject(packInfo.getPack());
        }
        out.flush();
        installerJar.closeEntry();

        // Pack200 files
        Pack200.Packer packer = createAgressivePack200Packer();
        for (Integer key : pack200Map.keySet())
        {
            File file = pack200Map.get(key);
            installerJar.putNextEntry(new org.apache.tools.zip.ZipEntry(RESOURCES_PATH + "packs/pack200-" + key));
            JarFile jar = new JarFile(file);
            packer.pack(jar, installerJar);
            jar.close();
            installerJar.closeEntry();
        }
    }

    private Pack200.Packer createAgressivePack200Packer()
    {
        Pack200.Packer packer = Pack200.newPacker();
        Map<String, String> packerProperties = packer.properties();
        packerProperties.put(Pack200.Packer.EFFORT, "9");
        packerProperties.put(Pack200.Packer.SEGMENT_LIMIT, "-1");
        packerProperties.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.FALSE);
        packerProperties.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.FALSE);
        packerProperties.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.LATEST);
        packerProperties.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "LineNumberTable", Pack200.Packer.STRIP);
        packerProperties.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "LocalVariableTable", Pack200.Packer.STRIP);
        packerProperties.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "SourceFile", Pack200.Packer.STRIP);
        return packer;
    }

    private boolean isNotSignedJar(File file) throws IOException
    {
        JarFile jar = new JarFile(file);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith("META-INF") && entry.getName().endsWith(".SF"))
            {
                jar.close();
                return false;
            }
        }
        jar.close();
        return true;
    }

    /**
     * ********************************************************************************************
     * Stream utilites for creation of the installer.
     * ********************************************************************************************
     */

    /* (non-Javadoc)
    * @see com.izforge.izpack.compiler.packager.IPackager#addConfigurationInformation(com.izforge.izpack.api.adaptator.IXMLElement)
    */
    @Override
    public void addConfigurationInformation(IXMLElement data)
    {
        // TODO Auto-generated method stub

    }
}
