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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.*;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.ResourceInterruptedException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.handler.ProgressHandler;
import com.izforge.izpack.core.handler.PromptUIHandler;
import com.izforge.izpack.data.ExecutableFile;
import com.izforge.izpack.data.ParsableFile;
import com.izforge.izpack.data.UpdateCheck;
import com.izforge.izpack.installer.bootstrap.Installer;
import com.izforge.izpack.installer.data.UninstallData;
import com.izforge.izpack.installer.event.InstallerListeners;
import com.izforge.izpack.installer.util.PackHelper;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.PlatformModelMatcher;
import com.izforge.izpack.util.file.DirectoryScanner;
import com.izforge.izpack.util.file.FileUtils;
import com.izforge.izpack.util.file.GlobPatternMapper;
import com.izforge.izpack.util.file.types.FileSet;
import com.izforge.izpack.util.os.FileQueue;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.Pack200;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.izforge.izpack.api.handler.Prompt.*;
import static com.izforge.izpack.installer.bootstrap.Installer.INSTALLER_AUTO;


/**
 * Abstract base class for all unpacker implementations.
 *
 * @author Dennis Reil, <izpack@reil-online.de>
 * @author Tim Anderson
 */
public abstract class UnpackerBase implements IUnpacker
{

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The uninstallation data.
     */
    private final UninstallData uninstallData;

    /**
     * The pack resources.
     */
    private final PackResources resources;

    /**
     * The rules engine.
     */
    private final RulesEngine rules;

    /**
     * The variable replacer.
     */
    private final VariableSubstitutor variableSubstitutor;

    /**
     * The file queue factory.
     */
    private final FileQueueFactory queueFactory;

    /**
     * The housekeeper.
     */
    private final Housekeeper housekeeper;

    /**
     * The listeners.
     */
    private final InstallerListeners listeners;

    /**
     * The installer listener.
     */
    private ProgressListener listener;

    /**
     * The absolute path of the source installation jar.
     */
    private File absoluteInstallSource;

    /**
     * The Pack200 unpacker.
     */
    private Pack200.Unpacker unpacker;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    /**
     * The result of the operation.
     */
    private boolean result = true;

    /**
     * Determines if unpack operations should be cancelled.
     */
    private final Cancellable cancellable;

    /**
     * The unpacking state.
     */
    private enum State
    {
        READY, UNPACKING, INTERRUPT, INTERRUPTED
    }

    /**
     * The current unpacking state.
     */
    private State state = State.READY;

    /**
     * If <tt>true</tt>, prevent interrupts.
     */
    private boolean disableInterrupt = false;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(UnpackerBase.class.getName());

    /**
     * Constructs an <tt>UnpackerBase</tt>.
     *
     * @param installData         the installation data
     * @param resources           the pack resources
     * @param rules               the rules engine
     * @param variableSubstitutor the variable substituter
     * @param uninstallData       the uninstallation data
     * @param factory             the file queue factory
     * @param housekeeper         the housekeeper
     * @param listeners           the listeners
     * @param prompt              the prompt
     * @param matcher             the platform-model matcher
     */
    public UnpackerBase(InstallData installData, PackResources resources, RulesEngine rules,
                        VariableSubstitutor variableSubstitutor, UninstallData uninstallData, FileQueueFactory factory,
                        Housekeeper housekeeper, InstallerListeners listeners, Prompt prompt,
                        PlatformModelMatcher matcher)
    {
        this.installData = installData;
        this.resources = resources;
        this.rules = rules;
        this.variableSubstitutor = variableSubstitutor;
        this.uninstallData = uninstallData;
        this.queueFactory = factory;
        this.housekeeper = housekeeper;
        this.listeners = listeners;
        this.prompt = prompt;
        this.matcher = matcher;
        cancellable = new Cancellable()
        {
            @Override
            public boolean isCancelled()
            {
                return isInterrupted();
            }
        };
    }

    /**
     * Sets the progress listener.
     *
     * @param listener the progress listener
     */
    @Override
    public void setProgressListener(ProgressListener listener)
    {
        this.listener = listener;
    }

    /**
     * Runs the unpacker.
     */
    public void run()
    {
        unpack();
    }

    /**
     * Unpacks the installation files.
     */
    public void unpack()
    {
        state = State.UNPACKING;
        try
        {
            List<ParsableFile> parsables = new ArrayList<ParsableFile>();
            List<ExecutableFile> executables = new ArrayList<ExecutableFile>();
            List<UpdateCheck> updateChecks = new ArrayList<UpdateCheck>();
            FileQueue queue = queueFactory.isSupported() ? queueFactory.create() : null;

            List<Pack> packs = installData.getSelectedPacks();
            preUnpack(packs);
            unpack(packs, queue, parsables, executables, updateChecks);
            postUnpack(packs, queue, updateChecks);
        }
        catch (Exception exception)
        {
            setResult(false);
            logger.log(Level.SEVERE, exception.getMessage(), exception);

            listener.stopAction();

            if (exception instanceof ResourceInterruptedException)
            {
                prompt.message(Type.INFORMATION, "Installation cancelled");
            }
            else
            {
                String message = exception.getMessage();
                if (message == null || "".equals(message))
                {
                    message = "Internal error occurred : " + exception.toString();
                }
                prompt.message(Type.ERROR, message);
            }
            // TODO - shouldn't do this. Should provide option to rollback changes
            housekeeper.shutDown(4);
        }
        finally
        {
            cleanup();
        }
    }

    /**
     * Return the state of the operation.
     *
     * @return true if the operation was successful, false otherwise.
     */
    public boolean getResult()
    {
        return result;
    }

    /**
     * Interrupts the unpacker, and waits for it to complete.
     * <p/>
     * If interrupts have been prevented ({@link #isInterruptDisabled} returns <tt>true</tt>), then this
     * returns immediately.
     *
     * @param timeout the maximum time to wait, in milliseconds
     * @return <tt>true</tt> if the interrupt will be performed, <tt>false</tt> if the interrupt will be discarded
     */
    @Override
    public boolean interrupt(long timeout)
    {
        boolean result;
        if (isInterruptDisabled())
        {
            result = false;
        }
        else
        {
            synchronized (this)
            {
                if (state != State.READY && state != State.INTERRUPTED)
                {
                    state = State.INTERRUPT;
                    try
                    {
                        wait(timeout);
                    }
                    catch (InterruptedException ignore)
                    {
                        // do nothing
                    }
                    result = state == State.INTERRUPTED;
                }
                else
                {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Determines if interrupts should be disabled.
     *
     * @param disable if <tt>true</tt> disable interrupts, otherwise enable them
     */
    @Override
    public synchronized void setDisableInterrupt(boolean disable)
    {
        if (state == State.INTERRUPT || state == State.INTERRUPTED)
        {
            throw new IllegalStateException("Cannot disable interrupts. Unpacking has already been interrupted");
        }
        disableInterrupt = disable;
    }

    /**
     * Determines if interrupts have been disabled or not.
     *
     * @return <tt>true</tt> if interrupts have been disabled, otherwise <tt>false</tt>
     */
    public synchronized boolean isInterruptDisabled()
    {
        return disableInterrupt;
    }

    /**
     * Invoked prior to unpacking.
     * <p/>
     * This notifies the {@link #getProgressListener listener}, and any registered {@link InstallerListener listeners}.
     *
     * @param packs the packs to unpack
     * @throws IzPackException for any error
     */
    protected void preUnpack(List<Pack> packs)
    {
        logger.fine("Unpacker starting");
        listener.startAction("Unpacking", packs.size());
        listeners.beforePacks(packs, listener);
    }

    /**
     * Unpacks the selected packs.
     *
     * @param packs        the packs to unpack
     * @param queue        the file queue, or {@code null} if queuing is not supported
     * @param parsables    used to collect parsable files in the pack
     * @param executables  used to collect executable files files in the pack
     * @param updateChecks used to collect update checks in the pack
     * @throws ResourceInterruptedException if unpacking is cancelled
     * @throws IzPackException              for any error
     */
    protected void unpack(List<Pack> packs, FileQueue queue, List<ParsableFile> parsables,
                          List<ExecutableFile> executables, List<UpdateCheck> updateChecks)
    {
        int count = packs.size();
        for (int i = 0; i < count; i++)
        {
            Pack pack = packs.get(i);
            if (shouldUnpack(pack))
            {
                listeners.beforePack(pack, i, listener);
                unpack(pack, i, queue, parsables, executables, updateChecks);
                checkInterrupt();

                logger.fine("Found " + parsables.size() + " parsable files");
                parseFiles(parsables);
                checkInterrupt();

                logger.fine("Found " + parsables.size() + " executable files");
                executeFiles(executables);
                checkInterrupt();

                // update checks should be done _after_ uninstaller was put, so we don't delete it. TODO
                performUpdateChecks(updateChecks);
                checkInterrupt();

                listeners.afterPack(pack, i, listener);
            }
        }
    }

    /**
     * Unpacks a pack.
     *
     * @param pack         the pack to unpack
     * @param packNo       the pack number
     * @param queue        the file queue, or {@code null} if queuing is not supported
     * @param parsables    used to collect parsable files in the pack
     * @param executables  used to collect executable files files in the pack
     * @param updateChecks used to collect update checks in the pack
     * @throws IzPackException for any error
     */
    protected void unpack(Pack pack, int packNo, FileQueue queue, List<ParsableFile> parsables,
                          List<ExecutableFile> executables, List<UpdateCheck> updateChecks)
    {
        InputStream in = null;
        ObjectInputStream packInputStream = null;
        try
        {
            in = resources.getPackStream(pack.getName());
            packInputStream = new ObjectInputStream(in);

            int fileCount = packInputStream.readInt();

            String stepName = getStepName(pack);
            listener.nextStep(stepName, packNo + 1, fileCount);

            for (int i = 0; i < fileCount; ++i)
            {
                // read the header
                PackFile file = (PackFile) packInputStream.readObject();
                if (shouldUnpack(file))
                {
                    // unpack the file
                    unpack(file, packInputStream, i, pack, queue);
                }
                else
                {
                    // condition is not fulfilled, so skip it
                    skip(file, pack, packInputStream);
                }
            }
            readParsableFiles(packInputStream, parsables);
            readExecutableFiles(packInputStream, executables);
            readUpdateChecks(packInputStream, updateChecks);
        }
        catch (IzPackException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            throw new InstallerException("Failed to unpack pack: " + pack.getName(), exception);
        }
        finally
        {
            FileUtils.close(packInputStream);
            FileUtils.close(in);
        }
    }

    /**
     * Determines if a file should be unpacked.
     *
     * @param file the file to check
     * @return {@code true} if the file should be unpacked; {@code false} if it should be skipped
     */
    private boolean shouldUnpack(PackFile file)
    {
        boolean result = true;
        if (file.hasCondition())
        {
            result = isConditionTrue(file.getCondition());
        }
        if (result && file.osConstraints() != null && !file.osConstraints().isEmpty())
        {
            result = matcher.matchesCurrentPlatform(file.osConstraints());
        }
        return result;
    }

    /**
     * Unpacks a pack file.
     *
     * @param file            the pack file
     * @param packInputStream the pack file input stream
     * @param fileNo          the pack file number
     * @param pack            the pack that the pack file comes from
     * @param queue           the file queue, or {@code null} if queuing is not supported
     * @throws IOException     for any I/O error
     * @throws IzPackException for any other error
     */
    protected void unpack(PackFile file, ObjectInputStream packInputStream, int fileNo, Pack pack, FileQueue queue)
            throws IOException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Unpack " + file.getTargetPath());
        }

        // translate & build the path
        Variables variables = getInstallData().getVariables();
        String path = IoHelper.translatePath(file.getTargetPath(), variables);
        File target = new File(path);
        File dir = target;
        if (!file.isDirectory())
        {
            dir = target.getParentFile();
        }

        createDirectory(dir, file, pack);

        // Add path to the log
        getUninstallData().addFile(path, pack.isUninstall());

        if (file.isDirectory())
        {
            return;
        }

        listeners.beforeFile(target, file, pack);

        listener.progress(fileNo, path);

        // if this file exists and should not be overwritten, check what to do
        if (target.exists() && (file.override() != OverrideType.OVERRIDE_TRUE) && !isOverwriteFile(file, target))
        {
            if (!file.isBackReference() && !pack.isLoose())
            {
                if (file.isPack200Jar())
                {
                    skip(packInputStream, Integer.SIZE / 8);
                }
                else
                {
                    skip(packInputStream, file.length());
                }
            }
        }
        else
        {
            handleOverrideRename(file, target);
            extract(file, target, packInputStream, pack, queue);
        }
    }

    /**
     * Extracts a pack file.
     *
     * @param file            the pack file
     * @param target          the file to write to
     * @param packInputStream the pack file input stream
     * @param pack            the pack that the pack file comes from
     * @param queue           the file queue, or {@code null} if queuing is not supported
     * @throws IOException                  for any I/O error
     * @throws ResourceInterruptedException if installation is cancelled
     * @throws IzPackException              for any IzPack error
     */
    protected void extract(PackFile file, File target, ObjectInputStream packInputStream, Pack pack, FileQueue queue)
            throws IOException
    {
        ObjectInputStream packStream = packInputStream;
        InputStream in = null;
        try
        {
            FileUnpacker unpacker;

            if (!pack.isLoose() && file.isBackReference())
            {
                in = resources.getPackStream(file.previousPackId);
                packStream = new ObjectInputStream(in);
                // must wrap for blockdata use by ObjectStream (otherwise strange result)
                // skip on underlying stream (for some reason not possible on ObjectStream)
                skip(in, file.offsetInPreviousPack - 4);
                // but the stream header is now already read (== 4 bytes)
            }

            unpacker = createFileUnpacker(file, pack, queue, cancellable);
            unpacker.unpack(file, packStream, target);
            checkInterrupt();

            if (!unpacker.isQueued())
            {
                listeners.afterFile(target, file, pack);
            }
        }
        finally
        {
            FileUtils.close(in);
            if (packStream != packInputStream)
            {
                FileUtils.close(packStream);
            }
        }
    }

    /**
     * Skips a pack file.
     *
     * @param file            the pack file
     * @param pack            the pack
     * @param packInputStream the pack stream
     * @throws IOException if the file cannot be skipped
     */
    protected void skip(PackFile file, Pack pack, ObjectInputStream packInputStream) throws IOException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine("Skip " + file.getTargetPath());
        }

        if (!pack.isLoose() && !file.isBackReference())
        {
            skip(packInputStream, file.length());
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
    protected FileUnpacker createFileUnpacker(PackFile file, Pack pack, FileQueue queue, Cancellable cancellable)
            throws IOException, InstallerException
    {
        FileUnpacker unpacker;
        if (pack.isLoose())
        {
            unpacker = new LooseFileUnpacker(getAbsoluteInstallSource(), cancellable, queue, prompt);
        }
        else if (file.isPack200Jar())
        {
            unpacker = new Pack200FileUnpacker(cancellable, resources, getPack200Unpacker(), queue);
        }
        else
        {
            unpacker = new DefaultFileUnpacker(cancellable, queue);
        }
        return unpacker;
    }

    /**
     * Invoked after each pack has been unpacked.
     *
     * @param packs        the packs
     * @param queue        the file queue, or {@code null} if queuing is not supported
     * @param parsables    used to collect parsable files in the pack
     * @param executables  used to collect executable files files in the pack
     * @param updateChecks used to collect update checks in the pack
     * @throws ResourceInterruptedException if installation is cancelled
     * @throws IOException                  for any I/O error
     */
    protected void postUnpack(List<Pack> packs, FileQueue queue, List<UpdateCheck> updateChecks) throws IOException
    {
        InstallData installData = getInstallData();

        // commit the file queue if there are potentially blocked files
        if (queue != null && !queue.isEmpty())
        {
            queue.execute();
            installData.setRebootNecessary(queue.isRebootNecessary());
        }
        checkInterrupt();

        listeners.afterPacks(packs, listener);
        checkInterrupt();

        // write installation information
        writeInstallationInformation();

        // unpacking complete
        listener.stopAction();
    }

    /**
     * Invoked after unpacking has completed, in order to clean up.
     */
    protected void cleanup()
    {
        state = State.READY;
    }

    /**
     * Returns the installation data.
     *
     * @return the installation data
     */
    protected InstallData getInstallData()
    {
        return installData;
    }

    /**
     * Returns the uninstallation data.
     *
     * @return the uninstallation data
     */
    protected UninstallData getUninstallData()
    {
        return uninstallData;
    }

    /**
     * Returns the pack resources.
     *
     * @return the pack resources
     */
    protected PackResources getResources()
    {
        return resources;
    }

    /**
     * Returns the variable replacer.
     *
     * @return the variable replacer
     */
    protected VariableSubstitutor getVariableSubstitutor()
    {
        return variableSubstitutor;
    }

    /**
     * Returns the progress listener.
     *
     * @return the progress listener
     */
    protected ProgressListener getProgressListener()
    {
        return listener;
    }

    /**
     * Returns the prompt.
     *
     * @return the prompt
     */
    protected Prompt getPrompt()
    {
        return prompt;
    }

    /**
     * Determines if a pack should be unpacked.
     *
     * @param pack the pack
     * @return <tt>true</tt> if the pack should be unpacked, <tt>false</tt> if it should be skipped
     */
    protected boolean shouldUnpack(Pack pack)
    {
        boolean result = true;
        if (pack.hasCondition())
        {
            result = rules.isConditionTrue(pack.getCondition());
        }
        return result;
    }

    /**
     * Sets the result of the unpacking operation.
     *
     * @param result if <tt>true</tt> denotes success
     */
    protected void setResult(boolean result)
    {
        this.result = result;
    }

    protected boolean isConditionTrue(String id)
    {
        return rules.isConditionTrue(id);
    }

    /**
     * Returns the step name for a pack, for reporting purposes.
     *
     * @param pack the pack
     * @return the pack's step name
     */
    protected String getStepName(Pack pack)
    {
        // hide pack name if it is hidden
        return pack.isHidden() ? "" : PackHelper.getPackName(pack, installData.getMessages());
    }

    /**
     * Creates a directory including any necessary but nonexistent parent directories, associated with a pack file.
     * <p/>
     * If {@link InstallerListener}s are registered, these will be notified for each directory created.
     *
     * @param dir  the directory to create
     * @param file the pack file
     * @param pack the pack that {@code file} comes from
     * @throws IzPackException if the directory cannot be created or a listener throws an exception
     */
    protected void createDirectory(File dir, PackFile file, Pack pack)
    {
        if (!dir.exists())
        {
            if (!listeners.isFileListener())
            {
                // Create it in one step.
                if (!dir.mkdirs())
                {
                    throw new IzPackException("Could not create directory: " + dir.getPath());
                }
            }
            else
            {
                File parent = dir.getParentFile();
                if (parent != null)
                {
                    createDirectory(parent, file, pack);
                }
                listeners.beforeDir(dir, file, pack);
                if (!dir.mkdir())
                {
                    throw new IzPackException("Could not create directory: " + dir.getPath());
                }
                listeners.afterDir(dir, file, pack);
            }
        }
    }

    /**
     * Parses {@link ParsableFile} instances collected during unpacking.
     *
     * @param files the files to parse
     * @throws InstallerException           if parsing fails
     * @throws ResourceInterruptedException if installation is interrupted
     */
    private void parseFiles(List<ParsableFile> files)
    {
        if (!files.isEmpty())
        {
            ScriptParser parser = new ScriptParser(getVariableSubstitutor(), matcher);
            for (ParsableFile file : files)
            {
                try
                {
                    parser.parse(file);
                }
                catch (Exception exception)
                {
                    throw new InstallerException("Failed to parse: " + file.getPath(), exception);
                }
                checkInterrupt();
            }
        }
    }

    /**
     * Runs {@link ExecutableFile} instances collected during unpacking.
     *
     * @param executables the executables to run
     * @throws InstallerException if an executable fails
     */
    private void executeFiles(List<ExecutableFile> executables)
    {
        if (!executables.isEmpty())
        {
            FileExecutor executor = new FileExecutor(executables);
            PromptUIHandler handler = new ProgressHandler(listener, prompt);
            if (executor.executeFiles(ExecutableFile.POSTINSTALL, matcher, handler) != 0)
            {
                throw new InstallerException("File execution failed");
            }
        }
    }

    /**
     * Determines if the unpacker has been interrupted.
     *
     * @return <tt>true</tt> if the unpacker has been interrupted, otherwise <tt>false</tt>
     */
    protected synchronized boolean isInterrupted()
    {
        boolean result = false;
        if (state == State.INTERRUPT)
        {
            setResult(false);
            state = State.INTERRUPTED;
            result = true;
            notifyAll(); // notify threads waiting in interrupt()
        }
        else
        {
            if (state == State.INTERRUPTED)
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Throws an {@link ResourceInterruptedException} if installation has been interrupted.
     *
     * @throws ResourceInterruptedException if installation is interrupted
     */
    protected void checkInterrupt()
    {
        if (isInterrupted())
        {
            throw new ResourceInterruptedException("Installation cancelled");
        }
    }

    /**
     * Performs update checks.
     *
     * @param checks the update checks. May be {@code null}
     * @throws IzPackException for any error
     */
    protected void performUpdateChecks(List<UpdateCheck> checks)
    {
        if (checks != null && !checks.isEmpty())
        {
            logger.info("Cleaning up the target folder ...");

            File absoluteInstallPath = new File(installData.getInstallPath()).getAbsoluteFile();
            FileSet fileset = new FileSet();
            List<File> filesToDelete = new ArrayList<File>();
            List<File> dirsToDelete = new ArrayList<File>();

            try
            {
                fileset.setDir(absoluteInstallPath);

                for (UpdateCheck check : checks)
                {
                    if (check.includesList != null)
                    {
                        for (String include : check.includesList)
                        {
                            fileset.createInclude().setName(variableSubstitutor.substitute(include));
                        }
                    }

                    if (check.excludesList != null)
                    {
                        for (String exclude : check.excludesList)
                        {
                            fileset.createExclude().setName(variableSubstitutor.substitute(exclude));
                        }
                    }
                }
                DirectoryScanner scanner = fileset.getDirectoryScanner();
                scanner.scan();
                String[] srcFiles = scanner.getIncludedFiles();
                String[] srcDirs = scanner.getIncludedDirectories();

                Set<File> installedFiles = new TreeSet<File>();

                for (String name : uninstallData.getInstalledFilesList())
                {
                    File file = new File(name);

                    if (!file.isAbsolute())
                    {
                        file = new File(absoluteInstallPath, name);
                    }

                    installedFiles.add(file);
                }
                for (String srcFile : srcFiles)
                {
                    File newFile = new File(scanner.getBasedir(), srcFile);

                    // skip files we just installed
                    if (!installedFiles.contains(newFile))
                    {
                        filesToDelete.add(newFile);
                    }
                }
                for (String srcDir : srcDirs)
                {
                    // All directories except INSTALL_PATH
                    if (!srcDir.isEmpty())
                    {
                        File newDir = new File(scanner.getBasedir(), srcDir);

                        // skip directories we just installed
                        if (!installedFiles.contains(newDir))
                        {
                            dirsToDelete.add(newDir);
                        }
                    }
                }
            }
            catch (IzPackException exception)
            {
                throw exception;
            }
            catch (Exception exception)
            {
                throw new IzPackException(exception);
            }

            for (File f : filesToDelete)
            {
                if (!f.delete())
                {
                    logger.warning("Cleanup: Unable to delete file " + f);
                }
                else
                {
                    logger.fine("Cleanup: Deleted file " + f);
                }
            }

            // Sort directories, deepest path first to be able to
            // delete recursively
            Collections.sort(dirsToDelete);
            Collections.reverse(dirsToDelete);

            for (File d : dirsToDelete)
            {
                if (!d.exists())
                {
                    break;
                }

                // Don't try to delete non-empty directories, because they
                // probably must have been implicitly created as parents
                // of regular installation files
                File[] files = d.listFiles();
                if (files != null && files.length != 0)
                {
                    break;
                }

                // Only empty directories will be deleted
                if (!d.delete())
                {
                    logger.warning("Cleanup: Unable to delete directory " + d);
                }
                else
                {
                    logger.fine("Cleanup: Deleted directory " + d);
                }
            }
        }
    }

    /**
     * Writes information about the installed packs and the variables at installation time.
     *
     * @throws InstallerException for any installer error
     * @throws IOException        for any I/O error
     */
    protected void writeInstallationInformation() throws IOException
    {
        if (!installData.getInfo().isWriteInstallationInformation())
        {
            logger.fine("Skip writing installation information");
            return;
        }
        logger.fine("Writing installation information");
        String installDir = installData.getInstallPath();

        List<Pack> installedPacks = new ArrayList<Pack>(installData.getSelectedPacks());

        File installationInfo = new File(installDir + File.separator + InstallData.INSTALLATION_INFORMATION);
        if (!installationInfo.exists())
        {
            logger.fine("Creating info file" + installationInfo.getAbsolutePath());
            File dir = new File(installData.getInstallPath());
            if (!dir.exists())
            {
                // if no packs have been installed, then the installation directory won't exist
                if (!dir.mkdirs())
                {
                    throw new InstallerException("Failed to create directory: " + dir);
                }
            }
            if (!installationInfo.createNewFile())
            {
                throw new InstallerException("Failed to create file: " + installationInfo);
            }
        }
        else
        {
            logger.fine("Previous installation information found");
            // read in old information and update
            FileInputStream fin = new FileInputStream(installationInfo);
            ObjectInputStream oin = new ObjectInputStream(fin);

            List<Pack> packs;
            try
            {
                packs = (List<Pack>) oin.readObject();
            }
            catch (Exception exception)
            {
                throw new InstallerException("Failed to read previous installation information", exception);
            }
            for (Pack pack : packs)
            {
                installedPacks.add(pack);
            }
            FileUtils.close(oin);
            FileUtils.close(fin);
        }

        FileOutputStream fout = new FileOutputStream(installationInfo);
        ObjectOutputStream oout = new ObjectOutputStream(fout);
        oout.writeObject(installedPacks);
        /*
        int selectedpackscount = installData.selectedPacks.size();
        for (int i = 0; i < selectedpackscount; i++)
        {
            Pack pack = (Pack) installData.selectedPacks.get(i);
            oout.writeObject(pack);
        }
        */
        oout.writeObject(installData.getVariables().getProperties());
        logger.fine("Writing installation information finished");
        FileUtils.close(oout);
        FileUtils.close(fout);
    }

    protected File getAbsoluteInstallSource() throws IOException, InstallerException
    {
        if (absoluteInstallSource == null)
        {
            URI uri;
            try
            {
                uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            }
            catch (URISyntaxException exception)
            {
                throw new InstallerException(exception);
            }
            if (!"file".equals(uri.getScheme()))
            {
                throw new InstallerException("Unexpected scheme in JAR file URI: " + uri);
            }
            absoluteInstallSource = new File(uri.getSchemeSpecificPart()).getAbsoluteFile();
            if (absoluteInstallSource.getName().endsWith(".jar"))
            {
                absoluteInstallSource = absoluteInstallSource.getParentFile();
            }
        }
        return absoluteInstallSource;
    }

    /**
     * Skips bytes in a stream.
     *
     * @param stream the stream
     * @param bytes  the no. of bytes to skip
     * @throws IOException for any I/O error, or if the no. of bytes skipped doesn't match that expected
     */
    protected void skip(InputStream stream, long bytes) throws IOException
    {
        long skipped = stream.skip(bytes);
        if (skipped != bytes)
        {
            throw new IOException("Expected to skip: " + bytes + " in stream but skipped: " + skipped);
        }
    }

    /**
     * Determines if a file should be overwritten.
     *
     * @param pf   the pack file
     * @param file the file to check
     * @return {@code true} if the file should be overwritten
     */
    protected boolean isOverwriteFile(PackFile pf, File file)
    {
        boolean result = false;

        // don't overwrite file if the user said so
        if (pf.override() != OverrideType.OVERRIDE_FALSE)
        {
            if (pf.override() == OverrideType.OVERRIDE_TRUE)
            {
                result = true;
            }
            else
            {
                if (pf.override() == OverrideType.OVERRIDE_UPDATE)
                {
                    // check mtime of involved files
                    // (this is not 100% perfect, because the
                    // already existing file might
                    // still be modified but the new installed
                    // is just a bit newer; we would
                    // need the creation time of the existing
                    // file or record with which mtime
                    // it was installed...)
                    result = (file.lastModified() < pf.lastModified());
                }
                else
                {
                    Option defChoice = null;

                    if (pf.override() == OverrideType.OVERRIDE_ASK_FALSE)
                    {
                        defChoice = Option.NO;
                    }
                    else if (pf.override() == OverrideType.OVERRIDE_ASK_TRUE)
                    {
                        defChoice = Option.YES;
                    }

                    // are we running in automated mode? If so use default choice.
                    if (Installer.getInstallerMode() == INSTALLER_AUTO)
                    {
                        result = (defChoice == Option.YES);
                    }
                    else // ask the user
                    {
                        Messages messages = installData.getMessages();
                        Option answer = prompt.confirm(Type.QUESTION,
                                messages.get("InstallPanel.overwrite.title") + " - " + file.getName(),
                                messages.get("InstallPanel.overwrite.question") + file.getAbsolutePath(),
                                Options.YES_NO, defChoice);
                        result = (answer == Option.YES);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Renames a file, if it exists and the pack file defines how it should be handled.
     *
     * @param pf   the pack file
     * @param file the file to rename
     * @throws InstallerException if the file cannot be renamed
     */
    protected void handleOverrideRename(PackFile pf, File file)
    {
        if (file.exists() && pf.overrideRenameTo() != null)
        {
            GlobPatternMapper mapper = new GlobPatternMapper();
            mapper.setFrom("*");
            mapper.setTo(pf.overrideRenameTo());
            mapper.setCaseSensitive(true);
            String[] newFileNameArr = mapper.mapFileName(file.getName());
            if (newFileNameArr != null)
            {
                String newFileName = newFileNameArr[0];
                File newPathFile = new File(file.getParent(), newFileName);
                if (newPathFile.exists())
                {
                    if (!newPathFile.delete())
                    {
                        logger.warning("Failed to delete: " + newPathFile);
                    }
                }
                if (!file.renameTo(newPathFile))
                {
                    throw new InstallerException("The file " + file + " could not be renamed to " + newPathFile);
                }
            }
            else
            {
                throw new InstallerException("File name " + file.getName() + " cannot be mapped using the expression \""
                                                     + pf.overrideRenameTo() + "\"");
            }
        }
    }


    /**
     * Reads {@link ParsableFile parseable files} from the supplied stream.
     *
     * @param stream    the stream to read from
     * @param parsables used to collect the read objects
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialised object cannot be found
     */
    protected void readParsableFiles(ObjectInputStream stream, List<ParsableFile> parsables)
            throws IOException, ClassNotFoundException
    {
        int count = stream.readInt();
        for (int i = 0; i < count; ++i)
        {
            ParsableFile file = (ParsableFile) stream.readObject();
            logger.fine("Unpacked parsable: " + file.toString());
            if (!file.hasCondition() || isConditionTrue(file.getCondition()))
            {
                String path = IoHelper.translatePath(file.getPath(), installData.getVariables());
                file.setPath(path);
                parsables.add(file);
            }
        }
    }

    /**
     * Reads {@link ExecutableFile executable files} from the supplied stream.
     *
     * @param stream      the stream to read from
     * @param executables used to collect the read objects
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialised object cannot be found
     */
    protected void readExecutableFiles(ObjectInputStream stream, List<ExecutableFile> executables)
            throws IOException, ClassNotFoundException
    {
        // Load information about executable files
        int count = stream.readInt();
        for (int i = 0; i < count; ++i)
        {
            ExecutableFile file = (ExecutableFile) stream.readObject();
            logger.fine("Unpacked executable: " + file.toString());
            if (!file.hasCondition() || isConditionTrue(file.getCondition()))
            {
                Variables variables = installData.getVariables();
                file.path = IoHelper.translatePath(file.path, variables);
                if (null != file.argList && !file.argList.isEmpty())
                {
                    for (int j = 0; j < file.argList.size(); j++)
                    {
                        String arg = file.argList.get(j);
                        arg = IoHelper.translatePath(arg, variables);
                        file.argList.set(j, arg);
                    }
                }
                executables.add(file);
                if (file.executionStage == ExecutableFile.UNINSTALL)
                {
                    uninstallData.addExecutable(file);
                }
            }
        }
    }

    /**
     * Reads {@link UpdateCheck update checks} from the supplied stream.
     *
     * @param stream       the stream to read from
     * @param updateChecks used to collect the read objects
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if the class of a serialised object cannot be found
     */
    protected void readUpdateChecks(ObjectInputStream stream, List<UpdateCheck> updateChecks)
            throws IOException, ClassNotFoundException
    {
        int count = stream.readInt();
        for (int i = 0; i < count; ++i)
        {
            UpdateCheck check = (UpdateCheck) stream.readObject();
            updateChecks.add(check);
        }
    }

    /**
     * Returns the pack200 unpacker, creating it if required.
     *
     * @return the pack200 unpacker
     */
    private Pack200.Unpacker getPack200Unpacker()
    {
        if (unpacker == null)
        {
            unpacker = Pack200.newUnpacker();
        }
        return unpacker;
    }


}

