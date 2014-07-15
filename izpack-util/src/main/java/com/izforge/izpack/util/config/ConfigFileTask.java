/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2005,2009 Ivan SZKIBA
 * Copyright 2010,2011 Rene Krell
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

package com.izforge.izpack.util.config;

import java.io.File;
import java.util.List;

public abstract class ConfigFileTask extends SingleConfigurableTask
{
    protected File oldFile;

    protected File newFile;

    protected File toFile;

    protected boolean cleanup;


    /**
     * Use this to prepend a comment to the configuration file's header
     */
    private List<String> comment;

    /**
     * Location of the configuration file to be patched to; optional. If not set, any empty
     * reference file is assumed, instead.
     */
    public void setNewFile(File file)
    {
        this.newFile = file;
    }

    /**
     * Location of the configuration file to be patched from; optional. If not set, attributes
     * defining preservations of entries and values are ignored.
     */
    public void setOldFile(File file)
    {
        this.oldFile = file;
    }

    /**
     * Location of the resulting output file; required.
     */
    public void setToFile(File file)
    {
        this.toFile = file;
    }


    /**
     * Whether to delete the patchfile after the operation
     * @param cleanup True, if the patchfile should be deleted after the operation
     */
    public void setCleanup(boolean cleanup)
    {
        this.cleanup = cleanup;
    }

    /**
     * optional header comment for the file
     */
    public void setComment(List<String> hdr)
    {
        comment = hdr;
    }

    protected List<String> getComment()
    {
        return this.comment;
    }

    @Override
    protected void checkAttributes() throws Exception
    {
        if (this.toFile == null) { throw new Exception("The \"file\" attribute must be set"); }
    }

}
