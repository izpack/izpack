/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/ http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.izforge.izpack.installer.multiunpacker;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import com.izforge.izpack.api.resource.Messages;

/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class NextMediaFileFilter extends FileFilter
{

    private final String volumename;

    /**
     * The localised messages.
     */
    private final Messages messages;

    public NextMediaFileFilter(String volumename, Messages messages)
    {
        this.volumename = volumename;
        this.messages = messages;
    }

    /* (non-Javadoc)
    * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File file)
    {
        if (file.isDirectory())
        {
            return true;
        }
        String filepath = file.getAbsolutePath();
        return filepath.endsWith(this.volumename);
    }

    /* (non-Javadoc)
    * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription()
    {
        return messages.get("nextmedia.filedesc");
    }

}
