/*
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

package com.izforge.izpack.installer.data;

import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.api.data.ExecutableFile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds uninstallation data.
 *
 * @author Julien Ponge created October 27, 2002
 */
@ApplicationScoped
public class UninstallData
{

    /**
     * The installed files list.
     */
    private List<String> installedFilesList;

    /**
     * The uninstallable files list.
     */
    private List<String> uninstallableFilesList;

    /**
     * The executables list.
     */
    private List<ExecutableFile> executablesList;

    /**
     * The uninstaller jar filename.
     */
    private String uninstallerJarFilename;

    /**
     * The uninstaller path.
     */
    private String uninstallerPath;

    /**
     * The uninstaller listeners.
     */
    private final List<CustomData> listeners = new ArrayList<CustomData>();

    /**
     * The uninstall jar.
     */
    private final List<CustomData> jars = new ArrayList<CustomData>();

    /**
     * The native libraries that must be copied to the uninstaller.
     */
    private final List<String> nativeLibs = new ArrayList<String>();

    /**
     * Additional uninstall data like uninstaller listener list.
     */
    private Map<String, Object> additionalData;

    /**
     * Filesmap which should removed by the root user for another user
     */
    private ArrayList<String> unInstallScripts;

    /**
     * The constructor.
     */
    public UninstallData()
    {
        installedFilesList = new ArrayList<String>();
        uninstallableFilesList = new ArrayList<String>();
        executablesList = new ArrayList<ExecutableFile>();
        additionalData = new HashMap<String, Object>();
        unInstallScripts = new ArrayList<String>();
    }

    /**
     * Constant RootFiles = "rootfiles"
     */
    public final static String ROOTSCRIPT = "rootscript";

    /**
     * Adds a file to the data.
     *
     * @param path      The file to add.
     * @param uninstall If true, file must be uninstalled.
     */
    public synchronized void addFile(String path, boolean uninstall)
    {
        if (path != null)
        {
            installedFilesList.add(path);
            if (uninstall)
            {
                uninstallableFilesList.add(path);
            }
        }
    }

    /**
     * Returns the installed files list.
     *
     * @return The installed files list.
     */
    public List<String> getInstalledFilesList()
    {
        return installedFilesList;
    }

    /**
     * Returns the uninstallable files list.
     *
     * @return The uninstallable files list.
     */
    public List<String> getUninstalableFilesList()
    {
        return uninstallableFilesList;
    }

    /**
     * Adds an executable to the data.
     *
     * @param file The executable file.
     */
    public synchronized void addExecutable(ExecutableFile file)
    {
        executablesList.add(file);
    }

    /**
     * Returns the executables list.
     *
     * @return The executables list.
     */
    public List<ExecutableFile> getExecutablesList()
    {
        return executablesList;
    }

    /**
     * Returns the uninstaller jar filename.
     *
     * @return The uninstaller jar filename.
     */
    public synchronized String getUninstallerJarFilename()
    {
        return uninstallerJarFilename;
    }

    /**
     * Sets the uninstaller jar filename.
     *
     * @param name The uninstaller jar filename.
     */
    public synchronized void setUninstallerJarFilename(String name)
    {
        uninstallerJarFilename = name;
    }

    /**
     * Returns the path to the uninstaller.
     *
     * @return The uninstaller filename path.
     */
    public String getUninstallerPath()
    {
        return uninstallerPath;
    }

    /**
     * Sets the uninstaller path.
     *
     * @param path The uninstaller path.
     */
    public void setUninstallerPath(String path)
    {
        uninstallerPath = path;
    }

    /**
     * Adds an listener to invoke at uninstall.
     *
     * @param listener the listener
     */
    public void addUninstallerListener(CustomData listener)
    {
        listeners.add(listener);
    }

    /**
     * Returns the listeners to invoke at uninstall.
     *
     * @return the listeners
     */
    public List<CustomData> getUninstallerListeners()
    {
        return listeners;
    }

    /**
     * Adds a reference to a jar to be copied to the uninstaller.
     *
     * @param jar the jar
     */
    public void addJar(CustomData jar)
    {
        jars.add(jar);
    }

    /**
     * Returns the jars that must be merged to the uninstaller.
     *
     * @return the jar data
     */
    public List<CustomData> getJars()
    {
        return jars;
    }

    /**
     * Adds a reference to a native library, to be copied to the uninstaller.
     *
     * @param path the native library resource path
     */
    public void addNativeLibrary(String path)
    {
        nativeLibs.add(path);
    }

    /**
     * Returns the native library paths, to be copied to the uninstaller.
     *
     * @return the native library resource paths
     */
    public List<String> getNativeLibraries()
    {
        return nativeLibs;
    }

    /**
     * Returns additional uninstall data.
     *
     * @return additional uninstall data
     */
    public Map<String, Object> getAdditionalData()
    {
        return additionalData;
    }

    /**
     * Sets additional uninstall data.
     *
     * @param name  key for the additional uninstall data
     * @param value the additional uninstall data
     */
    public void addAdditionalData(String name, Object value)
    {
        additionalData.put(name, value);
    }

    /**
     * Adds the given File to delete several Shortcuts as Root for the given Users.
     *
     * @param aRootUninstallScript The Script to exec as Root at uninstall.
     */
    public void addUninstallScript(String aRootUninstallScript)
    {
        unInstallScripts.add(aRootUninstallScript == null ? "" : aRootUninstallScript);
    }

    /**
     * Returns the root data.
     *
     * @return root data
     */
    public ArrayList<String> getUninstallScripts()
    {
        return unInstallScripts;
    }

}
