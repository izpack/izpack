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
import com.izforge.izpack.api.data.Overrides;
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
    public void createInstallationRecord(InstallData idata, IXMLElement panelRoot)
    {
    }

    @Override
    public void runAutomated(InstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        unpacker.run();
        if (!unpacker.getResult())
        {
            throw new InstallerException("Unpacking failed (xml line " + panelRoot.getLineNr() + ")");
        }
    }

    @Override
    public void processOptions(InstallData installData, Overrides overrides)
    {
        unpacker.run();
        if (!unpacker.getResult())
        {
            throw new InstallerException("Unpacking failed");
        }
    }

    @Override
    public void startAction(String name, int noOfSteps)
    {
        System.out.println("[ Starting to unpack ]");
        this.noOfPacks = noOfSteps;
    }

    @Override
    public void stopAction()
    {
        System.out.println("[ Unpacking finished ]");
    }

    @Override
    public void progress(int val, String msg)
    {
        // silent for now. should log individual files here, if we had a verbose mode?
    }

    @Override
    public void nextStep(String packName, int stepno, int stepsize)
    {
        System.out.print("[ Processing package: " + packName + " (");
        System.out.print(stepno);
        System.out.print('/');
        System.out.print(this.noOfPacks);
        System.out.println(") ]");
    }

    @Override
    public void setSubStepNo(int noOfSubsteps)
    {
        // not used here
    }

    @Override
    public void progress(String message)
    {
        // no-op
    }

    @Override
    public void restartAction(String name, String overallMessage, String tip, int steps)
    {
        // no-op
    }
}
