/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.merge.resolve;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.FileUtil;

/**
 * Helper methods for resolve
 *
 * @author Anthonin Bonnefoy
 */
public class ResolveUtils
{
    public static final String CLASSNAME_PREFIX = "com.izforge.izpack.panels";
    public static final String BASE_CLASSNAME_PATH = CLASSNAME_PREFIX.replaceAll("\\.", "/") + "/";

    /**
     * Extract file name from url and test jar file
     *
     * @param url url to test. If it is a jar content, the jar file is extracted and treated as a jar
     * @return true if the file is a jar
     */
    public static boolean isJar(URL url)
    {
        if ("jar".equals(url.getProtocol()))
        {
            return true;
        }
        String file = FileUtil.convertUrlToFilePath(url);
        if (file.contains("!"))
        {
            file = file.substring(0, file.lastIndexOf('!'));
        }
        File classFile = new File(file);
        return isJar(classFile);
    }

    public static boolean isJar(File classFile)
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = new ZipFile(classFile);
            zipFile.getName();
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (zipFile != null)
            {
                try
                {
                    zipFile.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }
        return true;
    }
    
    /**
     * Checks if a URL designates a single file in a Jar or not.
     * This is useful to differentiate packages and single files on merges.
     * It is assumed that <code>ResolveUtils.isJar(resource)</code> returns <code>true</code>.
     *
     * @param resource the resource to test
     * @return True if the resource is a single file, false otherwise.
     */
    public static boolean isFileInJar(URL resource)
    {
        // If anyone has a better idea...
        return resource.getPath().matches(".*(\\.\\w+)");
    }

    public static String getCurrentClasspath()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (URL url : getClassPathUrl())
        {
            stringBuilder.append(FileUtil.convertUrlToFilePath(url));
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    static Collection<URL> getClassPathUrl()
    {
        Collection<URL> result = new HashSet<URL>();
        java.net.URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        result.addAll(Arrays.asList(loader.getURLs()));
        try
        {
            Enumeration<URL> urlEnumeration = loader.getResources("");
            result.addAll(Collections.list(urlEnumeration));
            urlEnumeration = loader.getResources("META-INF/");
            result.addAll(Collections.list(urlEnumeration));
        }
        catch (IOException ignored)
        {
        }
        return result;
    }

    /**
     * Search if the given path exist in the classpath and return it. If nothing is found,
     * try to load it as a file and return it if exists.
     *
     * @param path The path of File to load.
     * @return The file or null if nothing found.
     */
    public static URL getFileFromPath(String path)
    {
        URL resource = ClassLoader.getSystemResource(path);
        if (resource != null)
        {
            return resource;
        }
        try
        {
            File file = new File(path);
            if (file.exists())
            {
                return file.toURI().toURL();
            }
        }
        catch (MalformedURLException e)
        {
            throw new IzPackException(e);
        }
        return null;
    }

    public static Set<URL> getJarUrlForPackage(String packageName)
    {
        URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        Set<URL> result = new HashSet<URL>();
        try
        {
            Enumeration<URL> urls = loader.getResources(packageName);
            while (urls.hasMoreElements())
            {
                URL url = urls.nextElement();
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                result.add(connection.getJarFileURL());
            }
        }
        catch (IOException ioex)
        {
        }
        return result;
    }

    public static URL processUrlToJarUrl(URL url) throws MalformedURLException
    {
        return new URL("file", url.getHost(), processUrlToJarPath(url));
    }

    public static String processUrlToJarPath(URL resource)
    {
        String res = FileUtil.convertUrlToFilePath(resource);
        res = res.replaceAll("file:", "");
        if (res.contains("!"))
        {
            return res.substring(0, res.lastIndexOf("!"));
        }
        return res;
    }
    
    public static String processUrlToInsidePath(URL resource)
    {
        String path = resource.getPath();
        if (path.contains("!"))
        {
            return path.substring(path.lastIndexOf("!") + 2);
        }
        return path;
    }

    public static String processUrlToJarPackage(URL resource)
    {
        String res = FileUtil.convertUrlToFilePath(resource);
        res = res.replaceAll("file:", "");
        res = res.substring(res.lastIndexOf("!") + 1);
        res = res.replaceAll("^/", "");
        if (res.endsWith("/"))
        {
            return res;
        }
        return res + "/";
    }

    /**
     * Simply return the left side of the last .<br />
     * com.izpack.Aclass return com.izpack <br />
     * If the is no '.' in the charsequence, return the default package for panels.
     *
     * @param className className to process.
     * @return Extracted package from classname or the default package
     */
    public static String getPanelsPackagePathFromClassName(String className)
    {
        if (className.contains("."))
        {
            return className.substring(0, className.lastIndexOf(".")).replaceAll("\\.", "/") + "/";
        }
        return BASE_CLASSNAME_PATH;
    }

    public static boolean isFile(URL url)
    {
        return !FileUtil.convertUrlToFile(url).isDirectory();
    }

    public static String convertPathToPosixPath(String path)
    {
        return path.replaceAll("\\\\", "/");
    }

    public static String convertPathToPosixPath(File file)
    {
        return convertPathToPosixPath(file.getAbsolutePath());
    }
}
