/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.panels.install;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.event.ProgressListener;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.installer.automation.PanelAutomation;
import com.izforge.izpack.installer.automation.PanelAutomationHelper;
import com.izforge.izpack.installer.unpacker.IUnpacker;

/**
 * Functions to support automated usage of the InstallPanel
 *
 * @author Jonathan Halliday
 */
public class InstallPanelAutomationHelper extends PanelAutomationHelper implements PanelAutomation, ProgressListener
{

    /**
     * The unpacker.
     */
    private final IUnpacker unpacker;

    private int noOfPacks = 0;

    /**
     * Constructs an <tt>InstallPanelAutomationHelper</tt>.
     *
     * @param unpacker the unpacker
     */
    public InstallPanelAutomationHelper(IUnpacker unpacker)
    {
        this.unpacker = unpacker;
        unpacker.setProgressListener(this);
    }

    @Override
    public void createInstallationRecord(InstallData idata, IXMLElement panelRoot) {}

    /**
     * Perform the installation actions.
     *
     * @param panelRoot The panel XML tree root.
     * @return true if the installation was successful.
     */
    public void runAutomated(InstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        unpacker.run();
        if (!unpacker.getResult())
        {
            throw new InstallerException("Unpack failed (xml line " + panelRoot.getLineNr() + ")");
        }
    }

    /**
     * Reports progress on System.out
     */
    public void startAction(String name, int no_of_steps)
    {
        System.out.println("[ Starting to unpack ]");
        this.noOfPacks = no_of_steps;
    }

    /**
     * Sets state variable for thread sync.
     */
    public void stopAction()
    {
        System.out.println("[ Unpacking finished ]");
    }

    /**
     * Null op.
     *
     * @param val
     * @param msg
     */
    public void progress(int val, String msg)
    {
        // silent for now. should log individual files here, if we had a verbose
        // mode?
    }

    /**
     * Reports progress to System.out
     *
     * @param packName The currently installing pack.
     * @param stepno   The number of the pack
     * @param stepsize unused
     */
    public void nextStep(String packName, int stepno, int stepsize)
    {
        System.out.print("[ Processing package: " + packName + " (");
        System.out.print(stepno);
        System.out.print('/');
        System.out.print(this.noOfPacks);
        System.out.println(") ]");
    }

    /**
     * {@inheritDoc}
     */
    public void setSubStepNo(int no_of_substeps)
    {
        // not used here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress(String message)
    {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restartAction(String name, String overallMessage, String tip, int steps)
    {
        // no-op
    }
}
