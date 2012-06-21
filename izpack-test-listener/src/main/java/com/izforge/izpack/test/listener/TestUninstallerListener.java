package com.izforge.izpack.test.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.AbstractUIProgressHandler;

/**
 * An {@link UninstallerListener} that tracks invocations for testing purposes.
 * <p/>
 * As this will be loaded in a separate class loader or JVM to the test case, its state will be written to a file,
 * <em>TestUninstallerListener.log.&Lt;hash&gt;</em>, where <em>hash</em> is derived from the install path, in the
 * <em>java.io.tmpdir</em> directory.
 * <p/>
 * The addition of the hash lowers the possibility of clashes with other tests. For best results,
 *
 * @author Tim Anderson
 */
public class TestUninstallerListener implements UninstallerListener
{

    /**
     * The path to log the state to.
     */
    private String logPath;

    /**
     * The state.
     */
    private State state = new State();

    /**
     * The listener state.
     */
    public static class State implements Serializable
    {
        /**
         * Tracks invocations of {@link UninstallerListener#initialise()}.
         */
        public int initialiseCount;

        /**
         * Tracks the no. of invocations of {@link UninstallerListener#beforeDelete(List)}.
         */
        public int beforeListDeleteCount;

        /**
         * Tracks the no. of invocations of {@link UninstallerListener#afterDelete(List, ProgressListener)}.
         */
        public int afterListDeleteCount;

        /**
         * Tracks the no. of invocations of {@link UninstallerListener#beforeDelete(File)}.
         */
        public int beforeDeleteCount;

        /**
         * Tracks the no. of invocations of {@link UninstallerListener#afterDelete(File)}.
         */
        public int afterDeleteCount;
    }

    /**
     * Default constructor.
     */
    public TestUninstallerListener()
    {
        String installPath = getInstallPath();
        if (installPath != null)
        {
            logPath = getStatePath(installPath);
            if (logPath != null)
            {
                log("Logging to: " + logPath);
            }
        }
    }

    /**
     * Returns the path to write state to, derived from the install path.
     *
     * @param installPath the install path
     * @return the path
     */
    public static String getStatePath(String installPath)
    {
        return new File(System.getProperty("java.io.tmpdir"),
                "TestUninstallerListener.log." + installPath.hashCode()).getAbsolutePath();
    }

    /**
     * Reads the state at the specified path.
     *
     * @param path the path
     * @return the corresponding state
     * @throws IOException            for any I/O error
     * @throws ClassNotFoundException if a serialized class cannot be found
     */
    public static State readState(String path) throws IOException, ClassNotFoundException
    {
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
        try
        {
            return (State) stream.readObject();
        }
        finally
        {
            stream.close();
        }
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        ++state.initialiseCount;
        log("initialise");
    }

    /**
     * Determines if this listener will be informed of every delete operation.
     *
     * @return <tt>true</tt>
     */
    public boolean isFileListener()
    {
        return true;
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files all files which should be deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(List<File> files)
    {
        ++state.beforeListDeleteCount;
        log("beforeDelete: files=" + files.size());
    }

    /**
     * Invoked before a file is deleted.
     *
     * @param file the file which will be deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(File file)
    {
        ++state.beforeDeleteCount;
        log("beforeDelete: file=" + file);
    }

    /**
     * Invoked after a file is deleted.
     *
     * @param file the file which was deleted
     * @throws IzPackException for any error
     */
    @Override
    public void afterDelete(File file)
    {
        ++state.afterDeleteCount;
        log("afterDelete: file=" + file);
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files    the files which where deleted
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterDelete(List<File> files, ProgressListener listener)
    {
        ++state.afterListDeleteCount;
        log("afterDelete: files=" + files.size());
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files   all files which should be deleted
     * @param handler the UI progress handler
     */
    public void beforeDeletion(List files, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method should not be invoked.");
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files   all files which where deleted
     * @param handler the UI progress handler
     */
    public void afterDeletion(List files, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method should not be invoked.");
    }

    /**
     * Invoked before a file is deleted.
     *
     * @param file    file which should be deleted
     * @param handler the UI progress handler
     */
    public void beforeDelete(File file, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method should not be invoked.");
    }

    /**
     * Invoked after a file is deleted.
     *
     * @param file    file which was just deleted
     * @param handler the UI progress handler
     */
    public void afterDelete(File file, AbstractUIProgressHandler handler)
    {
        throw new IllegalStateException("Deprecated method should not be invoked.");
    }

    /**
     * Logs a message and updates the state file.
     *
     * @param message the message to log
     */
    private void log(String message)
    {
        System.out.println("TestUninstallerListener: " + message);
        if (logPath != null)
        {
            ObjectOutputStream stream = null;
            try
            {
                stream = new ObjectOutputStream(new FileOutputStream(logPath));
                stream.writeObject(state);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }
            finally
            {
                if (stream != null)
                {
                    try
                    {
                        stream.close();
                    }
                    catch (IOException ignore)
                    {
                        // do nothing
                    }
                }
            }
        }
    }

    /**
     * Determines the install path by reading te first entry of the <em>"/install.log"</em> resource.
     *
     * @return the install path or <tt>null</tt> if it cannot be found
     */

    private String getInstallPath()
    {
        String result = null;
        try
        {
            InputStream in = getClass().getResourceAsStream("/install.log");
            InputStreamReader inReader = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inReader);
            result = reader.readLine();
            reader.close();
        }
        catch (IOException exception)
        {
            System.err.println("TestUninstallerListener: unable to determine install path: " + exception.getMessage());
        }
        return result;
    }
}
