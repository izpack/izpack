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

package com.izforge.izpack.installer.multiunpacker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.List;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.io.FileSpanningInputStream;
import com.izforge.izpack.core.io.VolumeLocator;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.unpacker.Cancellable;
import com.izforge.izpack.installer.unpacker.FileQueueFactory;
import com.izforge.izpack.installer.unpacker.FileUnpacker;
import com.izforge.izpack.installer.unpacker.LooseFileUnpacker;
import com.izforge.izpack.installer.unpacker.PackResources;
import com.izforge.izpack.installer.unpacker.UnpackerBase;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.os.FileQueue;


/**
 * Unpacker class for a multi volume installation.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 * @author Tim Anderson
 */
public class MultiVolumeUnpacker extends UnpackerBase
{

    /**
     * The volume locator.
     */
    private VolumeLocator locator;

    /**
     * The pack data volumes stream.
     */
    private FileSpanningInputStream volumes;

    /**
     * Volume meta-data resource name.
     */
    static final String VOLUMES_INFO = "volumes.info";

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(MultiVolumeUnpacker.class.getName());

    /**
     * Constructs a <tt>MultiVolumeUnpacker</tt>.
     *
     * @param installData         the installation data
     * @param resources           the pack resources
     * @param rules               the rules engine
     * @param variableSubstitutor the variable substituter
     * @param uninstallData       the uninstallation data
     * @param queue               the queue
     * @param housekeeper         the housekeeper
     * @param listeners           the listeners
     * @param prompt              the prompt
     * @param locator             the multi-volume locator
     * @param matcher             the platform-model matcher
     */
    public MultiVolumeUnpacker(InstallData installData, PackResources resources, RulesEngine rules,
                               VariableSubstitutor variableSubstitutor, UninstallData uninstallData,
                               FileQueueFactory queue, Housekeeper housekeeper, InstallerListeners listeners,
                               Prompt prompt, VolumeLocator locator, PlatformModelMatcher matcher)
    {
        super(installData, resources, rules, variableSubstitutor, uninstallData, queue, housekeeper, listeners,
              prompt, matcher);
        this.locator = locator;
    }

    /**
     * Invoked prior to unpacking.
     * <p/>
     * This notifies the {@link #getProgressListener listener}, and any registered {@link InstallerListener listeners}.
     *
     * @param packs the packs to unpack
     * @throws IzPackException for any error
     */
    @Override
    protected void preUnpack(List<Pack> packs)
    {
        super.preUnpack(packs);

        InputStream in = null;
        ObjectInputStream objectIn = null;
        try
        {
            // get volume metadata
            in = getResources().getInputStream(VOLUMES_INFO);
            objectIn = new ObjectInputStream(in);
            int volumeCount = objectIn.readInt();
            String volumeName = objectIn.readUTF();
            logger.fine("Reading from " + volumeCount + " volumes with basename " + volumeName + " ");

            String mediaPath = getInstallData().getMediaPath();
            if ((mediaPath == null) || (mediaPath.length() == 0))
            {
                mediaPath = getDefaultMediaPath();
            }
            logger.fine("Using mediaDirectory = " + mediaPath);
            File volume = new File(mediaPath, volumeName);
            if (!volume.exists())
            {
                volume = locator.getVolume(volume.getAbsolutePath(), false);
            }
            volumes = new FileSpanningInputStream(volume, volumeCount);
            volumes.setLocator(locator);
        }
        catch (IOException exception)
        {
            throw new InstallerException(exception);
        }
        finally
        {
            FileUtils.close(in);
            FileUtils.close(objectIn);
        }
    }

    /**
     * Creates an unpacker to unpack a pack file.
     *
     * @param file        the pack file to unpack
     * @param pack        the parent pack
     * @param queue       the file queue. May be {@code null}
     * @param cancellable determines if the unpacker should be cancelled
     * @return the unpacker
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer error
     */
    @Override
    protected FileUnpacker createFileUnpacker(PackFile file, Pack pack, FileQueue queue, Cancellable cancellable)
            throws IOException, InstallerException
    {
        FileUnpacker unpacker;
        if (pack.isLoose())
        {
            unpacker = new LooseFileUnpacker(getLoosePackFileDir(file), cancellable, queue, getPrompt());
        }
        else
        {
            unpacker = new MultiVolumeFileUnpacker(volumes, cancellable, queue);
        }
        return unpacker;
    }

    /**
     * Skips a pack file.
     *
     * @param file            the pack file
     * @param pack            the pack
     * @param packInputStream the pack stream
     * @throws IOException if the file cannot be skipped
     */
    @Override
    protected void skip(PackFile file, Pack pack, ObjectInputStream packInputStream) throws IOException
    {
        // this operation is a no-op for MultiVolumeUnpacker as the file is not in the pack stream
    }

    /**
     * Invoked after unpacking has completed, in order to clean up.
     */
    @Override
    protected void cleanup()
    {
        super.cleanup();
        FileUtils.close(volumes);
    }

    /**
     * Tries to determine the source directory of a loose pack file.
     *
     * @param file the pack file
     * @return the source directory
     * @throws IOException        for any I/O error
     * @throws InstallerException for any installer error
     */
    private File getLoosePackFileDir(PackFile file) throws IOException, InstallerException
    {
        File result = getAbsoluteInstallSource();
        File loose = new File(result, file.getRelativeSourcePath());
        if (!loose.exists())
        {
            File volume = volumes.getVolume();
            File dir = volume.getParentFile();
            if (dir != null)
            {
                loose = new File(dir, file.getRelativeSourcePath());
                if (loose.exists())
                {
                    result = dir;
                }
            }
        }
        return result;
    }

    /**
     * Tries to return a sensible default media path for multi-volume installations.
     * <p/>
     * This returns:
     * <ul>
     * <li>the directory the installer is located in; or </li>
     * <li>the user directory, if the installer location can't be determined</li>
     * </ul>
     *
     * @return the default media path. May be <tt>null</tt>
     */
    private String getDefaultMediaPath()
    {
        String result = null;
        try
        {
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            if (codeSource != null)
            {
                URI uri = codeSource.getLocation().toURI();
                if ("file".equals(uri.getScheme()))
                {
                    File dir = new File(uri.getSchemeSpecificPart()).getAbsoluteFile();
                    if (dir.getName().endsWith(".jar"))
                    {
                        dir = dir.getParentFile();
                    }
                    result = dir.getPath();
                }
            }
        }
        catch (URISyntaxException exception)
        {
            // ignore
        }
        if (result == null)
        {
            result = System.getProperty("user.dir");
        }
        return result;
    }

}