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

package com.izforge.izpack.merge.jar;

import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.api.merge.MergeTarget;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.util.FileUtil;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jar files merger.
 *
 * @author Anthonin Bonnefoy
 */
public class JarMerge implements Mergeable
{
    private static final Logger LOGGER = Logger.getLogger(JarMerge.class.getName());
    private final String jarPath;
    private final String regexp;
    private final String destination;


    /**
     * Create a new JarMerge with a destination
     *
     * @param resource     the resource to merge
     * @param jarPath      Path to the jar to merge
     */
    public JarMerge(URL resource, String jarPath)
    {
        LOGGER.finer(() -> String.format("JarMerge(%s, %s)", resource, jarPath));
        this.jarPath = jarPath;
        destination = FileUtil.convertUrlToFilePath(resource)
                .replace(this.jarPath, "")
                .replace("file:", "")
                .replaceAll("!/?", "")
                .replace("//", "/");
        // make sure any $ characters are escaped, otherwise inner classes won't be merged
        regexp = getRegexp(destination.replace("$", "\\$"));
    }

    /**
     * Create a new JarMerge with a destination
     *
     * @param jarPath       Path to the jar to merge
     * @param pathInsideJar Inside path of the jar to merge. Can be a package or a file. Needed to build the regexp
     * @param destination   Destination of the package
     */
    public JarMerge(String jarPath, String pathInsideJar, String destination)
    {
        LOGGER.finer(() -> String.format("JarMerge(%s, %s, %s)", jarPath, pathInsideJar, destination));
        this.jarPath = jarPath;
        this.destination = destination;
        regexp = getRegexp(pathInsideJar);
    }

    private String getRegexp(String pathInsideJar)
    {
        StringBuilder builder = new StringBuilder(pathInsideJar);
        if (pathInsideJar.endsWith("/"))
        {
            builder.append("+(.*)");
        }
        else if (pathInsideJar.isEmpty())
        {
            builder.append("/*(.*)");
        }
        else
        {
            builder.append("($|/+)(.*)");
        }
        return builder.toString();
    }


    public File find(FileFilter fileFilter)
    {
        try
        {
            ArrayList<String> fileNameInZip = getFileNameInJar();
            for (String fileName : fileNameInZip)
            {
                File file = new File(jarPath + "!/" + fileName);
                if (fileFilter.accept(file))
                {
                    return file;
                }
            }
        }
        catch (IOException e)
        {
            throw new IzPackException(e);
        }
        return null;
    }

    public List<File> recursivelyListFiles(FileFilter fileFilter)
    {
        try
        {
            ArrayList<String> fileNameInZip = getFileNameInJar();
            ArrayList<File> result = new ArrayList<>();
            ArrayList<File> filteredResult = new ArrayList<>();
            for (String fileName : fileNameInZip)
            {
                result.add(new File(jarPath + "!" + fileName));
            }
            for (File file : result)
            {
                if (fileFilter.accept(file))
                {
                    filteredResult.add(file);
                }
            }
            return filteredResult;
        }
        catch (IOException e)
        {
            throw new MergeException(e);
        }
    }

    private ArrayList<String> getFileNameInJar() throws IOException
    {
        try (JarFile jarFile = new JarFile(jarPath))
        {
            ArrayList<String> arrayList = new ArrayList<>();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements())
            {
                JarEntry jarEntry = jarEntries.nextElement();
                arrayList.add(jarEntry.getName());
            }
            return arrayList;
        }
    }


    public void merge(MergeTarget mergeTarget)
    {
        Pattern pattern = Pattern.compile(regexp);
        try (JarFile jarFile = new JarFile(jarPath))
        {
            for (final Enumeration<JarEntry> jarFileEntries = jarFile.entries(); jarFileEntries.hasMoreElements(); )
            {
                final JarEntry jarEntry = jarFileEntries.nextElement();

                // Skip the JAR's manifest file to avoid overwriting it in the target JAR
                final String jarEntryName = jarEntry.getName();
                if (!isManifest(jarEntryName))
                {
                    Matcher matcher = pattern.matcher(jarEntryName);
                    if (matcher.matches()
                            && !isSignature(jarEntryName))
                    {
                        mergeTarget.offer(destination(matcher), jarEntry.getTime(),
                                outputStream -> IOUtils.copy(jarFile.getInputStream(jarEntry), outputStream));
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new IzPackException("Error accessing file: " + jarPath, e.getCause());
        }
    }

    private String destination(Matcher matcher) {
        String matchFile = matcher.group(matcher.groupCount());
        StringBuilder dest = new StringBuilder(destination);
        if (matchFile != null && !matchFile.isEmpty())
        {
            if (dest.length() > 0 && dest.charAt(dest.length() - 1) != '/')
            {
                dest.append('/');
            }
            dest.append(matchFile);
        }
        return dest.toString().replace("//", "/");
    }

    @Override
    public String toString()
    {
        return "JarMerge{" +
                "jarPath='" + jarPath + '\'' +
                ", regexp='" + regexp + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof JarMerge)
        {
            JarMerge jarMerge = (JarMerge) o;
            return Objects.equals(jarPath, jarMerge.jarPath);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return jarPath != null ? jarPath.hashCode() : 0;
    }

    /**
     * Determines if a zip entry corresponds to a signature file.
     * See <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html#Signed_JAR_File">Signed JAR File</a>
     * in the <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html">JAR File
     * Specification</a> for more details.
     *
     * @param name the zip entry name
     * @return {@code true} if the file is a signature file, otherwise {@code false}
     */
    private boolean isSignature(String name)
    {
        return name.matches("/?META-INF/.*\\.(SF|DSA|RSA)") || name.matches("/?META-INF/SIG-.*");
    }

    /**
     * Determines if a JAR entry is the manifest file for the JAR.
     *
     * @param name the JAR entry name
     * @return {@code true} if the file is the manifest, otherwise {@code false}
     */
    private boolean isManifest(String name)
    {
        return name.equals(JarFile.MANIFEST_NAME);
    }
}