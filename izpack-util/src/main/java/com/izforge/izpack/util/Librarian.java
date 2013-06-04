/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
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

package com.izforge.izpack.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.file.FileUtils;

/**
 * This class handles loading of native libraries. There must only be one instance of
 * <code>Librarian</code> per Java runtime, therefore this class is implemented as a 'Singleton'.
 * <br>
 * <br>
 * <code>Librarian</code> is capable of loading native libraries from a variety of different
 * source locations. However, you should place your library files in the 'native' directory. The
 * primary reason for supporting different source locations is to facilitate testing in a
 * development environment, without the need to actually packing the application into a *.jar file.
 *
 * @author Elmar Grom
 */
public class Librarian implements CleanupClient
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Librarian.class.getName());

    /**
     * Used to identify jar URL protocols
     */
    private static final String JAR_PROTOCOL = "jar";

    /**
     * Used to identify file URL protocols
     */
    private static final String FILE_PROTOCOL = "file";

    /**
     * The default directory for native library files.
     */
    private static final String NATIVE = "com/izforge/izpack/bin/native/";

    /**
     * A list that is used to track all libraries that have been loaded. This list is used to ensure
     * that each library is loaded only once.
     */
    private List<String> trackList = new ArrayList<String>();

    /**
     * A list of references to clients that use libraries that were extracted from a *.jar file.
     * This is needed because the clients need to be called for freeing their libraries.
     */
    private List<NativeLibraryClient> clients = new ArrayList<NativeLibraryClient>();

    /**
     * A list of fully qualified library names. This is needed to delete the temporary library files
     * after use. The index of each name corresponds to the index of the respective client in the
     * <code>clients</code> list.
     */
    private List<String> temporaryFileNames = new ArrayList<String>();

    /**
     * The extension to use for native libraries.
     */
    private String extension = "";


    /**
     * Constructs a <tt>Librarian</tt>.
     *
     * @param factory     the factory
     * @param housekeeper the house keeper
     */
    public Librarian(TargetFactory factory, Housekeeper housekeeper)
    {
        housekeeper.registerForCleanup(this);
        extension = '.' + factory.getNativeLibraryExtension();
    }

    /**
     * Loads a library.
     *
     * @param name   the library name
     * @param client the native library client
     * @throws UnsatisfiedLinkError if the library cannot be loaded
     */
    public synchronized void loadLibrary(String name, NativeLibraryClient client) throws UnsatisfiedLinkError
    {
        name = strip(name);
        if (!trackList.contains(name))
        {
            // no attempt has been made to load the library yet
            boolean loaded = loadArchSpecificLibrary(name, client);
            if (!loaded)
            {
                String name64 = name + "_x64";
                loaded = loadArchSpecificLibrary(name64, client);
            }
            if (loaded)
            {
                trackList.add(name);
            }
            else
            {
                throw new UnsatisfiedLinkError("Failed to load library: " + name);

            }
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * This method attempts to remove all native libraries that have been temporarily created from
     * the system.
     * This method calls LibraryRemover which starts a new process which
     * waits a little bit for exit of this process and tries than to delete the given files.
     * If the version is 1.5.x or higher this process should be exit in one second, else
     * the native libraries will be not deleted.
     * Tests with the different methods produces hinds that the
     * FreeLibraryAndExitThread (handle, 0) call in the dlls are the
     * reason for VM crashes (version 1.5.x). May be this is a bug in the VM.
     * But never seen a docu that this behavior is compatible with a VM.
     * Since more than a year all 1.5 versions produce this crash. Therfore we make
     * now a work around for it.
     * But the idea to exit the thread for removing the file locking to give the
     * possibility to delete the dlls are really nice. Therefore we use it with
     * VMs which are compatible with it.  (Klaus Bartz 2006.06.20)
     */
    @Override
    public void cleanUp()
    {
        // This method will be used the SelfModifier stuff of uninstall
        // instead of killing the thread in the dlls which provokes a
        // segmentation violation with a 1.5 (also known as 5.0) VM.

        if (!temporaryFileNames.isEmpty())
        {
            try
            {
                LibraryRemover.invoke(temporaryFileNames);
            }
            catch (IOException exception)
            {
                logger.log(Level.WARNING, "Cleanup failed for native libraries: " + exception.getMessage(), exception);
            }
        }
        clients.clear();
    }

    /**
     * Returns the resource URL for the named library.
     *
     * @param name the library name
     * @return the library's resource URL, or <tt>null</tt> if it is not found
     */
    protected URL getResourcePath(String name)
    {
        String resource = "/" + NATIVE + name + extension;
        return getClass().getResource(resource);
    }

    /**
     * Loads the requested library. If the library is already loaded, this method returns
     * immediately, without an attempt to load the library again.
     * <br>
     * <b>Invocation Example:</b> This assumes that the call is made from the class that links with
     * the library. If this is not the case, <code>this</code> must be replaced by the reference
     * of the class that links with the library. <br>
     * <br>
     * <code>
     * Librarian.getInstance ().loadLibrary ("MyLibrary", this);
     * </code> <br>
     * <br>
     * Loading of a native library file works as follows:<br>
     * <ul>
     * <li>If the library is already loaded there is nothing to do.
     * <li>An attempt is made to load the library by its name. If there is no system path set to
     * the library, this attempt will fail.
     * <li>If the client is located on the local file system, an attempt is made to load the
     * library from the local files system as well.
     * <li>If the library is located inside a *.jar file, it is extracted to 'java.io.tmpdir' and
     * an attempt is made to load it from there.
     * </ul>
     * <br>
     * <br>
     * Loading from the local file system and from the *.jar file is attempted for the following
     * potential locations of the library in this order:<br>
     * <ol>
     * <li>The same directory where the client is located
     * <li>The native library directory
     * </ol>
     *
     * @param name   the name of the library. A file extension and path are not needed, in fact if
     *               supplied, both is stripped off. A specific extension is appended.
     * @param client the object that made the load request
     * @return <tt>true</tt> if the
     */
    private boolean loadArchSpecificLibrary(String name, NativeLibraryClient client)
    {
        boolean result = false;
        if (loadFromDLLPath(name, client) || loadSystemLibrary(name, client) || loadFromClassPath(name, client))
        {
            result = true;
        }
        return result;
    }

    /**
     * Attempts to load a library from the <em>DLL_PATH</em> system property.
     *
     * @param name   the library name
     * @param client the native library client
     * @return <tt>true</tt> if the library was loaded successfully, otherwise <tt>false</tt>
     */
    private boolean loadFromDLLPath(String name, NativeLibraryClient client)
    {
        String property = System.getProperty("DLL_PATH");
        if (property != null)
        {
            String path = property + "/" + name + extension;
            path = path.replace('/', File.separatorChar);
            return load(path, client);
        }
        return false;
    }

    /**
     * Attempts  to load a library from the classpath.
     *
     * @param name   the library name
     * @param client the native library client
     * @return <tt>true</tt> if the library was loaded successfully, otherwise <tt>false</tt>
     */
    private boolean loadFromClassPath(String name, NativeLibraryClient client)
    {
        boolean result = false;
        URL url = getResourcePath(name);
        if (url != null)
        {
            String protocol = url.getProtocol();
            if (protocol.equalsIgnoreCase(FILE_PROTOCOL))
            {
                // its a local file
                try
                {
                    String path = new File(url.toURI()).getPath();
                    result = load(path, client);
                }
                catch (URISyntaxException exception)
                {
                    logger.log(Level.WARNING, "Failed to load library: " + name + ": " + exception.getMessage(),
                               exception);
                }
            }
            else if (protocol.equalsIgnoreCase(JAR_PROTOCOL))
            {
                // its a jar file. Extract and load it from 'java.io.tmpdir'
                result = loadJarLibrary(name, url, client);
            }
        }
        return result;
    }

    /**
     * Attempts to load a library from a jar.
     *
     * @param name   the library name
     * @param url    the library URL within the jar
     * @param client the native library client
     * @return <tt>true</tt> if the library was loaded successfully, otherwise <tt>false</tt>
     */
    private boolean loadJarLibrary(String name, URL url, NativeLibraryClient client)
    {
        boolean result = false;
        File file = null;
        InputStream in = null;
        FileOutputStream out = null;
        String path = null;
        try
        {
            file = FileUtils.createTempFile(name, extension);
            in = url.openStream();
            out = new FileOutputStream(file);
            IoHelper.copyStream(in, out);
            path = file.getAbsolutePath();
        }
        catch (IOException exception)
        {
            logger.log(Level.WARNING, "Failed to load library: " + name + ": " + exception.getMessage(), exception);
        }
        finally
        {
            FileUtils.close(in);
            FileUtils.close(out);
        }
        if (path != null)
        {
            result = load(path, client);
        }
        if (!result)
        {
            FileUtils.delete(file);
        }
        else
        {
            temporaryFileNames.add(path);
            file.deleteOnExit();
        }
        return result;
    }

    /**
     * Loads a system library.
     *
     * @param name   the library name
     * @param client the native library client
     * @return <tt>true</tt> if the library was loaded successfully, otherwise <tt>false</tt>
     */
    private boolean loadSystemLibrary(String name, NativeLibraryClient client)
    {
        try
        {
            System.loadLibrary(name);
            clients.add(client);
            return true;
        }
        catch (Throwable exception)
        {
            logger.log(Level.FINE, "Failed to load library: " + name + ": " + exception.getMessage(), exception);
        }
        return false;
    }

    /**
     * Loads a library given its path.
     *
     * @param path   the library path
     * @param client the native library client
     * @return <tt>true</tt> if the library was loaded successfully, otherwise <tt>false</tt>
     */
    private boolean load(String path, NativeLibraryClient client)
    {
        boolean result = false;
        try
        {
            System.load(path);
            clients.add(client);
            result = true;
        }
        catch (Throwable exception)
        {
            logger.log(Level.FINE, "Failed to load library: " + path + ": " + exception.getMessage(), exception);
        }
        return result;
    }

    /**
     * Strips the extension of the library name, if it has one.
     *
     * @param name the name of the library
     * @return the name without an extension
     */
    private String strip(String name)
    {
        int extensionStart = name.lastIndexOf('.');
        int nameStart = name.lastIndexOf('/');
        if (nameStart < 0)
        {
            nameStart = name.lastIndexOf('\\');
        }
        nameStart++;

        String shortName;

        if (extensionStart > 0)
        {
            shortName = name.substring(nameStart, extensionStart);
        }
        else
        {
            shortName = name.substring(nameStart, name.length());
        }

        return (shortName);
    }

}
