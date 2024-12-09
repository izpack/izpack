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

package com.izforge.izpack.merge;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.merge.resolve.PathResolver;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * A mergeable file allow to chose files to merge in the installer.<br />
 * The source can be files in jar or directory. You may also filters sources with a given regexp.
 *
 * @author Anthonin Bonnefoy
 */
@ApplicationScoped
public class MergeManagerImpl implements MergeManager
{
    private final List<Mergeable> mergeableList;
    private final PathResolver pathResolver;

    @Inject
    public MergeManagerImpl(PathResolver pathResolver)
    {
        this.pathResolver = pathResolver;
        mergeableList = new ArrayList<>();
    }

    @Override
    public void addResourceToMerge(Mergeable mergeable)
    {
        mergeableList.add(mergeable);
    }

    @Override
    public void addResourceToMerge(String resourcePath)
    {
        mergeableList.addAll(pathResolver.getMergeableFromPath(resourcePath));
    }

    @Override
    public void addResourceToMerge(String resourcePath, String destination)
    {
        mergeableList.addAll(pathResolver.getMergeableFromPath(resourcePath, destination));
    }

    @Override
    public void merge(ZipOutputStream outputStream)
    {
        for (Mergeable mergeable : mergeableList)
        {
            mergeable.merge(outputStream);
        }
        mergeableList.clear();
    }

    @Override
    public List<File> recursivelyListFiles(FileFilter fileFilter)
    {
        ArrayList<File> result = new ArrayList<>();
        for (Mergeable mergeable : mergeableList)
        {
            result.addAll(mergeable.recursivelyListFiles(fileFilter));
        }
        return result;
    }

    @Override
    public File find(FileFilter fileFilter)
    {
        for (Mergeable mergeable : mergeableList)
        {
            File file = mergeable.find(fileFilter);
            if (file != null)
            {
                return file;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return "MergeManagerImpl{" +
                "mergeableList=" + mergeableList +
                ", pathResolver=" + pathResolver +
                '}';
    }
}
