package com.izforge.izpack.test.listener;

import java.io.File;
import java.util.List;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;
import com.izforge.izpack.event.AbstractProgressInstallerListener;

/**
 * An {@link InstallerListener} that tracks invocations for testing purposes.
 *
 * @author Tim Anderson
 */
public class TestInstallerListener extends AbstractProgressInstallerListener
{

    /**
     * Tracks invocations of {@link com.izforge.izpack.api.event.InstallerListener#afterInstallerInitialization}.
     */
    private int initialiseCount;

    /**
     * Tracks invocations of {@link com.izforge.izpack.api.event.InstallerListener#beforePacks}.
     */
    private int beforePacksCount;

    /**
     * Tracks invocations of {@link #afterPacks}.
     */
    private int afterPacksCount;

    /**
     * Tracks invocations of {@link #beforePack}.
     */
    private int beforePackCount;

    /**
     * Tracks invocations of {@link #afterPack}.
     */
    private int afterPackCount;

    /**
     * Tracks invocations of {@link #beforeDir}.
     */
    private int beforeDirCount;

    /**
     * Tracks invocations of {@link #afterDir}.
     */
    private int afterDirCount;

    /**
     * Tracks invocations of {@link #beforeFile}.
     */
    private int beforeFileCount;

    /**
     * Tracks invocations of {@link #afterFile}.
     */
    private int afterFileCount;


    /**
     * Constructs a {@code TestInstallerListener}.
     *
     * @param installData the installation data
     */
    public TestInstallerListener(InstallData installData)
    {
        super(installData);
    }

    /**
     * Returns the no. of invocations of {@link #initialise()}.
     *
     * @return the no. of invocations
     */
    public int getInitialiseCount()
    {
        return initialiseCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforePacksCount}.
     *
     * @return the no. of invocations
     */
    public int getBeforePacksCount()
    {
        return beforePacksCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterPacks}.
     *
     * @return the no. of invocations
     */
    public int getAfterPacksCount()
    {
        return afterPacksCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforePack}.
     *
     * @return the no. of invocations
     */
    public int getBeforePackCount()
    {
        return beforePackCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterPack}.
     *
     * @return the no. of invocations
     */
    public int getAfterPackCount()
    {
        return afterPackCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforeDir}.
     *
     * @return the no. of invocations
     */
    public int getBeforeDirCount()
    {
        return beforeDirCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterDir}.
     *
     * @return the no. of invocations
     */
    public int getAfterDirCount()
    {
        return afterDirCount;
    }

    /**
     * Returns the no. of invocations of {@link #beforeFile}.
     *
     * @return the no. of invocations
     */
    public int getBeforeFileCount()
    {
        return beforeFileCount;
    }

    /**
     * Returns the no. of invocations of {@link #afterFile}.
     *
     * @return the no. of invocations
     */
    public int getAfterFileCount()
    {
        return afterFileCount;
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        ++initialiseCount;
        log("initialise");
    }

    /**
     * Invoked before packs are installed.
     *
     * @param packs the packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePacks(List<Pack> packs)
    {
        ++beforePacksCount;
        log("beforePacks: packs=" + packs.size());
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack  the pack
     * @param index the pack index within the list of packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void beforePack(Pack pack, int index)
    {
        ++beforePackCount;
        log("beforePack: pack=" + pack);
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack  the pack
     * @param index the pack index within the list of packs to be installed
     * @throws IzPackException for any error
     */
    @Override
    public void afterPack(Pack pack, int index)
    {
        ++afterPackCount;
        log("afterPack: pack=" + pack);
    }

    /**
     * Invoked after packs are installed.
     *
     * @param packs    the installed packs
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterPacks(List<Pack> packs, ProgressListener listener)
    {
        ++afterPacksCount;
        log("afterPacks");
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDir(File dir, PackFile packFile, Pack pack)
    {
        ++beforeDirCount;
        log("beforeDir: dir=" + dir);
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException
     *          for any error
     */
    @Override
    public void afterDir(File dir, PackFile packFile, Pack pack)
    {
        ++afterDirCount;
        log("afterDir: dir=" + dir);
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException
     *          for any error
     */
    @Override
    public void beforeFile(File file, PackFile packFile, Pack pack)
    {
        ++beforeFileCount;
        log("beforeFile: file=" + file);
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile the corresponding pack file
     * @param pack     the pack that {@code packFile} comes from
     * @throws IzPackException
     *          for any error
     */
    @Override
    public void afterFile(File file, PackFile packFile, Pack pack)
    {
        ++afterFileCount;
        log("afterFile: file=" + file);
    }

    /**
     * Called when the installer creates the listener instance, immediately after the install data is parsed.
     *
     * @param data object containing the current installation data
     */
    public void afterInstallerInitialization(AutomatedInstallData data)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked before packs are installed.
     *
     * @param data    the installation data
     * @param packs   number of packs which are defined for this installation
     * @param handler the UI progress handler
     */
    public void beforePacks(AutomatedInstallData data, Integer packs, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked after packs are installed.
     *
     * @param data    the install data
     * @param handler the UI progress handler
     */
    public void afterPacks(AutomatedInstallData data, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked before a pack is installed.
     *
     * @param pack    the pack
     * @param i       the pack number
     * @param handler the UI progress handler
     */
    public void beforePack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked after a pack is installed.
     *
     * @param pack    current pack object
     * @param i       current pack number
     * @param handler the UI progress handler
     */
    public void afterPack(Pack pack, Integer i, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Returns true if this listener would be informed at every file and directory installation,
     * else false.
     *
     * @return <tt>true</tt>
     */
    public boolean isFileListener()
    {
        return true;
    }

    /**
     * Invoked before a directory is created.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     */
    public void beforeDir(File dir, PackFile packFile)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked after a directory is created.
     *
     * @param dir      the directory
     * @param packFile corresponding pack file
     */
    public void afterDir(File dir, PackFile packFile)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked before a file is installed.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     */
    public void beforeFile(File file, PackFile packFile)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Invoked after a file is installed.
     *
     * @param file     the file
     * @param packFile corresponding pack file
     */
    public void afterFile(File file, PackFile packFile)
    {
        throw new IllegalStateException("Deprecated method shouldn't be invoked");
    }

    /**
     * Logs a message.
     *
     * @param message the message
     */
    private void log(String message)
    {
        System.out.println("TestInstallerListener: " + message);
    }

}
