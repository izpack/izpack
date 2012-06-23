/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
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

package com.izforge.izpack.uninstaller.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.util.file.FileUtils;


/**
 * The installation log.
 * <p/>
 * This holds the installation path and the list of installed files.
 *
 * @author Tim Anderson
 */
public class InstallLog
{

    /**
     * The install.log resource path.
     */
    private static final String INSTALL_LOG = "install.log";

    /**
     * The installation directory.
     */
    private final String installPath;

    /**
     * The installed files.
     */
    private final List<File> files;


    /**
     * Constructs an <tt>InstallLog</tt>.
     *
     * @param resources used to locate the <em>install.log</em> resource
     * @throws IzPackException if the resources cannot be found
     */
    public InstallLog(Resources resources)
    {
        InputStream in = null;
        InputStreamReader inReader = null;
        try
        {
            in = resources.getInputStream(INSTALL_LOG);
            inReader = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inReader);

            installPath = getInstallPath(reader);
            files = getFiles(reader);
        }
        catch (IOException exception)
        {
            throw new IzPackException(exception);
        }
        finally
        {
            FileUtils.close(inReader);
            FileUtils.close(in);
        }
    }

    /**
     * Returns the installation path.
     *
     * @return the installation path
     */
    public String getInstallPath()
    {
        return installPath;
    }

    /**
     * Returns the installed files, in leaf order.
     *
     * @return the installed files
     */
    public List<File> getInstalled()
    {
        return files;
    }

    /**
     * Helper to determine the installation path.
     *
     * @param resources used to locate the <em>install.log</em> resource
     * @throws IzPackException if the install path cannot be read
     */
    public static String getInstallPath(Resources resources)
    {
        String installPath = null;
        BufferedReader reader = null;
        InputStream in = null;

        try
        {
            in = resources.getInputStream(INSTALL_LOG);
            reader = new BufferedReader(new InputStreamReader(in));
            installPath = getInstallPath(reader);
        }
        catch (IOException exception)
        {
            throw new IzPackException(exception);
        }
        finally
        {
            FileUtils.close(reader);
            FileUtils.close(in);
        }
        return installPath;
    }

    /**
     * Helper to get the installation path.
     *
     * @param reader the <em>install.log</em> reader
     * @return the install path
     * @throws IOException if the install path is invalid, or an I/O error occurs
     */
    private static String getInstallPath(BufferedReader reader) throws IOException
    {
        String path = reader.readLine();
        if (path == null || path.trim().isEmpty())
        {
            throw new IOException("Cannot determine installation path");
        }
        return path;
    }

    /**
     * Returns the installed files, in leaf first order.
     *
     * @param reader the <em>install.log</em> reader
     * @return the installed files
     * @throws IOException for any I/O error
     */
    private List<File> getFiles(BufferedReader reader) throws IOException
    {
        TreeSet<File> files = new TreeSet<File>(Collections.reverseOrder());
        String read = reader.readLine();
        while (read != null)
        {
            files.add(new File(read));
            read = reader.readLine();
        }

        // We return it
        return new ArrayList<File>(files);
    }


}
