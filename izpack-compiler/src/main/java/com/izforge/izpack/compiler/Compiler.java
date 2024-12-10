/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2001 Johannes Lehtinen
 * Copyright 2002 Paul Wilkinson
 * Copyright 2004 Gaganis Giorgos
 *
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

package com.izforge.izpack.compiler;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackColor;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.data.binding.Stage;
import com.izforge.izpack.api.event.InstallationListener;
import com.izforge.izpack.api.event.InstallerListener;
import com.izforge.izpack.api.event.UninstallerListener;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.exception.IzPackClassNotFoundException;
import com.izforge.izpack.compiler.helper.CompilerHelper;
import com.izforge.izpack.compiler.util.CompilerClassLoader;
import com.izforge.izpack.compiler.packager.IPackager;
import com.izforge.izpack.data.CustomData;
import com.izforge.izpack.api.data.PackInfo;
import jakarta.inject.Inject;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The IzPack compiler class. This is now a java bean style class that can be
 * configured using the object representations of the install.xml
 * configuration. The install.xml configuration is now handled by the
 * CompilerConfig class.
 *
 * @author Julien Ponge
 * @author Tino Schwarze
 * @author Chadwick McHenry
 * @see CompilerConfig
 */
public class Compiler
{

    /**
     * Collects and packs files into installation jars, as told.
     */
    private IPackager packager;

    /**
     * Error code, set to false if compilation succeeded.
     */
    private boolean compileFailed = true;

    /**
     * Defines whether JAR contents comply to installer info.
     */
    private boolean javaVersionCorrect = true;

    /**
     * Expected Java target version from JAR.
     */
    private int expectedJavaVersion;

    /**
     * Compiler helper.
     */
    private final CompilerHelper compilerHelper;

    /**
     * The class loader.
     */
    private final CompilerClassLoader loader;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(Compiler.class.getName());


    /**
     * Constructs a <tt>Compiler</tt>.
     *
     * @param loader         the class loader to use to load classes
     * @param compilerHelper the compiler helper
     */
    @Inject
    public Compiler(CompilerClassLoader loader, CompilerHelper compilerHelper)
    {
        this.loader = loader;
        this.compilerHelper = compilerHelper;
    }

    /**
     * Sets the packager.
     * <p/>
     * This must be set before invoking any other methods.
     *
     * @param packager the packager
     */
    public void setPackager(IPackager packager)
    {
        this.packager = packager;
    }

    /**
     * Compiles the installation.
     *
     * @throws Exception Description of the Exception
     */
    public void createInstaller() throws Exception
    {
        // We ask the packager to create the installer
        packager.createInstaller();
        this.compileFailed = false;
    }

    /**
     * Returns whether the installation was successful or not.
     *
     * @return whether the installation was successful or not
     */
    public boolean wasSuccessful()
    {
        return !this.compileFailed;
    }

    /**
     * Verifies dependencies between packs.
     *
     * @throws CompilerException if there are circular dependencies between packs, or a dependency doesn't exist
     */
    public void checkDependencies() throws CompilerException
    {
        checkDependencies(packager.getPacksList());
    }

    /**
     * Verifies that no two preselected packs have the same excludeGroup.
     *
     * @throws CompilerException if two preselected packs have the same excludeGroup
     */
    public void checkExcludes() throws CompilerException
    {
        checkExcludes(packager.getPacksList());
    }

    /**
     * Verifies that no two preselected packs have the same excludeGroup.
     *
     * @param packs list of packs which should be checked
     * @throws CompilerException if two preselected packs have the same excludeGroup
     */
    public void checkExcludes(List<PackInfo> packs) throws CompilerException
    {
        for (int q = 0; q < packs.size(); q++)
        {
            PackInfo packinfo1 = packs.get(q);
            Pack pack1 = packinfo1.getPack();
            for (int w = 0; w < q; w++)
            {

                PackInfo packinfo2 = packs.get(w);
                Pack pack2 = packinfo2.getPack();
                if (pack1.getExcludeGroup() != null && pack2.getExcludeGroup() != null)
                {
                    if (pack1.getExcludeGroup().equals(pack2.getExcludeGroup()))
                    {
                        if (pack1.isPreselected() && pack2.isPreselected())
                        {
                            error("Packs " + pack1.getName() + " and " + pack2.getName() +
                                          " belong to the same excludeGroup " + pack1.getExcludeGroup() +
                                          " and are both preselected. This is not allowed.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Verifies dependencies between packs.
     *
     * @param packs the packs to check
     * @throws CompilerException if there are circular dependencies between packs, or a dependency doesn't exist
     */
    public void checkDependencies(List<PackInfo> packs) throws CompilerException
    {
        // Because we use package names in the configuration file we associate
        // the names with the objects
        Map<String, PackInfo> names = new HashMap<String, PackInfo>();
        for (PackInfo pack : packs)
        {
            names.put(pack.getPack().getName(), pack);
        }
        int result = dfs(packs, names);
        // @todo More informative messages to include the source of the error
        if (result == -2)
        {
            error("Circular dependency detected");
        }
        else if (result == -1)
        {
            error("A dependency doesn't exist");
        }
    }

    /**
     * We use the dfs graph search algorithm to check whether the graph is acyclic as described in:
     * Thomas H. Cormen, Charles Leiserson, Ronald Rivest and Clifford Stein. Introduction to
     * algorithms 2nd Edition 540-549,MIT Press, 2001
     *
     * @param packs The graph
     * @param names The name map
     * @return -2 if back edges exist, else 0
     */
    private int dfs(List<PackInfo> packs, Map<String, PackInfo> names)
    {
        Map<Edge, PackColor> edges = new HashMap<Edge, PackColor>();
        for (PackInfo pack : packs)
        {
            if (pack.colour == PackColor.WHITE)
            {
                if (dfsVisit(pack, names, edges) != 0)
                {
                    return -1;
                }
            }

        }
        return checkBackEdges(edges);
    }

    /**
     * This function checks for the existence of back edges.
     *
     * @param edges map to be checked
     * @return -2 if back edges exist, else 0
     */
    private int checkBackEdges(Map<Edge, PackColor> edges)
    {
        Set<Edge> keys = edges.keySet();
        for (final Edge key : keys)
        {
            PackColor color = edges.get(key);
            if (color == PackColor.GREY)
            {
                return -2;
            }
        }
        return 0;

    }

    /**
     * This class is used for the classification of the edges
     */
    private class Edge
    {

        PackInfo u;

        PackInfo v;

        Edge(PackInfo u, PackInfo v)
        {
            this.u = u;
            this.v = v;
        }
    }

    private int dfsVisit(PackInfo u, Map<String, PackInfo> names, Map<Edge, PackColor> edges)
    {
        u.colour = PackColor.GREY;
        List<String> deps = u.getDependencies();
        if (deps != null)
        {
            for (String name : deps)
            {
                PackInfo v = names.get(name);
                if (v == null)
                {
                    System.out.println("Failed to find dependency: " + name);
                    return -1;
                }
                Edge edge = new Edge(u, v);
                if (edges.get(edge) == null)
                {
                    edges.put(edge, v.colour);
                }

                if (v.colour == PackColor.WHITE)
                {

                    final int result = dfsVisit(v, names, edges);
                    if (result != 0)
                    {
                        return result;
                    }
                }
            }
        }
        u.colour = PackColor.BLACK;
        return 0;
    }

    /**
     * Raises an exception.
     *
     * @param message a brief error message
     * @throws CompilerException when invoked
     */
    private void error(String message) throws CompilerException
    {
        compileFailed = true;
        throw new CompilerException(message);
    }

    /**
     * Adds a jar.
     *
     * @param url         the JAR url
     * @param uninstaller if <tt>true</tt>, include jar in the uninstaller
     * @throws IOException if the jar cannot be read
     */
    public void addJar(URL url, boolean uninstaller) throws IOException
    {
        loader.addURL(url);
        List<String> paths = compilerHelper.getContainedFilePaths(url);
        if (uninstaller)
        {
            CustomData data = new CustomData(null, paths, null, CustomData.UNINSTALLER_JAR);
            packager.addCustomJar(data, url);
        }
        else
        {
            packager.addJarContent(url);
        }
    }
    
    /**
     * Checks JAR classes versions - wrapper method
     *
     * @param file JAR file to check
     * @param minimalJavaVersion minimal Java version from install.xml header  or default from constants
     * @throws IOException when file cannot be read
     * @throws FileNotFoundException when file cannot be found
     */
    public void checkJarVersions(File file, String minimalJavaVersion) throws FileNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream(file);
        extractJarInternals(file.toString(), fis, minimalJavaVersion);
    }
    
    /**
     * Extracts contents of JAR
     *
     * @param fileStr JAR file to check as string
     * @param is JAR file as input stream
     * @param minimalJavaVersion minimal Java version from install.xml header or default from constants
     * @throws IOException when file cannot be read
     * @throws FileNotFoundException when file cannot be found
     */
    private void extractJarInternals(String fileStr, InputStream is, String minimalJavaVersion) throws IOException
    {
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry ze;
        setJavaVersionCorrect(true);
        while ((ze = zis.getNextEntry()) != null) {
            if (ze.getName().endsWith(".class"))
            {
                checkClassTargetVersion(fileStr + ":" + ze.getName(), zis, minimalJavaVersion);
            }
            else if (ze.getName().endsWith(".jar"))
            {
                extractJarInternals(fileStr + ":" + ze.getName(), zis, minimalJavaVersion);
            }
        }
    }
    
    /**
     * Checks target version in class file
     *
     * @param fileStr JAR file as string
     * @param zis class file as input stream
     * @param minimalJavaVersion minimal Java version from install.xml header or default from constants
     * @throws IOException when file cannot be read
     * @throws FileNotFoundException when file cannot be found
     */
    private void checkClassTargetVersion(String fileStr, ZipInputStream zis, String minimalJavaVersion) throws IOException
    {
        DataInputStream dis = new DataInputStream(zis);
        if (dis.readInt() != 0xCAFEBABE)
        {
            throw new CompilerException("Class file cannot be read: " + fileStr);
        }
        dis.readUnsignedShort();
        int major = dis.readUnsignedShort();
        setJavaVersionExpected(major);
        String[] splitMinimalVersion = minimalJavaVersion.split("\\.");
        int minimalVersion = Integer.parseInt(splitMinimalVersion[0]);
        if (minimalVersion < 10 && splitMinimalVersion.length > 1) 
        {
        	minimalVersion = Integer.parseInt(splitMinimalVersion[1]);
        }
        
        if (major > (44 + minimalVersion) )
        {
            setJavaVersionCorrect(false);
        }
    }

    /**
     * Adds a listener to be invoked during installation or uninstallation.
     *
     * @param className   the listener class name
     * @param stage       the stage when the listener is invoked
     * @param constraints the list of constraints. May be <tt>null</tt>
     * @throws IzPackClassNotFoundException if the class cannot be found
     */
    public void addListener(String className, Stage stage, List<OsModel> constraints)
    {
        int type = (stage == Stage.install) ? CustomData.INSTALLER_LISTENER : CustomData.UNINSTALLER_LISTENER;
        Class<? extends InstallationListener> clazz;
        if (stage == Stage.install)
        {
            clazz = loader.loadClass(className, InstallerListener.class);
        }
        else
        {
            clazz = loader.loadClass(className, UninstallerListener.class);
        }
        CustomData data = new CustomData(clazz.getName(), null, constraints, type);
        packager.addCustomJar(data, null);
    }
    
    public void setJavaVersionCorrect(boolean javaVersionCorrect)
    {
        this.javaVersionCorrect = javaVersionCorrect;
    }

    public boolean getJavaVersionCorrect()
    {
        return javaVersionCorrect;
    }

    public void setJavaVersionExpected(int major)
    {
        this.expectedJavaVersion = major - 44;
    }

    public int getJavaVersionExpected()
    {
        return expectedJavaVersion;
    }
}
