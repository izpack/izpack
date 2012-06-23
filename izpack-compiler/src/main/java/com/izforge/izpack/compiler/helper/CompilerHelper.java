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

package com.izforge.izpack.compiler.helper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;


/**
 * Helper for compiler
 */
public class CompilerHelper
{
    /**
     * Given an event class, return the jar path
     *
     * @param name Name of the event class
     * @return Path to the jar
     */
    public String resolveCustomActionsJarPath(String name)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("bin/customActions/");
        stringBuilder.append("izpack");
        stringBuilder.append(convertNameToDashSeparated(name));
        stringBuilder.append(".jar");
        return stringBuilder.toString();
    }

    /**
     * Convert a camel-case class name to a separate dashes nanem
     *
     * @param name Class name
     * @return minuscule separate dashe name
     */
    public StringBuilder convertNameToDashSeparated(String name)
    {
        StringBuilder res = new StringBuilder();
        for (String part : StringUtils.splitByCharacterTypeCamelCase(name))
        {
            res.append('-');
            res.append(part.toLowerCase());
        }
        return res;
    }

    /**
     * Returns the qualified class name for the given class. This method expects as the url param a
     * jar file which contains the given class. It scans the zip entries of the jar file.
     *
     * @param url       url of the jar file which contains the class
     * @param className short name of the class for which the full name should be resolved
     * @return full qualified class name
     * @throws IOException if the jar cannot be read
     */
    public String getFullClassName(URL url, String className) throws IOException
    {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry;
        while ((zentry = jis.getNextEntry()) != null)
        {
            String name = zentry.getName();
            int lastPos = name.lastIndexOf(".class");
            if (lastPos < 0)
            {
                continue; // No class file.
            }
            name = name.replace('/', '.');
            int pos = -1;
            int nonCasePos = -1;
            if (className != null)
            {
                pos = name.indexOf(className);
                nonCasePos = name.toLowerCase().indexOf(className.toLowerCase());
            }
            if (pos != -1 && name.length() == pos + className.length() + 6) // "Main" class found
            {
                jis.close();
                return (name.substring(0, lastPos));
            }

            if (nonCasePos != -1 && name.length() == nonCasePos + className.length() + 6)
            // "Main" class with different case found
            {
                throw new IllegalArgumentException(
                        "Fatal error! The declared panel name in the xml file (" + className
                                + ") differs in case to the founded class file (" + name + ").");
            }
        }
        jis.close();
        return (null);
    }

    /**
     * Returns a list which contains the pathes of all files which are included in the given url.
     * This method expects as the url param a jar.
     *
     * @param url url of the jar file
     * @return full qualified paths of the contained files
     * @throws IOException if the jar cannot be read
     */
    public List<String> getContainedFilePaths(URL url) throws IOException
    {
        JarInputStream jis = new JarInputStream(url.openStream());
        ZipEntry zentry;
        ArrayList<String> fullNames = new ArrayList<String>();
        while ((zentry = jis.getNextEntry()) != null)
        {
            String name = zentry.getName();
            // Add only files, no directory entries.
            if (!zentry.isDirectory())
            {
                fullNames.add(name);
            }
        }
        jis.close();
        return (fullNames);
    }
}
