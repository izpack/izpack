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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tools.zip.ZipOutputStream;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.AbstractMerge;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.util.IoHelper;

/**
 * File merge. Can be a single file or a directory.
 *
 * @author Anthonin Bonnefoy
 */
public class FileMerge extends AbstractMerge
{

    private File sourceToCopy;

    private String destination;

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
        if (currentFile.isDirectory())
        {
            for (File files : currentFile.listFiles(fileFilter))
            {
                File file = findRecursivelyForFile(fileFilter, files);
                if (file != null)
                {
                    return file;
                }
            }
        }
        else
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
     * @return the first found file or null
     */
    private void findRecursivelyForFiles(FileFilter fileFilter, File currentFile, List<File> result)
    {
        if (currentFile.isDirectory())
        {
            for (File files : currentFile.listFiles(fileFilter))
            {
                result.add(currentFile);
                findRecursivelyForFiles(fileFilter, files, result);
            }
        }
        else
        {
            result.add(currentFile);
        }
    }

    public void merge(ZipOutputStream outputStream)
    {
        List<String> mergeList = getMergeList(outputStream);
        try
        {
            if (mergeList.contains(sourceToCopy.getAbsolutePath()))
            {
                return;
            }
            mergeList.add(sourceToCopy.getAbsolutePath());
            copyFileToJar(sourceToCopy, outputStream);
        }
        catch (IOException e)
        {
            throw new MergeException(e);
        }
    }

    public void merge(java.util.zip.ZipOutputStream outputStream)
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

    private void copyFileToJar(File fileToCopy, java.util.zip.ZipOutputStream outputStream) throws IOException
    {
        if (fileToCopy.isDirectory())
        {
            for (File file : fileToCopy.listFiles())
            {
                copyFileToJar(file, outputStream);
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

    private void copyFileToJar(File fileToCopy, ZipOutputStream outputStream) throws IOException
    {
        FileInputStream inputStream = null;
        if (fileToCopy.isDirectory())
        {
            for (File file : fileToCopy.listFiles())
            {
                copyFileToJar(file, outputStream);
            }
        } else
        {
            inputStream = new FileInputStream(fileToCopy);
        }

        String entryName = resolveName(fileToCopy, this.destination);
        List<String> mergeList = getMergeList(outputStream);
        if (mergeList.contains(entryName))
        {
            return;
        }
        mergeList.add(entryName);        
        if(inputStream != null)
        {
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
        if (destination.length() == 0)
        {
            return false;
        }
        if (!destination.contains("/"))
        {
            return true;
        }
        return !destination.endsWith("/");
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
