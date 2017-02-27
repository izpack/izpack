/*
 * Copyright  2001-2002,2004 The Apache Software Foundation
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
package com.izforge.izpack.util.file.types;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * FileList represents an explicitly named list of files. FileLists are useful
 * when you want to capture a list of files regardless of whether they currently
 * exist. By contrast, FileSet operates as a filter, only returning the name of
 * a matched file if it currently exists in the file system.
 */
public class FileList extends DataType
{

    private Vector<String> filenames = new Vector<String>();
    private File dir;

    /**
     * The default constructor.
     */
    public FileList()
    {
        super();
    }

    /**
     * A copy constructor.
     *
     * @param filelist a <code>FileList</code> value
     */
    protected FileList(FileList filelist)
    {
        this.dir = filelist.dir;
        this.filenames = filelist.filenames;
    }

    /**
     * Set the dir attribute.
     *
     * @param dir the directory this filelist is relative to.
     * @throws Exception if an error occurs
     */
    public void setDir(File dir) throws Exception
    {
        this.dir = dir;
    }

    /**
     * @return the directory attribute
     */
    public File getDir()
    {
        return dir;
    }

    /**
     * Set the filenames attribute.
     *
     * @param filenames a string contains filenames, separated by , or by
     * whitespace.
     */
    public void setFiles(String filenames)
    {
        if (filenames != null && filenames.length() > 0)
        {
            StringTokenizer tok = new StringTokenizer(
                    filenames, ", \t\n\r\f", false);
            while (tok.hasMoreTokens())
            {
                this.filenames.addElement(tok.nextToken());
            }
        }
    }

    /**
     * Returns the list of files represented by this FileList.
     *
     * @param p the current project
     * @return the list of files represented by this FileList.
     */
    public String[] getFiles() throws Exception
    {
        if (dir == null)
        {
            throw new Exception("No directory specified for filelist.");
        }

        if (filenames.size() == 0)
        {
            throw new Exception("No files specified for filelist.");
        }

        String[] result = new String[filenames.size()];
        filenames.copyInto(result);
        return result;
    }

    /**
     * Inner class corresponding to the &lt;file&gt; nested element.
     */
    public static class FileName
    {

        private String name;

        /**
         * The name attribute of the file element.
         *
         * @param name the name of a file to add to the file list.
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * @return the name of the file for this element.
         */
        public String getName()
        {
            return name;
        }
    }

    /**
     * Add a nested &lt;file&gt; nested element.
     *
     * @param name a configured file element with a name.
     */
    public void addConfiguredFile(FileName name) throws Exception
    {
        if (name.getName() == null)
        {
            throw new Exception(
                    "No name specified in nested file element");
        }
        filenames.addElement(name.getName());
    }
}
