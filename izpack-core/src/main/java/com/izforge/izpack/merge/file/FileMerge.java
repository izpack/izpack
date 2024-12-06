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

package com.izforge.izpack.merge.file;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.AbstractMerge;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.IoHelper;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * File merge. Can be a single file or a directory.
 *
 * @author Anthonin Bonnefoy
 */
public class FileMerge extends AbstractMerge
{

    private final File sourceToCopy;

    private final String destination;

    public FileMerge(URL url, Map<OutputStream, List<String>> mergeContent)
    {
        this(url, "", mergeContent);
    }

    public FileMerge(URL url, String destination, Map<OutputStream, List<String>> mergeContent)
    {
        this.mergeContent = mergeContent;
        this.sourceToCopy = FileUtil.convertUrlToFile(url);

        this.destination = destination;
    }

    public File find(FileFilter fileFilter)
    {
        return findRecursivelyForFile(fileFilter, sourceToCopy);
    }

    public List<File> recursivelyListFiles(FileFilter fileFilter)
    {
        List<File> result = new ArrayList<File>();
        findRecursivelyForFiles(fileFilter, sourceToCopy, result);
        return result;
    }

    /**
     * Recursively search a file matching the fileFilter
     *
     * @param fileFilter  Filter accepting directory and file matching a classname pattern
     * @param currentFile Current directory
     * @return the first found file or null
     */
    private File findRecursivelyForFile(FileFilter fileFilter, File currentFile)
    {
        if (currentFile != null && currentFile.isDirectory())
        {
            File[] files = currentFile.listFiles(fileFilter);
            if (files != null)
            {
                for (File file : files)
                {
                    File f = findRecursivelyForFile(fileFilter, file);
                    if (f != null)
                    {
                        return f;
                    }
                }
            }
        } else
        {
            return currentFile;
        }
        return null;
    }

    /**
     * Recursively search a all files matching the fileFilter
     *
     * @param fileFilter  Filter accepting directory and file matching a classname pattern
     * @param currentFile Current directory
     */
    private void findRecursivelyForFiles(FileFilter fileFilter, File currentFile, List<File> result)
    {
        if (currentFile != null)
        {
            if (currentFile.isDirectory())
            {
                File[] files = currentFile.listFiles(fileFilter);
                if (files != null)
                {
                    for (File file : files)
                    {
                        result.add(currentFile);
                        findRecursivelyForFiles(fileFilter, file, result);
                    }
                }
            } else
            {
                result.add(currentFile);
            }
        }
    }

    public void merge(ZipOutputStream outputStream)
    {
        try
        {
            copyFileToJar(sourceToCopy, outputStream);
        }
        catch (IOException e)
        {
            throw new MergeException(e);
        }
    }

    private void copyFileToJar(File fileToCopy, ZipOutputStream outputStream) throws IOException
    {
        if (fileToCopy.isDirectory())
        {
            File[] files = fileToCopy.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    copyFileToJar(file, outputStream);
                }
            }
        }
        else
        {
            String entryName = resolveName(fileToCopy, this.destination);
            List<String> mergeList = getMergeList(outputStream);
            if (mergeList.contains(entryName))
            {
                return;
            }
            mergeList.add(entryName);
            FileInputStream inputStream = new FileInputStream(fileToCopy);
            IoHelper.copyStreamToJar(inputStream, outputStream, entryName, fileToCopy.lastModified());
            inputStream.close();
        }
    }

    private String resolveName(File fileToCopy, String destination)
    {
        if (isFile(destination))
        {
            return destination;
        }
        String path = ResolveUtils.convertPathToPosixPath(this.sourceToCopy.getAbsolutePath());
        if (destination.equals(""))
        {
            path = ResolveUtils.convertPathToPosixPath(this.sourceToCopy.getParentFile().getAbsolutePath());
        }
        path = path + '/';
        StringBuilder builder = new StringBuilder();
        builder.append(destination);
        String absolutePath = ResolveUtils.convertPathToPosixPath(fileToCopy.getAbsolutePath());
        builder.append(absolutePath.replaceAll(path, ""));
        return builder.toString().replaceAll("//", "/");
    }

    private boolean isFile(String destination)
    {
        return destination.length() != 0 && (!destination.contains("/") || !destination.endsWith("/"));
    }

    @Override
    public String toString()
    {
        return "FileMerge{" +
                "sourceToCopy=" + sourceToCopy +
                ", destination='" + destination + '\'' +
                '}';
    }
}
