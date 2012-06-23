/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.api.event;

import java.io.File;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

/**
 * <p>
 * Implementations of this class are used to handle customizing installation. The defined methods
 * are called from the unpacker at different, well defined points of installation.
 * </p>
 *
 * @author Klaus Bartz
 * @author Tim Anderson
 */
public interface InstallerListener
{

    // ------------------------------------------------------------------------
    // Constant Definitions
    // ------------------------------------------------------------------------

    @Deprecated
    public static final int BEFORE_FILE = 1;

    @Deprecated
    public static final int AFTER_FILE = 2;

    @Deprecated
    public static final int BEFORE_DIR = 3;

    @Deprecated
    public static final int AFTER_DIR = 4;

    @Deprecated
    public static final int BEFORE_PACK = 5;

    @Deprecated
    public static final int AFTER_PACK = 6;

    @Deprecated
    public static final int BEFORE_PACKS = 7;

    @Deprecated
    public static final int AFTER_PACKS = 8;

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    void initialise();

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    void beforePacks(List<Pack> packs);

    /**
     * Invoked before a pack is installed.
     *
     * @param pack  the pack
     * @param index the pack index within the list of packs to be installed
     * @throws IzPackException for any error
     */
    void beforePack(Pack pack, int index);

    /**
     * Invoked after a pack is installed.
     *
     * @param pack the pack
     * @param index the pack index within the list of packs to be installed
     * @throws IzPackException for any error
     */
    void afterPack(Pack pack, int index);

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    void afterPacks(List<Pack> packs, ProgressListener listener);

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void beforeDir(File dir, PackFile packFile, Pack pack);

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void afterDir(File dir, PackFile packFile, Pack pack);

    /**
     * Determines if the listener should be notified of every file and directory installation.
     * <p/>
     * If <tt>true</tt>, the {@link #beforeFile} and {@link #afterFile} methods will be invoked for every installed
     * file, and {@link #beforeDir}, and {@link #afterDir} invoked for each directory creation.
     * <p/>
     * Listeners that return <tt>true</tt> should ensure they don't do any long running operations, to avoid
     * performance issues.
     *
     * @return <tt>true</tt> if the listener should be notified, otherwise <tt>false</tt>
     */
    boolean isFileListener();

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void beforeFile(File file, PackFile packFile, Pack pack);

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    void afterFile(File file, PackFile packFile, Pack pack);

    /**
     * Invoked when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @param data the installation data
     * @throws Exception for any error
     * @deprecated use {@link #initialise()}
     */
    @Deprecated
    void afterInstallerInitialization(AutomatedInstallData data) throws Exception;

    /**
     * Invoked before packs are installed.
     *
     * @param data    the installation data
     * @param packs   the number of packs which are defined for this installation
     * @param handler the UI progress handler
     * @throws Exception for any error
     * @deprecated use {@link #beforePacks(List)}
     */
    @Deprecated
    void beforePacks(AutomatedInstallData data, Integer packs, AbstractUIProgressHandler handler)
            throws Exception;

    /**
     * Invoked before a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception for any error
     * @deprecated use {@link #beforePack(Pack, int)}
     */
    @Deprecated
    void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     * @deprecated use {@link #beforeDir(File, PackFile, Pack)}
     */
    @Deprecated
    void beforeDir(File dir, PackFile packFile) throws Exception;

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     * @deprecated use {@link #afterDir(File, PackFile, Pack)}
     */
    @Deprecated
    void afterDir(File dir, PackFile packFile) throws Exception;

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @throws Exception if the listener throws an exception
     * @deprecated use {@link #beforeFile(File, PackFile, Pack)}
     */
    @Deprecated
    void beforeFile(File file, PackFile packFile) throws Exception;

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @throws Exception for any error
     * @deprecated use {@link #afterFile(File, PackFile, Pack)}
     */
    @Deprecated
    void afterFile(File file, PackFile packFile) throws Exception;

    /**
     * Invoked after a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     * @throws Exception for any error
     * @deprecated use {@link #afterPack(Pack, int)}
     */
    @Deprecated
    void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler) throws Exception;

    /**
     * Invoked after packs are installed.
     *
     * @param data    the installation data
     * @param handler the UI progress handler
     * @throws Exception for any error
     * @deprecated use {@link #afterPacks(List, ProgressListener)}
     */
    @Deprecated
    void afterPacks(AutomatedInstallData data, AbstractUIProgressHandler handler) throws Exception;

}
