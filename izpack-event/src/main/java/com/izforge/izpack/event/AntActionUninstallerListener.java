/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.event;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.event.AbstractUninstallerListener;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.util.IoHelper;

/**
 * Uninstaller listener for performing ANT actions at uninstall time. The definition of what should
 * be done here will be made in a specification file that is referenced by the resource id
 * "AntActionsSpec.xml". There should be an entry in the install.xml file in the sub ELEMENT "res"
 * of ELEMENT "resources" that references it. The specification of the xml file is done in the DTD
 * antaction.dtd. The xml file may contain an ELEMENT "uninstall_target" that should be performed
 * for uninstalling purposes.
 *
 * @author Klaus Bartz
 */
public class AntActionUninstallerListener extends AbstractUninstallerListener
{
    private List<AntAction> befDel = new ArrayList<AntAction>();

    /**
     * Ant actions to be performed after deletion
     */
    private List<AntAction> antActions = new ArrayList<AntAction>();

    /**
     * Default constructor
     */
    public AntActionUninstallerListener()
    {
        super();
    }

    /**
     * Initialises the listener.
     *
     * @throws IzPackException for any error
     */
    @Override
    public void initialise()
    {
        String buildResource = getBuildResource();
        List<AntAction> allActions;
        try
        {
            // Load the defined actions.
            InputStream in = getClass().getResourceAsStream("/antActions");
            if (in == null)
            { // No actions, nothing todo.
                return;
            }
            ObjectInputStream objIn = new ObjectInputStream(in);
            // The actions are stored at installation time as list of AntAction
            // objects.
            // See AntActionInstallerListener.afterPacks.
            allActions = (List<AntAction>) objIn.readObject();
            objIn.close();
            in.close();
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
        // There are two possible orders; before and after deletion.
        // Now we assign the actions to two different lists, the
        // local "before" list which we perform after the scan and
        // the class member "antActions" which should contain the
        // "afterdeletion" actions. Additionally we should save needed
        // files like the properties file for this order because if they're
        // part of the pack the'll be lost after the deletion has been
        // performed.
        for (AntAction action : allActions)
        {
            // See if we need to set the action with the build_resource that
            //  we extracted
            if (null != buildResource)
            {
                // We do
                action.setBuildFile(new File(buildResource));
            }
            //
            if (action.getUninstallOrder().equals(ActionBase.BEFOREDELETION))
            {
                befDel.add(action);
            }
            else
            {// We need the build and the properties file(s) outside the
                // install dir.
                if (null == buildResource)
                {
                    // We have not copied a build_resource to a temporary file
                    //  so now copy the local build file to a temporary file
                    //  and set it as the build file for the action
                    File tmpFile;
                    try
                    {
                        tmpFile = IoHelper.copyToTempFile(action.getBuildFile(), ".xml");
                    }
                    catch (IOException exception)
                    {
                        throw new IzPackException(exception);
                    }
                    action.setBuildFile(tmpFile);
                }
                List<String> props = action.getPropertyFiles();
                if (props != null)
                {
                    ArrayList<String> newProps = new ArrayList<String>();
                    for (String propName : props)
                    {
                        File propFile;
                        try
                        {
                            propFile = IoHelper.copyToTempFile(propName, ".properties");
                            newProps.add(propFile.getCanonicalPath());
                        }
                        catch (IOException exception)
                        {
                            throw new IzPackException(exception);
                        }
                    }
                    action.setPropertyFiles(newProps);
                }
                antActions.add(action);
            }
        }
    }

    /**
     * Invoked before files are deleted.
     *
     * @param files all files which should be deleted
     * @throws IzPackException for any error
     */
    @Override
    public void beforeDelete(List<File> files)
    {
        for (AntAction act : befDel)
        {
            act.performUninstallAction();
        }
    }

    /**
     * Invoked after files are deleted.
     *
     * @param files    the files which were deleted
     * @param listener the progress listener
     * @throws IzPackException for any error
     */
    @Override
    public void afterDelete(List<File> files, ProgressListener listener)
    {
        for (AntAction act : antActions)
        {
            act.performUninstallAction();
        }
    }

    private String getBuildResource()
    {
        String buildResource = null;
        try
        {
            // See if we have an embedded build_resource.  If so, it will be stored
            //  under the /build_resource stream
            InputStream is = getClass().getResourceAsStream("/build_resource");
            if (is != null)
            {
                // There is an embedded build_resource.  The stream will contain a byte array
                //  of the contents of the embedded build_resouce.
                ObjectInputStream ois = new ObjectInputStream(is);
                byte[] content = (byte[]) ois.readObject();
                if (null != content)
                {
                    // Save it to a temporary file
                    ByteArrayInputStream bin = new ByteArrayInputStream(content);
                    File buildFile = IoHelper.copyToTempFile(bin, "xml", null);
                    buildResource = buildFile.getAbsolutePath();
                }
                ois.close();
                is.close();
            }
        }
        catch (Exception exception)
        {
            throw new IzPackException(exception);
        }
        return buildResource;
    }

}
