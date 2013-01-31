/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * Copyright 2009 Dennis Reil
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
package com.izforge.izpack.panels;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsVersion;

public class DirInputField extends FileInputField
{

    private static final long serialVersionUID = 8494549823214831160L;

    private final boolean mustExist;

    private final boolean canCreate;

    public DirInputField(IzPanel parent, InstallData data, boolean directory, String set, int size,
            List<ValidatorContainer> validatorConfig, boolean mustExist, boolean canCreate)
    {
        super(parent, data, directory, set, size, validatorConfig, null, null);
        this.mustExist = mustExist;
        this.canCreate = canCreate;
    }

    @Override
    protected void prepareFileChooser(JFileChooser filechooser)
    {
        filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    @Override
    protected boolean _validate(File dir)
    {
        //System.err.println(dir.getAbsolutePath() + " - isDir: " + dir.isDirectory()
        //        + " - mustExist: " + mustExist + " - canCreate: " + canCreate);
        if (dir.isDirectory())
        {
            return true;
        }
        else if (mustExist)
        {
            return false;
        }
        else if (canCreate)
        {
            // try to create the directory, if requested
            return verifyCreateOK(dir);
        }
        else
        {
            return true;
        }
    }

    @Override
    protected void showMessage(int k)
    {
        if (k == INVALID)
        {
            showMessage("dir.notdirectory");
        }
        else if (k == EMPTY)
        {
            showMessage("dir.nodirectory");
        }
    }

    private boolean verifyCreateOK(File path)
    {
        if (!path.exists())
        {
            if (!parent.emitNotificationFeedback(parent.getI18nStringForClass("createdir",
                    "TargetPanel")
                    + "\n" + path.getAbsolutePath())) return false;
        }

        // We assume, that we would install something into this dir
        if (!isWriteable(path))
        {
            parent.emitError(parentFrame.langpack.getString("installer.error"), parent
                    .getI18nStringForClass("notwritable", "TargetPanel"));
            return false;
        }
        
        return path.mkdirs(); 
        //if (path.mkdir())
        //{
        //    path.delete();
        //    return true;
        //}
        //return false;
    }

    /**
     * This method determines whether the chosen dir is writeable or not.
     * 
     * @return whether the chosen dir is writeable or not
     */
    private static boolean isWriteable(File path)
    {
        File existParent = IoHelper.existingParent(path);
        if (existParent == null) { return false; }

        // On windows we cannot use canWrite because
        // it looks to the dos flags which are not valid
        // on NT or 2k XP or ...
        if (OsVersion.IS_WINDOWS)
        {
            File tmpFile;
            try
            {
                tmpFile = File.createTempFile("izWrTe", ".tmp", existParent);
                tmpFile.deleteOnExit();
            }
            catch (IOException e)
            {
                Debug.trace(e.toString());
                return false;
            }
            return true;
        }
        return existParent.canWrite();
    }

}
