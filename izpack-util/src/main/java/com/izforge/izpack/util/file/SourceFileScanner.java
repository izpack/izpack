/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.izforge.izpack.util.file;

import com.izforge.izpack.util.file.types.Resource;
import com.izforge.izpack.util.file.types.ResourceFactory;

import java.io.File;
import java.util.Vector;

/**
 * Utility class that collects the functionality of the various scanDir methods
 * that have been scattered in several tasks before.
 * <p/>
 * <p>
 * The only method returns an array of source files. The array is a subset of
 * the files given as a parameter and holds only those that are newer than their
 * corresponding target files.</p>
 */
public class SourceFileScanner implements ResourceFactory
{

    private File destDir; // base directory of the fileset

    public SourceFileScanner()
    {
    }

    /**
     * Restrict the given set of files to those that are newer than their
     * corresponding target files.
     *
     * @param files the original set of files
     * @param srcDir all files are relative to this directory
     * @param destDir target files live here. if null file names returned by the
     * mapper are assumed to be absolute.
     * @param mapper knows how to construct a target file names from source file
     * names.
     */
    public String[] restrict(String[] files, File srcDir, File destDir,
            FileNameMapper mapper) throws Exception
    {
        return restrict(files, srcDir, destDir, mapper,
                FileUtils.getFileTimestampGranularity());
    }

    /**
     * Restrict the given set of files to those that are newer than their
     * corresponding target files.
     *
     * @param files the original set of files
     * @param srcDir all files are relative to this directory
     * @param destDir target files live here. if null file names returned by the
     * mapper are assumed to be absolute.
     * @param mapper knows how to construct a target file names from source file
     * names.
     * @param granularity The number of milliseconds leeway to give before
     * deciding a target is out of date.
     */
    public String[] restrict(String[] files, File srcDir, File destDir,
            FileNameMapper mapper, long granularity)
            throws Exception
    {
        // record destdir for later use in getResource
        this.destDir = destDir;
        Vector<Resource> v = new Vector<Resource>();
        for (String file : files)
        {
            File src = FileUtils.resolveFile(srcDir, file);
            v.addElement(new Resource(file, src.exists(),
                    src.lastModified(), src.isDirectory()));
        }
        Resource[] sourceresources = new Resource[v.size()];
        v.copyInto(sourceresources);

        // build the list of sources which are out of date with
        // respect to the target
        Resource[] outofdate
                = ResourceUtils.selectOutOfDateSources(/*task, */sourceresources,
                        mapper, this, granularity);
        String[] result = new String[outofdate.length];
        for (int counter = 0; counter < outofdate.length; counter++)
        {
            result[counter] = outofdate[counter].getName();
        }
        return result;
    }

    /**
     * Convinience layer on top of restrict that returns the source files as
     * File objects (containing absolute paths if srcDir is absolute).
     */
    public File[] restrictAsFiles(String[] files, File srcDir, File destDir,
            FileNameMapper mapper) throws Exception
    {
        return restrictAsFiles(files, srcDir, destDir, mapper,
                FileUtils.getFileTimestampGranularity());
    }

    /**
     * Convinience layer on top of restrict that returns the source files as
     * File objects (containing absolute paths if srcDir is absolute).
     */
    public File[] restrictAsFiles(String[] files, File srcDir, File destDir,
            FileNameMapper mapper, long granularity)
            throws Exception
    {
        String[] res = restrict(files, srcDir, destDir, mapper, granularity);
        File[] result = new File[res.length];
        for (int i = 0; i < res.length; i++)
        {
            result[i] = new File(srcDir, res[i]);
        }
        return result;
    }

    /**
     * returns resource information for a file at destination
     *
     * @param name relative path of file at destination
     * @return data concerning a file whose relative path to destDir is name
     */
    public Resource getResource(String name) throws Exception
    {
        File src = FileUtils.resolveFile(destDir, name);
        return new Resource(name, src.exists(), src.lastModified(),
                src.isDirectory());
    }
}
