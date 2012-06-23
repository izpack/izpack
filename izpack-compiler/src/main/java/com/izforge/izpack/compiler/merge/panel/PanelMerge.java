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

package com.izforge.izpack.compiler.merge.panel;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.zip.ZipOutputStream;

import com.izforge.izpack.api.merge.Mergeable;

/**
 * Merge for a panel
 *
 * @author Anthonin Bonnefoy
 */
public class PanelMerge implements Mergeable
{
    private List<Mergeable> packageMerge;
    private Class panelClass;
    private FileFilter fileFilter;

    public PanelMerge(final Class panelClass, List<Mergeable> packageMergeable)
    {
        this.panelClass = panelClass;
        packageMerge = packageMergeable;
        fileFilter = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory() ||
                        pathname.getAbsolutePath().contains("/" + panelClass + ".class");
            }
        };
    }

    public void merge(ZipOutputStream outputStream)
    {
        for (Mergeable mergeable : packageMerge)
        {
            mergeable.merge(outputStream);
        }
    }

    public List<File> recursivelyListFiles(FileFilter fileFilter)
    {
        ArrayList<File> result = new ArrayList<File>();
        for (Mergeable mergeable : packageMerge)
        {
            result.addAll(mergeable.recursivelyListFiles(fileFilter));
        }
        return result;
    }

    public File find(FileFilter fileFilter)
    {
        for (Mergeable mergeable : packageMerge)
        {
            File file = mergeable.find(fileFilter);
            if (file != null)
            {
                return file;
            }
        }
        return null;
    }

    public void merge(java.util.zip.ZipOutputStream outputStream)
    {
        for (Mergeable mergeable : packageMerge)
        {
            mergeable.merge(outputStream);
        }
    }

    public Class getPanelClass()
    {
        return panelClass;
    }

    @Override
    public String toString()
    {
        return "PanelMerge{" +
                "packageMerge=" + packageMerge +
                ", panelClass=" + panelClass +
                ", fileFilter=" + fileFilter +
                '}';
    }
}
